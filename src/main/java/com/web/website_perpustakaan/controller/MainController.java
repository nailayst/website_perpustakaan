package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login(Model model) {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("profile", new Profile());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, @ModelAttribute Profile profile, Model model) {
        try {
            userService.registerUser(user, profile);
            model.addAttribute("success", true);
            model.addAttribute("message", "Pendaftaran berhasil. Silakan cek email Anda untuk verifikasi.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", true);
            model.addAttribute("message", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Gagal mendaftar. Silakan coba lagi.");
        }
        return "auth/register";
    }
    
    @GetMapping("/verify")
    public String verifyUser(@RequestParam String token, Model model) {
        if (userService.verifyUser(token)) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Akun Anda telah diverifikasi. Silakan login.");
            return "redirect:/login";
        } else {
            model.addAttribute("error", true);
            model.addAttribute("message", "Token verifikasi tidak valid atau sudah digunakan.");
            return "auth/verify";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        if (user.getLevelUser() != null && "admin".equals(user.getLevelUser().getLevelUser())) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/member/dashboard";
        }
    }
}