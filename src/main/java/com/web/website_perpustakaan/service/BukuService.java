package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.Buku.StatusBuku; // Pastikan import ini ada
import com.web.website_perpustakaan.model.Buku.KondisiBuku; // Pastikan import ini ada
import com.web.website_perpustakaan.repository.BukuRepository;
import jakarta.validation.Valid; // Mungkin tidak perlu jika validasi di controller
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class BukuService {

    private final BukuRepository bukuRepository;
    // Gunakan Path.of() untuk membangun Path yang lebih aman
    private static final Path COVER_DIR = Paths.get("src/main/resources/static/upload/images/");
    private static final Path PDF_DIR = Paths.get("src/main/resources/static/upload/pdfs/");

    public BukuService(BukuRepository bukuRepository) {
        this.bukuRepository = bukuRepository;
        try {
            // Pastikan direktori ada saat aplikasi dimulai
            Files.createDirectories(COVER_DIR);
            Files.createDirectories(PDF_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload: " + e.getMessage(), e);
        }
    }

    public void tambahBuku(@Valid Buku buku, MultipartFile coverFile, MultipartFile pdfFile) throws IOException {
        // Set status dan kondisi default saat menambah buku baru
        buku.setStatusBuku(StatusBuku.TERSEDIA);
        buku.setKondisi(KondisiBuku.BAIK);

        // Simpan buku ke database terlebih dahulu untuk mendapatkan ID
        Buku savedBuku = bukuRepository.save(buku);
        Long bukuId = savedBuku.getBukuId();

        // Proses file cover
        if (coverFile != null && !coverFile.isEmpty()) {
            String contentType = coverFile.getContentType();
            if (contentType == null || (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType))) {
                throw new IllegalArgumentException("File cover harus JPG atau PNG");
            }
            if (coverFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File cover maksimal 10MB");
            }

            String extension = "image/jpeg".equals(contentType) ? "jpg" : "png";
            String fileName = bukuId + "_cover." + extension;
            Path uploadPath = COVER_DIR.resolve(fileName); // Menggunakan resolve
            Files.copy(coverFile.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            savedBuku.setCoverPath("/upload/images/" + fileName); // Path relatif untuk web
        }

        // Proses file PDF
        if (pdfFile != null && !pdfFile.isEmpty()) {
            if (!"application/pdf".equals(pdfFile.getContentType())) {
                throw new IllegalArgumentException("File harus PDF");
            }
            if (pdfFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File PDF maksimal 10MB");
            }

            String pdfName = bukuId + "_file.pdf";
            Path pdfPath = PDF_DIR.resolve(pdfName); // Menggunakan resolve
            Files.createDirectories(pdfPath.getParent()); // Pastikan direktori induk ada
            Files.copy(pdfFile.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);
            savedBuku.setPdfPath("/upload/pdfs/" + pdfName); // Path relatif untuk web
        }

        // Simpan ulang untuk memperbarui path file
        bukuRepository.save(savedBuku);
    }

    // Metode BARU: updateBuku (akan dipanggil dari modal edit)
    public Buku updateBuku(Buku updatedBuku, MultipartFile newCoverFile, MultipartFile newPdfFile) throws IOException {
        Buku existingBuku = bukuRepository.findById(updatedBuku.getBukuId())
                .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + updatedBuku.getBukuId() + " tidak ditemukan."));

        // Update properti buku (kecuali path file, karena itu ditangani secara terpisah)
        existingBuku.setJudul(updatedBuku.getJudul());
        existingBuku.setIsbn(updatedBuku.getIsbn());
        existingBuku.setPenulis(updatedBuku.getPenulis());
        existingBuku.setPenerbit(updatedBuku.getPenerbit());
        existingBuku.setKategori(updatedBuku.getKategori());
        existingBuku.setStok(updatedBuku.getStok());
        existingBuku.setTanggalTerbit(updatedBuku.getTanggalTerbit());

        // Setelah maintenance, buku seharusnya kembali BAIK dan TERSEDIA
        existingBuku.setStatusBuku(StatusBuku.TERSEDIA);
        existingBuku.setKondisi(KondisiBuku.BAIK);

        // Handle update cover file
        if (newCoverFile != null && !newCoverFile.isEmpty()) {
            String contentType = newCoverFile.getContentType();
            if (contentType == null || (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType))) {
                throw new IllegalArgumentException("File cover harus JPG atau PNG");
            }
            if (newCoverFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File cover maksimal 10MB");
            }

            // Hapus file lama jika ada dan berbeda
            if (existingBuku.getCoverPath() != null) {
                // Ekstrak nama file dari path relatif
                String oldFileName = existingBuku.getCoverPath().substring(existingBuku.getCoverPath().lastIndexOf("/") + 1);
                Path oldFilePath = COVER_DIR.resolve(oldFileName);
                Files.deleteIfExists(oldFilePath);
            }

            // Simpan file baru
            String extension = "image/jpeg".equals(contentType) ? "jpg" : "png";
            String newFileName = existingBuku.getBukuId() + "_cover." + extension;
            Path newFilePath = COVER_DIR.resolve(newFileName);
            Files.copy(newCoverFile.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
            existingBuku.setCoverPath("/upload/images/" + newFileName);
        }

        if (newPdfFile != null && !newPdfFile.isEmpty()) {
            if (!"application/pdf".equals(newPdfFile.getContentType())) {
                throw new IllegalArgumentException("File harus PDF");
            }
            if (newPdfFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File PDF maksimal 10MB");
            }

            if (existingBuku.getPdfPath() != null) {
                 String oldFileName = existingBuku.getPdfPath().substring(existingBuku.getPdfPath().lastIndexOf("/") + 1);
                 Path oldFilePath = PDF_DIR.resolve(oldFileName);
                 Files.deleteIfExists(oldFilePath);
            }

            String newFileName = existingBuku.getBukuId() + "_file.pdf";
            Path newFilePath = PDF_DIR.resolve(newFileName);
            Files.copy(newPdfFile.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
            existingBuku.setPdfPath("/upload/pdfs/" + newFileName);
        }
        return bukuRepository.save(existingBuku);
    }


    public Optional<Buku> getBukuById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID buku tidak boleh null");
        }
        return bukuRepository.findById(id); 
    }

    public List<Buku> getSemuaBuku() {
        return bukuRepository.findAllByOrderByBukuIdAsc();
    }
}