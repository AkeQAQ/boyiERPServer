package com.boyi.config;

import com.boyi.common.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@Slf4j
public class ShutDownConfig {
    @PreDestroy
    public  void preDestroyMethod(){
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append("关机时间:").append(now.toString());
        try {
            EmailUtils.sendMail(EmailUtils.MONITOR_NAME, "244454526@qq.com",new String[]{}, sb.toString());
        } catch (MessagingException e) {
            log.error("error:",e);
        }
        log.info("{} 关机了",now);
    }
}
