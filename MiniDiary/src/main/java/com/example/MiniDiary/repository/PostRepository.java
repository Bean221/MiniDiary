package com.example.MiniDiary.repository;

import com.example.MiniDiary.entity.Post;
import com.example.MiniDiary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByUser(User user);
}
