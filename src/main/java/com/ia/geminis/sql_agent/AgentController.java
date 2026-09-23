package com.ia.geminis.sql_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agente")
public class AgentController {

    private final ChatClient chatClient;

    public AgentController(ChatClient.Builder chatClientBuilder, DatabaseTools databaseTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        Eres un tutor pedagógico y analista experto en bases de datos PostgreSQL.
                        
                        Esquema de la base de datos disponible en Docker:
                        - Tabla 'usuarios': id (SERIAL, PK), nombre (VARCHAR), email (VARCHAR), fecha_registro (TIMESTAMP)
                        - Tabla 'ventas': id (SERIAL, PK), usuario_id (INT, FK), producto (VARCHAR), monto (NUMERIC), fecha (TIMESTAMP)
                        
                        Reglas de actuación:
                        1. Analiza la petición del usuario y formula la consulta SQL SELECT adecuada.
                        2. Llama automáticamente a la herramienta para obtener los datos de la base de datos.
                        3. Responde de forma clara y pedagógica, explicando la respuesta y mostrando la consulta SQL que utilizaste.
                        4. Por motivos de seguridad, solo se permiten consultas SELECT. Rechaza categóricamente cualquier instrucción DELETE, DROP, INSERT o UPDATE.
                        """)
                .defaultTools(databaseTools) // <--- Registra la herramienta JPA
                .build();
    }

    @PostMapping("/preguntar")
    public String ask(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}