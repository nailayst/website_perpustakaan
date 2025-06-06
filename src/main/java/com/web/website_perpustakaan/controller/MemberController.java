package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.Profile;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.model.Denda;
import com.web.website_perpustakaan.service.UserService;
import com.web.website_perpustakaan.service.BukuService;
import com.web.website_perpustakaan.service.PeminjamanService;
import com.web.website_perpustakaan.service.DendaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private DendaService dendaService;

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

        peminjamanAktif.forEach(p -> p.setDenda(dendaService.getDendaByPeminjamanId(p.getPeminjamanId())));
        riwayatPeminjaman.forEach(p -> p.setDenda(dendaService.getDendaByPeminjamanId(p.getPeminjamanId())));

        model.addAttribute("member", user);
        model.addAttribute("peminjamanAktif", peminjamanAktif);
        model.addAttribute("riwayatPeminjaman", riwayatPeminjaman);
        model.addAttribute("dendaService", dendaService); // Tambahkan dendaService ke model
        return "member/peminjaman";
    }

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

    @PostMapping("/peminjaman/{id}/kembalikan")
    public String kembalikanBuku(@PathVariable("id") Long peminjamanId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        try {
            peminjamanService.kembalikanBuku(peminjamanId);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil dikembalikan.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/member/peminjaman";
    }

    @GetMapping("/denda/{id}")
    public String tampilkanDenda(@PathVariable("id") Long dendaId, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        Denda denda = dendaService.getDendaById(dendaId);
        if (denda == null) {
            redirectAttributes.addFlashAttribute("error", "Denda tidak ditemukan.");
            return "redirect:/member/peminjaman";
        }

        Peminjaman peminjaman = peminjamanService.getAllPeminjaman().stream()
                .filter(p -> p.getPeminjamanId().equals(denda.getPeminjamanId()))
                .findFirst().orElse(null);
        if (peminjaman == null) {
            redirectAttributes.addFlashAttribute("error", "Peminjaman terkait tidak ditemukan.");
            return "redirect:/member/peminjaman";
        }

        // Hitung keterlambatan
        long hariTerlambat = ChronoUnit.DAYS.between(peminjaman.getTanggalPengembalian(), 
            peminjaman.getTanggalDikembalikan() != null ? peminjaman.getTanggalDikembalikan() : LocalDate.now());

        model.addAttribute("denda", denda);
        model.addAttribute("peminjaman", peminjaman);
        model.addAttribute("hariTerlambat", hariTerlambat);
        model.addAttribute("member", user);
        return "member/denda";
    }

    @PostMapping("/denda/{id}/bayar")
    public String bayarDenda(@PathVariable("id") Long dendaId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        try {
            dendaService.bayarDenda(dendaId);
            redirectAttributes.addFlashAttribute("success", "Denda berhasil dibayar.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/member/peminjaman";
    }
}