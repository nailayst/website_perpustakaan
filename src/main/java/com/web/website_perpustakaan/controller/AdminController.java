package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.*;
import com.web.website_perpustakaan.model.Maintenance.JenisMaintenance;
import com.web.website_perpustakaan.model.Maintenance.StatusMaintenance;
import com.web.website_perpustakaan.repository.*;
import com.web.website_perpustakaan.service.*;
import jakarta.validation.Valid; // Pastikan ini diimport
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final BukuService bukuService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LevelUserRepository levelUserRepository;
    @Autowired
    private PeminjamanService peminjamanService;
    @Autowired
    private MaintenanceService maintenanceService;
    @Autowired
    private PengusulanService pengusulanService;

    public AdminController(BukuService bukuService) {
        this.bukuService = bukuService;
    }

    @GetMapping("/buku-data/{id}")
    @ResponseBody
    public ResponseEntity<Buku> getBukuData(@PathVariable Long id) {
        Optional<Buku> buku = bukuService.getBukuById(id);
        return buku.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        long totalMembers = userRepository.countByLevelUser_LevelUser("member");
        long totalAdmins = userRepository.countByLevelUser_LevelUser("admin");
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalAdmins", totalAdmins);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
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
        model.addAttribute("buku", new Buku());
        if (success != null && success) {
            model.addAttribute("berhasil", true);
            model.addAttribute("message", "Buku berhasil ditambahkan!");
        }
        model.addAttribute("isEditMode", false);
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
        if (bulanTerbit < 1 || bulanTerbit > 12) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Bulan terbit harus antara 1 dan 12");
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            return "admin/tambah-buku";
        }
        if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Tahun terbit tidak valid");
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            return "admin/tambah-buku";
        }

        if (result.hasErrors()) {
            model.addAttribute("error", true);
            model.addAttribute("message", result.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            return "admin/tambah-buku";
        }

        try {
            YearMonth yearMonth = YearMonth.of(tahunTerbit, bulanTerbit);
            buku.setTanggalTerbit(yearMonth);
            bukuService.tambahBuku(buku, gambarBuku, filePdf);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/admin/tambah-buku?success=true";
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Gagal menambahkan buku: " + e.getMessage());
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            return "admin/tambah-buku";
        }
    }

    @GetMapping("/peminjaman")
    public String getPeminjaman(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        List<Peminjaman> peminjamanList = peminjamanService.getAllPeminjaman();
        model.addAttribute("peminjamanList", peminjamanList);
        return "admin/peminjaman";
    }

    @GetMapping("/maintenance")
    public String getMaintenance(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        List<Buku> daftarBuku = java.util.Collections.emptyList();
        List<Maintenance> maintenanceList = java.util.Collections.emptyList();
        try {
            daftarBuku = bukuService.getSemuaBuku();
            maintenanceList = maintenanceService.getAllMaintenance();
        } catch (Exception e) {
            model.addAttribute("error", "Gagal memuat data maintenance: " + e.getMessage());
        }

        model.addAttribute("daftarBuku", daftarBuku);
        model.addAttribute("maintenanceList", maintenanceList);
        return "admin/daftar-maintenance";
    }

    @GetMapping("/maintenance/tambah")
    public String showTambahMaintenanceForm(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        List<Buku> daftarBuku = bukuService.getSemuaBuku();
        model.addAttribute("daftarBuku", daftarBuku);
        model.addAttribute("maintenance", new Maintenance());
        model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
        return "admin/tambah-maintenance";
    }

    @PostMapping("/maintenance/tambah")
    public String tambahMaintenance(
            @RequestParam(value = "bukuId", required = false) Long bukuId,
            @RequestParam(value = "jenisMaintenance", required = false) JenisMaintenance jenisMaintenance,
            @RequestParam("keterangan") String keterangan,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (Objects.isNull(bukuId)) {
                model.addAttribute("error", "Pilih buku untuk maintenance.");
                model.addAttribute("admin", userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
                model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
                model.addAttribute("maintenance", new Maintenance());
                model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
                return "admin/tambah-maintenance";
            }
            if (Objects.isNull(jenisMaintenance)) {
                model.addAttribute("error", "Pilih jenis maintenance.");
                model.addAttribute("admin", userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
                model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
                model.addAttribute("maintenance", new Maintenance());
                model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
                return "admin/tambah-maintenance";
            }

            maintenanceService.mulaiMaintenance(bukuId, jenisMaintenance, keterangan);
            redirectAttributes.addFlashAttribute("success", "Maintenance berhasil dimulai.");
            return "redirect:/admin/maintenance";
        } catch (Exception e) {
            model.addAttribute("error", "Gagal memulai maintenance: " + e.getMessage());
            model.addAttribute("admin", userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
            model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
            model.addAttribute("maintenance", new Maintenance());
            model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
            return "admin/tambah-maintenance";
        }
    }

    @GetMapping("/maintenance/edit/{maintenanceId}")
    public String showEditMaintenanceForm(@PathVariable Long maintenanceId, Model model, RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        try {
            Maintenance maintenance = maintenanceService.getMaintenanceById(maintenanceId)
                    .orElseThrow(() -> new IllegalArgumentException("Maintenance dengan ID " + maintenanceId + " tidak ditemukan."));

            if (maintenance.getStatus() != StatusMaintenance.DALAM_PROSES) {
                redirectAttributes.addFlashAttribute("error", "Maintenance dengan ID " + maintenanceId + " tidak dalam status 'Dalam Proses' untuk diedit.");
                return "redirect:/admin/maintenance";
            }

            Buku buku = maintenance.getBuku();
            model.addAttribute("maintenance", maintenance);
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", true);

            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));

            return "admin/maintenance-edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/maintenance";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memuat data edit maintenance: " + e.getMessage());
            return "redirect:/admin/maintenance";
        }
    }

    @PostMapping("/maintenance/edit-buku")
    public String editBukuFromMaintenancePage(
            @RequestParam("maintenanceId") Long maintenanceId,
            @RequestParam("bukuId") Long bukuId,
            @RequestParam(value = "keteranganSelesai", required = false) String keteranganSelesai,
            @Valid @ModelAttribute("buku") Buku buku, // Anotasi @Valid dikembalikan
            BindingResult result,
            @RequestParam(value = "gambarBuku", required = false) MultipartFile gambarBuku,
            @RequestParam(value = "filePdf", required = false) MultipartFile filePdf,
            @RequestParam("bulanTerbit") int bulanTerbit,
            @RequestParam("tahunTerbit") int tahunTerbit,
            RedirectAttributes redirectAttributes) {
        try {
            if (bulanTerbit < 1 || bulanTerbit > 12) {
                redirectAttributes.addFlashAttribute("error", "Bulan terbit harus antara 1 dan 12");
                return "redirect:/admin/maintenance/edit/" + maintenanceId;
            }
            if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
                redirectAttributes.addFlashAttribute("error", "Tahun terbit tidak valid");
                return "redirect:/admin/maintenance/edit/" + maintenanceId;
            }

            buku.setBukuId(bukuId);
            buku.setTanggalTerbit(YearMonth.of(tahunTerbit, bulanTerbit));

            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Validasi buku gagal: " + result.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/admin/maintenance/edit/" + maintenanceId;
            }

            bukuService.updateBuku(buku,
                    (gambarBuku != null && !gambarBuku.isEmpty()) ? gambarBuku : null,
                    (filePdf != null && !filePdf.isEmpty()) ? filePdf : null);

            maintenanceService.selesaikanPerbaikanBuku(maintenanceId, keteranganSelesai);

            redirectAttributes.addFlashAttribute("success", "Maintenance selesai dan buku berhasil diperbarui!");
            return "redirect:/admin/maintenance";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/maintenance/edit/" + maintenanceId;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah atau memproses file: " + e.getMessage());
            return "redirect:/admin/maintenance/edit/" + maintenanceId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem: " + e.getMessage());
            return "redirect:/admin/maintenance/edit/" + maintenanceId;
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Terjadi error tak terduga: " + ex.getMessage());
        return "redirect:/admin/maintenance";
    }

    @GetMapping("/pengusulan")
    public String listPengusulan(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        List<Pengusulan> pengusulanList = java.util.Collections.emptyList();
        try {
            pengusulanList = pengusulanService.getAllPengusulanBuku();
        } catch (Exception e) {
            model.addAttribute("error", "Gagal memuat data pengusulan buku: " + e.getMessage());
        }

        model.addAttribute("pengusulanList", pengusulanList);
        model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class);
        return "admin/pengusulan";
    }

    @GetMapping("/pengusulan/{id}/detail")
    public String detailPengusulan(@PathVariable("id") Long idPengusulan, Model model, RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userService.findByUsername(username);
        model.addAttribute("admin", admin);

        try {
            Pengusulan pengusulan = pengusulanService.getPengusulanBukuById(idPengusulan)
                    .orElseThrow(() -> new IllegalArgumentException("Pengusulan buku dengan ID " + idPengusulan + " tidak ditemukan."));

            model.addAttribute("pengusulan", pengusulan);
            model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class);
            return "admin/detail-pengusulan";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pengusulan";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memuat detail usulan: " + e.getMessage());
            return "redirect:/admin/pengusulan";
        }
    }

    @PostMapping("/pengusulan/{id}/update-status")
    public String updatePengusulanStatus(@PathVariable("id") Long idPengusulan,
                                         @RequestParam("status") Pengusulan.StatusPengusulan newStatus,
                                         RedirectAttributes redirectAttributes) {
        try {
            pengusulanService.updateStatusPengusulan(idPengusulan, newStatus);
            redirectAttributes.addFlashAttribute("success", "Status pengusulan buku berhasil diperbarui!");
        }  catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memperbarui status pengusulan.");
        }
        return "redirect:/admin/pengusulan/" + idPengusulan + "/detail";
    }

    @PostMapping("/pengusulan/{id}/delete")
    public String deletePengusulan(@PathVariable("id") Long idPengusulan, RedirectAttributes redirectAttributes) {
        try {
            pengusulanService.deletePengusulan(idPengusulan);
            redirectAttributes.addFlashAttribute("success", "Pengusulan buku berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus pengusulan buku: " + e.getMessage());
        }
        return "redirect:/admin/pengusulan";
    }
}