package com.fencgut.base.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreIngestionScheduler {

    private final FileIngestionService fileIngestionService;

    /**
     * Tarea programada ejecutada de forma desatendida cada 5 segundos.
     */
    @Scheduled(fixedRate = 5000)

    /**
     * Se ejecuta automáticamente todas las noches a las 03:00 AM.
     */
//    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Madrid")
    public void processStoreBatches() {
        log.info("[processStoreBatches] - Iniciando escaneo automatizado de lotes en /input/...");
        fileIngestionService.processInputDirectory();
        log.info("[processStoreBatches] - Fin escaneo automatizado de lotes en /input/...");
    }
}