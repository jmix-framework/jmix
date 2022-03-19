/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.imap.crypto;

import io.jmix.imap.ImapEncryptionProperties;
import io.jmix.imap.entity.ImapMailBox;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@Component("imap_DefaultEncryptor")
public class DefaultEncryptor implements Encryptor {

    private final static Logger log = LoggerFactory.getLogger(DefaultEncryptor.class);

    protected static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    protected final ImapEncryptionProperties encryptionProperties;

    protected SecretKey secretKey;

    protected byte[] iv;

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "CdiInjectionPointsInspection"})
    @Autowired
    public DefaultEncryptor(ImapEncryptionProperties encryptionProperties) {
        this.encryptionProperties = encryptionProperties;
    }

    @PostConstruct
    void initKey() {
        if (StringUtils.isBlank(encryptionProperties.getKey())) {
            throw new IllegalStateException(String.format(
                    "Cannot configure encryptor %s, property \"imap.encryption.key\" is not set",
                    getClass().getName()
            ));
        }
        byte[] encryptionKey = Base64.getDecoder().decode(encryptionProperties.getKey());
        secretKey = new SecretKeySpec(encryptionKey, "AES");

        String encryptionIv = encryptionProperties.getIv();
        if (StringUtils.isNotBlank(encryptionIv)) {
            iv = Base64.getDecoder().decode(encryptionIv);
        }

        log.info("Encryptor has been initialised");
    }

    @Override
    public String getEncryptedPassword(ImapMailBox mailBox) {
        if (mailBox.getAuthentication().getPassword() == null) {
            return null;
        }
        log.debug("Encrypt password for {}", mailBox);
        try {
            byte[] encrypted = getCipher(Cipher.ENCRYPT_MODE)
                    .doFinal(saltedPassword(mailBox).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Can't encrypt password for mailbox " + mailBox, e);
        }
    }

    protected String saltedPassword(ImapMailBox mailBox) {
        String password = mailBox.getAuthentication().getPassword();
        return RandomStringUtils.random(16) + password;
    }

    @Override
    public String getPlainPassword(ImapMailBox mailBox) {
        if (mailBox.getAuthentication().getPassword() == null) {
            return null;
        }
        log.debug("Decrypt password for {}", mailBox);
        try {
            byte[] password = Base64.getDecoder().decode(mailBox.getAuthentication().getPassword());
            byte[] decrypted = getCipher(Cipher.DECRYPT_MODE).doFinal(password);
            String saltedPassword = new String(decrypted, StandardCharsets.UTF_8);
            return saltedPassword.substring(16);
        } catch (Exception e) {
            throw new RuntimeException("Can't decrypt password for mailbox " + mailBox, e);
        }
    }

    protected Cipher getCipher(int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        if (iv != null) {
            cipher.init(mode, secretKey, getAlgorithmParameterSpec());
        } else {
            cipher.init(mode, secretKey);
        }
        return cipher;
    }

    protected AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return new IvParameterSpec(iv, 0, iv.length);
    }

}
