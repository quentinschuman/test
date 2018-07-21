package com.example.demo.common;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by qianshu on 2018/7/20.
 */
public class AESUtils {
    private static Key key;
    private static final String AES = "AES";

    /**
     * 加密key为空，默认为类名
     */
    public AESUtils() {
        setKey(this.getClass().getName());
    }

    /**
     * 设置加密key
     * @param keyStr
     */
    public AESUtils(String keyStr){
        setKey(keyStr);
    }

    /**
     * 设置加密的校验码
     * @param keyStr
     */
    public void setKey(String keyStr) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(AES);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(keyStr.getBytes());
            kgen.init(128,random);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec aesKey = new SecretKeySpec(enCodeFormat,AES);
            key = aesKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对字符串进行AES加密，返回BASE64编码的加密字符串
     * @param str
     * @return
     */
    public final String encryptString(String str){
        try {
            Cipher cipher = Cipher.getInstance(AES);
            byte[] bytes = str.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] result = cipher.doFinal(bytes);
            return Base64.encodeBase64URLSafeString(result);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 对BASE64编码的加密字符串进行解密，返回解密后的字符串
     * @param str
     * @return
     */
    public final String decryptString(String str){
        try {
            byte[] bytes = Base64.decodeBase64(str);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] result = cipher.doFinal(bytes);
            return new String(result);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
