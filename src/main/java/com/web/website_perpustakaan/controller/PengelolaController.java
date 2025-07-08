package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.*;
import com.web.website_perpustakaan.model.Maintenance.JenisMaintenance;
import com.web.website_perpustakaan.model.Maintenance.StatusMaintenance;
import com.web.website_perpustakaan.repository.UserRepository;
import com.web.website_perpustakaan.service.*;
import jakarta.validation.Valid; // Perhatikan anotasi ini
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/pengelola")
@PreAuthorize("hasRole('PENGELOLA')")
public class PengelolaController {

    private final BukuService bukuService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PeminjamanService peminjamanService;
    @Autowired
    private MaintenanceService maintenanceService;
    @Autowired
    private PengusulanService pengusulanService;

    public PengelolaController(BukuService bukuService) {
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
    public String pengelolaDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        long totalBuku = bukuService.countAllBuku();
        long totalPeminjamanAktif = peminjamanService.countByStatusPeminjaman("DIPINJAM");
        long totalPengusulanBaru = pengusulanService.countByStatusPengusulan("MENUNGGU_REVIEW");
        long totalMembers = userRepository.countByLevelUser_LevelUser("member");
        long totalAdmins = userRepository.countByLevelUser_LevelUser("admin");
        long totalPengelolaUser = userRepository.countByLevelUser_LevelUser("pengelola");

        model.addAttribute("totalBuku", totalBuku);
        model.addAttribute("totalPeminjamanAktif", totalPeminjamanAktif);
        model.addAttribute("totalPengusulanBaru", totalPengusulanBaru);
        
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalPengelola", totalPengelolaUser);

        return "pengelola/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "username,asc") String sort,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);
        
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

        return "pengelola/users";
    }

    @GetMapping("/daftar-buku")
    public String listBuku(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "judul,asc") String sort,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(sortDirection, sortBy);

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        Page<Buku> bukuPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            bukuPage = bukuService.searchBukuPaginated(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            bukuPage = bukuService.getAllBukuPaginated(pageable);
        }

        model.addAttribute("bukuPage", bukuPage);
        model.addAttribute("currentPage", bukuPage.getNumber());
        model.addAttribute("pageSize", bukuPage.getSize());
        model.addAttribute("totalPages", bukuPage.getTotalPages());
        model.addAttribute("totalElements", bukuPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());
        
