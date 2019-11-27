package com.common.web.error;

import com.common.web.util.RestResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 异常处理器
 *
 * @version V1.0
 **/
@ControllerAdvice
public class ExceptionTranslator {
    /**
     * 处理业务异常
     *
     * @param ex 异常对象
     * @return 返回前端的对象
     */
    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<RestResult<Void>> baseBusinessExceptionHandler(BaseBusinessException ex) {
        RestResult<Void> restResult = RestResult.fail(ex.getErrorCode())
                .msg(ex.getErrorMessage())
                .details(ex.getDetails());
        return ResponseEntity.ok(restResult);
    }

    /**
     * 运行时异常处理器
     *
     * @param ex 运行时异常
     * @return 返回前端的对象
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestResult<Void>> runtimeExceptionHandler(RuntimeException ex) {
        RestResult<Void> restResult = RestResult.fail("500")
                .msg(ex.getMessage());
        return ResponseEntity.ok(restResult);
    }
}
