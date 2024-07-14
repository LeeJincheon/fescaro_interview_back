package com.fescaro.fescaro_interview_back.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "file_metadata")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "original_file_path")
    private String originalFilePath;

    @Column(name = "encrypted_file_name")
    private String encryptedFileName;

    @Column(name = "encrypted_file_path")
    private String encryptedFilePath;

    @Column(name = "iv")
    private String iv;

    @Column(name = "uploaded_at")
    private ZonedDateTime uploadedAt;

    @PrePersist
    private void prePersist() {
        this.uploadedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}
