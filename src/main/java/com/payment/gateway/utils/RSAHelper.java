package com.payment.gateway.utils;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;

public class RSAHelper {

    public static String encrypt(String message, String privateKeyPath, String privateKeyPwd) throws IOException {
        //获取私钥
        PrivateKey privateKey = loadPrivateKeyFromPKCS12(readBytes(privateKeyPath), privateKeyPwd);
        return encryptByPrivateKey(Base64.getEncoder().encodeToString(message.getBytes()), privateKey);
    }

    public static String decrypt(String message, String publicKeyPath) throws Exception{
        String contentBuilder = readKeyText(publicKeyPath);
        CertificateFactory e = CertificateFactory.getInstance("X509");
        Certificate certificate = e.generateCertificate(new ByteArrayInputStream(
                Base64.getDecoder().decode(contentBuilder)));
        PublicKey publicKey = certificate.getPublicKey();
        return decryptByPublicKey(message, publicKey);
    }

    private static String encryptByPrivateKey(String plainText, PrivateKey privateKey) {
        byte[] destBytes = encryptBySegments(plainText.getBytes(), privateKey);
        return destBytes == null ? null : (BaseEncoding.base16().lowerCase().encode(destBytes));
    }

    public static String decryptByPublicKey(String encryptedData, PublicKey publicKey) {
        byte[] destBytes = decryptBySegments(BaseEncoding.base16().lowerCase().decode(encryptedData), publicKey);
        return new String(destBytes, StandardCharsets.UTF_8);
    }

