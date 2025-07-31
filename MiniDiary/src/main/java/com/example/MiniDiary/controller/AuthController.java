package com.example.MiniDiary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.MiniDiary.entity.User;
import com.example.MiniDiary.repository.UserRepository;
import com.example.MiniDiary.repository.PostRepository;
import com.example.MiniDiary.entity.Post;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, HttpSession session, Model model) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "login";
        }
        session.setAttribute("user", user);
        return "redirect:/feed";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, Model model) {
        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        User user = new User();
        user.setUsername(username);
        user.setRole("USER");
        userRepository.save(user);
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("posts", postRepository.findByUser(user));
        return "profile";
    }

    @GetMapping("/profile/{username}")
    public String profileByUsername(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username);
        if (user == null) return "redirect:/feed";
        model.addAttribute("user", user);
        model.addAttribute("posts", postRepository.findByUser(user));
        return "profile";
    }

    @PostMapping("/profile/deletePost")
    public String deletePost(@RequestParam Integer postId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null && post.getUser().getId().equals(user.getId())) {
            postRepository.delete(post);
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/editPost")
    public String editPost(@RequestParam Integer postId, @RequestParam String title, @RequestParam String body, @RequestParam String status, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null && post.getUser().getId().equals(user.getId())) {
            post.setTitle(title);
            post.setBody(body);
            post.setStatus(status);
            postRepository.save(post);
        }
        return "redirect:/profile";
    }
} 