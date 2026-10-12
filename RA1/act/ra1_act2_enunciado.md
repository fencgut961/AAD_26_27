# 🌐 ra1_act2: AadTex Cloud Dispatcher & Advanced Transport (App 2)
## Escaneo Recursivo (Files.walk), Integridad SHA-256, Codificación Base64, Reporte Cloud JSON v2.0, Réplicas Zero-Copy (FileChannel) y Exportación Multi-Charset

- **Módulo:** Acceso a Datos (AAD)
- **RA1:** Manejo de Ficheros
- **Importante:** ambas entregas deben contener la misma versión del proyecto. Antes de realizar la entrega, se deberá comprobar que la aplicación se ejecuta correctamente siguiendo las instrucciones proporcionadas y que incluye todos los recursos necesarios para su funcionamiento.
- **Recomendación:** mantener el proyecto actualizado en GitHub y, una vez finalizado, generar el archivo `ra1_act2.zip` con todo el contenido del repositorio para entregarlo en Moodle.

---

## 1. Contexto de Negocio Empresarial (Caso AadTex v2.0 - Sede Central)

### ❓ ¿Cuál es el problema real que estamos resolviendo con la App 2?
Una vez que el motor de ingestión (**App 1**) ha procesado los lotes brutos de las tiendas y ha publicado los archivos validados en la carpeta `c:/workspace/store_data/output/`, la Sede Central de **AadTex** requiere una segunda aplicación independiente de backend (**App 2: Cloud Dispatcher**) que consuma estos datos para **seis propósitos corporativos clave**:

1. **Escaneo Recursivo Multirregional Profundo**: Los archivos procesados por la App 1 y filiales regionales pueden estar organizados en árboles de subdirectorios. La App 2 debe explorar el directorio `/output/` a cualquier nivel de profundidad usando **`Files.walk()`** con Streams.
2. **Auditoría Criptográfica de Integridad (SHA-256)**: Para verificar que ningún fichero fue manipulado ni alterado durante el tránsito entre aplicaciones, la App 2 calcula el **Hash SHA-256** del archivo original antes de consolidarlo.
3. **Transporte Binario-Texto mediante Base64 (`Base64.getEncoder()`)**: La API REST del Cloud Provider exige que la imagen de la firma escaneada (`.jpg`) no se envíe suelta, sino **convertida en una cadena de texto Base64** e incrustada directamente dentro del informe JSON (`"signatureBase64"`).
4. **Consolidación y Publicación del Reporte Cloud JSON v2.0**: Generación de un informe JSON de auditoría enriquecido que combina metadatos de ejecución, información de la tienda, métricas de ventas por categoría, el Hash SHA-256 y la firma en Base64, subiéndolo al Bucket de S3 (`/output/bucket/`).
5. **Réplicas de Respaldo Ultra-Rápida (*Zero-Copy*)**: Para crear copias de seguridad inmediatas en `/output/backup/` sin sobrecargar la memoria RAM del servidor, la App 2 utiliza la API de bajo nivel **`FileChannel.transferTo()`**.
6. **Exportación Contable Legacy Multi-Charset (`ISO-8859-1` / `UTF-16`)**: El departamento financiero utiliza un sistema ERP antiguo que solo soporta archivos de texto recodificados explícitamente en **`ISO-8859-1` (Latín-1)** y **`UTF-16`**.

---

## 2. Flujo de Datos Inter-App y Estructura de `/output/`

### 🔄 Conexión entre las Aplicaciones:
La **App 2** utiliza como punto de entrada exclusivo la carpeta **`/workspace/store_data/output/`** publicada por la **App 1**:

```text
c:/workspace/store_data/
└── output/                       <-- Punto de entrada consumido por la App 2
    ├── processed/                <-- Leído recursivamente por la App 2 con Files.walk()
    │   ├── daily_sales_spain.csv
    │   └── signatures/
    │       └── signature_spain.jpg
    ├── bucket/                   <-- La App 2 deposita aquí el Reporte JSON v2.0 Enriquecido
    │   └── aadtex-daily-reports/
    │       └── 2026/
    │           └── 09/
    │               └── summary_batch_101.json
    ├── backup/                   <-- La App 2 genera réplicas Zero-Copy con FileChannel
    │   └── daily_sales_spain_backup.csv
    └── exports/                  <-- La App 2 genera exportaciones ISO-8859-1 y UTF-16
        ├── summary_latin1.txt
        └── summary_utf16.txt
```

---

## 3. Objetivos de Ingeniería de Software (RA1 Avanzado)

* [x] **Escaneo Recursivo con Streams (`Files.walk()`)**: Exploración declarativa profunda del directorio `/output/`.
* [x] **Verificación Criptográfica de Integridad (`MessageDigest SHA-256`)**: Cálculo del Hash hexadecimal del fichero original.
* [x] **Codificación Binario-Texto con Base64 (`Base64.getEncoder()`)**: Conversión de imágenes `.jpg` a texto para su integración en JSON.
* [x] **Generación de Reporte Cloud JSON v2.0**: Documento JSON estructurado con métricas, hash, metadatos S3 y firma Base64.
* [x] **I/O de Alto Rendimiento (*Zero-Copy*) con `FileChannel`**: Réplica directa a disco con `transferTo()`.
* [x] **Recodificación Multi-Charset (`ISO-8859-1` y `UTF-16`)**: Exportación contable mediante `InputStreamReader` y `OutputStreamWriter`.

