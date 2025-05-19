package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.LevelUser;
import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.repository.LevelUserRepository;
import com.web.website_perpustakaan.repository.UserRepository;
import com.web.website_perpustakaan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LevelUserRepository levelUserRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User admin = userService.findByUsername(username);
        if (admin == null || admin.getLevelUser() == null || 
            !"admin".equalsIgnoreCase(admin.getLevelUser().getLevelUser())) {
            return "redirect:/login";
        }
        long totalMembers = userRepository.countByLevelUser_LevelUser("member");
        long totalAdmins = userRepository.countByLevelUser_LevelUser("admin");
        model.addAttribute("admin", admin);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalAdmins", totalAdmins);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User admin = userService.findByUsername(username);
        if (admin == null || admin.getLevelUser() == null || 
            !"admin".equalsIgnoreCase(admin.getLevelUser().getLevelUser())) {
            return "redirect:/login";
        }
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User admin = userService.findByUsername(username);
        if (admin == null || admin.getLevelUser() == null || 
            !"admin".equalsIgnoreCase(admin.getLevelUser().getLevelUser())) {
            return "redirect:/login";
        }
        model.addAttribute("user", new User());
        model.addAttribute("profile", new Profile());
        return "admin/add-user";
    }

    @PostMapping("/add-user")
    public String addUser(@ModelAttribute User user, @ModelAttribute Profile profile, 
                         @RequestParam String role, Model model) {
        try {
            LevelUser levelUser = levelUserRepository.findByLevelUser(role)
                    .orElseThrow(() -> new IllegalStateException("Level user '" + role + "' tidak ditemukan"));
            user.setLevelUser(levelUser);
            userService.registerUser(user, profile);
            model.addAttribute("success", true);
            model.addAttribute("message", "User berhasil ditambahkan.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", true);
            model.addAttribute("message", e.getMessage());
        }
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "admin/add-user";
    }
}