    private static byte[] decryptBySegments(byte[] encryptedData, PublicKey publicKey)  {
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            int keySize = ((java.security.interfaces.RSAKey) publicKey).getModulus().bitLength();
            int segmentSize = getSegmentSize(keySize, false); // 解密段大小

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (int i = 0; i < encryptedData.length; i += segmentSize) {
                int endIndex = Math.min(encryptedData.length, i + segmentSize);
                byte[] segment = Arrays.copyOfRange(encryptedData, i, endIndex);

                byte[] decryptedSegment = cipher.doFinal(segment);
                outputStream.write(decryptedSegment);
            }

            return outputStream.toByteArray();

        }catch (Exception e){
            e.printStackTrace();
        }
        return new byte[0];
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
    /**
     * RSA 分段加密
     */
    private static byte[] encryptBySegments(byte[] data, PrivateKey privateKey) {
        try {

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            int keySize = ((java.security.interfaces.RSAKey) privateKey).getModulus().bitLength();
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



    private static PrivateKey loadPrivateKeyFromPKCS12(byte[] pfxBytes, String priKeyPass) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (ByteArrayInputStream bis = new ByteArrayInputStream(pfxBytes)) {
                keyStore.load(new ByteArrayInputStream(pfxBytes), priKeyPass.toCharArray());
                Enumeration<String> aliases = keyStore.aliases();
                // 遍历找到私钥
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    // 检查是否有私钥
                    if (keyStore.isKeyEntry(alias)) {
                        // 获取私钥（需要密码）
                        Key key = keyStore.getKey(alias, priKeyPass.toCharArray());

                        if (key instanceof PrivateKey) {
                            return (PrivateKey) key;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static String readKeyText(String location) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(openResource(location), StandardCharsets.UTF_8))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (!currentLine.startsWith("-")) {
                    contentBuilder.append(currentLine);
                }
            }
        } catch (IOException e) {
            System.out.println("读取文件时出错: " + e.getMessage());
        }
        return contentBuilder.toString();
    }

    private static byte[] readBytes(String location) throws IOException {
        try (InputStream in = openResource(location)) {
            return in.readAllBytes();
        }
    }

    private static InputStream openResource(String location) throws IOException {
        if (location == null || location.trim().isEmpty()) {
            throw new FileNotFoundException("证书路径为空");
        }

        String normalized = location.trim();
        if (normalized.startsWith("classpath:")) {
            InputStream in = getClassLoader().getResourceAsStream(normalized.substring("classpath:".length()));
            if (in == null) {
                throw new FileNotFoundException("classpath资源不存在: " + location);
            }
            return in;
        }

        try {
            Path path = Paths.get(normalized);
            if (Files.exists(path)) {
                return Files.newInputStream(path);
            }
        } catch (InvalidPathException ignored) {
            // fallback to classpath loading below
        }

        String resourcePath = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        InputStream in = getClassLoader().getResourceAsStream(resourcePath);
        if (in != null) {
            return in;
        }

        throw new FileNotFoundException("找不到证书资源: " + location);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        return context != null ? context : RSAHelper.class.getClassLoader();
    }


    public static void  main(String[] args) throws Exception {

        String result = new String(Base64.getDecoder().decode(decrypt("5bf9dd2e42c09a5c99ac33f46dbfd790fba1f47168bbaed83a2093dcd50b86c16d12f26aea6ef2cc6661db595b814f05f9b52cdbf87f6ad44797bd4858aba7bc9f7a5847e0ba63b1fe2e8e0b83190d1805084d8adb8b55072c8fa47b6eaaf5349079eb34b6dc8cae4e0c23f4bd08b754cbd614351a0f53c62974e0a679cfcb94d6c0e68b1c268e8f14b8b5af7398286782cef577736f12d81504756ebd93859fa3136eb844e071e80e1ce23196b1dee89668449fb50f9cef9e60e5c8f3813f72370602b703a68d42161e0bfd41a12efd9d2f016f2bdc7ac07eb4fa69b68ed29eb829af30489414f0e56c33d140a7466fedfc0381ee44970104510562d89f101e58977bd861f849338ab641440bf7a054afe4b15f37200bb4ce48ab81c6267e8919173c0c4ea05fb374059ff030768d9964af281721eb57e74d6cb5fdde1a1052dc458cd3cba79800260d8d6df60255d33c0612190329c3e9ba8aae2a7b9c0417e68ff8ed2950107b0147429d661fb40e954d5ea1b9c97f39a9d8b3d53907fe596b996d07750fbf9063368ab06826d569333d4de71a5a44df93849ab3b9374be9bd83915e303902d16aed4853b53a992d7c330abc8cb5b5feccfe04ef416ecfe5c0613e7d38cd103c1f6d01162118817fbbb70675746b3dfd7ac3d5426ebac1588a8af4c7fb51450f15d8b71ce1424e4baf0c90c39b5c63d0c128a5fa57963ed0aa92482dfa8b2806249bf9b2304ab3318ff99e9cee26abd52b486b6c965369d255ac9cf351d85ff6e5f4eca076ecdb66a245cd83b6a830df24e20f5bb2ada81b595d9571bdb469527420f738b9bb9f782f4a89863a96eca1a4fb41bf502e0478fb1fb2b8f6a1380af8c972eb38920f7e7ee201b34776c4b556c3cdb990838d0d8dcf45df09d6ef2bd5c7665c8272cb35d17e8c92f69176ad914e84ecfeb2dd31c3f2f514d028ca4a068f0332902b5e48e7488f6f6d8635a08121fee7fbd46db62077eaaa65018ccdaca1e8d7d288b461a7042eda223a004e88ac179b8f8495f9890ca0832ada99b033fbb181d066406ffad010a3d5da509ffe68d1b6faefa4d29960fcccde07ce88f92dc97e0d594aa9a31230e61ea943db12948b9886b9d82f214bc4aa9e9ef756bb938a606eefab2c8939cf919e03541f5051921d7523c65a96fadff71085ed76c29988dfb4b2e75028478dd29373b995edb0c2a27d7ab9a44021690614c90c866d8e95f24bc8335c9f3c952af5e3022cb525acd958dc06329038bb28b0911f78f83229fe7746d0f9671f0fbc608b7e6a29f1b6d5cf005979cce3bf620d14291b7c7be29489d715c4382245f2bf92fbc949aceedca2a57e6890bd43fd684334a0a86de9466ca6da05bb5b1f7a1a65ad6b41a35edc6c927c6dd202515e25e7c489cda927c7fa7ff359c1c03ee13337592b30e137d0d83a71c4",
                "/Users/lishanjie/workassist/src/main/resources/certificate/MANDAO_826500751636_pub.cer")), "UTF-8");

        System.out.println(result);

    }
}
