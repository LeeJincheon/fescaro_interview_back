package com.fescaro.fescaro_interview_back.service;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public void upload(MultipartFile file) throws Exception;

    public Page<FileResponseDto> findFiles(int page, int size);
}
