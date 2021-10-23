package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.*;
import com.boyi.mapper.RepositoryStockMapper;
import com.boyi.service.RepositoryStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * @since 2021-09-02
 */
@Service
public class RepositoryStockServiceImpl extends ServiceImpl<RepositoryStockMapper, RepositoryStock> implements RepositoryStockService {

    @Autowired
    RepositoryStockMapper repositoryStockMapper;

    @Override
    public void addNumByMaterialIdFromMap(Map<String, Double> needAddMap) {
        for(Map.Entry<String,Double> entry : needAddMap.entrySet()){
            String materialId = entry.getKey();
            Double num = entry.getValue();

            RepositoryStock stock = this.getByMaterialId(materialId);
            if(stock == null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(num);
                stock.setUpdated(LocalDateTime.now());
                this.save(stock);
            }else{
                UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
                updateWrapper
                        .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                        .setSql(" num = num +"+num)
                        .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
                this.update(updateWrapper);
            }

        }
    }

    @Override
    public void addNumByMaterialId(String materialId, Double num) {

        RepositoryStock stock = this.getByMaterialId(materialId);
        if(stock == null){
            stock = new RepositoryStock();
            stock.setMaterialId(materialId);
            stock.setNum(num);
            stock.setUpdated(LocalDateTime.now());
            this.save(stock);
        }else{
            UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                    .setSql(" num = num +"+num)
                    .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
            this.update(updateWrapper);
        }
    }

    @Override
    public void subNumByMaterialIdNum(String materialId, Double num) {

        UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                .setSql(" num = num -"+num)
                .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
        this.update(updateWrapper);

    }

    @Override
    public void subNumByMaterialId(Map<String, Double> needSubMap)throws Exception {
        for(Map.Entry<String,Double> entry : needSubMap.entrySet()){
            String materialId = entry.getKey();
            Double num = entry.getValue();

            UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                    .setSql(" num = num -"+num)
                    .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
            this.update(updateWrapper);
        }
    }


    @Override
    public void subNumReturnMaterialId(List<RepositoryReturnMaterialDetail> details)throws Exception {
        HashMap<String, Double> map = new HashMap<>();// 一个物料，需要减少的数目

        // 1. 遍历获取一个物料要减少的数目。
        for (RepositoryReturnMaterialDetail detail : details) {
            Double materialNum = map.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            map.put(detail.getMaterialId(),materialNum+detail.getNum());
        }


        // 2. 不够则返回
        for(Map.Entry<String,Double> entry : map.entrySet()){
            String materialId = entry.getKey();
            RepositoryStock stock = this.getByMaterialId(entry.getKey());
            if(stock == null){
                throw new Exception("，该物料库存："+materialId+"不存在，不能减库存!");
            }
            if(stock.getNum() < entry.getValue()){
                throw new Exception("该物料："+materialId+",库存数量:"+stock.getNum()+"小于退料的数量:"+entry.getValue()+"不能减库存!");
            }
        }

        // 3. 够，则减少DB
        for(Map.Entry<String,Double> entry : map.entrySet()){
            String materialId = entry.getKey();
            Double num = entry.getValue();

            UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                    .setSql(" num = num -"+num)
                    .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
            this.update(updateWrapper);
        }
    }

    @Override
    public RepositoryStock getByMaterialId( String materialId) {
        return this.getOne(new QueryWrapper<RepositoryStock>()
                .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME, materialId));
    }

    @Override
    public void removeByMaterialId(String[] ids) {
        this.remove(new QueryWrapper<RepositoryStock>()
                .in(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,ids));
    }

    @Override
    public Page<RepositoryStock> pageBySearch(Page page, String queryField, String searchField, String searchStr) {
        return repositoryStockMapper.page(page
                ,new QueryWrapper<RepositoryStock>()
                        .like(StrUtil.isNotBlank(searchStr) && StrUtil.isNotBlank(searchField),queryField,searchStr));
    }

    @Override
    public void validStockNum(Map<String, Double> needSubMap)throws Exception {
        for(Map.Entry<String,Double> entry : needSubMap.entrySet()){
            String materialId = entry.getKey();
            RepositoryStock stock = this.getByMaterialId(materialId);
            if(stock == null){
                throw new Exception("该物料库存："+materialId+"不存在，不能减库存!");
            }
            if(stock.getNum() < entry.getValue()){
                throw new Exception("该物料："+materialId+",库存数量:"+stock.getNum()+"小于要减少的数量:"+entry.getValue()+"不能减库存!");
            }
        }

    }

    @Override
    public List<RepositoryStock> listByMaterialIds(List<String> ids) {
        return this.list(new QueryWrapper<RepositoryStock>()
                .in(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public void updateNum(String materialId, Double checkNum) {
        UpdateWrapper<RepositoryStock> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq(DBConstant.TABLE_REPOSITORY_STOCK.MATERIAL_ID_FIELDNAME,materialId)
                .set(DBConstant.TABLE_REPOSITORY_STOCK.NUM_FIELDNAME,checkNum)
                .set(DBConstant.TABLE_REPOSITORY_STOCK.UPDATED_FIELDNAME,LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    public List<RepositoryStock> listStockNumLTZero() {
        return this.list(new QueryWrapper<RepositoryStock>().lt(DBConstant.TABLE_REPOSITORY_STOCK.NUM_FIELDNAME,0));
    }
}
