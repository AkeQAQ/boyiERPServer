package com.boyi.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.EmailUtils;
import com.boyi.controller.*;
import com.boyi.entity.*;
import com.boyi.mapper.OtherMapper;
import com.boyi.mapper.RepositoryStockHistoryMapper;
import com.boyi.mapper.RepositoryStockMapper;
import com.boyi.mapper.SysUserMapper;
import com.boyi.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@Slf4j
public class SaticScheduleTask {

    @Autowired
    private RepositoryStockService repositoryStockService;

    @Autowired
    private BaseMaterialService baseMaterialService;


    @Autowired
    private ScheduleTaskService scheduleTaskService;

    private Long heartInterval = 30000L;



    @Value("${boyi.toEmail}")
    private String toEmail;

    @Value("${boyi.csEmails}")
    private String csEmails;

    // 库存预警线
    @Scheduled(cron = "0 0 8 * * ?")
    private void validStock() {
        // 1. 查看有设置低预警线的物料，然后对比库存
        List<BaseMaterial> warnings = baseMaterialService.getLowWarningLines();
        log.info("【库存预警线】,查出{}物料设置了预警线", warnings);
        StringBuilder sb = new StringBuilder();
        for (BaseMaterial bm : warnings) {
            RepositoryStock stock = repositoryStockService.getByMaterialId(bm.getId());
            if (stock != null && stock.getNum() != null && stock.getNum() <= bm.getLowWarningLine()) {
                sb.append("物料编码[" + bm.getId() + "]-[" + bm.getName() + "]，库存数目[" + stock.getNum() + "][" + bm.getUnit() + "] <= 预警数目[" + bm.getLowWarningLine() + "]");
                sb.append("<br>");
            }
        }
        if (sb.length() == 0) {
            return;
        }
        try {
            EmailUtils.sendMail(EmailUtils.MODULE_NAME, toEmail, csEmails.split(","), sb.toString());
        } catch (MessagingException e) {
            log.error("【库存预警线】发送邮件报错..", e);
        }
    }

    // 心跳检测
    @Scheduled(cron = "0/10 * * * * ?")
    private void validOnline() {
        Set<String> removeSets = new HashSet<>();
        for (Map.Entry<String, Long> entry : HeartController.onlineMap.entrySet()) {
            String key = entry.getKey();
            String[] split = key.split(HeartController.KEY_SPERATOR);
            String ipAndUserName = split[0];
            String userJwt = split[1];

            String[] ipUserName = ipAndUserName.split(":");
            String name = ipUserName[1];
            Long value = entry.getValue();
            long now = System.currentTimeMillis();


            if ((now - value) > heartInterval) {
                // 超过阈值，用户心跳丢失。变成不在线
                log.info("【心跳检测】用户:{},jwt:{},上次心跳时间:{}，当前时间:{}, 间隔超出阈值:{},改成下线状态.",
                        ipAndUserName, userJwt, new Date(value), new Date(now), heartInterval);
                removeSets.add(key);

                // 同时移除 该用户占用的锁
                log.info("【心跳检测】用户:{},移除锁", name);
                removeLock(name, RepositoryBuyinDocumentController.locks);
                removeLock(name, RepositoryBuyoutDocumentController.locks);
                removeLock(name, RepositoryPickMaterialController.locks);
                removeLock(name, RepositoryReturnMaterialController.locks);
                removeLock(name, OrderBuyorderDocumentController.locks);

            }
        }
        for (String key : removeSets) {
            HeartController.onlineMap.remove(key);
        }
    }

    private void removeLock(String name, Map<Long, String> locks) {
        HashSet<Long> removeDocSets = new HashSet<>();
        for (Map.Entry<Long, String> docId_userName : locks.entrySet()) {
            Long docId = docId_userName.getKey();
            String username = docId_userName.getValue();
            if (name.equals(username)) {
                removeDocSets.add(docId);
            }
        }
        for (Long docId : removeDocSets) {
            locks.remove(docId);
        }
    }

    //3.每日修改单据的ID前缀为当日日期,每日 新增昨日的历史库存
    @Scheduled(cron = "0 * * * * ?")
    private void configureTasks() {
        this.scheduleTaskService.configureTasks();
    }

    // 因为产品订单的订单号每年会重复，因此在每天的初始，就去把去年同日期的移动到历史表。把订单号加上年份，存储
    @Scheduled(cron = "0 * * * * ?")
    private void changeProductOrderAndProgress() {
        this.scheduleTaskService.changeProductOrderAndProgress();

    }

    @Scheduled(cron = "0 * * * * ?")
    private void changeProduceBatch() {
        this.scheduleTaskService.changeProduceBatchTranService();
    }

    @Scheduled(cron = "0 0 9 * * ?")
    private void monitorDisk(){
        InputStream in = null;
        InputStreamReader isr=null;
        BufferedReader read = null;
        try {
            String[] cmds = {"/bin/sh","-c","df -h"};
            Process pro = Runtime.getRuntime().exec(cmds);
//            pro.waitFor(); 较长时间需要
             in = pro.getInputStream();
             isr = new InputStreamReader(in);
             read = new BufferedReader(isr);
            String line = null;
            StringBuilder sb = new StringBuilder();

            while((line = read.readLine())!=null){
                sb.append(line);
                sb.append("<br>");
            }
            EmailUtils.sendMail(EmailUtils.MONITOR_NAME, "244454526@qq.com",new String[]{}, sb.toString());

            read.close();
            isr.close();
            in.close();
        }catch (IOException ioe){
            try {
            log.error("IO报错,",ioe);
            if(read!=null){
                read.close();
            }
            if(isr!=null){
                isr.close();
            }
            if(in!=null){
                in.close();
            }
            EmailUtils.sendMail(EmailUtils.MONITOR_NAME, "244454526@qq.com",new String[]{}, ioe.getMessage());
            } catch (Exception e) {
                log.error("报错.",e);
            }
        }catch (Exception e){
            log.error("报错,",e);
        }

    }

}



