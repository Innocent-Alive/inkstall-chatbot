package com.example.ocr.controller;

import com.example.ocr.model.Enrollment;
import com.example.ocr.model.User;
import com.example.ocr.repository.EnrollmentRepository;
import com.example.ocr.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enroll")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> enrollUser(@RequestBody Enrollment enrollmentRequest) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // Set the user to the enrollment
        enrollmentRequest.setUser(user);

        // Save enrollment
        Enrollment savedEnrollment = enrollmentRepository.save(enrollmentRequest);

        // Update the user's isEnrolled flag
        user.setEnrolled(true);
        userRepository.save(user);

        return ResponseEntity.ok(savedEnrollment);
    }
}
