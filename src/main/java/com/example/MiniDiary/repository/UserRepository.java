package com.example.MiniDiary.repository;

import com.example.MiniDiary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
} 