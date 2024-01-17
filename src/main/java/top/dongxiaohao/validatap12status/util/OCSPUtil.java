package top.dongxiaohao.validatap12status.util;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 13:56
 */

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.cert.ocsp.UnknownStatus;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import top.dongxiaohao.validatap12status.dto.CertStatusDTO;


/**
 * 使用 OCSP协议请求 验证证书状态（有效|撤销|未知）
 *
 * @author Dongxiaohao
 * @date 2024/1/17 15:56
 */
public class OCSPUtil {

    //苹果根证书对象
    private static X509Certificate issuerCert;

    private static X509Certificate getIssuerCert() {
        if (issuerCert == null) {
            String issuerCertStr = "MIIEUTCCAzmgAwIBAgIQfK9pCiW3Of57m0R6wXjF7jANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzETMBEGA1UEChMKQXBwbGUgSW5jLjEmMCQGA1UECxMdQXBwbGUgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkxFjAUBgNVBAMTDUFwcGxlIFJvb3QgQ0EwHhcNMjAwMjE5MTgxMzQ3WhcNMzAwMjIwMDAwMDAwWjB1MUQwQgYDVQQDDDtBcHBsZSBXb3JsZHdpZGUgRGV2ZWxvcGVyIFJlbGF0aW9ucyBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTELMAkGA1UECwwCRzMxEzARBgNVBAoMCkFwcGxlIEluYy4xCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2PWJ/KhZC4fHTJEuLVaQ03gdpDDppUjvC0O/LYT7JF1FG+XrWTYSXFRknmxiLbTGl8rMPPbWBpH85QKmHGq0edVny6zpPwcR4YS8Rx1mjjmi6LRJ7TrS4RBgeo6TjMrA2gzAg9Dj+ZHWp4zIwXPirkbRYp2SqJBgN31ols2N4Pyb+ni743uvLRfdW/6AWSN1F7gSwe0b5TTO/iK1nkmw5VW/j4SiPKi6xYaVFuQAyZ8D0MyzOhZ71gVcnetHrg21LYwOaU1A0EtMOwSejSGxrC5DVDDOwYqGlJhL32oNP/77HK6XF8J4CjDgXx9UO0m3JQAaN4LSVpelUkl8YDib7wIDAQABo4HvMIHsMBIGA1UdEwEB/wQIMAYBAf8CAQAwHwYDVR0jBBgwFoAUK9BpR5R2Cf70a40uQKb3R01/CF4wRAYIKwYBBQUHAQEEODA2MDQGCCsGAQUFBzABhihodHRwOi8vb2NzcC5hcHBsZS5jb20vb2NzcDAzLWFwcGxlcm9vdGNhMC4GA1UdHwQnMCUwI6AhoB+GHWh0dHA6Ly9jcmwuYXBwbGUuY29tL3Jvb3QuY3JsMB0GA1UdDgQWBBQJ/sAVkPmvZAqSErkmKGMMl+ynsjAOBgNVHQ8BAf8EBAMCAQYwEAYKKoZIhvdjZAYCAQQCBQAwDQYJKoZIhvcNAQELBQADggEBAK1lE+j24IF3RAJHQr5fpTkg6mKp/cWQyXMT1Z6b0KoPjY3L7QHPbChAW8dVJEH4/M/BtSPp3Ozxb8qAHXfCxGFJJWevD8o5Ja3T43rMMygNDi6hV0Bz+uZcrgZRKe3jhQxPYdwyFot30ETKXXIDMUacrptAGvr04NM++i+MZp+XxFRZ79JI9AeZSWBZGcfdlNHAwWx/eCHvDOs7bJmCS1JgOLU5gm3sUjFTvg+RTElJdI+mUcuER04ddSduvfnSXPN/wmwLCTbiZOTCNwMUGdXqapSqqdv+9poIZ4vvK7iqF0mDr8/LvOnP6pVxsLRFoszlh6oKw0E6eVzaUDSdlTs=";
            try {
                byte[] issuerByte = Base64Decoder.decode(issuerCertStr);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                issuerCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(issuerByte));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return issuerCert;
    }


    public static CertStatusDTO checkCertStatus(String certStr, String pwd, int retry) {
        CertStatusDTO certStatusDTO = new CertStatusDTO();
        int resultNum;
        while (retry > 0) {
            try {
                byte[] p12Byte = Base64Decoder.decode(certStr);
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(new ByteArrayInputStream(p12Byte), pwd.toCharArray());
                Enumeration<String> aliasenum = null;
                aliasenum = ks.aliases();
                String keyAlias = "1";
                if (aliasenum.hasMoreElements()) {
                    keyAlias = aliasenum.nextElement();
                }
                X509Certificate cert = (X509Certificate) ks.getCertificate(keyAlias);
                OCSPReq ocspReq = GenOcspReq(cert, getIssuerCert());
                String ocspUrl = getOCSPUrl(cert);
                OCSPResp ocspResp = requestOCSPResponse(ocspUrl, ocspReq);
                int status = ocspResp.getStatus();
                if (OCSPResp.SUCCESSFUL == status) {
                    BasicOCSPResp basic = (BasicOCSPResp) ocspResp.getResponseObject();
                    SingleResp[] resps = basic.getResponses();
                    if (resps != null && resps.length == 1) {
                        SingleResp resp = resps[0];
                        CertificateStatus certStatus = resp.getCertStatus();
                        if (certStatus == CertificateStatus.GOOD) {
                            resultNum = CertStatusDTO.Status.VALID.getCode();
                            certStatusDTO.setStatus(resultNum);
                        } else if (certStatus instanceof RevokedStatus) {
                            resultNum = CertStatusDTO.Status.REVOKED.getCode();
                            RevokedStatus revokedStatus = (RevokedStatus) certStatus;
                            certStatusDTO.setCertRevokeTime(DateUtil.format(revokedStatus.getRevocationTime(), "yyyy-MM-dd HH:mm:ss"));
                            certStatusDTO.setCertRevokeReason(revokedStatus.getRevocationReason());
                            certStatusDTO.setStatus(resultNum);
                        } else if (certStatus instanceof UnknownStatus) {
                            resultNum = CertStatusDTO.Status.UNKNOWN.getCode();
                            certStatusDTO.setStatus(resultNum);
                        }
                        retry = 0;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                retry--;
            }
        }

        return certStatusDTO;
    }

    /**
     * 创建OCSP请求
     *
     * @param nextCert
     * @param nextIssuer
     * @return
     * @throws OCSPException
     * @throws OperatorCreationException
     * @throws CertificateEncodingException
     * @throws IOException
     */
    public static OCSPReq GenOcspReq(X509Certificate nextCert,
                                     X509Certificate nextIssuer) throws OCSPException, OperatorCreationException, CertificateEncodingException, IOException {
        OCSPReqBuilder ocspRequestGenerator = new OCSPReqBuilder();
        DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
        // 获取 certId
        CertificateID certId = new CertificateID(
                (new BcDigestCalculatorProvider())
                        .get(CertificateID.HASH_SHA1),
                new X509CertificateHolder(nextIssuer.getEncoded()),
                nextCert.getSerialNumber());
        ocspRequestGenerator.addRequest(certId);
        BigInteger nonce = BigInteger.valueOf(System.currentTimeMillis());
        Extension ext = new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false, new DEROctetString(nonce.toByteArray()));
        ocspRequestGenerator.setRequestExtensions(new Extensions(new Extension[]{ext}));
        return ocspRequestGenerator.build();
    }


    /**
     * 发送请求并接收返回值
     *
     * @param url     请求地址 从证书中获取
     * @param ocspReq 请求对象
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    public static OCSPResp requestOCSPResponse(String url, OCSPReq ocspReq) throws IOException, MalformedURLException {
        byte[] ocspReqData = ocspReq.getEncoded();
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setRequestProperty("Content-Type", "application/ocsp-request");
            con.setRequestProperty("Accept", "application/ocsp-response");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            OutputStream out = con.getOutputStream();
            try {
                IOUtils.write(ocspReqData, out);
                out.flush();
            } finally {
                IOUtils.closeQuietly(out);
            }

            byte[] responseBytes = IOUtils.toByteArray(con.getInputStream());
            OCSPResp ocspResp = new OCSPResp(responseBytes);

            return ocspResp;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }


    /**
     * 获取证书中 OSCP的请求地址
     *
     * @param certificate
     * @return
     * @throws IOException
     */
    public static String getOCSPUrl(X509Certificate certificate) throws IOException {
        ASN1Primitive obj;
        try {
            obj = getExtensionValue(certificate, Extension.authorityInfoAccess.getId());
        } catch (IOException ex) {
            return null;
        }

        if (obj == null) {
            return null;
        }

        AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(obj);

        AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
        for (AccessDescription accessDescription : accessDescriptions) {
            boolean correctAccessMethod = accessDescription.getAccessMethod().equals(X509ObjectIdentifiers.ocspAccessMethod);
            if (!correctAccessMethod) {
                continue;
            }

            GeneralName name = accessDescription.getAccessLocation();
            if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
                continue;
            }

            DERIA5String derStr = DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
            return derStr.getString();
        }

        return null;
    }

    /**
     * @param certificate the certificate from which we need the ExtensionValue
     * @param oid         the Object Identifier value for the extension.
     * @return the extension value as an ASN1Primitive object
     * @throws IOException
     */
    private static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
        byte[] bytes = certificate.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }
}
