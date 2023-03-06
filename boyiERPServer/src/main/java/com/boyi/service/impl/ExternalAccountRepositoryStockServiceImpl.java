package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.entity.ExternalAccountRepositoryStock;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.entity.ExternalAccountRepositoryStock;
import com.boyi.mapper.ExternalAccountRepositoryStockMapper;
import com.boyi.mapper.ExternalAccountRepositoryStockMapper;
import com.boyi.service.ExternalAccountRepositoryStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
@Slf4j
public class ExternalAccountRepositoryStockServiceImpl extends ServiceImpl<ExternalAccountRepositoryStockMapper, ExternalAccountRepositoryStock> implements ExternalAccountRepositoryStockService {

    @Autowired
    ExternalAccountRepositoryStockMapper repositoryStockMapper;

    /**
     *  采购入库修改时调用
     * @param needAddMap
     */
    @Override
    public void addNumByMaterialIdFromMap(Map<String, Double> needAddMap) {
        for(Map.Entry<String,Double> entry : needAddMap.entrySet()){
            String materialId = entry.getKey();
            Double num = entry.getValue();

            ExternalAccountRepositoryStock stock = this.getByMaterialId(materialId);
            if(stock == null){
                stock = new ExternalAccountRepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(num);
                stock.setUpdated(LocalDateTime.now());
                log.info("【库存缩减日志】物料:{},新增数量:{}", materialId,num);
                this.save(stock);
            }else{
                // 因为 num数据库类型是double类型，精度会丢失，所以目前方案：查出数据库数据，进行java计算，然后更新进去
                ExternalAccountRepositoryStock queryStock = this.getByMaterialId(materialId);
                Double dbNum = queryStock.getNum() ;
                dbNum = dbNum == null ? 0D : dbNum;
                double finalNum = BigDecimalUtil.add(dbNum, num).doubleValue();
                UpdateWrapper<ExternalAccountRepositoryStock> updateWrapper = new UpdateWrapper<>();
                log.info("【库存缩减日志】物料:{},db数量:{},新增数量:{},设置数量:{}", materialId,dbNum,num,finalNum);

                updateWrapper
                        .eq(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                        .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.NUM_FIELDNAME,finalNum)
//                        .setSql(" num = num +"+num)
                        .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
                this.update(updateWrapper);
            }

        }
    }

    /**
     *  采购订单下推入库调用方法
     *  采购入库save方法调用
     * @param materialId
     * @param num
     */
    @Override
    public void addNumByMaterialId(String materialId, Double num) {

        ExternalAccountRepositoryStock stock = this.getByMaterialId(materialId);
        if(stock == null){
            stock = new ExternalAccountRepositoryStock();
            stock.setMaterialId(materialId);
            stock.setNum(num);
            log.info("【库存缩减日志】物料:{},新增数量:{}", materialId,num);
            stock.setUpdated(LocalDateTime.now());
            this.save(stock);
        }else{
            // 因为 num数据库类型是double类型，精度会丢失，所以目前方案：查出数据库数据，进行java计算，然后更新进去
            ExternalAccountRepositoryStock queryStock = this.getByMaterialId(materialId);
            Double dbNum = queryStock == null ? 0D: queryStock.getNum() ;
            dbNum = dbNum == null ? 0D : dbNum;
            double finalNum = BigDecimalUtil.add(dbNum, num).doubleValue();
            log.info("【库存缩减日志】物料:{},db数量:{},新增数量:{},设置数量:{}", materialId,dbNum,num,finalNum);

            UpdateWrapper<ExternalAccountRepositoryStock> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                    .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.NUM_FIELDNAME,finalNum)

//                    .setSql(" num = num +"+num)
                    .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
            this.update(updateWrapper);
        }
    }

    /**
     *  采购入库-采购订单来源时，被删除时调用
     * @param materialId
     * @param num
     */
    @Override
    public void subNumByMaterialIdNum(String materialId, Double num) {
        // 因为 num数据库类型是double类型，精度会丢失，所以目前方案：查出数据库数据，进行java计算，然后更新进去
        ExternalAccountRepositoryStock queryStock = this.getByMaterialId(materialId);
        Double dbNum = queryStock.getNum() ;
        dbNum = dbNum == null ? 0D : dbNum;
        double finalNum = BigDecimalUtil.sub(dbNum, num).doubleValue();
        log.info("【库存缩减日志】物料:{},db数量:{},减少数量:{},设置数量:{}", materialId,dbNum,num,finalNum);

        UpdateWrapper<ExternalAccountRepositoryStock> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.NUM_FIELDNAME,finalNum)
