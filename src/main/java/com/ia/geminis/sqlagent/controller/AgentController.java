package com.ia.geminis.sqlagent.controller;

import com.ia.geminis.sqlagent.constants.SqlAgentConstants;
import com.ia.geminis.sqlagent.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(SqlAgentConstants.API_BASE_PATH)
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping(SqlAgentConstants.ASK_PATH)
    public String ask(@RequestBody String message) {
        validateMessage(message);
        return agentService.answer(message);
    }

    private void validateMessage(String message) {
        validateNotBlank(message);
        validateMaximumLength(message);
    }

    private void validateNotBlank(String message) {
        if (StringUtils.isBlank(message)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje no puede estar vacío.");
        }
    }

    private void validateMaximumLength(String message) {
        if (message.length() > SqlAgentConstants.MAX_MESSAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El mensaje supera el límite de " + SqlAgentConstants.MAX_MESSAGE_LENGTH + " caracteres.");
        }
    }
}