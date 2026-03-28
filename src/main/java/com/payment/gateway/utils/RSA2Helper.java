package com.payment.gateway.utils;

import com.google.common.io.BaseEncoding;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;

import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;


public class RSA2Helper {

    /**
     * RSA2签名
     * @param data
     * @param privateKeyPath
     * @return
     */
    public static String sign(String data, String privateKeyPath, String privateKeyPwd) {
        try{
            //获取私钥
            PrivateKey privateKey = loadPrivateKeyFromPKCS12(Files.readAllBytes(Paths.get(privateKeyPath)), privateKeyPwd);
            //获取签名对象
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initSign(privateKey);
            //传入原文
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signData = signature.sign();
            return BaseEncoding.base16().lowerCase().encode(signData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA2验签
     * @param data
     * @param sign
     * @param publicKeyPath
     * @return
     */
    public static boolean verify(String data, String sign, String publicKeyPath)  {

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(publicKeyPath))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if(!currentLine.startsWith("-")) {
                    contentBuilder.append(currentLine);
                }
            }
        } catch (IOException e) {
            System.out.println("读取文件时出错: " + e.getMessage());
        }
        PublicKey publicKey = getPublicKeyByText(contentBuilder.toString());
        Signature signature;
        try {
            signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes("utf-8"));
            return signature.verify(BaseEncoding.base16().lowerCase().decode(sign));
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String encryptByPublicKey(String data, String publicKeyPath){
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(publicKeyPath))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if(!currentLine.startsWith("-")) {
                    contentBuilder.append(currentLine);
                }
            }
        } catch (IOException e) {
            System.out.println("读取文件时出错: " + e.getMessage());
        }
        PublicKey publicKey = getPublicKeyByText(contentBuilder.toString());

        return encryptByPublicKey(data, publicKey);
    }
    private static String encryptByPublicKey(String plainText, PublicKey publicKey) {
        byte[] destBytes = encryptBySegments(plainText.getBytes(), publicKey);
        return destBytes == null ? null : (BaseEncoding.base16().lowerCase().encode(destBytes));
    }

    public static String decryptByPrivateKey(String encryptedData, String privateKeyPath, String privateKeyPwd) {
        try {
            PrivateKey privateKey = loadPrivateKeyFromPKCS12(Files.readAllBytes(Paths.get(privateKeyPath)), privateKeyPwd);
            byte[] destBytes = decryptBySegments(BaseEncoding.base16().lowerCase().decode(encryptedData), privateKey);
            return new String(destBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decryptBySegments(byte[] data, PrivateKey privateKey) {
        try {

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            int keySize = ((java.security.interfaces.RSAKey) privateKey).getModulus().bitLength();
            int segmentSize = getSegmentSize(keySize, false);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (int i = 0; i < data.length; i += segmentSize) {
                int endIndex = Math.min(data.length, i + segmentSize);
                byte[] segment = Arrays.copyOfRange(data, i, endIndex);

                byte[] decryptedSegment = cipher.doFinal(segment);
                outputStream.write(decryptedSegment);
            }

            return outputStream.toByteArray();

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] encryptBySegments(byte[] data, PublicKey publicKey) {
        try {

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            int keySize = ((java.security.interfaces.RSAKey) publicKey).getModulus().bitLength();
            int segmentSize = getSegmentSize(keySize, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (int i = 0; i < data.length; i += segmentSize) {
                int endIndex = Math.min(data.length, i + segmentSize);
                byte[] segment = Arrays.copyOfRange(data, i, endIndex);

                byte[] encryptedSegment = cipher.doFinal(segment);
                outputStream.write(encryptedSegment);
            }

            return outputStream.toByteArray();

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static int getSegmentSize(int keySize, boolean forEncryption) {
        int keyBytes = keySize / 8;
        if (forEncryption) {
            // 加密：需要减去填充字节
            return keyBytes - 11; // PKCS#1 v1.5 填充
        } else {
            // 解密：直接使用密钥大小
            return keyBytes;
        }
    }

    private static PrivateKey loadPrivateKeyFromPKCS12(byte[] pfxBytes, String priKeyPass) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] charPriKeyPass = priKeyPass.toCharArray();
            ks.load(new ByteArrayInputStream(pfxBytes), charPriKeyPass);
            PrivateKey privateKey = null;
            Enumeration<String> aliasEnum = ks.aliases();
            String keyAlias;
            while (aliasEnum.hasMoreElements()) {
                keyAlias = aliasEnum.nextElement();
                privateKey = (PrivateKey) ks.getKey(keyAlias, charPriKeyPass);
                if (privateKey != null) {
                    break;
                }
            }
            return privateKey;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static PublicKey getPublicKeyByText(String pubKeyText) {
        try {
            CertificateFactory e = CertificateFactory.getInstance("X509");
            Certificate certificate = e.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(pubKeyText)));
            return certificate.getPublicKey();
        } catch (Exception var6) {
            System.out.println("解析公钥内容失败:" + var6);
            return null;
        }
    }

}