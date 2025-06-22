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
import java.util.Optional;

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

    public void tambahBuku(@Valid Buku buku, MultipartFile coverFile, MultipartFile pdfFile) throws IOException {
        buku.setStatusBuku(StatusBuku.TERSEDIA);
        buku.setKondisi(KondisiBuku.BAIK);

        Buku savedBuku = bukuRepository.save(buku);
        Long bukuId = savedBuku.getBukuId();

        if (coverFile != null && !coverFile.isEmpty()) {
            String contentType = coverFile.getContentType();
            if (contentType == null || (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType))) {
                throw new IllegalArgumentException("File cover harus JPG atau PNG.");
            }
            if (coverFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Ukuran file cover maksimal 10MB.");
            }

            String extension = "image/jpeg".equals(contentType) ? "jpg" : "png";
            String fileName = bukuId + "_cover." + extension;
            Path uploadPath = COVER_DIR.resolve(fileName);
            Files.copy(coverFile.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            savedBuku.setCoverPath("/upload/images/" + fileName);
        }

        if (pdfFile != null && !pdfFile.isEmpty()) {
            if (!"application/pdf".equals(pdfFile.getContentType())) {
                throw new IllegalArgumentException("File harus PDF.");
            }
            if (pdfFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Ukuran file PDF maksimal 10MB.");
            }

            String pdfName = bukuId + "_file.pdf";
            Path pdfPath = PDF_DIR.resolve(pdfName);
            Files.copy(pdfFile.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);
            savedBuku.setPdfPath("/upload/pdfs/" + pdfName);
        }

        bukuRepository.save(savedBuku);
    }

    public Buku updateBuku(Buku updatedBuku, MultipartFile newCoverFile, MultipartFile newPdfFile) throws IOException {
        Buku existingBuku = bukuRepository.findById(updatedBuku.getBukuId())
                .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + updatedBuku.getBukuId() + " tidak ditemukan."));

        existingBuku.setJudul(updatedBuku.getJudul());
        existingBuku.setIsbn(updatedBuku.getIsbn());
        existingBuku.setPenulis(updatedBuku.getPenulis());
        existingBuku.setPenerbit(updatedBuku.getPenerbit());
        existingBuku.setKategori(updatedBuku.getKategori());
        existingBuku.setStok(updatedBuku.getStok());
        existingBuku.setTanggalTerbit(updatedBuku.getTanggalTerbit());

        if (newCoverFile != null && !newCoverFile.isEmpty()) {
            String contentType = newCoverFile.getContentType();
            if (contentType == null || (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType))) {
                throw new IllegalArgumentException("File cover harus JPG atau PNG.");
            }
            if (newCoverFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Ukuran file cover maksimal 10MB.");
            }

            if (existingBuku.getCoverPath() != null && !existingBuku.getCoverPath().isEmpty()) {
                String oldFileName = existingBuku.getCoverPath().substring(existingBuku.getCoverPath().lastIndexOf("/") + 1);
                Path oldFilePath = COVER_DIR.resolve(oldFileName);
                Files.deleteIfExists(oldFilePath);
            }

            String extension = "image/jpeg".equals(contentType) ? "jpg" : "png";
            String newFileName = existingBuku.getBukuId() + "_cover." + extension;
            Path newFilePath = COVER_DIR.resolve(newFileName);
            Files.copy(newCoverFile.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
            existingBuku.setCoverPath("/upload/images/" + newFileName);
        }

        if (newPdfFile != null && !newPdfFile.isEmpty()) {
            if (!"application/pdf".equals(newPdfFile.getContentType())) {
                throw new IllegalArgumentException("File harus PDF.");
            }
            if (newPdfFile.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Ukuran file PDF maksimal 10MB.");
            }

            if (existingBuku.getPdfPath() != null && !existingBuku.getPdfPath().isEmpty()) {
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
            return Optional.empty();
        }
        return bukuRepository.findById(id);
    }

    public List<Buku> getSemuaBuku() {
        return bukuRepository.findAllByOrderByBukuIdAsc();
    }

    public List<Buku> searchBuku(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bukuRepository.findAll();
        }
        String lowerCaseKeyword = keyword.toLowerCase();
        return bukuRepository.findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCaseOrPenerbitContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword, lowerCaseKeyword);
    }

    public List<Buku> getRekomendasiBuku() {
        return bukuRepository.findTop5ByOrderByTanggalTerbitDesc();
    }
}