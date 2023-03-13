package com.boyi.aop;

import com.boyi.entity.*;
import com.boyi.service.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Component
@Aspect
@Slf4j
public class CastAop {

    @Autowired
    private AnalysisRequestService analysisRequestService;

    @Autowired
    private FinanceCloseService financeCloseService;

    @Autowired
    public FinanceSupplierPayshoesService financeSupplierPayshoesService;

    @Autowired
    public FinanceSupplierChangeService financeSupplierChangeService;

    @Autowired
    public FinanceSupplierRoundDownService financeSupplierRoundDownService;

    @Autowired
    public FinanceSupplierFineService financeSupplierFineService;

    @Autowired
    public FinanceSupplierTaxDeductionService financeSupplierTaxDeductionService;
    @Autowired
    public FinanceSupplierTaxSupplementService financeSupplierTaxSupplementService;
    @Autowired
    public FinanceSupplierTestService financeSupplierTestService;


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


    @Around(value =
                    "execution(* com.boyi.controller.FinanceSupplierPayshoesController.save(..)) " +
                    "|| execution(* com.boyi.controller.FinanceSupplierPayshoesController.statusReturn(..)) "+
                    "|| execution(* com.boyi.controller.FinanceSupplierPayshoesController.update(..)) " +

                    "|| execution(* com.boyi.controller.FinanceSupplierChangeController.save(..)) " +
                    "|| execution(* com.boyi.controller.FinanceSupplierChangeController.statusReturn(..)) "+
                    "|| execution(* com.boyi.controller.FinanceSupplierChangeController.update(..)) " +

                    "|| execution(* com.boyi.controller.FinanceSupplierFineController.save(..)) " +
                    "|| execution(* com.boyi.controller.FinanceSupplierFineController.statusReturn(..)) "+
                    "|| execution(* com.boyi.controller.FinanceSupplierFineController.update(..)) " +

                    "|| execution(* com.boyi.controller.FinanceSupplierTestController.save(..)) " +
                    "|| execution(* com.boyi.controller.FinanceSupplierTestController.statusReturn(..)) "+
                    "|| execution(* com.boyi.controller.FinanceSupplierTestController.update(..)) " +

                    "|| execution(* com.boyi.controller.FinanceSupplierTaxSupplementController.save(..)) " +
                    "|| execution(* com.boyi.controller.FinanceSupplierTaxSupplementController.statusReturn(..)) "+
                    "|| execution(* com.boyi.controller.FinanceSupplierTaxSupplementController.update(..)) " +

                    "|| execution(* com.boyi.controller.FinanceSupplierTaxDeductionController.save(..)) " +
                    "|| execution(* com.boyi.controller.FinanceSupplierTaxDeductionController.statusReturn(..)) "+
                    "|| execution(* com.boyi.controller.FinanceSupplierTaxDeductionController.update(..)) "

    )
    public Object closeValid(ProceedingJoinPoint joinPoint) throws Throwable{

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();//这个RequestContextHolder是Springmvc提供来获得请求的东西
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        String url = request.getRequestURL().toString();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String requestMethod = request.getMethod();
        String remoteIp = request.getRemoteAddr();

        try {
            Object[] args = joinPoint.getArgs();
            Object arg = args[1];

            valid(arg,url);


        }catch (Exception e){
            log.error("【AOP 日志】 分析请求 ，入库报错.",e);
            throw e;
        }
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long end = System.currentTimeMillis();
        long cast = end - start;
        log.info("【AOP 日志】【closeValid】请求用户:{},请求url:{},请求方式:{},来源ip:{},请求参数:{},执行class方法:{} ,耗时:{}ms",
                principal,url,requestMethod,remoteIp,joinPoint.getArgs(),
                joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName()
                , cast);

        return proceed;
    }

    private void valid(Object arg,String url) {
        FinanceClose close = financeCloseService.listLatestOne();
        if(close==null){
            return;
        }
        Boolean flag = true;

        if(url.contains("/finance/supplierChange")){
            if(arg instanceof Long){
                Long id = (Long)arg;
                FinanceSupplierChange obj = financeSupplierChangeService.getById(id);
                 flag = validFinanceIsClosed(close.getCloseDate(), obj.getChangeDate());
            }else if(arg instanceof FinanceSupplierChange){
                FinanceSupplierChange obj = (FinanceSupplierChange)arg;
                 flag = validFinanceIsClosed(close.getCloseDate(), obj.getChangeDate());
            }
            if(!flag){
                throw new RuntimeException("日期请设置在财务关账日之后");
            }
        }

        if(url.contains("/finance/supplierPayshoes")){
            if(arg instanceof Long){
                Long id = (Long)arg;
                FinanceSupplierPayshoes obj = financeSupplierPayshoesService.getById(id);
                flag = validFinanceIsClosed(close.getCloseDate(), obj.getPayDate());
            }else if(arg instanceof FinanceSupplierPayshoes){
                FinanceSupplierPayshoes obj = (FinanceSupplierPayshoes)arg;
                flag = validFinanceIsClosed(close.getCloseDate(), obj.getPayDate());
            }
            if(!flag){
                throw new RuntimeException("日期请设置在财务关账日之后");
            }
        }

        if(url.contains("/finance/supplierFine")){
            FinanceSupplierFine obj = null;
            if(arg instanceof Long){
                Long id = (Long)arg;
                obj = financeSupplierFineService.getById(id);
            }else if(arg instanceof FinanceSupplierFine){
                obj = (FinanceSupplierFine)arg;
            }
            flag = validFinanceIsClosed(close.getCloseDate(), obj.getFineDate());
            if(!flag){
                throw new RuntimeException("日期请设置在财务关账日之后");
            }
        }

        if(url.contains("/finance/supplierTest")){
            FinanceSupplierTest obj = null;
            if(arg instanceof Long){
                Long id = (Long)arg;
                obj = financeSupplierTestService.getById(id);
            }else if(arg instanceof FinanceSupplierTest){
                obj = (FinanceSupplierTest)arg;
            }
            flag = validFinanceIsClosed(close.getCloseDate(), obj.getTestDate());
            if(!flag){
                throw new RuntimeException("日期请设置在财务关账日之后");
            }
        }

        if(url.contains("/finance/supplierTaxSupplement")){
            FinanceSupplierTaxSupplement obj = null;
            if(arg instanceof Long){
                Long id = (Long)arg;
                obj = financeSupplierTaxSupplementService.getById(id);
            }else if(arg instanceof FinanceSupplierTaxSupplement){
                obj = (FinanceSupplierTaxSupplement)arg;
            }
            flag = validFinanceIsClosed(close.getCloseDate(), obj.getDocumentDate());
            if(!flag){
                throw new RuntimeException("日期请设置在财务关账日之后");
            }
        }

        if(url.contains("/finance/supplierTaxDeduction")){
            FinanceSupplierTaxDeduction obj = null;
            if(arg instanceof Long){
                Long id = (Long)arg;
                obj = financeSupplierTaxDeductionService.getById(id);
            }else if(arg instanceof FinanceSupplierTaxDeduction){
                obj = (FinanceSupplierTaxDeduction)arg;
            }
            flag = validFinanceIsClosed(close.getCloseDate(), obj.getDocumentDate());
            if(!flag){
                throw new RuntimeException("日期请设置在财务关账日之后");
            }
        }

    }

    public Boolean validFinanceIsClosed(LocalDate dbCloseDate,LocalDate theDate) {
        return theDate.isAfter(dbCloseDate);
    }

}
