package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.LevelUser;
import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.repository.LevelUserRepository;
import com.web.website_perpustakaan.repository.UserRepository;
import com.web.website_perpustakaan.service.UserService;
import com.web.website_perpustakaan.service.BukuService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LevelUserRepository levelUserRepository;
    @Autowired
    private BukuService bukuService; 

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        long totalMembers = userRepository.countByLevelUser_LevelUser("member");
        long totalAdmins = userRepository.countByLevelUser_LevelUser("admin");
        long totalPengelola = userRepository.countByLevelUser_LevelUser("pengelola");
        long totalBuku = bukuService.countAllBuku(); 


        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalPengelola", totalPengelola);
        model.addAttribute("totalBuku", totalBuku); 

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "username,asc") String sort,
            Model model) {


        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(sortDirection, sortBy);

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userService.searchUsersPaginated(keyword, pageable); 
            model.addAttribute("keyword", keyword); 
        } else {
            userPage = userRepository.findAll(pageable); 
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("pageSize", userPage.getSize());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());

        return "admin/users";
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("profile", new Profile());
        List<LevelUser> roles = levelUserRepository.findAll();
        model.addAttribute("roles", roles);
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
        List<LevelUser> roles = levelUserRepository.findAll();
        model.addAttribute("roles", roles);
        return "admin/add-user";
    }

    @GetMapping("/edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User tidak ditemukan.");
            return "redirect:/admin/users";
        }
        User user = userOptional.get();
        model.addAttribute("user", user);
        model.addAttribute("profile", user.getProfile());
        model.addAttribute("roles", levelUserRepository.findAll());
        return "admin/edit-user";
    }

    @PostMapping("/edit-user")
    public String editUser(@ModelAttribute User user, @ModelAttribute Profile profile,
                           @RequestParam String role, RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userRepository.findById(user.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan."));

            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());

            Profile existingProfile = existingUser.getProfile();
            if (existingProfile == null) {
                existingProfile = new Profile();
                existingUser.setProfile(existingProfile);
            }
            existingProfile.setNamaLengkap(profile.getNamaLengkap());
            existingProfile.setJenisKelamin(profile.getJenisKelamin());
            existingProfile.setTahunAngkatan(profile.getTahunAngkatan());
            existingProfile.setProgramStudi(profile.getProgramStudi());
            existingProfile.setFakultas(profile.getFakultas());

            LevelUser levelUser = levelUserRepository.findByLevelUser(role)
                    .orElseThrow(() -> new IllegalStateException("Level user '" + role + "' tidak ditemukan"));
            existingUser.setLevelUser(levelUser);

            userService.saveUser(existingUser);
            userService.updateProfile(existingProfile);

            redirectAttributes.addFlashAttribute("success", "User berhasil diupdate.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan saat mengupdate user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-user-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan."));

            int currentStatus = user.getValidasi() != null ? user.getValidasi().getStatus() : 0;
            user.getValidasi().setStatus(currentStatus == 1 ? 0 : 1);
            userRepository.save(user); 
            redirectAttributes.addFlashAttribute("success", "Status user berhasil diubah.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengubah status user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}