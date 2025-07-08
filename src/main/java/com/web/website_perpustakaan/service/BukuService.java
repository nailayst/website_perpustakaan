package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.Buku.StatusBuku;
import com.web.website_perpustakaan.model.Buku.KondisiBuku;
import com.web.website_perpustakaan.repository.BukuRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;     
import org.springframework.data.domain.Pageable; 
import jakarta.transaction.Transactional; 

@Service
public class BukuService {

    private final BukuRepository bukuRepository;
    private final Path COVER_DIR;
    private final Path PDF_DIR;

    public BukuService(BukuRepository bukuRepository, @Value("${file.cover-dir}") String coverDir, @Value("${file.pdf-dir}") String pdfDir) {
        this.bukuRepository = bukuRepository;
        this.COVER_DIR = Paths.get(coverDir);
        this.PDF_DIR = Paths.get(pdfDir);
        try {
            Files.createDirectories(COVER_DIR);
            Files.createDirectories(PDF_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload: " + e.getMessage());
        }
    }

    @Transactional
    public void tambahBuku(@Valid Buku buku, MultipartFile coverFile, MultipartFile pdfFile) throws IOException {
        // Validasi kode buku unik
        if (bukuRepository.existsByKodeBuku(buku.getKodeBuku())) {
            throw new IllegalArgumentException("Kode buku '" + buku.getKodeBuku() + "' sudah ada. Harap gunakan kode unik lain.");
        }

        buku.setStatusBuku(StatusBuku.TERSEDIA);
        buku.setKondisi(KondisiBuku.BAIK);

        Buku savedBuku = bukuRepository.save(buku); // Simpan dulu untuk mendapatkan ID

        // Penanganan file hanya jika buku berhasil disimpan
        String coverPath = handleFileUpload(coverFile, savedBuku.getBukuId(), COVER_DIR, "cover", "image");
        if (coverPath != null) savedBuku.setCoverPath(coverPath);

        // PERBAIKAN DI SINI: expectedContentTypePrefix untuk PDF menjadi "application"
        String pdfPath = handleFileUpload(pdfFile, savedBuku.getBukuId(), PDF_DIR, "file", "application"); // <--- INI PERUBAHANNYA
        if (pdfPath != null) savedBuku.setPdfPath(pdfPath);

        bukuRepository.save(savedBuku); // Update buku dengan path file
    }

    @Transactional
    public Buku updateBuku(Buku updatedBuku, MultipartFile newCoverFile, MultipartFile newPdfFile) throws IOException {
        Buku existingBuku = bukuRepository.findById(updatedBuku.getBukuId())
                .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + updatedBuku.getBukuId() + " tidak ditemukan."));

        // Validasi kode buku unik, kecuali jika itu kode buku yang sama dengan buku yang sedang diedit
        if (!existingBuku.getKodeBuku().equalsIgnoreCase(updatedBuku.getKodeBuku()) && bukuRepository.existsByKodeBuku(updatedBuku.getKodeBuku())) {
            throw new IllegalArgumentException("Kode buku '" + updatedBuku.getKodeBuku() + "' sudah ada. Harap gunakan kode unik lain.");
        }

        // Perbarui field yang diizinkan untuk diupdate
        existingBuku.setJudul(updatedBuku.getJudul());
        existingBuku.setKodeBuku(updatedBuku.getKodeBuku());
        existingBuku.setIsbn(updatedBuku.getIsbn());
        existingBuku.setPenulis(updatedBuku.getPenulis());
        existingBuku.setPenerbit(updatedBuku.getPenerbit());
        existingBuku.setKategori(updatedBuku.getKategori());
        existingBuku.setStok(updatedBuku.getStok());
        existingBuku.setTanggalTerbit(updatedBuku.getTanggalTerbit());
        existingBuku.setStatusBuku(updatedBuku.getStatusBuku());
        existingBuku.setKondisi(updatedBuku.getKondisi()); 

        // Handle file upload/replacement for cover
        String newCoverPath = handleFileUpload(newCoverFile, existingBuku.getBukuId(), COVER_DIR, "cover", "image");
        if (newCoverPath != null) {
            deleteExistingFile(existingBuku.getCoverPath(), COVER_DIR);
            existingBuku.setCoverPath(newCoverPath);
        }

        // PERBAIKAN DI SINI: expectedContentTypePrefix untuk PDF menjadi "application"
        String newPdfPath = handleFileUpload(newPdfFile, existingBuku.getBukuId(), PDF_DIR, "file", "application"); // <--- INI PERUBAHANNYA
        if (newPdfPath != null) {
            deleteExistingFile(existingBuku.getPdfPath(), PDF_DIR);
            existingBuku.setPdfPath(newPdfPath);
        }

