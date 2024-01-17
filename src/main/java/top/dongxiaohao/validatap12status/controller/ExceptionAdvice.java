package top.dongxiaohao.validatap12status.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.dongxiaohao.validatap12status.dto.BaseReponse;

/**
 * @Author: Dennis
 * @Date: 2024/1/17 17:44
 */
@RestControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler({Exception.class})
    public BaseReponse<String> handleException(Exception e) {
        BaseReponse<String> baseReponse = new BaseReponse();
        baseReponse.setCode(500);
        baseReponse.setMsg(e.getMessage());
        return baseReponse;
    }
}
