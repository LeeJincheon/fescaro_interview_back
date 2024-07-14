package com.fescaro.fescaro_interview_back.controller;

import com.fescaro.fescaro_interview_back.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/files")
    public ResponseEntity<Void> upload(@RequestParam MultipartFile file) throws Exception {
        fileService.upload(file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
