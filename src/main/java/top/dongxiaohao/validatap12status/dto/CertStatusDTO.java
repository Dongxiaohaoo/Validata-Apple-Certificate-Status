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
    private Integer status;
    private String certName;
    private String certSerialNumber;
    private String certCreateTime;
    private String certExpireTime;
    private String certRevokeTime;
    private Integer certRevokeReason;
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
