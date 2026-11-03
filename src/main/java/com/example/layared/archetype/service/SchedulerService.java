package com.example.layared.archetype.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Programa el procesamiento periódico de ficheros.
 *
 * <p>{@code @Service} registra esta clase en Spring, {@code @RequiredArgsConstructor}
 * genera el constructor para inyectar {@link ProcessFilesService} y {@code @Slf4j}
 * proporciona el registrador {@code log}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ProcessFilesService processFilesService;

    /**
     * Invoca el servicio de procesamiento cada segundo.
     *
     * <p>{@code fixedRate} programa cada ejecución con un intervalo de un segundo
     * entre sus inicios.</p>
     */
    @Scheduled(fixedRate = 1_000)
    public void scheduleFileProcessing() {
        log.info("Starting scheduled file processing");
        processFilesService.processFilesService();
    }
}
