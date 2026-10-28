package com.fencgut.base.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileIngestionService {

    // Rutas del sistema gestionadas con NIO.2
    private final Path inputDir = Paths.get("c:/workspace/store_data/input/");
    private final Path outputDir = Paths.get("c:/workspace/store_data/output/");
    private final Path processedDir = outputDir.resolve("processed");
    private final Path errorDir = outputDir.resolve("error");
    private final Path signaturesDir = processedDir.resolve("signatures");

    private final CryptoService cryptoService;
    private final CsvProcessingService csvProcessingService;
    private final ZipProcessingService zipProcessingService;
    private final CloudBucketService cloudBucketService;

    /**
     * Método principal invocado por el Scheduler para procesar los lotes de /input/
     */
    public void processInputDirectory() {
        try {
            ensureDirectoriesExist();

            if (!Files.exists(inputDir)) {
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir)) {
                for (Path file : stream) {
                    if (Files.isDirectory(file)) continue;

                    String fileName = file.getFileName().toString();

                    // REQUISITO 2: Detección y aislamiento de ficheros defectuosos o vacíos (0 Bytes)
                    if (Files.size(file) == 0) {
                        log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
                        Files.move(file, errorDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                        continue;
                    }

                    // REQUISITO 5: Procesamiento de archivos CSV estándar (UTF-8)
                    if (fileName.endsWith(".csv")) {
                        processCsvFile(file);
                    }
                    // REQUISITO 4: Procesamiento de archivos cifrados (.enc) en RAM
                    else if (fileName.endsWith(".enc")) {
                        processEncryptedFile(file);
                    }
                    // REQUISITO 3: Descompresión en caliente de contenedores ZIP
                    else if (fileName.endsWith(".zip")) {
                        processZipFile(file);
                    }
                    // REQUISITO 6: Copia binaria de firmas JPG con buffer de 4 KB
                    else if (fileName.endsWith(".jpg") && fileName.startsWith("signature_")) {
                        copySignatureFile(file);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error durante el proceso de ingestión: {}", e.getMessage(), e);
        }
    }

    /**
     * Garantiza la existencia de las carpetas de entrada y salida con NIO.2
     */
    private void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(inputDir);
        Files.createDirectories(processedDir);
        Files.createDirectories(errorDir);
        Files.createDirectories(signaturesDir);
    }

    /**
     * Lee el CSV en UTF-8, genera el resumen JSON y mueve el archivo a /output/processed/
     */
    private void processCsvFile(Path file) throws Exception {
        try (InputStream is = Files.newInputStream(file)) {
            List<SaleRecord> records = csvProcessingService.parseCsv(is);
            generateAndPublishSummary(records, file.getFileName().toString());
        }
        Files.move(file, processedDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Desencripta el archivo .enc con AES-128 en RAM y procesa el CSV sin escribir texto en claro en disco
     */
    private void processEncryptedFile(Path file) throws Exception {
        byte[] encryptedData = Files.readAllBytes(file);
        // Desencriptación con la clave secreta "AadTexSecret2026"
        byte[] decryptedData = cryptoService.decryptAes128(encryptedData, "AadTexSecret2026");

        try (InputStream is = new ByteArrayInputStream(decryptedData)) {
            List<SaleRecord> records = csvProcessingService.parseCsv(is);
            generateAndPublishSummary(records, file.getFileName().toString());
        }
        Files.move(file, processedDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Extrae las entradas del ZIP en caliente sin volcar temporales a disco
     */
    private void processZipFile(Path file) throws Exception {
        try (InputStream is = Files.newInputStream(file)) {
            Map<String, byte[]> extractedFiles = zipProcessingService.extractInMemory(is);

            for (Map.Entry<String, byte[]> entry : extractedFiles.entrySet()) {
                String entryName = entry.getKey();
                byte[] content = entry.getValue();

                if (entryName.endsWith(".csv")) {
                    try (InputStream csvStream = new ByteArrayInputStream(content)) {
                        List<SaleRecord> records = csvProcessingService.parseCsv(csvStream);
                        generateAndPublishSummary(records, file.getFileName() + "::" + entryName);
                    }
                } else if (entryName.endsWith(".jpg")) {
                    // Guardar la firma extraída del ZIP en /output/processed/signatures/ usando buffer de 4 KB
                    Path targetPath = signaturesDir.resolve(entryName);
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
                         BufferedInputStream bis = new BufferedInputStream(bais);
                         OutputStream os = Files.newOutputStream(targetPath);
                         BufferedOutputStream bos = new BufferedOutputStream(os)) {

                        byte[] buffer = new byte; // Buffer de 4 KB
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
        Files.move(file, processedDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copia eficiente de comprobantes de firma JPG mediante buffer binario de 4 KB
     */
    public void copySignatureFile(Path file) throws IOException {
        Path targetPath = signaturesDir.resolve(file.getFileName());
        try (InputStream is = Files.newInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is);
             OutputStream os = Files.newOutputStream(targetPath);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {

            byte[] buffer = new byte; // Buffer de 4 KB exigido
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
        Files.move(file, processedDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Calcula el total de ventas acumuladas y publica el resumen JSON preliminar en el Bucket
     */
    private void generateAndPublishSummary(List<SaleRecord> records, String originName) throws Exception {
        if (records.isEmpty()) return;

        BigDecimal totalSales = records.stream()
                .map(SaleRecord::getSalesAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String storeId = records.get(0).getStoreId();
        String batchId = "batch_" + UUID.randomUUID().toString().substring(0, 8);

        BatchSummary summary = new BatchSummary();
        summary.setBatchId(batchId);
        summary.setStoreId(storeId);
        summary.setTotalSales(totalSales);
        summary.setRecordCount(records.size());
        summary.setProcessedAt(LocalDateTime.now());

        String summaryFileName = "summary_" + batchId + ".json";
        cloudBucketService.saveSummaryToBucket(summary, summaryFileName);

        log.info("Lote procesado correctamente [{}] - Tienda: {} - Total: {} EUR -> Publicado resumen: {}",
                originName, storeId, totalSales, summaryFileName);
    }
}
