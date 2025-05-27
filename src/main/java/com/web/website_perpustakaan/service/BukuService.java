package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.repository.BukuRepository;
import jakarta.validation.Valid;
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
    private static final String COVER_DIR = "src/main/resources/static/upload/images/";
    private static final String PDF_DIR = "src/main/resources/static/upload/pdfs/";

    public BukuService(BukuRepository bukuRepository) {
        this.bukuRepository = bukuRepository;
    }

    public void tambahBuku(@Valid Buku buku, MultipartFile coverFile, MultipartFile pdfFile) throws IOException {
        // Simpan buku ke database terlebih dahulu
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
            Path uploadPath = Paths.get(COVER_DIR + fileName);
            Files.createDirectories(uploadPath.getParent());
            Files.copy(coverFile.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            savedBuku.setCoverPath("/upload/images/" + fileName);
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
            Path pdfPath = Paths.get(PDF_DIR + pdfName);
            Files.createDirectories(pdfPath.getParent());
            Files.copy(pdfFile.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);
            savedBuku.setPdfPath("/upload/pdfs/" + pdfName);
        }

        // Simpan ulang untuk memperbarui path file
        bukuRepository.save(savedBuku);
    }

    public List<Buku> getSemuaBuku() {
        return bukuRepository.findAllByOrderByBukuIdAsc();
    }

    public Buku getBukuById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID buku tidak boleh null");
        }
        Optional<Buku> bukuOptional = bukuRepository.findById(id);
        if (bukuOptional.isEmpty()) {
            throw new IllegalArgumentException("Buku dengan ID " + id + " tidak ditemukan");
        }
        return bukuOptional.get();
    }
}