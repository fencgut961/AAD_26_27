# Agente SQL

Agente conversacional para consultar PostgreSQL. Al iniciar, lee los nombres y tipos de las columnas del esquema
`public` y los añade al contexto del agente.

## Arquitectura

- `AgentController` valida la petición HTTP.
- `SchemaService` lee una vez el esquema `public` al iniciar la aplicación.
- `AgentService` coordina la conversación con Spring AI, incorpora el esquema al prompt y registra la herramienta SQL.
- `DatabaseTools` valida y ejecuta consultas `SELECT` con `JdbcTemplate` en una transacción de solo lectura. El tiempo
  máximo se configura en `application.yml`.
- PostgreSQL se configura con un usuario de aplicación que solo tiene permisos `SELECT`; el usuario administrador se
  reserva para inicializar la base de datos.

## Configuración

Define estas variables de entorno antes de iniciar la aplicación:

| Variable                  | Uso                                        |
|---------------------------|--------------------------------------------|
| `GEMINI_API_KEY`          | Clave de Google Gemini                     |
| `DB_READONLY_PASSWORD`    | Contraseña del usuario SQL de solo lectura |
| `POSTGRES_ADMIN_PASSWORD` | Contraseña de administración de PostgreSQL |

Opcionalmente, `DB_READONLY_USER` (por defecto `sql_agent`), `POSTGRES_ADMIN_USER` (por defecto `postgres`) y `DB_URL`
permiten cambiar los usuarios y la URL de conexión.

En PowerShell, por ejemplo:

```powershell
$env:GEMINI_API_KEY = "..."
$env:DB_READONLY_PASSWORD = "..."
$env:POSTGRES_ADMIN_PASSWORD = "..."
docker compose -f src/main/resources/docker-compose.yml up -d
```

El script `init.sql` solo se ejecuta al crear por primera vez el volumen de PostgreSQL. Si el volumen ya existía antes
de configurar estas credenciales, crea el usuario de solo lectura y concede permisos manualmente o inicializa un volumen
nuevo.

La aplicación requiere que PostgreSQL esté disponible y que existan tablas en `public` al arrancar; el esquema se carga
una vez por inicio.

La API recibe texto en `POST /api/agente/preguntar` con `Content-Type: text/plain`. Las peticiones vacías y los mensajes
de más de 4000 caracteres se rechazan.
