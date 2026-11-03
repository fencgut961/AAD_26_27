package com.ia.geminis.sqlagent.service;

import com.ia.geminis.sqlagent.constants.SqlAgentConstants;
import com.ia.geminis.sqlagent.tool.DatabaseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final ChatClient chatClient;

    public AgentService(ChatClient.Builder chatClientBuilder, DatabaseTools databaseTools, SchemaService schemaService) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SqlAgentConstants.SYSTEM_PROMPT.formatted(schemaService.getSchema()))
                .defaultTools(databaseTools)
                .build();
    }

    public String answer(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
