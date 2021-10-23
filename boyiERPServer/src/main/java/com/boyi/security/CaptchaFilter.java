package com.boyi.security;

import com.boyi.common.exception.CaptchaException;
import com.boyi.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 图片验证码校验过滤器，在登录过滤器前
 */
@Slf4j
@Component
public class CaptchaFilter  extends OncePerRequestFilter {
    private final String loginUrl = "/login";
    @Autowired
    RedisUtils redisUtil;
    @Autowired
    LoginFailureHandler loginFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*String url = request.getRequestURI();
        if (loginUrl.equals(url) && request.getMethod().equals("POST")) {
            log.info("获取到login链接，正在校验验证码 -- " + url);
            try {
                validate(request);
            } catch (CaptchaException e) {
                log.info(e.getMessage());            // 交给登录失败处理器处理
                loginFailureHandler.onAuthenticationFailure(request, response, e);
            }
        }*/
//        log.info("CaptchaFilter...starting...");
        Map<String, String[]> parameterMap = request.getParameterMap();
        filterChain.doFilter(request, response);
    }

    private void validate(HttpServletRequest request) throws CaptchaException{
        String code = request.getParameter("code");
        String token = request.getParameter("token");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(token)) {
            throw new CaptchaException("验证码不能为空");
        }
        if (!code.equals(redisUtil.hget("captcha", token))) {
            throw new CaptchaException("验证码不正确");
        }      // 一次性使用
        redisUtil.hdel("captcha", token);
    }
}
