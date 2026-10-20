# 🏬 ra1_act1: AadTex Store Ingestion Engine (App 1) — Ficheros de Ejemplo (Input)

Este documento especifica los ficheros de prueba que las tiendas físicas depositan en la carpeta `/workspace/store_data/input/` para ser procesados por la **App 1**.

---

## 📁 Estructura de la Carpeta `/workspace/store_data/input/`

```text
c:/workspace/store_data/
├── keys/
│   └── AadTexSecret.key                  <-- Clave secreta AES-128 ("AadTexSecret2026")
└── input/                                <-- ENTRADA BRUTA DE LAS TIENDAS
    ├── daily_sales_spain.csv             <-- CSV (UTF-8 con tildes y eñes)
    ├── signature_spain.jpg               <-- Comprobante de firma binario (.jpg)
    ├── daily_sales_china.csv             <-- CSV (UTF-8 con caracteres CJK)
    ├── signature_china.jpg               <-- Comprobante de firma binario (.jpg)
    ├── daily_sales_financial.enc         <-- CSV cifrado en origen con AES-128
    ├── signature_financial.jpg           <-- Comprobante de firma binario (.jpg)
    ├── batch_usa.zip                     <-- Contenedor ZIP (Contiene CSV + JPG)
    └── daily_sales_broken_store.csv      <-- Fichero defectuoso de 0 Bytes (Alerta)
```

---

## 📄 Detalle y Contenido de los Ficheros de Entrada

### 1. Clave Secreta AES-128: `keys/AadTexSecret.key`
* **Tipo**: Fichero de texto plano (`UTF-8`).
* **Contenido**:
```text
AadTexSecret2026
```

---

### 2. Lote España: `input/daily_sales_spain.csv`
* **Tipo**: Texto en **`UTF-8`** con caracteres en español.
* **Pareja obligatoria**: `signature_spain.jpg`.
* **Contenido**:
```csv
StoreID,StoreName,SalesAmount,Category,TransactionDate
ZARA_ES_001,Zara Puerta del Sol Madrid,14500.50,WOMEN,2026-09-13
ZARA_ES_001,Zara Puerta del Sol Madrid,3600.30,MEN,2026-09-13
ZARA_ES_001,Zara Gran Vía Madrid,2000.00,KIDS,2026-09-13
```

---

### 3. Lote China (CJK): `input/daily_sales_china.csv`
* **Tipo**: Texto en **`UTF-8`** con caracteres chinos CJK.
* **Pareja obligatoria**: `signature_china.jpg`.
* **Contenido**:
```csv
StoreID,StoreName,SalesAmount,Category,TransactionDate
ZARA_CH_001,Zara 上海南京东路店,28900.00,WOMEN,2026-09-13
ZARA_CH_001,Zara 上海南京东路店,12400.80,MEN,2026-09-13
```

---

### 4. Lote Cifrado (Financiero): `input/daily_sales_financial.enc`
* **Tipo**: Binario cifrado con **AES-128 / ECB / PKCS5Padding** usando la clave `AadTexSecret2026`.
* **Pareja obligatoria**: `signature_financial.jpg`.
* **Texto Plano Equivalente (antes de cifrar)**:
```csv
StoreID,StoreName,SalesAmount,Category,TransactionDate
ZARA_CENTRAL_FIN,Central Financial Office,95000.00,FINANCIAL_REPORT,2026-09-13
```

---

### 5. Comprobantes de Firma: `input/signature_*.jpg`
* **Tipo**: Binario gráfico JPEG.
* **Magic Bytes**: `FF D8 FF E0 ...`
* **Tratamiento en App 1**: Copia eficiente a `/output/signatures/` utilizando buffer de **4 KB (`byte[4096]`)**.

---

### 6. Contenedor Comprimido: `input/batch_usa.zip`
* **Tipo**: Archivo comprimido `.zip`.
* **Tratamiento en App 1**: Descompresión en caliente con `ZipInputStream` para extraer:
  * `daily_sales_usa.csv`
  * `signature_usa.jpg`
* **Contenido real de los ficheros dentro del ZIP**:

1. `daily_sales_usa.csv`
```csv
StoreID,StoreName,SalesAmount,Category,TransactionDate
ZARA_US_001,Zara Times Square New York,42000.00,WOMEN,2026-09-13
ZARA_US_001,Zara Times Square New York,18850.75,MEN,2026-09-13
ZARA_US_001,Zara 5th Avenue New York,7600.00,KIDS,2026-09-13
```

2. `signature_usa.jpg`
```text
Archivo binario JPEG (fichero gráfico). Se representa como binario, no como texto plano.
Magic Bytes esperados: FF D8 FF E0 ...
Contenido esperado: comprobante de firma del lote USA, almacenado como JPG y copiado a /output/signatures/
```

---

### 7. Fichero Defectuoso (0 Bytes): `input/daily_sales_broken_store.csv`
* **Tipo**: Fichero de **0 Bytes** / sin firma.
* **Resultado**: Se emite alerta a `soporte@aadtex.com` y se desplaza a `/output/error/`.
