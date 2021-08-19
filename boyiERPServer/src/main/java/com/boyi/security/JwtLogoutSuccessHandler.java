package com.boyi.security;

import cn.hutool.json.JSONUtil;
import com.boyi.common.utils.JwtUtils;
import com.boyi.controller.base.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {
    @Autowired
    JwtUtils jwtUtils;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            String name = authentication.getName();
            log.info("用户[{}] 退出成功",name);
        }
        response.setContentType("application/json;charset=UTF-8");
        log.info("设置头部信息jwt为空");
        response.setHeader(jwtUtils.getHeader(), "");
        ServletOutputStream out = response.getOutputStream();
        ResponseResult result = ResponseResult.succ("");
        out.write(JSONUtil.toJsonStr(result).getBytes("UTF-8"));
        out.flush();
        out.close();
    }
}