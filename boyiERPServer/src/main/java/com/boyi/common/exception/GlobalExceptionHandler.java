package com.boyi.common.exception;

import com.boyi.controller.base.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

/**
 * 全局异常处理
 * @RestControllerAdvice 定义全局控制器异常处理
 * @ExceptionHandler表示针对性异常处理，可对每种异常针对性处理。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseResult handler(AccessDeniedException e) {
        log.info("security权限不足：----------------{}", e.getMessage());
        return ResponseResult.fail(ResponseResult.ERROR_CODE_401,"权限不足");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseResult handler(MethodArgumentNotValidException e) {
        log.info("实体校验异常：----------------{}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        return ResponseResult.fail(ResponseResult.ERROR_CODE_402,objectError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseResult handler(IllegalArgumentException e) {
        log.error("Assert异常：----------------{}", e.getMessage());
        return ResponseResult.fail(ResponseResult.ERROR_CODE_402,e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseResult handler(RuntimeException e) {
        log.error("运行时异常：----------------{}", e);
        if(e instanceof DuplicateKeyException){
            return ResponseResult.fail("有字段重复冲突，请检查!");
        }
        return ResponseResult.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = Exception.class)
    public ResponseResult handler(Exception e) throws Exception{
        log.error("系统内部其他异常：----------------{}", e);
        return ResponseResult.fail(e.getMessage());
    }
}
