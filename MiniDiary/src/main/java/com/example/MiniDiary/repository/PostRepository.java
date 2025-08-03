package com.example.MiniDiary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MiniDiary.entity.Post;
import com.example.MiniDiary.entity.User;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByUser(User user);
	List<Post> findByStatus(String status); 
}