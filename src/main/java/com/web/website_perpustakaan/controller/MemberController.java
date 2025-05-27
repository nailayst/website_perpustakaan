package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.service.UserService;
import com.web.website_perpustakaan.service.BukuService;
import com.web.website_perpustakaan.service.PeminjamanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private UserService userService;

    @Autowired
    private BukuService bukuService;

    @Autowired
    private PeminjamanService peminjamanService;

    // ========================= DASHBOARD =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) return "redirect:/login";

        List<Buku> daftarBuku = bukuService.getSemuaBuku();
        List<Peminjaman> peminjamanAktif = peminjamanService.getPeminjamanAktif(user.getUserId());
        List<Peminjaman> riwayatPeminjaman = peminjamanService.getRiwayatPeminjaman(user.getUserId());

        model.addAttribute("member", user);
        model.addAttribute("daftarBuku", daftarBuku);
        model.addAttribute("peminjamanAktif", peminjamanAktif);
        model.addAttribute("riwayatPeminjaman", riwayatPeminjaman);
        return "member/dashboard";
    }

    // ========================= DETAIL BUKU =========================
    @GetMapping("/buku/{id}")
    public String detailBuku(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) return "redirect:/login";

        Buku buku;
        try {
            buku = bukuService.getBukuById(id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Buku dengan ID " + id + " tidak ditemukan.");
            return "redirect:/member/dashboard";
        }

        List<Peminjaman> peminjamanAktif = peminjamanService.getPeminjamanAktif(user.getUserId());
        boolean bisaPinjam = true;
        if (peminjamanAktif != null) {
            for (Peminjaman p : peminjamanAktif) {
                if (p.getBuku() != null && p.getBuku().getBukuId().equals(buku.getBukuId())) {
                    bisaPinjam = false;
                    break;
                }
            }
        }

        model.addAttribute("member", user);
        model.addAttribute("buku", buku);
        model.addAttribute("bisaPinjam", bisaPinjam);
        return "member/detail-buku";
    }

    // ========================= PINJAM BUKU =========================
    @PostMapping("/buku/{id}/pinjam")
    public String pinjamBuku(@PathVariable("id") Long bukuId, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        try {
            peminjamanService.pinjamBuku(user.getUserId(), bukuId);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil dipinjam.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/member/buku/" + bukuId;
    }

    // ========================= RIWAYAT PEMINJAMAN =========================
    @GetMapping("/peminjaman")
    public String riwayatPeminjaman(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) return "redirect:/login";

        List<Peminjaman> peminjamanAktif = peminjamanService.getPeminjamanAktif(user.getUserId());
        List<Peminjaman> riwayatPeminjaman = peminjamanService.getRiwayatPeminjaman(user.getUserId());

        model.addAttribute("member", user);
        model.addAttribute("peminjamanAktif", peminjamanAktif);
        model.addAttribute("riwayatPeminjaman", riwayatPeminjaman);
        return "member/peminjaman";
    }

    // ========================= TAMPIL PROFIL =========================
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) return "redirect:/login";

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setNamaLengkap(user.getUsername());
            user.setProfile(profile);
            userService.saveUser(user);
        }

        model.addAttribute("member", user);
        model.addAttribute("profile", profile);
        return "member/profile";
    }

    // ========================= UPDATE PROFIL =========================
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("profile") Profile profile, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        Profile existingProfile = user.getProfile();
        if (existingProfile == null) {
            existingProfile = new Profile();
            user.setProfile(existingProfile);
        }

        existingProfile.setJenisKelamin(profile.getJenisKelamin());
        existingProfile.setProgramStudi(profile.getProgramStudi());
        existingProfile.setFakultas(profile.getFakultas());
        existingProfile.setTahunAngkatan(profile.getTahunAngkatan());

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui profil: " + e.getMessage());
        }

        return "redirect:/member/profile";
    }

    // ========================= GANTI PASSWORD =========================
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password tidak cocok.");
            return "redirect:/member/profile";
        }

        boolean result = userService.changePassword(principal.getName(), oldPassword, newPassword);
        if (result) {
            redirectAttributes.addFlashAttribute("success", "Password berhasil diubah.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Password lama salah.");
        }

        return "redirect:/member/profile";
    }
}