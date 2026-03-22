package com.example.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// ✅ CHANGES:
//  1. Path traversal protection added.
//     The original code resolved user-supplied @PathVariables directly and served the
//     result, meaning a request like:
//         GET /files/../../etc/passwd
//     could escape the uploads directory and read arbitrary server files.
//
//     Fix: after resolving the full path, we verify it still starts with storageRoot.
//     If it doesn't, we return 400 Bad Request immediately.
//
//  2. Everything else (route, response headers, content-type detection) is unchanged.

@RestController
@RequestMapping("/files")
public class FileController {

    private final Path storageRoot;

    public FileController() {
        this.storageRoot = Paths.get("./uploads").toAbsolutePath().normalize();
    }

    @GetMapping("/{folder:.+}/{userId}/{subfolder}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String folder,
            @PathVariable String userId,
            @PathVariable String subfolder,
            @PathVariable String filename) {

        try {
            Path file = storageRoot
                    .resolve(folder)
                    .resolve(userId)
                    .resolve(subfolder)
                    .resolve(filename)
                    .normalize();

            // ✅ FIX: reject any path that escapes the storage root
            if (!file.startsWith(storageRoot)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(file);
            MediaType mediaType = contentType != null
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok().contentType(mediaType).body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
