package com.example.ocr.controller;

import com.example.ocr.service.QuestionGenerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionGenerationController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final QuestionGenerationService questionService;

    public QuestionGenerationController(QuestionGenerationService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("difficulty") String difficulty,
            @RequestParam("questionTypes") String questionTypes,
            @RequestParam("numQuestions") int numQuestions,
            @RequestParam("totalMarks") int totalMarks,
            @RequestParam("paperTitle") String paperTitle
    ) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath); // Create if not exists

            File uploadedFile = new File(uploadPath.toFile(), file.getOriginalFilename());
            file.transferTo(uploadedFile); // Save uploaded file

            String jsonResponse = questionService.generateQuestionsJson(
                    uploadedFile, subject, difficulty, questionTypes, numQuestions, totalMarks, paperTitle
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("IO Error writing file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error generating questions: " + e.getMessage());
        }
    }
}
