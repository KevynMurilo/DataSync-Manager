package br.gov.formosa.backup.shared.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;

@Component
@Converter(autoApply = false)
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    @Value("${app.crypto.key}")
    private String keyValue;

    private Key secretKey;

    @PostConstruct
    public void init() {
        if (keyValue == null || keyValue.length() != 16) {
            throw new IllegalArgumentException("A chave 'app.crypto.key' deve ser definida e ter exatamente 16 caracteres.");
        }
        secretKey = new SecretKeySpec(keyValue.getBytes(), "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(attribute.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criptografar atributo", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao descriptografar atributo", e);
        }
    }
}