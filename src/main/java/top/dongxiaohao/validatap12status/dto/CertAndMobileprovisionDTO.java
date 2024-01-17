package top.dongxiaohao.validatap12status.dto;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 17:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CertAndMobileprovisionDTO {
    /**
     * 描述文件是否和证书匹配 true - 匹配 false - 不匹配
     */
    private Boolean isMatch;
    /**
     * 描述文件创建时间
     */
    private DateTime mobileprovisionCreateTime;
    /**
     * 描述文件过期时间
     */
    private DateTime mobileprovisionExpireTime;
    /**
     * 描述文件剩余天数
     */
    private Long mobileprovisionRemainingDays;
    /**
     * 证书信息
     */
    private CertStatusDTO certStatusDTO;
}
