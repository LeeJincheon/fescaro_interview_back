package com.fescaro.fescaro_interview_back.service;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import com.fescaro.fescaro_interview_back.entity.FileMetadata;
import com.fescaro.fescaro_interview_back.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String SECRET_KEY = "0123456789abcdef";
    private static final String UPLOADED_PATH = "C:/fescaro-interview/uploaded/";
    private static final String ENCRYPTED_PATH = "C:/fescaro-interview/encrypted/";

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public Page<FileResponseDto> findFiles(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return fileMetadataRepository.findAll(pageRequest)
                .map(FileResponseDto::from);
    }

    @Override
    public void upload(MultipartFile file) throws Exception {
        // try-catch 활용

        if (file.isEmpty()) {
            // 빈 파일인 경우 예외 처리 로직
        }

        File uploadedFileDirectory = new File(UPLOADED_PATH);
        File encryptedFileDirectory = new File(ENCRYPTED_PATH);
        if (!uploadedFileDirectory.exists()) {
            uploadedFileDirectory.mkdirs();
        }
        if (!encryptedFileDirectory.exists()) {
            encryptedFileDirectory.mkdirs();
        }

        // 파일 이름 구분
        String originalFilename = file.getOriginalFilename();
        String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String encryptedFilename = baseFilename + "_enc" + extension;

        // 원본 파일 저장
        File originalFile = new File(UPLOADED_PATH + originalFilename);
        file.transferTo(originalFile); // MultipartFile -> File

        // 암호화 결과 파일 생성
        File encryptedFile = new File(ENCRYPTED_PATH + encryptedFilename);

        // 파일 암호화
        byte[] iv = createIv();
        encryptFile(SECRET_KEY, iv, originalFile, encryptedFile);

        // 파일 정보 DB에 저장
        FileMetadata fileMetadata = FileMetadata.builder()
                .originalFileName(originalFilename)
                .originalFilePath(originalFile.getAbsolutePath())
                .encryptedFileName(encryptedFilename)
                .encryptedFilePath(encryptedFile.getAbsolutePath())
                .iv(Base64.getEncoder().encodeToString(iv))
                .build();

        fileMetadataRepository.save(fileMetadata);
    }

    private byte[] createIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private void encryptFile(String key, byte[] iv, File originalFile, File encryptedFile) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] inputBytes = Files.readAllBytes(Paths.get(originalFile.toURI()));
        byte[] outputBytes = cipher.doFinal(inputBytes);

        try (FileOutputStream outputStream = new FileOutputStream(encryptedFile)) {
            outputStream.write(iv); // Prepend the IV for use in decryption
            outputStream.write(outputBytes);
        }
    }
}
