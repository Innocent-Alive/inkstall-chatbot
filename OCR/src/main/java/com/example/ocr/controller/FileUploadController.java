package com.example.ocr.controller;

import com.example.ocr.service.PythonOCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FileUploadController {

    private static final String UPLOAD_DIR = "C:/OCR/uploads/";

    @Autowired
    private PythonOCRService pythonOCRService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
//             Create upload directory if it doesn't exist
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Save the uploaded file
            String filePath = UPLOAD_DIR + file.getOriginalFilename();
            File savedFile = new File(filePath);
            file.transferTo(savedFile);

            // Call OCR service to extract text
            String extractedText = pythonOCRService.runOCRScript(filePath);

            // Return OCR output
            return ResponseEntity.ok("✅ Text extracted successfully:\n\n" + extractedText);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Error uploading file: " + e.getMessage());
        }
    }
}
