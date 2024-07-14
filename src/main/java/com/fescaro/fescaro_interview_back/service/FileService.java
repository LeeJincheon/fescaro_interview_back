package com.fescaro.fescaro_interview_back.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public void upload(MultipartFile file) throws Exception;
}
