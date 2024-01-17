package top.dongxiaohao.validatap12status.dto;

import lombok.Data;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 17:37
 */
@Data
public class BaseResponse<T> {
    private Integer code;
    private String msg;
    private T data;
}