        model.addAttribute("StatusBukuEnum", Buku.StatusBuku.class);
        model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.class);

        return "pengelola/daftar-buku";
    }

    @GetMapping("/edit-buku/{id}")
    public String showEditBukuForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        try {
            Buku buku = bukuService.getBukuById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + id + " tidak ditemukan."));
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", true);

            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            
            return "pengelola/tambah-buku";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pengelola/daftar-buku";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memuat data edit buku: " + e.getMessage());
            return "redirect:/pengelola/daftar-buku";
        }
    }

    @PostMapping("/edit-buku")
    public String editBuku(
            @Valid @ModelAttribute("buku") Buku buku,
            BindingResult result,
            @RequestParam(value = "gambarBuku", required = false) MultipartFile gambarBuku,
            @RequestParam(value = "filePdf", required = false) MultipartFile filePdf,
            @RequestParam("bulanTerbit") int bulanTerbit,
            @RequestParam("tahunTerbit") int tahunTerbit,
            RedirectAttributes redirectAttributes) {
        try {
            if (bulanTerbit < 1 || bulanTerbit > 12) {
                redirectAttributes.addFlashAttribute("error", "Bulan terbit harus antara 1 dan 12");
                return "redirect:/pengelola/edit-buku/" + buku.getBukuId();
            }
            if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
                redirectAttributes.addFlashAttribute("error", "Tahun terbit tidak valid");
                return "redirect:/pengelola/edit-buku/" + buku.getBukuId();
            }

            buku.setTanggalTerbit(YearMonth.of(tahunTerbit, bulanTerbit));

            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Validasi buku gagal: " + result.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/pengelola/edit-buku/" + buku.getBukuId();
            }

            bukuService.updateBuku(buku,
                    (gambarBuku != null && !gambarBuku.isEmpty()) ? gambarBuku : null,
                    (filePdf != null && !filePdf.isEmpty()) ? filePdf : null);

            redirectAttributes.addFlashAttribute("success", "Buku berhasil diperbarui!");
            return "redirect:/pengelola/daftar-buku";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pengelola/edit-buku/" + buku.getBukuId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah atau memproses file: " + e.getMessage());
            return "redirect:/pengelola/edit-buku/" + buku.getBukuId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem: " + e.getMessage());
            return "redirect:/pengelola/edit-buku/" + buku.getBukuId();
        }
    }

    @PostMapping("/delete-buku/{id}")
    public String deleteBuku(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bukuService.deleteBuku(id);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus buku: " + e.getMessage());
        }
        return "redirect:/pengelola/daftar-buku";
    }

    @GetMapping("/tambah-buku")
    public String tampilkanFormTambahBuku(Model model, @RequestParam(value = "success", required = false) Boolean success) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        model.addAttribute("buku", new Buku());
        if (success != null && success) {
            model.addAttribute("berhasil", true);
            model.addAttribute("message", "Buku berhasil ditambahkan!");
        }
        model.addAttribute("isEditMode", false);
        model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
        model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
        return "pengelola/tambah-buku";
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
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        if (bulanTerbit < 1 || bulanTerbit > 12) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Bulan terbit harus antara 1 dan 12");
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/tambah-buku";
        }
        if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Tahun terbit tidak valid");
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/tambah-buku";
        }

        if (result.hasErrors()) {
            model.addAttribute("error", true);
            model.addAttribute("message", result.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/tambah-buku";
        }

        try {
            YearMonth yearMonth = YearMonth.of(tahunTerbit, bulanTerbit);
            buku.setTanggalTerbit(yearMonth);
            bukuService.tambahBuku(buku, gambarBuku, filePdf);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/pengelola/tambah-buku?success=true";
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Gagal menambahkan buku: " + e.getMessage());
            model.addAttribute("buku", buku);
            model.addAttribute("isEditMode", false);
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/tambah-buku";
        }
    }

    @GetMapping("/peminjaman")
    public String getPeminjaman(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "tanggalPeminjaman,desc") String sort,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(sortDirection, sortBy);

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        Page<Peminjaman> peminjamanPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            peminjamanPage = peminjamanService.searchPeminjamanPaginated(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            peminjamanPage = peminjamanService.getAllPeminjamanPaginated(pageable);
        }

        model.addAttribute("peminjamanPage", peminjamanPage);
        model.addAttribute("currentPage", peminjamanPage.getNumber());
        model.addAttribute("pageSize", peminjamanPage.getSize());
        model.addAttribute("totalPages", peminjamanPage.getTotalPages());
        model.addAttribute("totalElements", peminjamanPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());

        return "pengelola/peminjaman";
    }

    @GetMapping("/maintenance")
    public String getMaintenance(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "tanggalMaintenance,desc") String sort,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(sortDirection, sortBy);

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        Page<Maintenance> maintenancePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            maintenancePage = maintenanceService.searchMaintenancePaginated(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            maintenancePage = maintenanceService.getAllMaintenancePaginated(pageable);
        }
        
        List<Buku> daftarBuku = bukuService.getSemuaBuku();
        model.addAttribute("daftarBuku", daftarBuku);

        model.addAttribute("maintenancePage", maintenancePage);
        model.addAttribute("currentPage", maintenancePage.getNumber());
        model.addAttribute("pageSize", maintenancePage.getSize());
        model.addAttribute("totalPages", maintenancePage.getTotalPages());
        model.addAttribute("totalElements", maintenancePage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());

        return "pengelola/maintenance";
    }

    @GetMapping("/maintenance/tambah")
    public String showTambahMaintenanceForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        List<Buku> daftarBuku = bukuService.getSemuaBuku();
        model.addAttribute("daftarBuku", daftarBuku);
        model.addAttribute("maintenance", new Maintenance());
        model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
        return "pengelola/tambah-maintenance";
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
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        try {
            if (Objects.isNull(bukuId)) {
                model.addAttribute("error", "Pilih buku untuk maintenance.");
                model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
                model.addAttribute("maintenance", new Maintenance());
                model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
                return "pengelola/tambah-maintenance";
            }
            if (Objects.isNull(jenisMaintenance)) {
                model.addAttribute("error", "Pilih jenis maintenance.");
                model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
                model.addAttribute("maintenance", new Maintenance());
                model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
                return "pengelola/tambah-maintenance";
            }

            maintenanceService.mulaiMaintenance(bukuId, jenisMaintenance, keterangan);
            redirectAttributes.addFlashAttribute("success", "Maintenance berhasil dimulai.");
            return "redirect:/pengelola/maintenance";
        } catch (Exception e) {
            model.addAttribute("error", "Gagal memulai maintenance: " + e.getMessage());
            model.addAttribute("daftarBuku", bukuService.getSemuaBuku());
            model.addAttribute("maintenance", new Maintenance());
            model.addAttribute("jenisMaintenanceList", JenisMaintenance.values());
            return "pengelola/tambah-maintenance";
        }
    }

    @GetMapping("/maintenance/edit/{maintenanceId}")
    public String showEditMaintenanceForm(@PathVariable Long maintenanceId, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        try {
            Maintenance maintenance = maintenanceService.getMaintenanceById(maintenanceId)
                    .orElseThrow(() -> new IllegalArgumentException("Maintenance dengan ID " + maintenanceId + " tidak ditemukan."));

            if (maintenance.getStatus() != StatusMaintenance.DALAM_PROSES) {
                redirectAttributes.addFlashAttribute("error", "Maintenance dengan ID " + maintenanceId + " tidak dalam status 'Dalam Proses' untuk diedit.");
                return "redirect:/pengelola/maintenance";
            }

            // Tambahkan objek Buku ke model untuk form
            model.addAttribute("buku", maintenance.getBuku());
            model.addAttribute("maintenance", maintenance);
            model.addAttribute("isEditMode", true); // Ini mungkin tidak terlalu relevan di sini tapi tetap jaga

            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            
            // Tambahkan enum untuk dropdown jika ada Status/Kondisi Buku di form edit
            model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
            model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());

            return "pengelola/maintenance-edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pengelola/maintenance";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memuat data edit maintenance: " + e.getMessage());
            return "redirect:/pengelola/maintenance";
        }
    }

    @PostMapping("/maintenance/edit-buku")
    public String editBukuFromMaintenancePage(
            @RequestParam("maintenanceId") Long maintenanceId,
            @RequestParam("bukuId") Long bukuId,
            @RequestParam(value = "keteranganSelesai", required = false) String keteranganSelesai,
            @Valid @ModelAttribute("buku") Buku submittedBuku, // Gunakan @Valid untuk validasi
            BindingResult result,
            @RequestParam(value = "gambarBuku", required = false) MultipartFile gambarBuku,
            @RequestParam(value = "filePdf", required = false) MultipartFile filePdf,
            @RequestParam("bulanTerbit") int bulanTerbit,
            @RequestParam("tahunTerbit") int tahunTerbit,
            RedirectAttributes redirectAttributes,
            Model model // Tambahkan Model untuk mengembalikan view jika ada error
            ) {
        // Memastikan model "pengelola" tersedia untuk layout jika terjadi error
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        try {
            // Validasi tanggal terbit
            if (bulanTerbit < 1 || bulanTerbit > 12) {
                model.addAttribute("error", "Bulan terbit harus antara 1 dan 12"); // Gunakan error langsung ke model
                // Pastikan semua atribut yang dibutuhkan template dikirim kembali
                model.addAttribute("maintenance", maintenanceService.getMaintenanceById(maintenanceId).orElseThrow());
                model.addAttribute("buku", submittedBuku);
                model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
                model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());
                model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
                model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
                return "pengelola/maintenance-edit";
            }
            if (tahunTerbit < 1900 || tahunTerbit > YearMonth.now().getYear()) {
                model.addAttribute("error", "Tahun terbit tidak valid");
                model.addAttribute("maintenance", maintenanceService.getMaintenanceById(maintenanceId).orElseThrow());
                model.addAttribute("buku", submittedBuku);
                model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
                model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());
                model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
                model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
                return "pengelola/maintenance-edit";
            }
            submittedBuku.setTanggalTerbit(YearMonth.of(tahunTerbit, bulanTerbit));

            // Periksa hasil validasi @Valid
            if (result.hasErrors()) {
                model.addAttribute("error", "Validasi buku gagal: " + result.getAllErrors().get(0).getDefaultMessage());
                model.addAttribute("maintenance", maintenanceService.getMaintenanceById(maintenanceId).orElseThrow());
                model.addAttribute("buku", submittedBuku);
                model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
                model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());
                model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
                model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
                return "pengelola/maintenance-edit";
            }

            // Ambil objek Buku yang ada dari database untuk mempertahankan properti yang tidak diubah
            Buku existingBuku = bukuService.getBukuById(bukuId)
                    .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + bukuId + " tidak ditemukan."));

            // Salin properti yang diupdate dari submittedBuku ke existingBuku
            existingBuku.setJudul(submittedBuku.getJudul());
            existingBuku.setKodeBuku(submittedBuku.getKodeBuku()); // Penting! Ambil dari hidden input
            existingBuku.setIsbn(submittedBuku.getIsbn());
            existingBuku.setPenulis(submittedBuku.getPenulis());
            existingBuku.setPenerbit(submittedBuku.getPenerbit());
            existingBuku.setKategori(submittedBuku.getKategori());
            existingBuku.setStok(submittedBuku.getStok());
            existingBuku.setTanggalTerbit(submittedBuku.getTanggalTerbit());
            // Jika ada dropdown status atau kondisi buku di form, tambahkan baris ini:
            // existingBuku.setStatusBuku(submittedBuku.getStatusBuku());
            // existingBuku.setKondisi(submittedBuku.getKondisi());

            bukuService.updateBuku(existingBuku, // Kirim existingBuku yang sudah diupdate
                    (gambarBuku != null && !gambarBuku.isEmpty()) ? gambarBuku : null,
                    (filePdf != null && !filePdf.isEmpty()) ? filePdf : null);

            maintenanceService.selesaikanPerbaikanBuku(maintenanceId, keteranganSelesai);

            redirectAttributes.addFlashAttribute("success", "Maintenance selesai dan buku berhasil diperbarui!");
            return "redirect:/pengelola/maintenance";
        } catch (IllegalArgumentException e) {
            // Log error lebih detail
            System.err.println("IllegalArgumentException in editBukuFromMaintenancePage: " + e.getMessage());
            e.printStackTrace();
            // Kembalikan ke form dengan error message
            model.addAttribute("error", e.getMessage());
            // Pastikan semua atribut yang dibutuhkan template dikirim kembali
            try {
                model.addAttribute("maintenance", maintenanceService.getMaintenanceById(maintenanceId).orElseThrow());
            } catch (Exception ex) {
                // Tangani jika maintenance tidak ditemukan juga saat error
                redirectAttributes.addFlashAttribute("error", "Maintenance tidak ditemukan setelah error: " + ex.getMessage());
                return "redirect:/pengelola/maintenance";
            }
            model.addAttribute("buku", submittedBuku);
            model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
            model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/maintenance-edit";

        } catch (IOException e) {
            System.err.println("IOException in editBukuFromMaintenancePage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Gagal mengunggah atau memproses file: " + e.getMessage());
            // Pastikan semua atribut yang dibutuhkan template dikirim kembali
            try {
                model.addAttribute("maintenance", maintenanceService.getMaintenanceById(maintenanceId).orElseThrow());
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Maintenance tidak ditemukan setelah error: " + ex.getMessage());
                return "redirect:/pengelola/maintenance";
            }
            model.addAttribute("buku", submittedBuku);
            model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
            model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/maintenance-edit";

        } catch (Exception e) {
            System.err.println("Unexpected Exception in editBukuFromMaintenancePage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Terjadi kesalahan sistem: " + e.getMessage());
            // Pastikan semua atribut yang dibutuhkan template dikirim kembali
            try {
                model.addAttribute("maintenance", maintenanceService.getMaintenanceById(maintenanceId).orElseThrow());
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Maintenance tidak ditemukan setelah error: " + ex.getMessage());
                return "redirect:/pengelola/maintenance";
            }
            model.addAttribute("buku", submittedBuku);
            model.addAttribute("StatusBukuEnum", Buku.StatusBuku.values());
            model.addAttribute("KondisiBukuEnum", Buku.KondisiBuku.values());
            model.addAttribute("bulanTerbitList", java.util.stream.IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
            model.addAttribute("tahunTerbitList", java.util.stream.IntStream.rangeClosed(1900, YearMonth.now().getYear()).boxed().sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()));
            return "pengelola/maintenance-edit";
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Terjadi error tak terduga: " + ex.getMessage());
        return "redirect:/pengelola/maintenance";
    }

    @GetMapping("/pengusulan")
    public String listPengusulan(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "tanggalPengusulan,desc") String sort,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(sortDirection, sortBy);

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        Page<Pengusulan> pengusulanPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            pengusulanPage = pengusulanService.searchPengusulanPaginated(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            pengusulanPage = pengusulanService.getAllPengusulanPaginated(pageable);
        }

        model.addAttribute("pengusulanPage", pengusulanPage);
        model.addAttribute("currentPage", pengusulanPage.getNumber());
        model.addAttribute("pageSize", pengusulanPage.getSize());
        model.addAttribute("totalPages", pengusulanPage.getTotalPages());
        model.addAttribute("totalElements", pengusulanPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());

        model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class);

        return "pengelola/pengusulan";
    }

    @GetMapping("/pengusulan/{id}/detail")
    public String detailPengusulan(@PathVariable("id") Long idPengusulan, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        String username = auth.getName();
        User pengelola = userService.findByUsername(username);
        model.addAttribute("pengelola", pengelola);

        try {
            Pengusulan pengusulan = pengusulanService.getPengusulanBukuById(idPengusulan)
                    .orElseThrow(() -> new IllegalArgumentException("Pengusulan buku dengan ID " + idPengusulan + " tidak ditemukan."));

            model.addAttribute("pengusulan", pengusulan);
            model.addAttribute("StatusPengusulanEnum", Pengusulan.StatusPengusulan.class);
            return "pengelola/detail-pengusulan";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pengelola/pengusulan";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memuat detail usulan: " + e.getMessage());
            return "redirect:/pengelola/pengusulan";
        }
    }

    @PostMapping("/pengusulan/{id}/update-status")
    public String updatePengusulanStatus(@PathVariable("id") Long idPengusulan,
                                         @RequestParam("status") Pengusulan.StatusPengusulan newStatus,
                                         RedirectAttributes redirectAttributes) {
        try {
            pengusulanService.updateStatusPengusulan(idPengusulan, newStatus);
            redirectAttributes.addFlashAttribute("success", "Status pengusulan buku berhasil diperbarui!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem saat memperbarui status pengusulan.");
        }
        return "redirect:/pengelola/pengusulan/" + idPengusulan + "/detail";
    }

    @PostMapping("/pengusulan/{id}/delete")
    public String deletePengusulan(@PathVariable("id") Long idPengusulan, RedirectAttributes redirectAttributes) {
        try {
            pengusulanService.deletePengusulan(idPengusulan);
            redirectAttributes.addFlashAttribute("success", "Pengusulan buku berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus pengusulan buku: " + e.getMessage());
        }
        return "redirect:/pengelola/pengusulan";
    }

    @GetMapping("/profile")
    public String viewPengelolaProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return "redirect:/login";
        }

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setNamaLengkap(user.getUsername());
            user.setProfile(profile);
            userService.saveUser(user);
        }

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("pengelola", user);
        return "pengelola/profile";
    }

    @PostMapping("/profile")
    public String updatePengelolaProfile(@ModelAttribute("profile") Profile submittedProfile,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes,
                                         Principal principal) {
        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User tidak ditemukan.");
            return "redirect:/pengelola/profile";
        }

        Profile existingProfile = user.getProfile();
        if (existingProfile == null) {
            existingProfile = new Profile();
            user.setProfile(existingProfile);
        }

        existingProfile.setNamaLengkap(submittedProfile.getNamaLengkap());
        existingProfile.setJenisKelamin(submittedProfile.getJenisKelamin());

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui profil: " + e.getMessage());
            System.err.println("Error updating profile for user " + principal.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/pengelola/profile";
    }

    @PostMapping("/change-password")
    public String changePengelolaPassword(@RequestParam("oldPassword") String oldPassword,
                                          @RequestParam("newPassword") String newPassword,
                                          @RequestParam("confirmPassword") String confirmPassword,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password tidak cocok.");
            return "redirect:/pengelola/profile";
        }
        
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password baru minimal 8 karakter.");
            return "redirect:/pengelola/profile";
        }

        boolean result = userService.changePassword(principal.getName(), oldPassword, newPassword);
        if (result) {
            redirectAttributes.addFlashAttribute("success", "Password berhasil diubah.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Password lama salah atau terjadi masalah saat mengubah password.");
        }

        return "redirect:/pengelola/profile";
    }
}