package de.alexdigital.mcwi.util;

import com.google.common.io.Files;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileCheck {

    private static List<String> allowedExtensions = Arrays.asList(
            "properties", "yml", "txt", "json", "bat", "log"
    );

    public static boolean isEditable(Path path) {
        if (path.toFile().isDirectory()) {
            return false;
        } else {
            return allowedExtensions.contains(Files.getFileExtension(path.toString()));
        }
    }

}
