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
import com.example.MiniDiary.repository.FollowRepository;
import com.example.MiniDiary.entity.Follow;
import com.example.MiniDiary.entity.FollowId;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepository followRepository;

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
        // Thêm danh sách userId đã follow
        List<Follow> follows = followRepository.findAll();
        Set<Integer> followedUserIds = follows.stream()
            .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
            .map(f -> f.getFollowedUser().getId())
            .collect(Collectors.toSet());
        model.addAttribute("followedUserIds", followedUserIds);
        // Tổng số người đã follow
        long totalFollowing = follows.stream()
            .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
            .count();
        model.addAttribute("totalFollowing", totalFollowing);
        // Danh sách user đã follow
        List<User> followingUsers = follows.stream()
            .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
            .map(Follow::getFollowedUser)
            .collect(Collectors.toList());
        model.addAttribute("followingUsers", followingUsers);
        session.setAttribute("lastPage", "/profile");
        return "profile";
    }

    @GetMapping("/profile/{username}")
    public String profileByUsername(@PathVariable String username, Model model, HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user == null) return "redirect:/feed";
        model.addAttribute("user", user);
        model.addAttribute("posts", postRepository.findByUser(user));
        // Thêm danh sách userId đã follow
        User currentUser = (User) session.getAttribute("user");
        List<Follow> follows = followRepository.findAll();
        Set<Integer> followedUserIds = follows.stream()
            .filter(f -> f.getFollowingUser().getId().equals(currentUser.getId()))
            .map(f -> f.getFollowedUser().getId())
            .collect(Collectors.toSet());
        model.addAttribute("followedUserIds", followedUserIds);
        // Nếu là profile của chính mình thì truyền thêm totalFollowing và followingUsers
        if (user.getId().equals(currentUser.getId())) {
            long totalFollowing = follows.stream()
                .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
                .count();
            model.addAttribute("totalFollowing", totalFollowing);
            List<User> followingUsers = follows.stream()
                .filter(f -> f.getFollowingUser().getId().equals(user.getId()))
                .map(Follow::getFollowedUser)
                .collect(Collectors.toList());
            model.addAttribute("followingUsers", followingUsers);
        }
        session.setAttribute("lastPage", "/profile/" + username);
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

    @PostMapping("/follow")
    public String followUser(@RequestParam Integer userId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";
        if (currentUser.getId().equals(userId)) return "redirect:/feed";
        User toFollow = userRepository.findById(userId).orElse(null);
        if (toFollow == null) return "redirect:/feed";
        FollowId followId = new FollowId(currentUser.getId(), userId);
        if (!followRepository.existsById(followId)) {
            Follow follow = new Follow();
            follow.setFollowingUser(currentUser);
            follow.setFollowedUser(toFollow);
            followRepository.save(follow);
        }
        String lastPage = (String) session.getAttribute("lastPage");
        return "redirect:" + (lastPage != null ? lastPage : "/feed");
    }

    @PostMapping("/unfollow")
    public String unfollowUser(@RequestParam Integer userId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";
        FollowId followId = new FollowId(currentUser.getId(), userId);
        followRepository.deleteById(followId);
        String lastPage = (String) session.getAttribute("lastPage");
        return "redirect:" + (lastPage != null ? lastPage : "/profile");
    }
} 