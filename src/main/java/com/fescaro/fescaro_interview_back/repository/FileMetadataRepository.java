package com.fescaro.fescaro_interview_back.repository;

import com.fescaro.fescaro_interview_back.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
