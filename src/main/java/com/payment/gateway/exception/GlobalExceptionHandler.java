package com.payment.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, Object> handleException(Exception e) {
        log.error("全局异常捕获, error={}", e.getMessage(), e);
        Map<String, Object> result = new HashMap<>();
        result.put("code", "500");
        result.put("message", e.getMessage());
        return result;
    }

}