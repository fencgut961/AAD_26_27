# 🏬 ra1_act1: AadTex Store Ingestion Engine (App 1)
## Sistema de Ingestión, Descompresión ZIP, Cifrado AES, Transcodificación UTF-8, Buffers y Programación de Tareas (Scheduler)

- **Módulo:** Acceso a Datos (AAD)
- **RA1:** Manejo de Ficheros
- **Entregables:**
  - **Repositorio GitHub:** enlace al repositorio que contenga el proyecto completo desarrollado con Spring Boot y Java, incluyendo todo el código fuente y los archivos necesarios para su configuración, instalación, ejecución y uso. Se deberá incluir un archivo `README.md` con las instrucciones correspondientes.
  - **Archivo ZIP:** archivo `ra1_act1.zip` con una copia completa del contenido del repositorio de GitHub, incluyendo todos los archivos necesarios para ejecutar la aplicación. Se deberá subir a Moodle.

**Importante:** ambas entregas deben contener la misma versión del proyecto. Antes de realizar la entrega, se deberá comprobar que la aplicación se ejecuta correctamente siguiendo las instrucciones proporcionadas y que incluye todos los recursos necesarios para su funcionamiento.

**Recomendación:** mantener el proyecto actualizado en GitHub y, una vez finalizado, generar el archivo `ra1_act1.zip` con todo el contenido del repositorio para entregarlo en Moodle.

---

## 1. Contexto de Negocio Empresarial (Caso AadTex v1.0)

Una multinacional textil con tiendas físicas distribuidas por todo el mundo (**AadTex**) necesita **centralizar e ingerir automáticamente cada noche las ventas, balances financieros e imágenes de firmas de todas sus tiendas** (España, EE. UU., China, etc.) sin intervención humana manual.

Sin un pipeline de ingeniería de software automatizado, la empresa sufre problemas de integración críticos:
1. **Fugas de Información Confidencial**: Los balances financieros no pueden viajar desprotegidos por Internet; requieren **cifrado simétrico en origen (AES-128)**.
2. **Corrupción de Caracteres Internacionales (*Mojibake*)**: Las tiendas de China envían caracteres CJK (Chino, Japonés, Coreano) y las de España tildes y eñes. Si no se fuerzan codificaciones explícitas (**`UTF-8`**), los nombres de productos y tiendas se corrompen.
3. **Gestión de Lotes Comprimidos (`.zip`)**: Ciertas filiales regionales envían su cierre diario agrupado en un único archivo comprimido (`batch_usa.zip`). El motor debe **descomprimir e ingerir en caliente (`ZipInputStream`)**.
4. **Gestión Eficiente de Memoria (RAM)**: Cargar imágenes o ficheros grandes enteros en la memoria del servidor provocaría un error `OutOfMemoryError`. Se exige el uso de **flujos con buffer de 4 KB**.
5. **Aislamiento de Archivos Vacíos o Defectuosos**: Si una tienda envía un fichero corrupto o de 0 bytes, el pipeline debe detectarlo, emitir una **alerta simulada a soporte técnico (`soporte@aadtex.com`)** y aislar el archivo en la carpeta de errores `/workspace/store_data/output/error/`.
6. **Ejecución Automatizada Desatendida**: La ingestión debe ejecutarse de forma automática en segundo plano mediante **Spring Scheduler (`@Scheduled`)**.

---

## 2. Estructura de Entrada y Salida (`/input/` y `/output/`)

### 📦 ¿Qué deposita cada tienda física al cerrar en `/workspace/store_data/input/`?
Cada noche a las 03:00 AM, **cada tienda deposita un lote (*batch*)** en la carpeta `/input/`:

| Fichero de la Tienda | Formato / Extensión | Finalidad y Tratamiento Técnico |
| :--- | :---: | :--- |
| **1. Ventas Estándar** | `.csv` (Texto UTF-8) | Transacciones del día. Soporta caracteres internacionales (España, China CJK). |
| **2. Ventas Financieras Cifradas** | `.enc` (Cifrado AES) | Balance confidencial. Se desencripta en caliente en RAM sin guardar texto en claro en disco. |
| **3. Lote Comprimido** | `.zip` (Contenedor) | Paquete que agrupa el CSV de ventas y la imagen de firma. Se descomprime en caliente. |
| **4. Comprobante de Firma** | `.jpg` (Binario) | Imagen escaneada de conformidad (`signature_*.jpg`). Se copia mediante buffer de 4 KB. |
| **5. Fichero Defectuoso** | `.csv` / `.enc` (0 Bytes / Incompleto) | Lote corrupto. Lanza alerta a `soporte@aadtex.com` y se mueve a `/output/error/`. |

---

### 🏬 Árbol de Directorios del Sistema

```text
c:/workspace/store_data/
├── input/                        <-- Punto de entrada donde las tiendas depositan sus lotes
│   ├── daily_sales_spain.csv     <-- CSV estándar en UTF-8 (España)
│   ├── signature_spain.jpg       <-- Firma JPG (España)
│   ├── daily_sales_financial.enc <-- Balance cifrado en AES-128
│   ├── signature_financial.jpg   <-- Firma JPG (Central)
│   ├── batch_usa.zip             <-- Contenedor comprimido (EE. UU.)
│   └── daily_sales_broken.csv    <-- Fichero defectuoso de 0 bytes
└── output/                       <-- Punto de salida publicado para la App 2
    ├── processed/                <-- Lotes validados y procesados
    │   └── signatures/           <-- Firmas JPG copiadas
    ├── error/                    <-- Ficheros corruptos o vacíos aislados
    └── bucket/                   <-- Resúmenes JSON preliminares para la nube
        └── aadtex-daily-reports/
            └── 2026/
                └── 09/
                    ├── summary_batch_1.json
                    └── summary_batch_2.json
```

