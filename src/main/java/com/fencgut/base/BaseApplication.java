package com.fencgut.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Boot.
 *
 * <p>Esta clase actúa como punto de entrada del sistema y permite
 * inicializar el contexto de Spring junto con la configuración automática
 * de la aplicación.</p>
 *
 * <p>La anotación {@link SpringBootApplication} combina la configuración
 * automática de Spring Boot, el escaneo de componentes y la configuración
 * de la aplicación en una sola anotación.</p>
 *
 * <p>La anotación {@link EnableScheduling} habilita la ejecución de tareas
 * programadas mediante {@code @Scheduled}, permitiendo que el sistema ejecute
 * lógica periódica sin necesidad de un proceso externo.</p>
 *
 * @author fencgut961
 * @version 1.0
 * @since 2026
 */
@SpringBootApplication
@EnableScheduling // Habilita la ejecución de tareas programadas en el contexto de Spring
public class BaseApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * <p>Este método crea y configura el contexto de Spring, inicia la
     * aplicación y deja listo el entorno para que los componentes gestionados
     * por Spring (controladores, servicios, repositorios, etc.) puedan ejecutarse.</p>
     *
     * @param args Argumentos de línea de comandos que pueden recibir
     *             configuraciones adicionales o parámetros de ejecución.
     */
    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);
    }

}
