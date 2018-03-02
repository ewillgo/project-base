package cc.sportsdb.common.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;

public abstract class AES256Util {

    private static final int KEY_GENERATOR_BIT = 256;
    private static final String ALGORITHM_NAME = "AES";
    private static final String CIPHER_INSTANCE_NAME = "AES/ECB/PKCS7Padding";

    private static Logger logger = LoggerFactory.getLogger(AES256Util.class);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private AES256Util() {

    }

    public static String encodeToUrlSafeString(byte[] bytes) {
        return Base64Utils.encodeToUrlSafeString(bytes);
    }

    public static byte[] decodeFromUrlSafeString(String content) {
        return Base64Utils.decodeFromUrlSafeString(content);
    }

    public static byte[] encrypt(byte[] bytes, String salt) {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_NAME);
            keyGenerator.init(KEY_GENERATOR_BIT, new SecureRandom(DigestUtils.sha256(salt.getBytes())));
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyGenerator.generateKey().getEncoded(), ALGORITHM_NAME));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            logger.error("AES encrypt error.", e);
        }

        return null;
    }

    public static byte[] decrypt(byte[] bytes, String salt) {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_NAME);
            keyGenerator.init(KEY_GENERATOR_BIT, new SecureRandom(DigestUtils.sha256(salt.getBytes())));
            Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME, "BC");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyGenerator.generateKey().getEncoded(), ALGORITHM_NAME));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            logger.error("AES decrypt error.", e);
        }

        return null;
    }

}