//                .setSql(" num = num -"+num)
                .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
        this.update(updateWrapper);

    }

    /**
     *  采购入库，修改时调用
     *  采购入库，删除时调用
     * @param needSubMap
     * @throws Exception
     */
    @Override
    public void subNumByMaterialId(Map<String, Double> needSubMap)throws Exception {
        for(Map.Entry<String,Double> entry : needSubMap.entrySet()){
            String materialId = entry.getKey();
            Double num = entry.getValue();

            // 因为 num数据库类型是double类型，精度会丢失，所以目前方案：查出数据库数据，进行java计算，然后更新进去
            ExternalAccountRepositoryStock queryStock = this.getByMaterialId(materialId);
            Double dbNum = queryStock.getNum() ;
            dbNum = dbNum == null ? 0D : dbNum;
            double finalNum = BigDecimalUtil.sub(dbNum, num).doubleValue();
            log.info("【库存缩减日志】物料:{},db数量:{},减少数量:{},设置数量:{}", materialId,dbNum,num,finalNum);

            UpdateWrapper<ExternalAccountRepositoryStock> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                    .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.NUM_FIELDNAME,finalNum)

//                    .setSql(" num = num -"+num)
                    .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
            this.update(updateWrapper);
        }
    }



    @Override
    public ExternalAccountRepositoryStock getByMaterialId( String materialId) {
        return this.getOne(new QueryWrapper<ExternalAccountRepositoryStock>()
                .eq(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME, materialId));
    }

    @Override
    public void removeByMaterialId(String[] ids) {
        this.remove(new QueryWrapper<ExternalAccountRepositoryStock>()
                .in(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,ids));
    }

    @Override
    public Page<ExternalAccountRepositoryStock> pageBySearch(Page page, String queryField, String searchField, String searchStr) {
        return repositoryStockMapper.page(page
                ,new QueryWrapper<ExternalAccountRepositoryStock>()
                        .like(StrUtil.isNotBlank(searchStr) && StrUtil.isNotBlank(searchField),queryField,searchStr));
    }

    @Override
    public void validStockNum(Map<String, Double> needSubMap)throws Exception {
        for(Map.Entry<String,Double> entry : needSubMap.entrySet()){
            String materialId = entry.getKey();
            ExternalAccountRepositoryStock stock = this.getByMaterialId(materialId);
            if(stock == null){
                throw new Exception("该物料库存："+materialId+"不存在，不能减库存!");
            }
            log.info("校验库存。物料{},待减少数目[{}],库存数目:[{}]",materialId,entry.getValue(),stock.getNum());
            if(stock.getNum() < entry.getValue()){
                throw new Exception("该物料："+materialId+",库存数量:"+stock.getNum()+"小于要减少的数量:"+entry.getValue()+"不能减库存!");
            }
        }
    }

    @Override
    public void validStockNumWithErrorMsg(Map<String, Double> needSubMap, List<Map<String, String>> strList)  {
        for(Map.Entry<String,Double> entry : needSubMap.entrySet()){
            String materialId = entry.getKey();
            ExternalAccountRepositoryStock stock = this.getByMaterialId(materialId);
            if(stock == null){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","该物料库存："+materialId+"不存在，不能减库存!");
                strList.add(map);
            }
            if(stock.getNum() < entry.getValue()){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","该物料："+materialId+",库存数量:"+stock.getNum()+"小于要减少的数量:"+entry.getValue()+"不能减库存!");
                strList.add(map);
            }
        }
    }

    @Override
    public List<ExternalAccountRepositoryStock> listByMaterialIds(List<String> ids) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryStock>()
                .in(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public void updateNum(String materialId, Double checkNum) {
        UpdateWrapper<ExternalAccountRepositoryStock> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq(DBConstant.TABLE_EA_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.NUM_FIELDNAME,checkNum)
                .set(DBConstant.TABLE_EA_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    public List<ExternalAccountRepositoryStock> listStockNumLTZero() {
        return this.list(new QueryWrapper<ExternalAccountRepositoryStock>().lt(DBConstant.TABLE_EA_REPOSITORY_STOCK.NUM_FIELDNAME,0));
    }
}
