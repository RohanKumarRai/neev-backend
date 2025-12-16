package com.example.demo.service.impl;

import com.example.demo.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path storageRoot;
    private final String baseUrl;

    public LocalStorageService(@Value("${file.storage.path:./uploads}") String storagePath,
                               @Value("${file.base.url:http://localhost:8080/files}") String baseUrl) {
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + this.storageRoot, e);
        }
    }

    /**
     * folder example: "workers/1" or "workers/1/photos"
     */
    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Empty file");

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int i = original.lastIndexOf('.');
        if (i >= 0) ext = original.substring(i);

        String name = UUID.randomUUID().toString() + ext;
        Path targetDir = storageRoot.resolve(folder).normalize();
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(name).normalize();

        // Security check - ensure path inside storage root
        if (!target.startsWith(storageRoot)) {
            throw new IOException("Invalid file destination");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return a URL clients can use to fetch the file (dev mode)
        return baseUrl + "/" + folder + "/" + name;
    }

    // helper to get storage root (if needed elsewhere)
    public Path getStorageRoot() {
        return storageRoot;
    }
}
