package com.example.base.archetype.util;

import java.nio.file.Path;
import java.util.Locale;

public enum FileExtension {
    CSV(".csv"),
    ENCRYPTED(".enc");

    private final String suffix;

    FileExtension(String suffix) {
        this.suffix = suffix;
    }

    public boolean matches(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(suffix);
    }
}
