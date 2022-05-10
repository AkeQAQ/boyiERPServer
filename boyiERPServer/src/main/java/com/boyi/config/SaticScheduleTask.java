package com.boyi.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    private OtherMapper otherMapper;

    @Autowired
    private RepositoryStockService repositoryStockService;

    @Autowired
    private RepositoryStockHistoryService repositoryStockHistoryService;

    @Autowired
    private BaseMaterialService baseMaterialService;

    @Autowired
    private OrderProductOrderService orderProductOrderService;

    @Autowired
    private ProduceOrderMaterialProgressService produceOrderMaterialProgressService;

    @Autowired
    private HisProduceOrderMaterialProgressService hisProduceOrderMaterialProgressService;

    @Autowired
    private HisOrderProductOrderService hisOrderProductOrderService;

    @Autowired
    private ProduceBatchService produceBatchService;

    @Autowired
    private HisProduceBatchService hisProduceBatchService;

    private Long heartInterval = 30000L;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    private Set<String> tables = new HashSet<>();

    {
        tables.add("repository_buyin_document");
        tables.add("repository_buyout_document");
        tables.add("repository_pick_material");
        tables.add("repository_return_material");
        tables.add("order_buyorder_document");

    }

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

    // 每日库存保存
    private void everyDayStock() {
        List<RepositoryStock> stockList = repositoryStockService.list();
        LocalDate yestoday = LocalDate.now().plusDays(-1);
        ArrayList<RepositoryStockHistory> saveList = new ArrayList<>();
        for (RepositoryStock repositoryStock : stockList) {
            RepositoryStockHistory history = new RepositoryStockHistory();
            history.setMaterialId(repositoryStock.getMaterialId());
            history.setNum(repositoryStock.getNum());
            history.setDate(yestoday);
            saveList.add(history);
        }
        long start = System.currentTimeMillis();
        repositoryStockHistoryService.saveBatch(saveList);
        long end = System.currentTimeMillis();
        log.info("成功复制日期:{}的及时库存，条数:{} 到历史表.复制耗时:{}ms", yestoday, stockList.size(), (end - start));
    }

    //3.每日修改单据的ID前缀为当日日期,每日 新增昨日的历史库存
    @Scheduled(cron = "0 * * * * ?")
    private void configureTasks() {
        System.err.println("【定时任务】执行静态定时任务时间: " + LocalDateTime.now());

        LocalDate yestoday = LocalDate.now().plusDays(-1);
        String yestodayStr = yestoday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int count = repositoryStockHistoryService.count(new QueryWrapper<RepositoryStockHistory>().eq("date", yestodayStr));
        if (count == 0) {
            this.everyDayStock();
        }

        for (String dbName : tables) {
            // 1. 获取数据库的 自增ID 的时间段
            String autoIncrement = otherMapper.getAutoIncrement(dbName) + "";
            String dbDayStr = autoIncrement.length() < 10 ? autoIncrement : autoIncrement.substring(0, 6);
            // 2. 应用服务器的时间所在日期
            Date today = new Date();
            String todayStr = sdf.format(today);
            if (!dbDayStr.equals(todayStr)) {
                Long increment = Long.valueOf(todayStr + "0001");
                if (dbName.equals("repository_buyin_document")) {
                    otherMapper.alertBuyInAutoIncrement(increment);
                } else if (dbName.equals("repository_buyout_document")) {
                    otherMapper.alertBuyOutAutoIncrement(increment);

                } else if (dbName.equals("repository_pick_material")) {
                    otherMapper.alertPickMaterialAutoIncrement(increment);

                } else if (dbName.equals("repository_return_material")) {
                    otherMapper.alertReturnMaterialAutoIncrement(increment);

                } else if (dbName.equals("order_buyorder_document")) {
                    otherMapper.alertBuyOrderAutoIncrement(increment);
                } else {
                    return;
                }

                log.info("【定时任务】dbName[{}] ,todayStr [{}],dbDayStr[{}]不相等,修改成[{}]", dbName, todayStr, dbDayStr, increment);
                System.err.println();
            } else {
                log.info("【定时任务】dbName[{}] ,todayStr[{}],dbDayStr[{}]相等", dbName, todayStr, dbDayStr);
            }
        }

    }

    // 因为产品订单的订单号每年会重复，因此在每天的初始，就去把去年同日期的移动到历史表。把订单号加上年份，存储
    @Scheduled(cron = "0 * * * * ?")
    private void changeProductOrderAndProgress() {
        LocalDate now = LocalDate.now();
        int m = now.getMonthValue();
        int d = now.getDayOfMonth();
        String md = m+""+d;
        int year = now.getYear()-1;
        // 1. 获取老的产品订单表
        List<OrderProductOrder> lists =orderProductOrderService.listByMonthAndDay(md);
        log.info("【定时任务】获取月日%{},去年创建的产品订单数据{}",md,lists);
        if(lists.isEmpty()){
            return;
        }
        // 2. 获取老的进度表
        Set<Long> orderIds = new HashSet<>();
        for(OrderProductOrder order : lists){
            orderIds.add(order.getId());
        }
        List<ProduceOrderMaterialProgress> progresses = produceOrderMaterialProgressService.listByOrderIds(orderIds);
        log.info("【定时任务】获取采购进度，订单号{}",orderIds);
        // 3. 添加到历史表，并且移除原表
        addOrderHisAndRemove(lists,year);
        log.info("【定时任务】添加产品订单历史表{}，删除产品订单表",lists);
        addProgressHisAndRemove(progresses,year);
        log.info("【定时任务】添加进度表{}，删除进度表",progresses);

    }

    @Scheduled(cron = "0 * * * * ?")
    private void changeProduceBatch() {
        LocalDate now = LocalDate.now();
        int m = now.getMonthValue();
        int d = now.getDayOfMonth();
        String md = m+""+d;
        int year = now.getYear()-1;

        // 3. 移动生产序号表
        List<ProduceBatch> batches = produceBatchService.listByMonthAndDay(md);
        if(batches==null || batches.size() ==0){
            return;
        }
        addBatchHisAndRemove(batches,year);
        log.info("【定时任务】添加生产序号历史表，删除生产序号表{}",batches);
    }

    private void addBatchHisAndRemove(List<ProduceBatch> batches, int year) {

        // 1. 添加到历史表（订单号,批次号加年份）
        ArrayList<HisProduceBatch> hisLists = new ArrayList<>();
        ArrayList<Long> removeIds = new ArrayList<>();

        for(ProduceBatch batch : batches){
            HisProduceBatch hisBatch = new HisProduceBatch();
            BeanUtils.copyProperties(batch,hisBatch);
            hisBatch.setId(batch.getId());
            hisBatch.setOrderNum(year+""+hisBatch.getOrderNum());
            hisBatch.setBatchId(Integer.valueOf(year+""+hisBatch.getBatchId()));

            hisLists.add(hisBatch);
            removeIds.add(batch.getId());
        }
        hisProduceBatchService.saveBatch(hisLists);
        // 2. 删除原表
        produceBatchService.removeByIds(removeIds);
    }

    private void addProgressHisAndRemove(List<ProduceOrderMaterialProgress> progresses,int year) {
        // 1. 添加到历史表
        ArrayList<HisProduceOrderMaterialProgress> hisLists = new ArrayList<>();
        ArrayList<Long> removeIds = new ArrayList<>();

        for(ProduceOrderMaterialProgress progress : progresses){
            HisProduceOrderMaterialProgress his = new HisProduceOrderMaterialProgress();
            BeanUtils.copyProperties(progress,his);
            hisLists.add(his);
            removeIds.add(progress.getId());
        }
        hisProduceOrderMaterialProgressService.saveBatch(hisLists);
        // 2. 删除原表
        produceOrderMaterialProgressService.removeByIds(removeIds);
    }

    private void addOrderHisAndRemove(List<OrderProductOrder> lists,int year) {

        // 1. 添加到历史表（订单号加年份）
        ArrayList<HisOrderProductOrder> hisLists = new ArrayList<>();
        ArrayList<Long> removeIds = new ArrayList<>();

        for(OrderProductOrder order : lists){
            HisOrderProductOrder hisOrder = new HisOrderProductOrder();
            BeanUtils.copyProperties(order,hisOrder);
            hisOrder.setId(order.getId());
            hisOrder.setOrderNum(year+""+hisOrder.getOrderNum());
            hisLists.add(hisOrder);
            removeIds.add(order.getId());
        }
        hisOrderProductOrderService.saveBatch(hisLists);
        // 2. 删除原表
        orderProductOrderService.removeByIds(removeIds);
    }
}



