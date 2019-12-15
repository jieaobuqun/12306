package com.trz.railway;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author jieaobuqun  2017-09-18.
 */
@SuppressWarnings("unused")
public class Security {

    /** 加密算法 */
    private static final String     ENCRYPT_ALGORITHM = "AES";
    /** 加解密的KEY */
    private static final String     AES_KEY = "jieaobuqun123456";
    /** 加解密的工具 */
    private static Cipher           cipher;
    /** 加密Key */
    private static SecretKeySpec    secretKeySpec;

    static {
        try {
            secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), ENCRYPT_ALGORITHM);
            cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * AES加密
     * @param content
     * @return
     */
    public static String aesEncrypt(String content) {
        try {
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return new BASE64Encoder().encode( cipher.doFinal(byteContent) );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     * @param content
     * @return
     */
    public static String aesDecrypt(String content) {
        try {
            byte[] contentBytes = new BASE64Decoder().decodeBuffer(content);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(contentBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
