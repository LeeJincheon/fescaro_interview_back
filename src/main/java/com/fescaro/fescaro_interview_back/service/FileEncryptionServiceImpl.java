package com.fescaro.fescaro_interview_back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

@Service
public class FileEncryptionServiceImpl implements FileEncryptionService {

    @Value("${encryption.key}")
    private String secretKey;

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final Charset ENCODING_TYPE = StandardCharsets.UTF_8;

    @Override
    public void encryptFile(IvParameterSpec iv, File originalFile, File encryptedFile) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(ENCODING_TYPE), "AES");

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv.getIV());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);

        byte[] inputBytes = Files.readAllBytes(Paths.get(originalFile.toURI()));
        byte[] outputBytes = cipher.doFinal(inputBytes);

        try (FileOutputStream outputStream = new FileOutputStream(encryptedFile)) {
            outputStream.write(iv.getIV());
            outputStream.write(outputBytes);
        }
    }

    @Override
    public IvParameterSpec createIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
