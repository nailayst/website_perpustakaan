package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Pengusulan; 
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.repository.PengusulanRepository;
import com.web.website_perpustakaan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PengusulanService {

    @Autowired
    private PengusulanRepository pengusulanRepository; 

    @Autowired
    private UserRepository userRepository;

    public Pengusulan usulkanBuku(Long userId, String judulBuku, String penulis, String penerbit, String kategori, Integer tahunTerbit, String keteranganPengusulan) {
        if (judulBuku == null || judulBuku.trim().isEmpty()) {
            throw new IllegalArgumentException("Judul buku tidak boleh kosong.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Pengguna dengan ID " + userId + " tidak ditemukan."));

        Pengusulan pengusulan = new Pengusulan();
        pengusulan.setUser(user);
        pengusulan.setJudulBuku(judulBuku.trim());
        pengusulan.setPenulis(penulis != null ? penulis.trim() : null);
        pengusulan.setPenerbit(penerbit != null ? penerbit.trim() : null);
        pengusulan.setKategori(kategori != null ? kategori.trim() : null);
        pengusulan.setTahunTerbit(tahunTerbit);
        pengusulan.setKeteranganPengusulan(keteranganPengusulan != null ? keteranganPengusulan.trim() : null); 

        return pengusulanRepository.save(pengusulan);
    }

    public List<Pengusulan> getAllPengusulanBuku() { 
        return pengusulanRepository.findAll();
    }

    public List<Pengusulan> getPengusulanBukuByStatus(Pengusulan.StatusPengusulan status) { 
        return pengusulanRepository.findByStatusPengusulan(status); 
    }

    public List<Pengusulan> getPengusulanBukuByUserId(Long userId) { 
        return pengusulanRepository.findByUser_UserIdOrderByTanggalPengusulanDesc(userId);
    }

    public Pengusulan updateStatusPengusulan(Long idPengusulan, Pengusulan.StatusPengusulan newStatus) { 
        if (newStatus == Pengusulan.StatusPengusulan.MENUNGGU_REVIEW) { 
            throw new IllegalArgumentException("Status pengusulan tidak dapat diatur kembali ke MENUNGGU_REVIEW melalui metode ini.");
        }

        Pengusulan pengusulan = pengusulanRepository.findById(idPengusulan) 
                .orElseThrow(() -> new IllegalArgumentException("Pengusulan buku dengan ID " + idPengusulan + " tidak ditemukan."));

        pengusulan.setStatusPengusulan(newStatus); 
        return pengusulanRepository.save(pengusulan);
    }

    public Optional<Pengusulan> getPengusulanBukuById(Long idPengusulan) { 
        return pengusulanRepository.findById(idPengusulan);
    }

    public void deletePengusulan(Long idPengusulan) { 
        pengusulanRepository.deleteById(idPengusulan);
    }
}