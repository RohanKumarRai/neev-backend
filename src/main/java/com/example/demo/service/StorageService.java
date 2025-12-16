package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    /**
     * Uploads a file into the specified folder and returns a public URL (or relative path).
     * Folder examples: "workers/1", "workers/1/audio"
     */
    String upload(MultipartFile file, String folder) throws IOException;
}
