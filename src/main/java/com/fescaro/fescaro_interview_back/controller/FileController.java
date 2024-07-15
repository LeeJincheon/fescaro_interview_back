package com.fescaro.fescaro_interview_back.controller;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import com.fescaro.fescaro_interview_back.exception.FileUploadException;
import com.fescaro.fescaro_interview_back.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 제공되지 않았습니다.");
        }
        fileService.upload(file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/files/{fileName}")
    public void download(@PathVariable String fileName,
                         @RequestParam String status,
                         HttpServletResponse response) throws IOException {

        fileService.download(fileName, status, response);
    }

    @GetMapping("/files")
    public ResponseEntity<Page<FileResponseDto>> findFiles(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "5") int size) {

        Page<FileResponseDto> files = fileService.findFiles(page, size);
        return ResponseEntity.ok()
                .body(files);
    }
}
