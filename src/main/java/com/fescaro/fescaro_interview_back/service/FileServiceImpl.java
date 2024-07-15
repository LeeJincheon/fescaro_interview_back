package com.fescaro.fescaro_interview_back.service;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import com.fescaro.fescaro_interview_back.entity.FileMetadata;
import com.fescaro.fescaro_interview_back.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final Charset ENCODING_TYPE = StandardCharsets.UTF_8;
    private final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";


    @Value("${encryption.key}")
    private String secretKey;

    @Value("${file.uploaded-path}")
    private String uploadedPath;

    @Value("${file.encrypted-path}")
    private String encryptedPath;

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

        File uploadedFileDirectory = new File(uploadedPath);
        File encryptedFileDirectory = new File(encryptedPath);
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
        File originalFile = new File(uploadedPath + originalFilename);
        file.transferTo(originalFile);

        // 암호화 결과 파일 생성
        File encryptedFile = new File(encryptedPath + encryptedFilename);

        // 파일 암호화
        IvParameterSpec iv = createIv();
        encryptFile(secretKey, iv, originalFile, encryptedFile);

        // 파일 정보 DB에 저장
        FileMetadata fileMetadata = FileMetadata.builder()
                .originalFileName(originalFilename)
                .originalFilePath(originalFile.getAbsolutePath())
                .encryptedFileName(encryptedFilename)
                .encryptedFilePath(encryptedFile.getAbsolutePath())
                .iv(Base64.getEncoder().encodeToString(iv.getIV()))
                .build();

        fileMetadataRepository.save(fileMetadata);
    }

    @Override
    public Resource download(String fileName, String status) throws IOException {
        String pathName = "";
        if (status.equals("original")) {
            pathName = uploadedPath + fileName;
        }
        if (status.equals("encrypted")) {
            pathName = encryptedPath + fileName;
        }

        File file = new File(pathName);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        return new FileSystemResource(file);
    }

    private IvParameterSpec createIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private void encryptFile(String key, IvParameterSpec iv, File originalFile, File encryptedFile) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(ENCODING_TYPE), "AES");

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv.getIV());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] inputBytes = Files.readAllBytes(Paths.get(originalFile.toURI()));
        byte[] outputBytes = cipher.doFinal(inputBytes);

        try (FileOutputStream outputStream = new FileOutputStream(encryptedFile)) {
            outputStream.write(iv.getIV());
            outputStream.write(outputBytes);
        }
    }
}
