package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.LevelUser;
import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.repository.LevelUserRepository;
import com.web.website_perpustakaan.repository.UserRepository;
import com.web.website_perpustakaan.service.BukuService;
import com.web.website_perpustakaan.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BukuService bukuService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LevelUserRepository levelUserRepository;

    AdminController(BukuService bukuService) {
        this.bukuService = bukuService;
    }

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

    @GetMapping("/tambah-buku")
    public String tampilkanFormTambahBuku(Model model, @RequestParam(value = "success", required = false) Boolean success) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User admin = userService.findByUsername(username);
        if (admin == null || admin.getLevelUser() == null || !"admin".equalsIgnoreCase(admin.getLevelUser().getLevelUser())) {
            return "redirect:/login";
        }

        model.addAttribute("buku", new Buku());
        if (success != null && success) {
            model.addAttribute("berhasil", true);
            model.addAttribute("message", "Buku berhasil ditambahkan!");
        }
        return "admin/tambah-buku";
    }

    @PostMapping("/tambah-buku")
    public String tambahBuku(
            @Valid @ModelAttribute("buku") Buku buku,
            BindingResult result,
            @RequestParam(value = "gambarBuku", required = false) MultipartFile gambarBuku,
            @RequestParam(value = "filePdf", required = false) MultipartFile filePdf,
            @RequestParam("bulanTerbit") int bulanTerbit,
            @RequestParam("tahunTerbit") int tahunTerbit,
            Model model,
            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User admin = userService.findByUsername(username);
        if (admin == null || admin.getLevelUser() == null || !"admin".equalsIgnoreCase(admin.getLevelUser().getLevelUser())) {
            return "redirect:/login";
        }

        if (bulanTerbit < 1 || bulanTerbit > 12) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Bulan terbit harus antara 1 dan 12");
            model.addAttribute("buku", buku);
            return "admin/tambah-buku";
        }
        if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Tahun terbit tidak valid");
            model.addAttribute("buku", buku);
            return "admin/tambah-buku";
        }

        if (result.hasErrors()) {
            model.addAttribute("error", true);
            model.addAttribute("message", result.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("buku", buku);
            return "admin/tambah-buku";
        }

        try {
            YearMonth yearMonth = YearMonth.of(tahunTerbit, bulanTerbit);
            buku.setTanggalTerbit(yearMonth);
            bukuService.tambahBuku(buku, gambarBuku, filePdf);
            redirectAttributes.addAttribute("success", true); // Menggunakan addAttribute untuk parameter URL
            return "redirect:/admin/tambah-buku?success=true"; // Pastikan parameter success dikirim
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Gagal menambahkan buku: " + e.getMessage());
            model.addAttribute("buku", buku);
            return "admin/tambah-buku";
        }
    }
}