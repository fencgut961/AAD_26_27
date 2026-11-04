package com.example.base.archetype.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class TrafficDataPaths {

    private final Path root;

    public TrafficDataPaths(@Value("${traffic-data.directory}") String directory) {
        this.root = Path.of(directory);
    }

    public Path inputDirectory() {
        return root.resolve("input");
    }

    public Path processedDirectory() {
        return root.resolve("output").resolve("processed");
    }

    public Path errorDirectory() {
        return root.resolve("output").resolve("error");
    }

    public Path payloadDirectory() {
        return root.resolve("output").resolve("payload");
    }

    public Path keyFile() {
        return root.resolve("keys").resolve("dgt_secret.key");
    }
}
