package com.example.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    private final Path storageRoot;

    public FileController() {
        // default matches LocalStorageService default path. If you changed file.storage.path, this should match it.
        this.storageRoot = Paths.get("./uploads").toAbsolutePath().normalize();
    }

    @GetMapping("/{folder:.+}/{userId}/{subfolder}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String userId,
            @PathVariable String subfolder,
            @PathVariable String filename) {

        try {
            Path file = storageRoot.resolve(folder).resolve(userId).resolve(subfolder).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(file);
            MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok().contentType(mediaType).body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
