package com.boyi.aop;

import com.boyi.entity.AnalysisRequest;
import com.boyi.service.AnalysisRequestService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Component
@Aspect
@Slf4j
public class CastAop {

    @Autowired
    private AnalysisRequestService analysisRequestService;

    @Around(value = "execution(* com.boyi.controller.*.*(..))")
    public Object round(ProceedingJoinPoint joinPoint) throws Throwable{
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long end = System.currentTimeMillis();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();//这个RequestContextHolder是Springmvc提供来获得请求的东西
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        String url = request.getRequestURL().toString();

        if(url.endsWith("sendHeart")){
            return proceed;
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String requestMethod = request.getMethod();
        String remoteIp = request.getRemoteAddr();
        long cast = end - start;
        log.info("【AOP 日志】请求用户:{},请求url:{},请求方式:{},来源ip:{},请求参数:{},执行class方法:{} ,耗时:{}ms",
                principal,url,requestMethod,remoteIp,url.contains("/order/productPricePre") ? "":joinPoint.getArgs(),
                joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()
        , cast);
        AnalysisRequest pojo = new AnalysisRequest();
        pojo.setUserName(principal.toString());
        pojo.setUrl(url);
        pojo.setIp(remoteIp);
        pojo.setClassMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        pojo.setCast(cast);
        pojo.setCreatedTime(LocalDateTime.now());
        try {
            analysisRequestService.save(pojo);
        }catch (Exception e){
            log.error("【AOP 日志】 分析请求 ，入库报错.",e);
        }
        return proceed;
    }

}
