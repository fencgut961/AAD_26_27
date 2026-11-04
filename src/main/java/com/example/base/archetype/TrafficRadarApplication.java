package com.example.base.archetype;

import com.example.base.archetype.service.TrafficIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class TrafficRadarApplication implements CommandLineRunner {

    public final TrafficIngestionService trafficService;

    public static void main(String[] args) {
        SpringApplication.run(TrafficRadarApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        trafficService.createOutputDirectories();
        trafficService.processTrafficData();
    }
}
