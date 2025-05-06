package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String memberDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User member = userService.findByUsername(username);
        if (member == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", member);
        return "member/dashboard";
    }

    @GetMapping("/profile")
    public String memberProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User member = userService.findByUsername(username);
        if (member == null) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Pengguna tidak ditemukan. Silakan login kembali.");
            return "redirect:/login";
        }

        Profile profile = member.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setNamaLengkap(member.getUsername()); // Default nama dari username
            member.setProfile(profile);
            userService.saveUser(member);
        }
        model.addAttribute("member", member);
        model.addAttribute("profile", profile);
        return "member/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("profile") Profile profile, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User member = userService.findByUsername(username);
        if (member == null) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Pengguna tidak ditemukan. Silakan login kembali.");
            return "redirect:/login";
        }

        Profile existingProfile = member.getProfile();
        if (existingProfile == null) {
            existingProfile = new Profile();
            member.setProfile(existingProfile);
        }

        existingProfile.setJenisKelamin(profile.getJenisKelamin());
        existingProfile.setProgramStudi(profile.getProgramStudi());
        existingProfile.setFakultas(profile.getFakultas());
        existingProfile.setTahunAngkatan(profile.getTahunAngkatan());

        try {
            userService.saveUser(member);
            model.addAttribute("success", true);
            model.addAttribute("message", "Profil berhasil diperbarui.");
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Gagal memperbarui profil: " + e.getMessage());
        }

        model.addAttribute("member", member);
        model.addAttribute("profile", existingProfile);
        return "member/profile";
    }
}