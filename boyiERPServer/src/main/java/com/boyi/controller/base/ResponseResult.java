package com.boyi.controller.base;

import lombok.Data;

import java.io.Serializable;

/**
 *  和前端的 统一接口格式
 */
@Data
public class ResponseResult implements Serializable {

    public static final int SUCCESS_CODE = 200; // 正常状态码
    public static final String SUCCESS_MSG = "操作成功";

    public static final int ERROR_CODE_400 = 400; // 系统内部代码异常的状态码
    public static final int ERROR_CODE_401 = 401; // 无权限 状态码

    public static final int ERROR_CODE_402 = 402; // 校验异常
    public static final int ERROR_CODE_403 = 403; // 认证异常




    private int code; // 200是正常，非200表示异常
    private String msg;
    private Object data;

    public static ResponseResult succ(Object data) {
        return succ(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static ResponseResult succ(int code, String msg, Object data) {
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static ResponseResult fail(String msg) {
        return fail(ERROR_CODE_400, msg, null);
    }
    public static ResponseResult fail(int code, String msg) {
        return fail(code, msg, null);
    }

    public static ResponseResult fail(String msg, Object data) {
        return fail(ERROR_CODE_400, msg, data);
    }

    public static ResponseResult fail(int code, String msg, Object data) {
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
