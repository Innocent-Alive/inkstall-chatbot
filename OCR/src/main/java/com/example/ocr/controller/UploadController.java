package com.example.ocr.controller;

import com.example.ocr.service.PythonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pdf")
public class UploadController {

    private final PythonService pythonService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UploadController(PythonService pythonService) {
        this.pythonService = pythonService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("range") String range
    ) {
        try {
            Files.createDirectories(Paths.get(uploadDir));

            File uploadedFile = new File(uploadDir + File.separator + file.getOriginalFilename());
            file.transferTo(uploadedFile);

            String pythonOutput = pythonService.splitPdf(uploadedFile.getAbsolutePath(), range);

            return ResponseEntity.ok("PDF uploaded and split successfully.\n" + pythonOutput);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during PDF upload/splitting: " + e.getMessage());
        }
    }
}