---

## 4. Requisitos Detallados de Desarrollo

### 📍 Requisito 1: Escaneo Recursivo con `Files.walk()`
1. Recorrer el directorio `/workspace/store_data/output/processed/` usando `Files.walk(rootPath)`.
2. Filtrar únicamente los ficheros regulares usando `filter(Files::isRegularFile)`.

### 📍 Requisito 2: Verificación de Integridad con Hash SHA-256
1. Para cada archivo CSV o de firma procesado, calcular su Hash SHA-256 utilizando `MessageDigest.getInstance("SHA-256")`.
2. Formatear la huella digital en una cadena hexadecimal de 64 caracteres mediante `HexFormat.of().formatHex(digest)`.

### 📍 Requisito 3: Codificación de Firma en Base64 para JSON
1. Leer los bytes de la imagen de firma `.jpg` copiada por la App 1.
2. Convertir los bytes a una cadena de texto usando `Base64.getEncoder().encodeToString(bytes)`.
3. Asignar la cadena resultante al campo `"signatureBase64"` del informe JSON.

### 📍 Requisito 4: Reporte Cloud JSON v2.0 Enriquecido
1. Construir y guardar el archivo `summary_batch_[ID].json` en `/output/bucket/aadtex-daily-reports/2026/09/`.
2. Estructura exacta del JSON:
```json
{
  "reportMetadata": {
    "batchId": 101,
    "executionTimestamp": "2026-09-13T04:15:00Z",
    "s3ObjectKey": "aadtex-daily-reports/2026/09/summary_batch_101.json"
  },
  "storeInfo": {
    "storeId": "ZARA_ES_001",
    "storeName": "Zara Puerta del Sol Madrid",
    "region": "EMEA",
    "country": "Spain"
  },
  "ingestionSource": {
    "inputFormat": "CSV_STANDARD",
    "originalFileName": "daily_sales_spain.csv",
    "fileHashSHA256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "encodingUsed": "UTF-8"
  },
  "salesMetrics": {
    "totalSalesAmount": 14500.50,
    "currency": "EUR",
    "totalTransactions": 142,
    "averageTicket": 102.11,
    "salesByCategory": {
      "WOMEN": 8900.20,
      "MEN": 3600.30,
      "KIDS": 2000.00
    }
  },
  "complianceAndSignature": {
    "managerSigned": true,
    "signatureFileName": "signature_spain.jpg",
    "signatureMimeType": "image/jpeg",
    "signatureBase64": "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAMUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
  }
}
```

### 📍 Requisito 5: Réplica de Respaldo Zero-Copy con `FileChannel`
1. Crear el servicio `ZeroCopyBackupService`.
2. Realizar copias de seguridad instantáneas hacia `/workspace/store_data/output/backup/` usando `FileChannel.open()` y ejecutando:  
   `sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);`

### 📍 Requisito 6: Exportación Contable Legacy Multi-Charset
1. Crear el servicio `CharsetExportService` para tomar los resúmenes de ventas en `UTF-8` y generar dos archivos en `/output/exports/`:
   - `summary_latin1.txt` en codificación **`ISO-8859-1` (Latín-1)**.
   - `summary_utf16.txt` en codificación **`UTF-16`**.
2. Utilizar `InputStreamReader` y `OutputStreamWriter` especificando los `Charset` explícitos de `StandardCharsets`.

---

## 5. Entrega Moodle y Defensa

1. **Entrega en Moodle (`ra1_act2.zip`)**:
   - La entrega se realizará exclusivamente a través de la plataforma **Moodle** subiendo un único archivo comprimido en formato ZIP denominado estrictamente **`ra1_act2.zip`**.
   - El archivo `.zip` debe contener todo el proyecto Spring Boot estructurado y listo con sus archivos de datos de prueba para que, al descomprimirlo, ejecutarse sin requerir ajustes adicionales.
2. **Vincular Repositorio GitHub en `README.md`**:
   - En la raíz del proyecto, el archivo **`README.md`** debe incluir obligatoriamente un enlace directo (*link*) al repositorio de GitHub donde se encuentra alojado el código fuente versionado.
3. **Defensa Presencial**:
   - Para la defensa presencial, se utilizará **únicamente el contenido del archivo `ra1_act2.zip` entregado en Moodle**.
   - La aplicación deberá arrancar con normalidad (`mvn spring-boot:run` o desde el IDE) y demostrar el procesamiento automatizado en tiempo real.
   - El alumno responderá de forma individual a preguntas teóricas y/o prácticas sobre la solución desarrollada.