package com.sk.web1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileMimeTypeResolver implements MimeTypeResolver {
    @Override
    public String getMimeType(String path) {
        try {
            return Files.probeContentType(Paths.get(path));
        } catch (IOException e) {
            // Handle exception (e.g., log error)
            return null; // Or a default MIME type
        }
    }
}