---

## 3. Objetivos de Ingeniería de Software (RA1 Core)

* [x] **Automatización con Spring Scheduler (`@EnableScheduling` / `@Scheduled`)**: Ejecución desatendida continua en segundo plano.
* [x] **Control de Flujo con NIO.2**: Control estricto de directorios con `Path`, `Files.exists()`, `Files.createDirectories()` y `Files.move()`.
* [x] **Ingestión y Descompresión en Caliente de ZIPs (`ZipInputStream` / `ZipEntry`)**: Extracción directa de componentes desde archivos `.zip`.
* [x] **Criptografía de Ficheros (`javax.crypto`)**: Desencriptación simétrica AES-128 atómica en RAM de archivos `.enc`.
* [x] **Transcodificación Explicitada en `UTF-8` (`java.io`)**: Lectura de CSVs usando `InputStreamReader` con `StandardCharsets.UTF_8`.
* [x] **Copia Binaria Eficiente mediante Buffers (4 KB)**: Copia de comprobantes JPG con `BufferedInputStream` y `BufferedOutputStream`.
* [x] **Validación de Integridad y Alertas a Soporte**: Notificación por log/e-mail a `soporte@aadtex.com` y aislamiento en `/output/error/`.
* [x] **Publicación de Datos para la App 2**: Organización de ficheros limpios en `/output/` para su consumo por el Dispatcher Cloud.

---

## 4. Requisitos Detallados de Desarrollo

### 📍 Requisito 1: Automatización con Spring Scheduler (`@Scheduled`)
1. Activar el motor de tareas programadas en la clase principal `AadApplication.java` anotándola con **`@EnableScheduling`**.
2. Crear la clase `StoreIngestionScheduler` anotada con **`@Component`**.
3. Implementar el método `processStoreBatches()` ejecutado cada 5 segundos mediante `@Scheduled(fixedRate = 5000)`.

### 📍 Requisito 2: Escaneo y Control de Flujo con NIO.2
1. Verificar la existencia de `/workspace/store_data/input/` y `/workspace/store_data/output/`. Crear directorios con `Files.createDirectories()`.
2. Escanear `/input/` detectando ficheros `.csv`, `.enc`, `.jpg` y `.zip`.
3. **Aislamiento de Ficheros de 0 Bytes / Corruptos**: Si `Files.size(path) == 0`, emitir log de alerta:  
   `log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);`  
   y mover el archivo atómicamente a `/output/error/` con `Files.move(..., StandardCopyOption.REPLACE_EXISTING)`.

### 📍 Requisito 3: Descompresión de Paquetes ZIP (`ZipInputStream`)
1. Si se detecta un archivo `.zip` en `/input/`, abrirlo con `ZipInputStream`.
2. Extraer sus entradas (`ZipEntry`) procesando el CSV de ventas y la firma JPG en caliente sin volcar temporales en disco.

### 📍 Requisito 4: Desencriptación Simétrica AES-128 en RAM
1. Para los archivos `.enc`, utilizar `CryptoService` con la clave secreta `"AadTexSecret2026"`.
2. Pasar los bytes descifrados a un `ByteArrayInputStream` para leer el CSV directamente en memoria RAM.

### 📍 Requisito 5: Lectura CSV Multi-Idioma en `UTF-8`
1. Leer las líneas del CSV con `BufferedReader` e `InputStreamReader` especificando `StandardCharsets.UTF_8`.
2. Procesar correctamente campos con caracteres en español y glifos chinos CJK.
3. Calcular el importe total acumulado de ventas del lote.

### 📍 Requisito 6: Copia Binaria de Firmas con Buffers (4 KB)
1. Copiar la imagen de firma (`signature_*.jpg`) a `/workspace/store_data/output/processed/signatures/`.
2. Utilizar un buffer de **4 KB (`byte[4096]`)** combinando `BufferedInputStream` y `BufferedOutputStream`.

### 📍 Requisito 7: Generador de Datos de Prueba Multilenguaje
1. Crear la clase `MultiLanguageDataGenerator` (`CommandLineRunner`) para poblar automáticamente la carpeta `/input/` al arrancar.

### 📍 Requisito 8: Resumen JSON Preliminar y Bucket Local
1. Generar un archivo JSON de resumen preliminar `summary_batch_[ID].json`.
2. Invocar `CloudBucketService` para escribir el archivo en `/output/bucket/aadtex-daily-reports/2026/09/`.

---

## 5. Entrega Moodle y Defensa

1. **Entrega en Moodle (`ra1_act1.zip`)**:
   - La entrega se realizará exclusivamente a través de la plataforma **Moodle** subiendo un único archivo comprimido en formato ZIP denominado estrictamente **`ra1_act1.zip`**.
   - El archivo `.zip` debe contener todo el proyecto Spring Boot estructurado y listo con sus archivos de datos de prueba para que, al descomprimirlo, ejecutarse sin requerir ajustes adicionales.
2. **Vincular Repositorio GitHub en `README.md`**:
   - En la raíz del proyecto, el archivo **`README.md`** debe incluir obligatoriamente un enlace directo (*link*) al repositorio de GitHub donde se encuentra alojado el código fuente versionado.
3. **Defensa Presencial**:
   - Para la defensa presencial, se utilizará **únicamente el contenido del archivo `ra1_act1.zip` entregado en Moodle**.
   - La aplicación deberá arrancar con normalidad (`mvn spring-boot:run` o desde el IDE) y demostrar el procesamiento automatizado en tiempo real.
   - El alumno responderá de forma individual a preguntas teóricas y/o prácticas sobre la solución desarrollada.
