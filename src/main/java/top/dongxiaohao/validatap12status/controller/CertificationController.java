package top.dongxiaohao.validatap12status.controller;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartFile;
import top.dongxiaohao.validatap12status.dto.BaseReponse;
import top.dongxiaohao.validatap12status.dto.CertAndMobileprovisionDTO;
import top.dongxiaohao.validatap12status.dto.CertStatusDTO;
import top.dongxiaohao.validatap12status.util.CertUtil;
import top.dongxiaohao.validatap12status.util.OCSPUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 17:45
 */
@RestController
@RequestMapping("/api")
public class CertificationController {

    private static final Integer RETRY_COUNT = 3;

    @PostMapping({"/checkP12"})
    public BaseReponse<CertStatusDTO> check(MultipartFile cert, String password) throws Exception {
        Assert.notNull(cert, "file can not be null");
        String p12Str = Base64Encoder.encode(cert.getBytes());
        CertStatusDTO certStatusDTO = OCSPUtil.checkCertStatus(p12Str, password, RETRY_COUNT);
        CertUtil.getCertInfo(p12Str, password, certStatusDTO);
        BaseReponse<CertStatusDTO> certStatusDTOBaseReponse = new BaseReponse();
        certStatusDTOBaseReponse.setCode(200);
        certStatusDTOBaseReponse.setMsg("success");
        certStatusDTOBaseReponse.setData(certStatusDTO);
        return certStatusDTOBaseReponse;
    }

    @PostMapping({"/checkMobileProvesion"})
    public BaseReponse<CertAndMobileprovisionDTO> checkMobileProvesion(MultipartFile cert, MultipartFile mobileprovision, String password) throws Exception {
        Assert.notNull(cert, "cert can not be null");
        Assert.notNull(mobileprovision, "mobileprovision can not be null");
        Assert.notBlank(password, "password can not blank");
        String p12Str = Base64Encoder.encode(cert.getBytes());
        CertStatusDTO certStatusDTO = OCSPUtil.checkCertStatus(p12Str, password, RETRY_COUNT);
        CertUtil.getCertInfo(p12Str, password, certStatusDTO);
        String certName = certStatusDTO.getCertName();
        String mobileContent = new String(mobileprovision.getBytes(), "UTF-8");
        Map<String, String> mobileInfo = this.getMobileInfo(mobileContent);
        String teamName = mobileInfo.get("teamName");
        String expirationDate = mobileInfo.get("expirationDate");
        String creationDate = mobileInfo.get("creationDate");
        CertAndMobileprovisionDTO certAndMobileprovisionDTO = new CertAndMobileprovisionDTO();
        DateTime expireDateParse = DateUtil.parse(expirationDate, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTime createDateParse = DateUtil.parse(creationDate, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        certAndMobileprovisionDTO.setCertStatusDTO(certStatusDTO);
        certAndMobileprovisionDTO.setMobileprovisionCreateTime(createDateParse);
        certAndMobileprovisionDTO.setMobileprovisionExpireTime(expireDateParse);
        certAndMobileprovisionDTO.setMobileprovisionRemainingDays(DateUtil.betweenDay(expireDateParse, new Date(), false));
        certAndMobileprovisionDTO.setIsMatch(Objects.equals(certName, teamName));
        BaseReponse<CertAndMobileprovisionDTO> response = new BaseReponse();
        response.setCode(200);
        response.setMsg("success");
        response.setData(certAndMobileprovisionDTO);
        return response;
    }

    public Map<String, String> getMobileInfo(String inputString) {
        Map<String, String> res = new HashMap(16);
        String expirationDatePatternString = "<key>ExpirationDate</key>\\s*<date>(.*?)</date>";
        String creationDatePatternString = "<key>CreationDate</key>\\s*<date>(.*?)</date>";
        String namePatternString = "<key>TeamName</key>\\s*<string>(.*?)</string>";
        Pattern expirationDatePattern = Pattern.compile(expirationDatePatternString);
        Pattern creationDatePattern = Pattern.compile(creationDatePatternString);
        Pattern namePattern = Pattern.compile(namePatternString);
        Matcher expirationDateMatcher = expirationDatePattern.matcher(inputString);
        Matcher creationDateMatcher = creationDatePattern.matcher(inputString);
        Matcher nameMatcher = namePattern.matcher(inputString);
        String teamName;
        String creationDate;
        String expirationDate;
        if (expirationDateMatcher.find()) {
            expirationDate = expirationDateMatcher.group(1);
            res.put("expirationDate", expirationDate);
        }
        if (creationDateMatcher.find()) {
            creationDate = creationDateMatcher.group(1);
            res.put("creationDate", creationDate);
        }
        if (nameMatcher.find()) {
            teamName = nameMatcher.group(1);
            res.put("teamName", teamName);
        }
        return res;
    }
}
