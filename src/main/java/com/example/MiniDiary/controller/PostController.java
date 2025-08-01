package com.example.MiniDiary.controller;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

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
        // Chỉ lấy bài viết công khai
        List<Post> feedPosts = postRepository.findByStatus("public");
        List<Follow> follows = followRepository.findAll();
        // Lấy danh sách userId đã follow
        Set<Integer> followedUserIds = follows.stream()
            .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
            .map(f -> f.getFollowedUser().getId())
            .collect(Collectors.toSet());
        model.addAttribute("posts", feedPosts);
        model.addAttribute("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        model.addAttribute("followedUserIds", followedUserIds);
        model.addAttribute("activePage", "feed");
        model.addAttribute("activePage", "feed");
        session.setAttribute("lastPage", "/feed");
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
    @GetMapping("/following")
    public String followingPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Follow> follows = followRepository.findAll();
        Set<Integer> followedUserIds = follows.stream()
            .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
            .map(f -> f.getFollowedUser().getId())
            .collect(Collectors.toSet());

        // Lấy post của người được follow, chỉ status == 'friends'
        List<Post> friendPosts = postRepository.findAll().stream()
            .filter(p -> "friends".equals(p.getStatus()) && followedUserIds.contains(p.getUser().getId()))
            .collect(Collectors.toList());

        model.addAttribute("posts", friendPosts);
        model.addAttribute("activePage", "following");
        session.setAttribute("lastPage", "/following");

        return "following";
    }

    
}