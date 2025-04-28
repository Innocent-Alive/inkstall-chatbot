package com.example.ocr.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ocr.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
