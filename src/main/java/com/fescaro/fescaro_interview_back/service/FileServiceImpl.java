package com.fescaro.fescaro_interview_back.service;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import com.fescaro.fescaro_interview_back.entity.FileMetadata;
import com.fescaro.fescaro_interview_back.exception.FileProcessingException;
import com.fescaro.fescaro_interview_back.repository.FileMetadataRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileEncryptionService fileEncryptionService;

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
        if (file.isEmpty()) {
            throw new FileProcessingException("업로드할 파일이 비어 있습니다.");
        }

        try {
            File uploadedFileDirectory = new File(uploadedPath);
            File encryptedFileDirectory = new File(encryptedPath);

            if (!uploadedFileDirectory.exists()) {
                boolean created = uploadedFileDirectory.mkdirs();
                if (!created) {
                    throw new FileProcessingException("업로드 파일을 위한 폴더 생성에 실패했습니다.");
                }
            }
            if (!encryptedFileDirectory.exists()) {
                boolean created = encryptedFileDirectory.mkdirs();
                if (!created) {
                    throw new FileProcessingException("암호화 파일을 위한 폴더 생성에 실패했습니다.");
                }
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
            IvParameterSpec iv = fileEncryptionService.createIv();
            fileEncryptionService.encryptFile(iv, originalFile, encryptedFile);

            // 파일 정보 DB에 저장
            FileMetadata fileMetadata = FileMetadata.builder()
                    .originalFileName(originalFilename)
                    .originalFilePath(originalFile.getAbsolutePath())
                    .encryptedFileName(encryptedFilename)
                    .encryptedFilePath(encryptedFile.getAbsolutePath())
                    .iv(Base64.getEncoder().encodeToString(iv.getIV()))
                    .build();

            fileMetadataRepository.save(fileMetadata);
        } catch (Exception ex) {
            throw new FileProcessingException("파일 처리 중 오류가 발생했습니다: " + file.getOriginalFilename(), ex);
        }
    }

    @Override
    public void download(String fileName, String status, HttpServletResponse response) throws IOException {
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

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            long fileSize = file.length();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Length", String.valueOf(fileSize));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
        }
    }

}
