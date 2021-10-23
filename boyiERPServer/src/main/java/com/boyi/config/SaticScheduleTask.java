package com.boyi.config;

import com.boyi.controller.HeartController;
import com.boyi.entity.RepositoryStock;
import com.boyi.entity.RepositoryStockHistory;
import com.boyi.mapper.OtherMapper;
import com.boyi.mapper.RepositoryStockHistoryMapper;
import com.boyi.mapper.RepositoryStockMapper;
import com.boyi.mapper.SysUserMapper;
import com.boyi.service.BaseDepartmentService;
import com.boyi.service.RepositoryStockHistoryService;
import com.boyi.service.RepositoryStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@Slf4j
public class SaticScheduleTask {

    @Autowired
    private OtherMapper otherMapper;

    @Autowired
    private RepositoryStockService repositoryStockService;

    @Autowired
    private RepositoryStockHistoryService repositoryStockHistoryService;

    private Long heartInterval = 20000L;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    private Set<String> tables = new HashSet<>();
    {
        tables.add("repository_buyin_document");
        tables.add("repository_buyout_document");
        tables.add("repository_pick_material");
        tables.add("repository_return_material");
        tables.add("order_buyorder_document");

    }

    // 心跳检测
    @Scheduled(cron = "0/5 * * * * ?")
    private void validOnline() {
        Set<String> removeSets = new HashSet<>();
        for(Map.Entry<String,Long> entry:HeartController.onlineMap.entrySet()){
            String key = entry.getKey();
            String[] split = key.split(HeartController.KEY_SPERATOR);
            String userName = split[0];
            String userJwt = split[1];

            Long value = entry.getValue();
            long now = System.currentTimeMillis();


            if((now - value) > heartInterval){
                // 超过5秒阈值，用户心跳丢失。变成不在线
                log.info("【心跳检测】用户:{},jwt:{},上次心跳时间:{}，当前时间:{}, 间隔超出阈值:{},改成下线状态.",
                        userName,userJwt,new Date(value),new Date(now),heartInterval);
                removeSets.add(key);
            }
        }
        for (String key : removeSets){
            HeartController.onlineMap.remove(key);
        }
    }

    // 每日库存保存
    @Scheduled(cron = "0 0 0 * * ?")
    private void everyDayStock() {
        List<RepositoryStock> stockList = repositoryStockService.list();
        LocalDate yestoday = LocalDate.now().plusDays(-1);
        ArrayList<RepositoryStockHistory> saveList = new ArrayList<>();
        for (RepositoryStock repositoryStock : stockList){
            RepositoryStockHistory history = new RepositoryStockHistory();
            history.setMaterialId(repositoryStock.getMaterialId());
            history.setNum(repositoryStock.getNum());
            history.setDate(yestoday);
            saveList.add(history);
        }
        long start = System.currentTimeMillis();
        repositoryStockHistoryService.saveBatch(saveList);
        long end = System.currentTimeMillis();
        log.info("成功复制日期:{}的及时库存，条数:{} 到历史表.复制耗时:{}ms",yestoday,stockList.size(),(end-start));
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
