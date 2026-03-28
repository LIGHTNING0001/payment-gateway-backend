package com.payment.gateway.controller;

import com.payment.gateway.utils.RSA2Helper;
import com.payment.gateway.utils.CFCAHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/security")
public class SecurityController {

    @Value("${security.rsa2.private-key-path}")
    private String rsa2PrivateKeyPath;

    @Value("${security.rsa2.public-key-path}")
    private String rsa2PublicKeyPath;

    @Value("${security.rsa2.private-key-pwd}")
    private String rsa2PrivateKeyPwd;

    @Value("${security.cfca.private-key-path}")
    private String cfcaPrivateKeyPath;

    @Value("${security.cfca.public-key-path}")
    private String cfcaPublicKeyPath;

    @Value("${security.cfca.private-key-pwd}")
    private String cfcaPrivateKeyPwd;

    // RSA2加签
    @PostMapping("/rsa2/sign")
    public Map<String, Object> rsa2Sign(@RequestBody Map<String, Object> request) {
        String data = request.get("data").toString();
        String sign = RSA2Helper.sign(data, rsa2PrivateKeyPath, rsa2PrivateKeyPwd);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("sign", sign);
        return result;
    }

    // RSA2验签
    @PostMapping("/rsa2/verify")
    public Map<String, Object> rsa2Verify(@RequestBody Map<String, Object> request) {
        String data = request.get("data").toString();
        String sign = request.get("sign").toString();
        boolean verifyResult = RSA2Helper.verify(data, sign, rsa2PublicKeyPath);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("verifyResult", verifyResult);
        return result;
    }

    // CFCA加密
    @PostMapping("/cfca/encrypt")
    public Map<String, Object> cfcaEncrypt(@RequestBody Map<String, Object> request) {
        String data = request.get("data").toString();
        String encryptedData = CFCAHelper.encryptByPublicKey(data, cfcaPublicKeyPath);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("encryptedData", encryptedData);
        return result;
    }

    // CFCA解密
    @PostMapping("/cfca/decrypt")
    public Map<String, Object> cfcaDecrypt(@RequestBody Map<String, Object> request) {
        String encryptedData = request.get("encryptedData").toString();
        String decryptedData = CFCAHelper.decryptByPrivateKey(encryptedData, cfcaPrivateKeyPath, cfcaPrivateKeyPwd);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("decryptedData", decryptedData);
        return result;
    }

}