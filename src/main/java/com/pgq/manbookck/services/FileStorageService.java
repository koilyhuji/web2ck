package com.pgq.manbookck.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path coverStorageLocation;
    private final Path ebookStorageLocation;

    public enum FileType { COVER, EBOOK }

    public FileStorageService(@Value("${app.upload.dir.covers}") String coverUploadDir,
                              @Value("${app.upload.dir.ebooks}") String ebookUploadDir) {
        this.coverStorageLocation = Paths.get(coverUploadDir).toAbsolutePath().normalize();
        this.ebookStorageLocation = Paths.get(ebookUploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.coverStorageLocation);
            Files.createDirectories(this.ebookStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file, FileType fileType) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            // no extension
        }
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation;
        if (fileType == FileType.COVER) {
            targetLocation = this.coverStorageLocation.resolve(storedFileName);
        } else {
            targetLocation = this.ebookStorageLocation.resolve(storedFileName);
        }

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName; // Return only the filename, not the full path
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName, FileType fileType) {
        try {
            Path filePath;
            if (fileType == FileType.COVER) {
                filePath = this.coverStorageLocation.resolve(fileName).normalize();
            } else {
                filePath = this.ebookStorageLocation.resolve(fileName).normalize();
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName, FileType fileType) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Path filePath;
            if (fileType == FileType.COVER) {
                filePath = this.coverStorageLocation.resolve(fileName).normalize();
            } else {
                filePath = this.ebookStorageLocation.resolve(fileName).normalize();
            }
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            System.err.println("Could not delete file: " + fileName + " - " + ex.getMessage());
        }
    }
}