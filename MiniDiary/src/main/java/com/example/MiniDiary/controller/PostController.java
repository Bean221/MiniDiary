package com.example.MiniDiary.controller;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.MiniDiary.entity.Follow;
import com.example.MiniDiary.entity.Post;
import com.example.MiniDiary.entity.User;
import com.example.MiniDiary.repository.FollowRepository;
import com.example.MiniDiary.repository.PostRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class PostController {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private FollowRepository followRepository;

    @GetMapping("/feed")
    public String feed(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<Post> feedPosts = new ArrayList<>();
        // Bài viết của mình
        feedPosts.addAll(postRepository.findByUser(user));
        // Bài viết của người mình theo dõi
        List<Follow> follows = followRepository.findAll();
        for (Follow f : follows) {
            if (f.getFollowingUser().getId().equals(user.getId())) {
                feedPosts.addAll(postRepository.findByUser(f.getFollowedUser()));
            }
        }
        model.addAttribute("posts", feedPosts);
        model.addAttribute("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return "feed";
    }

    @PostMapping("/feed")
    public String createPost(@RequestParam String title, @RequestParam String body, @RequestParam String status, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (title == null || title.trim().isEmpty() || body == null || body.trim().isEmpty()) {
            model.addAttribute("error", "Tiêu đề và nội dung không được để trống");
            model.addAttribute("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            return feed(model, session);
        }
        Post post = new Post();
        post.setTitle(title);
        post.setBody(body);
        post.setUser(user);
        post.setStatus(status);
        postRepository.save(post);
        return "redirect:/feed";
    }
}