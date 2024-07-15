package com.fescaro.fescaro_interview_back.service;

import javax.crypto.spec.IvParameterSpec;
import java.io.File;

public interface FileEncryptionService {

    public void encryptFile(IvParameterSpec iv, File originalFile, File encryptedFile) throws Exception;

    public IvParameterSpec createIv();
}