        return bukuRepository.save(existingBuku);
    }

    // Helper method untuk upload file (menghindari duplikasi kode)
    private String handleFileUpload(MultipartFile file, Long bukuId, Path directory, String type, String expectedContentTypePrefix) throws IOException {
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith(expectedContentTypePrefix)) {
                // Perbaiki pesan error agar lebih spesifik
                throw new IllegalArgumentException("File " + type + " harus berformat " + expectedContentTypePrefix + "/*."); // Contoh: image/* atau application/*
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Ukuran file " + type + " maksimal 10MB.");
            }

            String extension = "";
            // Logika ekstensi perlu disempurnakan agar lebih robust
            if (expectedContentTypePrefix.equals("image")) {
                // Ambil ekstensi dari contentType aslinya, misal "image/png" -> "png"
                extension = contentType.substring(contentType.lastIndexOf("/") + 1);
                // Khusus untuk JPEG, kita bisa konversi ke jpg jika diperlukan atau biarkan jpeg
                if ("jpeg".equals(extension)) {
                    extension = "jpg";
                }
            } else if (expectedContentTypePrefix.equals("application")) {
                extension = "pdf"; // Untuk PDF, kita tahu ekstensi pastinya
            }
            
            String fileName = bukuId + "_" + type + "." + extension;
            Path uploadPath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            return "/upload/" + (type.equals("cover") ? "images/" : "pdfs/") + fileName;
        }
        return null; // Tidak ada file baru diupload
    }

    // Helper method untuk menghapus file lama
    private void deleteExistingFile(String filePath, Path directory) throws IOException {
        if (filePath != null && !filePath.isEmpty()) {
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path oldFilePath = directory.resolve(fileName);
            Files.deleteIfExists(oldFilePath);
        }
    }

    public Optional<Buku> getBukuById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return bukuRepository.findById(id);
    }

    public List<Buku> getSemuaBuku() {
        return bukuRepository.findAllByOrderByBukuIdAsc();
    }

    public Page<Buku> getAllBukuPaginated(Pageable pageable) {
        return bukuRepository.findAll(pageable);
    }

    public List<Buku> searchBuku(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bukuRepository.findAll();
        }
        String lowerCaseKeyword = keyword.toLowerCase();
        return bukuRepository.findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCaseOrPenerbitContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword);
    }

    // NEW: Method untuk pencarian buku dengan paginasi (ditambah Kode Buku)
    public Page<Buku> searchBukuPaginated(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bukuRepository.findAll(pageable); // Jika keyword kosong, kembalikan semua dengan paginasi
        }
        String lowerCaseKeyword = keyword.toLowerCase();
        return bukuRepository.findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCaseOrPenerbitContainingIgnoreCaseOrIsbnContainingIgnoreCaseOrKodeBukuContainingIgnoreCase(
                lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword, pageable); // Ditambahkan lowerCaseKeyword untuk kodeBuku
    }

    // NEW: Method untuk menghapus buku (beserta file terkait)
    @Transactional
    public void deleteBuku(Long bukuId) throws IOException {
        Buku buku = bukuRepository.findById(bukuId)
                .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + bukuId + " tidak ditemukan."));

        // Hapus file cover jika ada
        if (buku.getCoverPath() != null && !buku.getCoverPath().isEmpty()) {
            String fileName = buku.getCoverPath().substring(buku.getCoverPath().lastIndexOf("/") + 1);
            Path filePath = COVER_DIR.resolve(fileName);
            Files.deleteIfExists(filePath);
        }

        // Hapus file PDF jika ada
        if (buku.getPdfPath() != null && !buku.getPdfPath().isEmpty()) {
            String fileName = buku.getPdfPath().substring(buku.getPdfPath().lastIndexOf("/") + 1);
            Path filePath = PDF_DIR.resolve(fileName);
            Files.deleteIfExists(filePath);
        }

        bukuRepository.deleteById(bukuId);
    }

    public List<Buku> getRekomendasiBuku() {
        return bukuRepository.findTop5ByOrderByTanggalTerbitDesc();
    }

    public long countAllBuku() {
        return bukuRepository.count();
    }

    public Map<String, Long> countBukuPerKategori() {
        return bukuRepository.findAll().stream()
                .collect(Collectors.groupingBy(Buku::getKategori, Collectors.counting()));
    }

    public long getTotalBuku() { 
        return bukuRepository.count();
    }
}