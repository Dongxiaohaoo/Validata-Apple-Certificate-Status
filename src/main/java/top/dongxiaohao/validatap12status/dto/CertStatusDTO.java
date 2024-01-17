package top.dongxiaohao.validatap12status.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 17:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CertStatusDTO {
    /**
     * 证书状态
     */
    private Integer status;
    /**
     * 证书名称
     */
    private String certName;
    /**
     * 证书序列号
     */
    private String certSerialNumber;
    /**
     * 证书创建时间
     */
    private String certCreateTime;
    /**
     * 证书过期时间
     */
    private String certExpireTime;
    /**
     * 证书吊销时间
     */
    private String certRevokeTime;
    /**
     * 证书吊销原因
     */
    private Integer certRevokeReason;
    /**
     * 证书剩余天数
     */
    private Long certRemainingDays;


    public enum Status {
        VALID(0, "有效"),
        INVALID(1, "无效"),
        REVOKED(2, "吊销"),
        UNKNOWN(3, "未知");

        private Integer code;
        private String msg;

        Status(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

    }
}
