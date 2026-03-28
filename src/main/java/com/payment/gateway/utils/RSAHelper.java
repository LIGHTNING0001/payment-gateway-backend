package com.payment.gateway.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Base64;

public class RSAHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密
     */
    public static String encrypt(String data, String privateKeyPath, String password) throws Exception {
        // 加载密钥库
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(privateKeyPath)) {
            keyStore.load(fis, password.toCharArray());
        }

        // 获取私钥
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

        // 加密
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, privateKey);

        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     */
    public static String decrypt(String data, String publicKeyPath) throws Exception {
        // 加载证书
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(publicKeyPath)) {
            Certificate cert = cf.generateCertificate(fis);
            PublicKey publicKey = cert.getPublicKey();

            // 解密
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, publicKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
            return new String(decrypted, "UTF-8");
        }
    }

    /**
     * 验签
     */
    public static boolean verify(String data, String sign, String publicKeyPath) throws Exception {
        // 加载证书
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(publicKeyPath)) {
            Certificate cert = cf.generateCertificate(fis);
            PublicKey publicKey = cert.getPublicKey();

            // 验签
            java.security.Signature signature = java.security.Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes("UTF-8"));

            return signature.verify(Base64.getDecoder().decode(sign));
        }
    }

    /**
     * 签名
     */
    public static String sign(String data, String privateKeyPath, String password) throws Exception {
        // 加载密钥库
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(privateKeyPath)) {
            keyStore.load(fis, password.toCharArray());
        }

        // 获取私钥
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

        // 签名
        java.security.Signature signature = java.security.Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes("UTF-8"));

        byte[] signed = signature.sign();
        return Base64.getEncoder().encodeToString(signed);
    }
}
