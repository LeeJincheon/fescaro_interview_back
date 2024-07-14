package com.fescaro.fescaro_interview_back.controller;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import com.fescaro.fescaro_interview_back.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) throws Exception {
        fileService.upload(file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName,
                                             @RequestParam String status) throws IOException {

        Resource resource = fileService.download(fileName, status);
        String contentDisposition = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/files")
    public ResponseEntity<Page<FileResponseDto>> findFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<FileResponseDto> files = fileService.findFiles(page, size);
        return ResponseEntity.ok()
                .body(files);
    }
}
