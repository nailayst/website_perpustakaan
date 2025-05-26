package com.web.website_perpustakaan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    private static final String COVER_DIR = "upload/images/"; // Path relatif dari static
    private static final String PDF_DIR = "upload/pdfs/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rootPath = System.getProperty("user.dir") + "/src/main/resources/static/";
        File imagesDir = new File(rootPath + COVER_DIR);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        File pdfsDir = new File(rootPath + PDF_DIR);
        if (!pdfsDir.exists()) {
            pdfsDir.mkdirs();
        }

        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations("file:" + rootPath + COVER_DIR)
                .setCachePeriod(0);
        registry.addResourceHandler("/upload/pdfs/**")
                .addResourceLocations("file:" + rootPath + PDF_DIR)
                .setCachePeriod(0);
    }
}