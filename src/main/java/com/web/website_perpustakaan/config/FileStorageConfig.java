package com.web.website_perpustakaan.config;

import jakarta.annotation.PostConstruct; 

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; 
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.cover-dir}")
    private String coverDirPath;

    @Value("${file.pdf-dir}")
    private String pdfDirPath;

    private String absoluteCoverPath;
    private String absolutePdfPath;

    @PostConstruct
    public void init() {
        try {
            Path coverPath = Paths.get(coverDirPath).toAbsolutePath().normalize();
            Path pdfsPath = Paths.get(pdfDirPath).toAbsolutePath().normalize();

            Files.createDirectories(coverPath);
            Files.createDirectories(pdfsPath);

            this.absoluteCoverPath = "file:" + coverPath.toString() + "/";
            this.absolutePdfPath = "file:" + pdfsPath.toString() + "/";

            System.out.println("Cover upload directory: " + absoluteCoverPath);
            System.out.println("PDF upload directory: " + absolutePdfPath);

        } catch (IOException e) {
            System.err.println("Failed to create upload directories: " + e.getMessage());
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) { 
        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations(absoluteCoverPath) 
                .setCachePeriod(0); 

        registry.addResourceHandler("/upload/pdfs/**")
                .addResourceLocations(absolutePdfPath)
                .setCachePeriod(0);
    }
}