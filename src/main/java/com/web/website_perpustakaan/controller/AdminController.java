package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.*;
import com.web.website_perpustakaan.model.Maintenance.JenisMaintenance; 
import com.web.website_perpustakaan.repository.*;
import com.web.website_perpustakaan.service.*;
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

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects; 

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

    @Autowired
    private PeminjamanService peminjamanService;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired 
    private PengusulanService pengusulanService; 

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
        List<Peminjaman> peminjamanList = peminjamanService.getAllPeminjaman();
        model.addAttribute("admin", admin);
        model.addAttribute("peminjamanList", peminjamanList);
        return "admin/peminjaman";
    }

    @GetMapping("/maintenance")
    public String getMaintenance(Model model) {
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
        
        List<Buku> daftarBuku = java.util.Collections.emptyList(); 
        List<Maintenance> maintenanceList = java.util.Collections.emptyList(); 
        try {
            daftarBuku = bukuService.getSemuaBuku();
            maintenanceList = maintenanceService.getAllMaintenance();
        } catch (Exception e) {
            model.addAttribute("error", "Gagal memuat data maintenance: " + e.getMessage());
            e.printStackTrace(); 
        }

        model.addAttribute("admin", admin);
        model.addAttribute("daftarBuku", daftarBuku); 
        model.addAttribute("maintenanceList", maintenanceList); 
        return "admin/maintenance";
    }

    @GetMapping("/maintenance/tambah")
    public String showTambahMaintenanceForm(Model model) {
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
        List<Buku> daftarBuku = bukuService.getSemuaBuku();
        model.addAttribute("admin", admin);
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
        
        try {
            if (Objects.isNull(bukuId)) { 
                model.addAttribute("error", "Pilih buku untuk maintenance."); 
                model.addAttribute("admin", admin);
                model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
                model.addAttribute("maintenance", new Maintenance());
                model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
                return "admin/tambah-maintenance"; 
            }
            if (Objects.isNull(jenisMaintenance)) { 
                model.addAttribute("error", "Pilih jenis maintenance.");
                model.addAttribute("admin", admin);
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
            model.addAttribute("admin", admin);
            model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
            model.addAttribute("maintenance", new Maintenance()); 
            model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
            return "admin/tambah-maintenance"; 
        }
    }

    @PostMapping("/maintenance/edit-buku-from-modal")
    public String editBukuFromMaintenanceModal(
            @RequestParam("maintenanceId") Long maintenanceId,
            @RequestParam("bukuId") Long bukuId,
            @RequestParam(value = "keteranganSelesai", required = false) String keteranganSelesai,
            @Valid @ModelAttribute("buku") Buku buku, 
            BindingResult result, 
            @RequestParam(value = "gambarBuku", required = false) MultipartFile gambarBuku,
            @RequestParam(value = "filePdf", required = false) MultipartFile filePdf,
            @RequestParam("bulanTerbit") int bulanTerbit,
            @RequestParam("tahunTerbit") int tahunTerbit,
            RedirectAttributes redirectAttributes,
            Model model 
            ) {
        
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

        try {
            maintenanceService.getBukuForMaintenanceEdit(maintenanceId, keteranganSelesai); 

            YearMonth yearMonth = YearMonth.of(tahunTerbit, bulanTerbit);
            buku.setTanggalTerbit(yearMonth);
            buku.setBukuId(bukuId); 

            if (bulanTerbit < 1 || bulanTerbit > 12) {
                redirectAttributes.addFlashAttribute("error", "Bulan terbit harus antara 1 dan 12");
                return "redirect:/admin/maintenance"; 
            }
            if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
                redirectAttributes.addFlashAttribute("error", "Tahun terbit tidak valid");
                return "redirect:/admin/maintenance";
            }
            if (result.hasErrors()) {
                 redirectAttributes.addFlashAttribute("error", "Validasi buku gagal: " + result.getAllErrors().get(0).getDefaultMessage());
                 return "redirect:/admin/maintenance";
            }
            
            bukuService.updateBuku(buku, gambarBuku, filePdf);
            redirectAttributes.addFlashAttribute("success", "Maintenance selesai dan buku berhasil diperbarui!");
            return "redirect:/admin/maintenance";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/maintenance";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah file: " + e.getMessage());
            return "redirect:/admin/maintenance";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memperbarui buku: " + e.getMessage());
            e.printStackTrace(); 
            return "redirect:/admin/maintenance";
        }
    }

        @GetMapping("/pengusulan") 
    public String listPengusulan(Model model) { 
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

        List<Pengusulan> pengusulanList = java.util.Collections.emptyList(); 
        try {
            pengusulanList = pengusulanService.getAllPengusulanBuku(); 
        } catch (Exception e) {
            model.addAttribute("error", "Gagal memuat data pengusulan buku: " + e.getMessage());
            e.printStackTrace();
        }
        
        model.addAttribute("admin", admin);
        model.addAttribute("pengusulanList", pengusulanList); 
        model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class); 
        return "admin/pengusulan"; 
    }

    // Endpoint untuk menampilkan detail pengusulan (BARU)
    @GetMapping("/pengusulan/{id}/detail")
    public String detailPengusulan(@PathVariable("id") Long idPengusulan, Model model, RedirectAttributes redirectAttributes) {
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

        try {
            Pengusulan pengusulan = pengusulanService.getPengusulanBukuById(idPengusulan)
                .orElseThrow(() -> new IllegalArgumentException("Pengusulan buku dengan ID " + idPengusulan + " tidak ditemukan."));
            
            model.addAttribute("admin", admin);
            model.addAttribute("pengusulan", pengusulan);
            model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class);
            return "admin/detail-pengusulan"; // Akan membuat file HTML ini
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pengusulan";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memuat detail usulan: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/pengusulan";
        }
    }


    @PostMapping("/pengusulan/{id}/update-status") 
    public String updatePengusulanStatus(@PathVariable("id") Long idPengusulan, 
                                     @RequestParam("status") Pengusulan.StatusPengusulan newStatus, 
                                     RedirectAttributes redirectAttributes) {
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

        try {
            pengusulanService.updateStatusPengusulan(idPengusulan, newStatus);
            redirectAttributes.addFlashAttribute("success", "Status pengusulan buku berhasil diperbarui!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memperbarui status pengusulan.");
            e.printStackTrace();
        }
        return "redirect:/admin/pengusulan/" + idPengusulan + "/detail"; // Redirect ke halaman detail setelah update
    }

    @PostMapping("/pengusulan/{id}/delete") 
    public String deletePengusulan(@PathVariable("id") Long idPengusulan, RedirectAttributes redirectAttributes) { 
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

        try {
            pengusulanService.deletePengusulan(idPengusulan);
            redirectAttributes.addFlashAttribute("success", "Pengusulan buku berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus pengusulan buku: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/pengusulan"; 
    }
}