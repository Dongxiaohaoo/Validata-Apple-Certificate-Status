package top.dongxiaohao.validatap12status.util;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 17:35
 */
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.date.DateUtil;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import top.dongxiaohao.validatap12status.dto.CertStatusDTO;

@Slf4j
public class CertUtil {

    public static void getCertInfo(String certStr, String passwd, CertStatusDTO certStatusDTO) throws Exception {
        byte[] p12Byte = Base64Decoder.decode(certStr);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(p12Byte), passwd.toCharArray());
        Enumeration<String> aliases = keyStore.aliases();
        String alias = "1";
        while(aliases.hasMoreElements()) {
            alias = aliases.nextElement();
        }
        Certificate certificate = keyStore.getCertificate(alias);
        if (certificate != null) {
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate)certificate;
                Date notBefore = x509Certificate.getNotBefore();
                certStatusDTO.setCertCreateTime(DateUtil.format(notBefore, "yyyy-MM-dd HH:mm:ss"));
                Date notAfter = x509Certificate.getNotAfter();
                certStatusDTO.setCertExpireTime(DateUtil.format(notAfter, "yyyy-MM-dd HH:mm:ss"));
                certStatusDTO.setCertSerialNumber(x509Certificate.getSerialNumber().toString());
                certStatusDTO.setCertName(getCertName(x509Certificate.getSubjectDN().toString()));
                certStatusDTO.setCertRemainingDays(DateUtil.betweenDay(new Date(), notAfter, false));
            } else {
                log.error("Invalid certificate type. Not an X.509 certificate.");
            }
        } else {
            log.error("Certificate not found for the given alias.");
        }

    }

    public static String getCertName(String getSubjectDNStr) {
        String patternString = "O=([^,]*)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(getSubjectDNStr);
        return matcher.find() ? matcher.group(1).replaceAll("\"", "") : null;
    }
}

