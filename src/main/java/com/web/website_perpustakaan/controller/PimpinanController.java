package com.web.website_perpustakaan.controller;

import com.web.website_perpustakaan.model.*;
import com.web.website_perpustakaan.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pimpinan")
@PreAuthorize("hasRole('PIMPINAN')")
public class PimpinanController {
    @Autowired
    private UserService userService;
    @Autowired
    private PeminjamanService peminjamanService;
    @Autowired
    private BukuService bukuService;
    @Autowired
    private MaintenanceService maintenanceService;
    @Autowired
    private PengusulanService pengusulanService;
    @Autowired
    private DendaService dendaService;
    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public String pimpinanDashboard(
            @RequestParam(value = "trendPeriod", defaultValue = "monthly") String trendPeriod,
            @RequestParam(value = "trendCount", defaultValue = "6") int trendCount,
            Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User pimpinan = userService.findByUsername(username);
        model.addAttribute("pimpinan", pimpinan);

        long totalBuku = bukuService.getTotalBuku();
        long totalPeminjamanAktif = peminjamanService.getTotalBukuDipinjam();
        long totalJumlahBukuTerlambat = peminjamanService.getJumlahBukuTerlambat();
        long totalBukuDalamMaintenance = maintenanceService.getTotalDalamMaintenance();
        long totalDendaBelumDibayar = dendaService.countAllDendaBelumDibayar();
        long totalPengusulanBaru = pengusulanService.countByStatusPengusulan("MENUNGGU_REVIEW");

        model.addAttribute("totalBuku", totalBuku);
        model.addAttribute("totalPeminjamanAktif", totalPeminjamanAktif);
        model.addAttribute("totalJumlahBukuTerlambat", totalJumlahBukuTerlambat);
        model.addAttribute("totalBukuDalamMaintenance", totalBukuDalamMaintenance);
        model.addAttribute("totalDendaBelumDibayar", totalDendaBelumDibayar);
        model.addAttribute("totalPengusulanBaru", totalPengusulanBaru);

        long totalDikembalikan = peminjamanService.countByStatusPeminjaman("DIKEMBALIKAN");
        model.addAttribute("peminjamanStatusLabels", List.of("Dikembalikan", "Dipinjam", "Terlambat"));
        model.addAttribute("peminjamanStatusData", List.of(totalDikembalikan, totalPeminjamanAktif, totalJumlahBukuTerlambat));

        Map<String, Long> bukuPerKategori = bukuService.countBukuPerKategori();
        model.addAttribute("bukuKategoriLabels", new ArrayList<>(bukuPerKategori.keySet()));
        model.addAttribute("bukuKategoriData", new ArrayList<>(bukuPerKategori.values()));

        Map<String, Long> peminjamanTrend = peminjamanService.getPeminjamanTrendData(trendPeriod, trendCount);
        model.addAttribute("peminjamanBulanLabels", new ArrayList<>(peminjamanTrend.keySet()));
        model.addAttribute("peminjamanBulanData", new ArrayList<>(peminjamanTrend.values()));
        model.addAttribute("trendPeriod", trendPeriod);
        model.addAttribute("trendCount", trendCount);

        return "pimpinan/dashboard";
    }

    @GetMapping("/dashboard/trend-data")
    @ResponseBody
    public Map<String, Object> getTrendData(
            @RequestParam(value = "trendPeriod", defaultValue = "monthly") String period,
            @RequestParam(value = "trendCount", defaultValue = "6") int count) {
        
        Map<String, Long> peminjamanTrend = peminjamanService.getPeminjamanTrendData(period, count);
        
        Map<String, Object> response = new HashMap<>();
        response.put("labels", new ArrayList<>(peminjamanTrend.keySet()));
        response.put("data", new ArrayList<>(peminjamanTrend.values()));
        
        return response;
    }

    @GetMapping("/laporan/download-excel")
    public ResponseEntity<Resource> downloadExcelReport(@RequestParam("type") String reportType) throws IOException {
        ByteArrayInputStream bis = reportService.generateExcelReport(reportType);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=laporan_" + reportType.toLowerCase() + ".xlsx"); 
        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/laporan/download-pdf")
    public ResponseEntity<Resource> downloadPdfReport(@RequestParam("type") String reportType) throws IOException {
        ByteArrayInputStream bis = reportService.generatePdfReport(reportType);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=laporan_" + reportType.toLowerCase() + ".pdf"); 

        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}