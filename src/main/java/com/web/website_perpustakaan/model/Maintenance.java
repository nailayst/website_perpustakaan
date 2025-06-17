package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance")
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Long maintenanceId;

    @ManyToOne
    @JoinColumn(name = "buku_id", nullable = false)
    private Buku buku;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_maintenance", nullable = false)
    private JenisMaintenance jenisMaintenance;

    @Column(name = "keterangan", nullable = false)
    private String keterangan;

    @Column(name = "tanggal_maintenance", nullable = false)
    private LocalDate tanggalMaintenance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusMaintenance status;

    public enum JenisMaintenance {
    PENARIKAN, PERBAIKAN
    }

    public enum StatusMaintenance {
    DALAM_PROSES, SELESAI, DITARIK_PERMANEN
    }

    public Long getMaintenanceId() { return maintenanceId; }
    public void setMaintenanceId(Long maintenanceId) { this.maintenanceId = maintenanceId; }
    public Buku getBuku() { return buku; }
    public void setBuku(Buku buku) { this.buku = buku; }
    public JenisMaintenance getJenisMaintenance() { return jenisMaintenance; }
    public void setJenisMaintenance(JenisMaintenance jenisMaintenance) { this.jenisMaintenance = jenisMaintenance; }
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    public LocalDate getTanggalMaintenance() { return tanggalMaintenance; }
    public void setTanggalMaintenance(LocalDate tanggalMaintenance) { this.tanggalMaintenance = tanggalMaintenance; }
    public StatusMaintenance getStatus() { return status; }
    public void setStatus(StatusMaintenance status) { this.status = status; }
}
