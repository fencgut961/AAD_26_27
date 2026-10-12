# 🌐 App 2: AadTex Cloud Dispatcher — Ficheros de Ejemplo (Input & Consumo)

Este documento especifica los ficheros de entrada que consume la **App 2** desde la carpeta `c:/workspace/store_data/output/` (generados previamente por la App 1) y el resultado corporativo final transmitido a la nube.

---

## 📁 Estructura Consumida en `/workspace/store_data/output/`

```text
c:/workspace/store_data/output/
├── daily_sales_spain.csv         <-- CSV limpio y validado en UTF-8
├── daily_sales_china.csv         <-- CSV con caracteres CJK validados
├── daily_sales_usa.csv           <-- CSV extraído del contenedor ZIP por App 1
├── daily_sales_financial.csv     <-- CSV descifrado de memoria RAM por App 1
├── summary_batch_101.json        <-- Resumen JSON inicial generado por App 1
└── signatures/                   <-- Subcarpeta con las firmas validadas
    ├── signature_spain.jpg
    ├── signature_china.jpg
    ├── signature_usa.jpg
    └── signature_financial.jpg
```

---

## 📄 Detalle y Contenido de los Ficheros Consumidos

### 1. Resumen Preliminar JSON (App 1): `output/summary_batch_101.json`
* **Origen**: Generado por la App 1 al procesar el lote de España.
* **Contenido**:
```json
{
  "batchId": 101,
  "storeId": "ZARA_ES_001",
  "storeName": "Zara Puerta del Sol Madrid",
  "totalSales": 20100.80,
  "status": "PROCESSED_SUCCESSFULLY"
}
```

---

### 2. CSVs Validados: `output/daily_sales_spain.csv`
* **Estado**: Fichero plano limpio en **`UTF-8`** listo para auditoría y comprobación de integridad.
* **Contenido**:
```csv
StoreID,StoreName,SalesAmount,Category,TransactionDate
ZARA_ES_001,Zara Puerta del Sol Madrid,14500.50,WOMEN,2026-09-13
ZARA_ES_001,Zara Puerta del Sol Madrid,3600.30,MEN,2026-09-13
ZARA_ES_001,Zara Gran Vía Madrid,2000.00,KIDS,2026-09-13
```

---

### 3. Firmas para Escaneo Recursivo: `output/signatures/signature_spain.jpg`
* **Localización**: Dentro del árbol de subcarpetas en `/output/signatures/`.
* **Tratamiento en App 2**:
  1. Localización mediante **`Files.walk()`**.
  2. Transformación binario-texto con **`Base64.getEncoder()`**.
  3. Incrustación en la propiedad `"signatureBase64"` del informe JSON corporativo v2.0.

---

## 🚀 Resultado Corporativo Final Generado por App 2

### Reporte Corporativo JSON v2.0: `output/bucket/aadtex-daily-reports/2026/09/summary_batch_101.json`
* **Descripción**: Documento final que consolida metadatos S3, huella de integridad criptográfica **SHA-256**, desglose por categorías y firma en **Base64**.

```json
{
  "reportMetadata": {
    "batchId": 101,
    "executionTimestamp": "2026-09-13T05:00:00Z",
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
    "totalSalesAmount": 20100.80,
    "currency": "EUR",
    "totalTransactions": 142,
    "averageTicket": 141.55,
    "salesByCategory": {
      "WOMEN": 14500.50,
      "MEN": 3600.30,
      "KIDS": 2000.00
    }
  },
  "complianceAndSignature": {
    "managerSigned": true,
    "signatureFileName": "signature_spain.jpg",
    "signatureMimeType": "image/jpeg",
    "signatureBase64": "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP////////////////////////////////////////////////////////////////////////////////──────"
  }
}
```
