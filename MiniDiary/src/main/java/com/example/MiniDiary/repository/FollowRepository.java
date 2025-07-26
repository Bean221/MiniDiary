package com.example.MiniDiary.repository;

import com.example.MiniDiary.entity.Follow;
import com.example.MiniDiary.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
} 