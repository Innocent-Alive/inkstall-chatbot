package com.example.ocr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.ocr.model.User;
import com.example.ocr.repository.UserRepository;
import com.example.ocr.service.JwtService;
import com.example.ocr.model.ForgotPasswordRequest;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody User user) {
		Optional<User> existing = userRepository.findByEmail(user.getEmail());
		if (existing.isPresent()) {
			return ResponseEntity.badRequest().body("User already exists");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("USER");
		userRepository.save(user);
		return ResponseEntity.ok("User registered successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

		if (existingUser.isEmpty() || !passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
			return ResponseEntity.status(401).body("Invalid credentials");
		}

		User foundUser = existingUser.get();
		String token = jwtService.generateToken(foundUser.getEmail());

		// Prepare JSON response
		return ResponseEntity.ok(Map.of("token", "Bearer " + token, "isEnrolled", foundUser.isEnrolled()));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
		if (userOptional.isEmpty()) {
			return ResponseEntity.badRequest().body("User not found with this email.");
		}

		User user = userOptional.get();
		String encodedPassword = passwordEncoder.encode(request.getNewPassword());
		user.setPassword(encodedPassword);
		userRepository.save(user);

		return ResponseEntity.ok("Password updated successfully.");
	}

}
