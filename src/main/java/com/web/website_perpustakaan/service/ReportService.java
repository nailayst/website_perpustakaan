package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Denda;
import com.web.website_perpustakaan.model.Maintenance;
import com.web.website_perpustakaan.model.Peminjaman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Import kelas Apache POI secara eksplisit
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment; 
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ReportService {

    @Autowired
    private PeminjamanService peminjamanService;
    @Autowired
    private MaintenanceService maintenanceService;
    @Autowired
    private DendaService dendaService;

    @Autowired
    private HtmlToPdfService htmlToPdfService;

    public ByteArrayInputStream generateExcelReport(String reportType) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet;
        Row headerRow;
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);

        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        dateCellStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle numericCellStyle = workbook.createCellStyle();
        numericCellStyle.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle textCellStyle = workbook.createCellStyle();
        textCellStyle.setAlignment(HorizontalAlignment.LEFT);

        if ("peminjaman".equalsIgnoreCase(reportType)) {
            sheet = workbook.createSheet("Laporan Peminjaman");
            headerRow = sheet.createRow(0);

            String[] headers = {"Peminjaman ID", "Judul Buku", "Peminjam", "Tanggal Pinjam", 
                                "Tanggal Jatuh Tempo", "Tanggal Kembali", "Status", "Denda (Rp)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Peminjaman> peminjamanList = peminjamanService.getAllPeminjaman();
            int rowNum = 1;
            for (Peminjaman p : peminjamanList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getPeminjamanId());
                row.createCell(1).setCellValue(p.getBuku() != null ? p.getBuku().getJudul() : "N/A");
                row.createCell(2).setCellValue(p.getUser() != null && p.getUser().getProfile() != null ? p.getUser().getProfile().getNamaLengkap() : "N/A");
                
                Cell tglPinjamCell = row.createCell(3);
                if (p.getTanggalPeminjaman() != null) {
                    tglPinjamCell.setCellValue(p.getTanggalPeminjaman());
                    tglPinjamCell.setCellStyle(dateCellStyle);
                } else {
                    tglPinjamCell.setCellValue("N/A");
                    tglPinjamCell.setCellStyle(textCellStyle);
                }

                Cell tglJatuhTempoCell = row.createCell(4); 
                if (p.getTanggalPengembalian() != null) { 
                    tglJatuhTempoCell.setCellValue(p.getTanggalPengembalian());
                    tglJatuhTempoCell.setCellStyle(dateCellStyle);
                } else {
                    tglJatuhTempoCell.setCellValue("N/A");
                    tglJatuhTempoCell.setCellStyle(textCellStyle);
                }

                Cell tglDikembalikanCell = row.createCell(5);
                if (p.getTanggalDikembalikan() != null) {
                    tglDikembalikanCell.setCellValue(p.getTanggalDikembalikan());
                    tglDikembalikanCell.setCellStyle(dateCellStyle);
                } else {
                    tglDikembalikanCell.setCellValue("Belum");
                    tglDikembalikanCell.setCellStyle(textCellStyle);
                }
                
                row.createCell(6).setCellValue(p.getStatusPeminjaman() != null ? p.getStatusPeminjaman().name().replace("_", " ") : "N/A");
                
                Cell dendaCell = row.createCell(7);
                dendaCell.setCellValue(p.getDenda() != null ? p.getDenda().getJumlahDenda() : 0.00);
                dendaCell.setCellStyle(numericCellStyle);
            }
            
            sheet.setColumnWidth(0, 256 * 12);
            sheet.autoSizeColumn(1);
            sheet.setColumnWidth(2, 256 * 20);
            sheet.setColumnWidth(3, 256 * 14);
            sheet.setColumnWidth(4, 256 * 18);
            sheet.setColumnWidth(5, 256 * 16);
            sheet.setColumnWidth(6, 256 * 14);
            sheet.setColumnWidth(7, 256 * 15);


        } else if ("keterlambatan".equalsIgnoreCase(reportType)) {
            sheet = workbook.createSheet("Laporan Keterlambatan");
            headerRow = sheet.createRow(0);

            String[] headers = {"Peminjaman ID", "Judul Buku", "Peminjam", "Tanggal Jatuh Tempo", "Hari Terlambat", "Jumlah Denda"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Peminjaman> terlambatList = peminjamanService.getBukuTerlambatSaatIni();
            int rowNum = 1;
            for (Peminjaman p : terlambatList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getPeminjamanId());
                row.createCell(1).setCellValue(p.getBuku() != null ? p.getBuku().getJudul() : "N/A");
                row.createCell(2).setCellValue(p.getUser() != null && p.getUser().getProfile() != null ? p.getUser().getProfile().getNamaLengkap() : "N/A");
                
                Cell tglJatuhTempoCell = row.createCell(3);
                if (p.getTanggalPengembalian() != null) { 
                    tglJatuhTempoCell.setCellValue(p.getTanggalPengembalian());
                    tglJatuhTempoCell.setCellStyle(dateCellStyle);
                } else {
                    tglJatuhTempoCell.setCellValue("N/A");
                    tglJatuhTempoCell.setCellStyle(textCellStyle);
                }
                
                long hariTerlambat = 0;
                if (p.getTanggalPengembalian() != null && (p.getTanggalDikembalikan() == null || p.getTanggalDikembalikan().isAfter(p.getTanggalPengembalian()))) { 
                     LocalDate tanggalAkhirHitung = (p.getTanggalDikembalikan() != null) ? p.getTanggalDikembalikan() : LocalDate.now();
                     hariTerlambat = java.time.temporal.ChronoUnit.DAYS.between(p.getTanggalPengembalian(), tanggalAkhirHitung);
                     if (hariTerlambat < 0) hariTerlambat = 0; 
                }
                row.createCell(4).setCellValue(hariTerlambat > 0 ? hariTerlambat + " hari" : "0 hari");
                
                Cell dendaCell = row.createCell(5);
                dendaCell.setCellValue(p.getDenda() != null ? p.getDenda().getJumlahDenda() : 0.00);
                dendaCell.setCellStyle(numericCellStyle);
            }

            sheet.setColumnWidth(0, 256 * 12);
            sheet.autoSizeColumn(1);
            sheet.setColumnWidth(2, 256 * 20);
            sheet.setColumnWidth(3, 256 * 18);
            sheet.setColumnWidth(4, 256 * 16);
            sheet.setColumnWidth(5, 256 * 15);

        } else if ("perbaikan".equalsIgnoreCase(reportType)) {
            sheet = workbook.createSheet("Laporan Pemeliharaan");
            headerRow = sheet.createRow(0);

            String[] headers = {"Maintenance ID", "Judul Buku", "Tanggal Maintenance", "Keterangan", "Status"}; 
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Maintenance> maintenanceList = maintenanceService.getAllMaintenance();
            int rowNum = 1;
            for (Maintenance m : maintenanceList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getMaintenanceId());
                row.createCell(1).setCellValue(m.getBuku() != null ? m.getBuku().getJudul() : "N/A");
                
                Cell tglMaintenanceCell = row.createCell(2);
                if (m.getTanggalMaintenance() != null) {
                    tglMaintenanceCell.setCellValue(m.getTanggalMaintenance());
                    tglMaintenanceCell.setCellStyle(dateCellStyle);
                } else {
                    tglMaintenanceCell.setCellValue("N/A");
                    tglMaintenanceCell.setCellStyle(textCellStyle);
                }

                row.createCell(3).setCellValue(m.getKeterangan() != null ? m.getKeterangan() : "");
                row.createCell(4).setCellValue(m.getStatus() != null ? m.getStatus().name().replace("_", " ") : "N/A");
            }

            sheet.setColumnWidth(0, 256 * 18);
            sheet.autoSizeColumn(1);
            sheet.setColumnWidth(2, 256 * 18);
            sheet.setColumnWidth(3, 256 * 40);
            sheet.setColumnWidth(4, 256 * 15);

        } else if ("denda".equalsIgnoreCase(reportType)) {
            sheet = workbook.createSheet("Laporan Denda");
            headerRow = sheet.createRow(0);

            String[] headers = {"Denda ID", "Peminjaman ID", "Jumlah Denda", "Status Pembayaran", "Tanggal Dibuat", "Tanggal Pembayaran", "Keterangan"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Denda> dendaList = dendaService.getAllDenda();
            int rowNum = 1;
            for (Denda d : dendaList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(d.getDendaId());
                row.createCell(1).setCellValue(d.getPeminjamanId()); 
                
                Cell jumlahDendaCell = row.createCell(2);
                jumlahDendaCell.setCellValue(d.getJumlahDenda());
                jumlahDendaCell.setCellStyle(numericCellStyle);

                row.createCell(3).setCellValue(d.getStatusPembayaran() != null ? d.getStatusPembayaran().name().replace("_", " ") : "N/A");
                
                Cell tglDibuatCell = row.createCell(4);
                if (d.getTanggalDibuat() != null) {
                    tglDibuatCell.setCellValue(d.getTanggalDibuat());
                    tglDibuatCell.setCellStyle(dateCellStyle);
                } else {
                    tglDibuatCell.setCellValue("N/A");
                    tglDibuatCell.setCellStyle(textCellStyle);
                }

                Cell tglPembayaranCell = row.createCell(5);
                if (d.getTanggalPembayaran() != null) {
                    tglPembayaranCell.setCellValue(d.getTanggalPembayaran());
                    tglPembayaranCell.setCellStyle(dateCellStyle);
                } else {
                    tglPembayaranCell.setCellValue("Belum Dibayar");
                    tglPembayaranCell.setCellStyle(textCellStyle);
                }
                row.createCell(6).setCellValue(d.getKeterangan() != null ? d.getKeterangan() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

        } else {
            workbook.close();
            throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generatePdfReport(String reportType) throws IOException {
        Map<String, Object> variables = new HashMap<>(); 
        variables.put("reportType", reportType.toUpperCase().replace("_", " "));
        variables.put("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        String templateName;

        if ("peminjaman".equalsIgnoreCase(reportType)) {
            variables.put("dataList", peminjamanService.getAllPeminjaman());
            templateName = "pimpinan/reports/peminjaman_pdf_template";
        } else if ("keterlambatan".equalsIgnoreCase(reportType)) {
            variables.put("dataList", peminjamanService.getBukuTerlambatSaatIni());
            templateName = "pimpinan/reports/keterlambatan_pdf_template";
        } else if ("perbaikan".equalsIgnoreCase(reportType)) {
            variables.put("dataList", maintenanceService.getAllMaintenance());
            templateName = "pimpinan/reports/perbaikan_pdf_template";
        } else if ("denda".equalsIgnoreCase(reportType)) {
            variables.put("dataList", dendaService.getAllDenda());
            templateName = "pimpinan/reports/denda_pdf_template";
        } else {
            throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }

        try {
            byte[] pdfBytes = htmlToPdfService.generatePdfFromHtml(templateName, variables);
            return new ByteArrayInputStream(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }
}