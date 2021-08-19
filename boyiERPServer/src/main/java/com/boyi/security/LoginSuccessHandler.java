package com.boyi.security;

import cn.hutool.json.JSONUtil;
import com.boyi.common.utils.JwtUtils;
import com.boyi.controller.base.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("loginSuccessHandler....");
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();            // 生成jwt返回
        String jwt = jwtUtils.generateToken(authentication.getName());
        log.info("根据name:{},生成jwt:{}",authentication.getName(),jwt);
        response.setHeader(jwtUtils.getHeader(), jwt);  // 把jwt 设置在响应头
        ResponseResult result = ResponseResult.succ("");
        outputStream.write(JSONUtil.toJsonStr(result).getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }
}