package com.boyi.config;

import com.boyi.mapper.OtherMapper;
import com.boyi.mapper.SysUserMapper;
import com.boyi.service.BaseDepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@Slf4j
public class SaticScheduleTask {

    @Autowired
    private OtherMapper otherMapper;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    private Set<String> tables = new HashSet<>();
    {
        tables.add("repository_buyin_document");
        tables.add("repository_buyout_document");
        tables.add("repository_pick_material");
        tables.add("repository_return_material");
        tables.add("order_buyorder_document");

    }

    //3.添加定时任务
    @Scheduled(cron = "0 * * * * ?")
    private void configureTasks() {
        System.err.println("【定时任务】执行静态定时任务时间: " + LocalDateTime.now());

        for (String dbName : tables){
            // 1. 获取数据库的 自增ID 的时间段
            String autoIncrement = otherMapper.getAutoIncrement(dbName)+"";
            String dbDayStr = autoIncrement .length() < 10 ? autoIncrement:autoIncrement .substring(0, 6);
            // 2. 应用服务器的时间所在日期
            Date today = new Date();
            String todayStr = sdf.format(today);
            if(!dbDayStr.equals(todayStr)){
                Long increment = Long.valueOf(todayStr + "0001");
                if(dbName.equals("repository_buyin_document")){
                    otherMapper.alertBuyInAutoIncrement(increment);
                }else if(dbName.equals("repository_buyout_document")){
                    otherMapper.alertBuyOutAutoIncrement(increment);

                }else if(dbName.equals("repository_pick_material")){
                    otherMapper.alertPickMaterialAutoIncrement(increment);

                }else if(dbName.equals("repository_return_material")){
                    otherMapper.alertReturnMaterialAutoIncrement(increment);

                }else if(dbName.equals("order_buyorder_document")){
                    otherMapper.alertBuyOrderAutoIncrement(increment);
                }else{
                    return;
                }

                log.info("【定时任务】dbName[{}] ,todayStr [{}],dbDayStr[{}]不相等,修改成[{}]",dbName,todayStr,dbDayStr,increment);
                System.err.println();
            }else{
                log.info("【定时任务】dbName[{}] ,todayStr[{}],dbDayStr[{}]相等",dbName,todayStr,dbDayStr);
            }
        }


    }
}
