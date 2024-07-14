package com.fescaro.fescaro_interview_back.service;

import com.fescaro.fescaro_interview_back.dto.FileResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    public void upload(MultipartFile file) throws Exception;
    public Resource download(String fileName, String status) throws IOException;
    public Page<FileResponseDto> findFiles(int page, int size);
}
