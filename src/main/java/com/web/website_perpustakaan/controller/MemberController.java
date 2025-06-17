package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.*;
import com.web.website_perpustakaan.service.*; 

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;  

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

    @Autowired 
    private PengusulanService pengusulanService;

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
    public String detailBuku(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) return "redirect:/login";

        Buku buku = null;
        try {
            Optional<Buku> optionalBuku = bukuService.getBukuById(id); 
            
            if (optionalBuku.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Buku dengan ID " + id + " tidak ditemukan.");
                return "redirect:/member/dashboard";
            }
            buku = optionalBuku.get();

            if (buku.getStatusBuku() == null) {
                buku.setStatusBuku(Buku.StatusBuku.TERSEDIA);
            }
            if (buku.getKondisi() == null) {
                buku.setKondisi(Buku.KondisiBuku.BAIK);
            }
            if (buku.getStok() == null) {
                buku.setStok(0);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan saat mengambil detail buku: " + e.getMessage());
            return "redirect:/member/dashboard";
        }

        List<Peminjaman> peminjamanAktif = peminjamanService.getPeminjamanAktif(user.getUserId());
        boolean bisaPinjam = true;
        
        if (peminjamanAktif != null) {
            for (Peminjaman p : peminjamanAktif) {
                if (p.getBuku() != null && p.getBuku().getBukuId() != null && p.getBuku().getBukuId().equals(buku.getBukuId())) {
                    bisaPinjam = false;
                    break;
                }
            }
        }
        
        bisaPinjam = bisaPinjam && buku.getStok() > 0 && 
                      (buku.getStatusBuku() == Buku.StatusBuku.TERSEDIA);

        model.addAttribute("member", user);
        model.addAttribute("buku", buku);
        model.addAttribute("bisaPinjam", bisaPinjam);
        model.addAttribute("StatusBukuEnum", Buku.StatusBuku.class);
        model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.class);
        
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

        peminjamanAktif = peminjamanAktif != null ? peminjamanAktif : Collections.emptyList();
        riwayatPeminjaman = riwayatPeminjaman != null ? riwayatPeminjaman : Collections.emptyList();

        model.addAttribute("member", user);
        model.addAttribute("peminjamanAktif", peminjamanAktif);
        model.addAttribute("riwayatPeminjaman", riwayatPeminjaman);
        model.addAttribute("StatusPeminjamanEnum", Peminjaman.StatusPeminjaman.class);
        model.addAttribute("StatusPembayaranEnum", Denda.StatusPembayaran.class);

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

        long hariTerlambat = 0;
        if (peminjaman.getTanggalPengembalian() != null) {
            LocalDate tanggalAkhirHitung = peminjaman.getTanggalDikembalikan() != null ? 
                                         peminjaman.getTanggalDikembalikan() : LocalDate.now();
            hariTerlambat = ChronoUnit.DAYS.between(peminjaman.getTanggalPengembalian(), tanggalAkhirHitung);
            if (hariTerlambat < 0) { 
                hariTerlambat = 0;
            }
        }

        model.addAttribute("denda", denda);
        model.addAttribute("peminjaman", peminjaman);
        model.addAttribute("hariTerlambat", hariTerlambat);
        model.addAttribute("member", user);
        
        model.addAttribute("StatusPeminjamanEnum", Peminjaman.StatusPeminjaman.class);
        model.addAttribute("StatusPembayaranEnum", Denda.StatusPembayaran.class);

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

        @GetMapping("/pengusulan") 
    public String managePengusulan(Model model) { 
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        model.addAttribute("pengusulan", new Pengusulan());
        List<Pengusulan> riwayatPengusulan = pengusulanService.getPengusulanBukuByUserId(user.getUserId());
        model.addAttribute("riwayatPengusulan", riwayatPengusulan); 
        model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class); 

        model.addAttribute("member", user);
        return "member/pengusulan"; 
    }

    @PostMapping("/pengusulan") 
    public String submitPengusulan(@ModelAttribute("pengusulan") Pengusulan pengusulan,
                                   RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        try {
            pengusulanService.usulkanBuku(user.getUserId(), pengusulan.getJudulBuku(), pengusulan.getPenulis(), 
                                           pengusulan.getPenerbit(), pengusulan.getKategori(), pengusulan.getTahunTerbit(), 
                                           pengusulan.getKeteranganPengusulan()); 
            redirectAttributes.addFlashAttribute("success", "Pengusulan buku berhasil dikirim!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengusulkan buku: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat mengusulkan buku.");
            e.printStackTrace();
        }
        return "redirect:/member/pengusulan"; 
    }
}