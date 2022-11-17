package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.utils.EmailUtils;
import com.boyi.entity.*;
import com.boyi.mapper.OtherMapper;
import com.boyi.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/***
 *  需要事务的定时任务，写在此类下
 */
@Service
@Slf4j
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

    private Set<String> tables = new HashSet<>();

    {
        tables.add("repository_buyin_document");
        tables.add("repository_buyout_document");
        tables.add("repository_pick_material");
        tables.add("repository_return_material");
        tables.add("order_buyorder_document");

    }

    @Autowired
    private OtherMapper otherMapper;

    @Autowired
    private RepositoryStockService repositoryStockService;

    @Autowired
    private RepositoryStockHistoryService repositoryStockHistoryService;


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

    @Autowired
    private RepositoryPickMaterialService repositoryPickMaterialService;

    @Autowired
    private RepositoryReturnMaterialService repositoryReturnMaterialService;

    @Autowired
    private ProduceBatchDelayService produceBatchDelayService;

    @Autowired
    private ProduceBatchProgressService produceBatchProgressService;

    @Autowired
    private HisProduceBatchDelayService hisProduceBatchDelayService;

    @Autowired
    private HisProduceBatchProgressService hisProduceBatchProgressService;

    @Override
    @Transactional
    public void changeProduceBatchTranService() {
        try{
            LocalDate now = LocalDate.now();
            int m = now.getMonthValue();
            int d = now.getDayOfMonth();
            String md = m+""+d;

            // 3. 移动生产序号表
            List<ProduceBatch> batches = produceBatchService.listByMonthAndDay(md);
            if(batches==null || batches.size() ==0){
                return;
            }
            LocalDateTime createdDate = batches.get(0).getCreated();
            String year = createdDate.getYear()+"";
            log.info("【定时任务】【移动produceBatch】【添加生产序号历史表，删除生产序号表】【开始........】数据:{},year:{}",batches,year);
            addBatchHisAndRemove(batches,year);
            log.info("【定时任务】【移动produceBatch】【添加生产序号历史表，删除生产序号表】【结束........】");

            log.info("【定时任务】【移动produceBatch】【修改领料表，退料表】【开始........】");
            // 修改领料表，退料表得生产序号，加上日期
            updatePickReturnBatchId(batches,year);
            log.info("【定时任务】【移动produceBatch】【修改领料表，退料表】【结束........】");

            log.info("【定时任务】【移动produceBatch】【移动车间延期信息表】【开始........】");
            // 移动车间延期信息表
            addBatchDelayHisAndRemove(batches);
            log.info("【定时任务】【移动produceBatch】【移动车间延期信息表】【结束........】");

            log.info("【定时任务】【移动produceBatch】【移动车间进度表】【开始........】");
            // 移动车间进度表
            addBatchProgressHisAndRemove(batches);
            log.info("【定时任务】【移动produceBatch】【移动车间进度表】【结束........】");

        }catch (Exception e){
            log.error("发生异常..");
            try{
                EmailUtils.sendMail("博艺ERP系统", "244454526@qq.com",new String[]{}, "【定时任务】【定期移动produceBatch】报错..");
            }catch (Exception e2){
                log.error("邮件发送失败..");
            }
            throw e;
        }

    }

    @Transactional
    @Override
    public void changeProductOrderAndProgress() {
        try {
            LocalDate now = LocalDate.now();
            int m = now.getMonthValue();
            int d = now.getDayOfMonth();
            String md = m+""+d;
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

            String year = lists.get(0).getCreated().getYear()+"";

            log.info("【定时任务】添加产品订单历史表{}，删除产品订单表.year:{}",lists,year);
            // 3. 添加到历史表，并且移除原表
            addOrderHisAndRemove(lists,year);

            log.info("【定时任务】添加进度表{}，删除进度表",progresses);
            addProgressHisAndRemove(progresses);
        }catch (Exception e){
            log.error("发生异常..");
            try{
                EmailUtils.sendMail("博艺ERP系统", "244454526@qq.com",new String[]{}, "【定时任务】【定期移动ProductOrderAndProgress】报错..");
            }catch (Exception e2){
                log.error("邮件发送失败..");
            }
            throw e;
        }

    }

    @Transactional
    @Override
    public void configureTasks() {
        try {
            log.info("【定时任务】执行静态定时任务时间: {}" , LocalDateTime.now());

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
                } else {
                    log.info("【定时任务】dbName[{}] ,todayStr[{}],dbDayStr[{}]相等", dbName, todayStr, dbDayStr);
                }
            }
        }catch (Exception e){
            log.error("发生异常..");
            try{
                EmailUtils.sendMail("博艺ERP系统", "244454526@qq.com",new String[]{}, "【定时任务】【定期修改出入库自增ID的日期】报错..");
            }catch (Exception e2){
                log.error("邮件发送失败..");
            }
            throw e;
        }
    }


    private void addProgressHisAndRemove(List<ProduceOrderMaterialProgress> progresses) {
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

    private void addOrderHisAndRemove(List<OrderProductOrder> lists,String year) {

        // 1. 添加到历史表（订单号加年份）
        ArrayList<HisOrderProductOrder> hisLists = new ArrayList<>();
        ArrayList<Long> removeIds = new ArrayList<>();

        for(OrderProductOrder order : lists){
            HisOrderProductOrder hisOrder = new HisOrderProductOrder();
            BeanUtils.copyProperties(order,hisOrder);
            hisOrder.setId(order.getId());
            hisOrder.setOrderNum(year+hisOrder.getOrderNum());
            hisLists.add(hisOrder);
            removeIds.add(order.getId());
        }
        hisOrderProductOrderService.saveBatch(hisLists);
        // 2. 删除原表
        orderProductOrderService.removeByIds(removeIds);
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

    private void addBatchProgressHisAndRemove(List<ProduceBatch> batches) {

        List<Long> produceBatchIds = new ArrayList<>();
        List<Long> removeIds = new ArrayList<>();

        // 1. 查询表数据
        for(ProduceBatch batch : batches){
            produceBatchIds.add(batch.getId());
        }
        List<ProduceBatchProgress> progresses = produceBatchProgressService.listByBatchIds(produceBatchIds);

        // 2. 添加到历史表（订单号,批次号加年份）
        List<HisProduceBatchProgress> hisLists = new ArrayList<>();

        for(ProduceBatchProgress progress : progresses){
            HisProduceBatchProgress hisProgress = new HisProduceBatchProgress();
            BeanUtils.copyProperties(progress,hisProgress);
            hisProgress.setId(progress.getId());
            hisLists.add(hisProgress);
            removeIds.add(progress.getId());
        }
        hisProduceBatchProgressService.saveBatch(hisLists);
        // 2. 删除原表
        produceBatchProgressService.removeByIds(removeIds);
    }

    private void addBatchDelayHisAndRemove(List<ProduceBatch> batches) {

        List<Long> produceBatchIds = new ArrayList<>();
        List<Long> removeIds = new ArrayList<>();

        // 1. 查询表数据
        for(ProduceBatch batch : batches){
            produceBatchIds.add(batch.getId());
        }
        List<ProduceBatchDelay> delays = produceBatchDelayService.listByBatchIds(produceBatchIds);

        // 2. 添加到历史表（订单号,批次号加年份）
        List<HisProduceBatchDelay> hisLists = new ArrayList<>();

        for(ProduceBatchDelay delay : delays){
            HisProduceBatchDelay hisDelay = new HisProduceBatchDelay();
            BeanUtils.copyProperties(delay,hisDelay);
            hisDelay.setId(delay.getId());
            hisLists.add(hisDelay);
            removeIds.add(delay.getId());
        }
        hisProduceBatchDelayService.saveBatch(hisLists);
        // 2. 删除原表
        produceBatchDelayService.removeByIds(removeIds);
    }

    private void updatePickReturnBatchId(List<ProduceBatch> batches, String year) {
        ArrayList<String> batchIds = new ArrayList<>();
        // 查询生产序号的领料表和退料表
        for(ProduceBatch pb : batches){
            batchIds.add(pb.getBatchId());
        }
        // 修改对应的领料表和退料表
        repositoryPickMaterialService.updateBatchIdAppendYearById(year,batchIds);
        repositoryReturnMaterialService.updateBatchIdAppendYearById(year,batchIds);
        log.info("【定时任务】修改领料表表，退料表，batchIds:{},添加年份:{}",batchIds,year);

    }


    private void addBatchHisAndRemove(List<ProduceBatch> batches, String year) {

        // 1. 添加到历史表（订单号,批次号加年份）
        ArrayList<HisProduceBatch> hisLists = new ArrayList<>();
        ArrayList<Long> removeIds = new ArrayList<>();

        for(ProduceBatch batch : batches){
            HisProduceBatch hisBatch = new HisProduceBatch();
            BeanUtils.copyProperties(batch,hisBatch);
            hisBatch.setId(batch.getId());
            hisBatch.setOrderNum(year+hisBatch.getOrderNum());
            hisBatch.setBatchId(year+hisBatch.getBatchId());

            hisLists.add(hisBatch);
            removeIds.add(batch.getId());
        }
        hisProduceBatchService.saveBatch(hisLists);
        // 2. 删除原表
        produceBatchService.removeByIds(removeIds);
    }
}