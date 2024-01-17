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
    private Boolean isMatch;
    private DateTime mobileprovisionCreateTime;
    private DateTime mobileprovisionExpireTime;
    private Long mobileprovisionRemainingDays;
    private CertStatusDTO certStatusDTO;
}
