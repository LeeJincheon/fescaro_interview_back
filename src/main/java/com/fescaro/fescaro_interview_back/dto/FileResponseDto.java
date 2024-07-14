package com.fescaro.fescaro_interview_back.dto;

import com.fescaro.fescaro_interview_back.entity.FileMetadata;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class FileResponseDto {

    private String originalFilename;
    private String encryptedFilename;
    private String iv;
    private ZonedDateTime uploadedAt;

    public static FileResponseDto from(FileMetadata fileMetadata) {
        return FileResponseDto.builder()
                .originalFilename(fileMetadata.getOriginalFileName())
                .encryptedFilename(fileMetadata.getEncryptedFileName())
                .iv(fileMetadata.getIv())
                .uploadedAt(fileMetadata.getUploadedAt())
                .build();
    }
}
