package com.payment.gateway.enums;

public enum MerchantEnum {

    MCH_891065321967_DEV(
            "891065321967",
            "支付宝渠道商户",
            "RSA2",
            "certificate/MANDAO_891065321967_pub.cer",
            "certificate/RSA2_pri.pfx",
            "123456",
            "2aa4b04ee322abaec67738b12bbc89d5",
            "https://test-api.huishouqian.com"
    ),
    // 移企付-间连商户
    MCH_841508774642_DEV(
            "841508774642",
            "",
            "RSA2",
            "certificate/MANDAO_841508774642_pub.cer",
            "certificate/RSA2_pri.pfx",
            "123456",
            "02a55b8bb960d06017ae7e6ac6116a75",
            "https://test-api.huishouqian.com"
    ),
    // 移企付-直连商户
    MCH_890943339002_DEV(
            "890943339002",
            "",
            "RSA2",
            "certificate/MANDAO_890943339002_pub.cer",
            "certificate/RSA2_pri.pfx",
            "123456",
            "8ad9b8f430bc5c33dcbfb16f071f6f3d",
            "http://10.254.145.190:7224"
    ),

    MCH_890905305161_DEV(
            "890905305161",
            "",
            "CFCA",
            "certificate/MANDAO_890905305161_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "2b990de81d1bdaf7128c3d4cc54321b3",
            "http://10.254.145.190:7224"
    ),

    MCH_853943386352_DEV(
            "853943386352",
            "",
            "CFCA",
            "certificate/MANDAO_853943386352_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "8977713496040f501d621488461abcc0",
            "http://10.254.145.190:7224"
    ),

    MCH_810074854851_PRO(
            "810074854851",
            "",
            "RSA2",
            "certificate/MANDAO_810074854851_pub.cer",
            "certificate/RSA2_pri.pfx",
            "123456",
            "daca64559a615aed7b2a30a223efc90a",
            "https://api-pre.huishouqian.com"
    ),
    MCH_803502151167_DEV(
            "803502151167",
            "鹏博士-合理宝渠道商户",
            "RSA2",
            "certificate/MANDAO_803502151167_pub.cer",
            "certificate/self.p12",
            "KtYBnbVEotFvYEFm",
            "0a0f16ec2c49c6d92a2fa1a8c4db117c",
            "https://test-api.huishouqian.com"
    ),
    MCH_817500561427_DEV(
            "817500561427",
                    "",
                    "RSA2",
            "certificate/MANDAO_817500561427_pub.cer",
            "certificate/RSA2_pri.pfx",
            "123456",
            "49be18cd58f78881c197b3ce8bbdd4f4",
            "http://10.254.145.190:7224"
    ),

    MCH_810073021248_PRO(
            "810073021248",
            "",
            "RSA2",
            "certificate/MANDAO_810073021248_pub.cer",
            "certificate/RSA2_pri.pfx",
            "123456",
            "45bd2b12dcee60ef8c13cb543ae7b5cd",
            "https://api-pre.huishouqian.com"

    ),

    MCH_826500751636_DEV(
            "826500751636",
            "",
            "CFCA",
            "certificate/MANDAO_826500751636_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "b4fca28a5599aad066e2c766a2436d7a",
            "https://test-api.huishouqian.com"
    ),

    MCH_802001351142_DEV(
            "802001351142",
            "",
            "CFCA",
            "certificate/MANDAO_802001351142_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "0a5a5aed7e260ce7db1aed25f8db2ae5",
            "https://test-api.huishouqian.com"
    ),

    MCH_815271709251_DEV(
            "815271709251",
            "",
            "CFCA",
            "certificate/MANDAO_815271709251_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "b96f64a8f0dc75005488f0553e1b48e6",
            "http://10.254.145.190:7224"
    ),

    MCH_805501002985_PRO(
            "805501002985",
            "",
            "CFCA",
            "certificate/MANDAO_805501002985_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "582d4406c15def763854c624ad1a398c",
            "https://api-pre.huishouqian.com"
    ),

    MCH_810500642571_PRO(
            "810500642571",
            "",
            "CFCA",
            "certificate/MANDAO_810500642571_pub.cer",
            "certificate/883007563082_pri.pfx",
            "123456",
            "aefcecb7cc33e2cc750064bf7c0dc583",
            "https://api-pre.huishouqian.com"
    ),

    MCH_816502003871_DEV(
            "816502003871",
            "",
            "RSA2",
            "certificate/MANDAO_816502003871_pub.cer",
            "certificate/self.p12",
            "KtYBnbVEotFvYEFm",
            "1fef4cbc2aa08cfdadce374aae16df8b",
            "http://api-nh.kafuner.com"
    );

    String merchantNo;
    String merchantName;
    String signType;
    String publicKey;
    String privateKey;
    String priKeyPass;
    String pwd;
    String url;

    MerchantEnum(String merchantNo, String merchantName, String signType, String publicKey, String privateKey, String priKeyPass, String pwd, String url) {
        this.merchantNo = merchantNo;
        this.merchantName = merchantName;
        this.signType = signType;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.priKeyPass = priKeyPass;
        this.pwd = pwd;
        this.url = url;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getSignType() {
        return signType;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPriKeyPass() {
        return priKeyPass;
    }

    public String getPwd() {
        return pwd;
    }

    public String getUrl() {
        return url;
    }

    public static MerchantEnum explain(String merchantNo){

        for(MerchantEnum merchantEnum: MerchantEnum.values()){

            if(merchantEnum.getMerchantNo().equals(merchantNo)){
                return merchantEnum;
            }
        }

        return null;
    }
}
