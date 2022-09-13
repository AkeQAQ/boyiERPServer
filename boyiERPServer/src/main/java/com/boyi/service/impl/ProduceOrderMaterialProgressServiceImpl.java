package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.mapper.ProduceOrderMaterialProgressComplementMapper;
import com.boyi.mapper.ProduceOrderMaterialProgressMapper;
import com.boyi.service.ProduceOrderMaterialProgressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-03-26
 */
@Service
@Slf4j
public class ProduceOrderMaterialProgressServiceImpl extends ServiceImpl<ProduceOrderMaterialProgressMapper, ProduceOrderMaterialProgress> implements ProduceOrderMaterialProgressService {

    @Autowired
    ProduceOrderMaterialProgressMapper produceOrderMaterialProgressMapper;

    @Autowired
    ProduceOrderMaterialProgressComplementMapper produceOrderMaterialProgressComplementMapper;


    @Override
    public Page<ProduceOrderMaterialProgress> complementInnerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> otherSearch) {
        QueryWrapper<ProduceOrderMaterialProgress> queryWrapper = new QueryWrapper<>();

        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return produceOrderMaterialProgressComplementMapper.page(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .isNull(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME)
                        .orderByDesc(DBConstant.TABLE_ORDER_PRODUCT_ORDER.CREATED_FIELDNAME)

        );
    }

    @Override
    public List<ProduceOrderMaterialProgress> listByOrderId(Long orderId) {
        return this.list(new QueryWrapper<ProduceOrderMaterialProgress>()
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME,orderId));
    }

    @Override
    public List<ProduceOrderMaterialProgress> listByOrderIds(Set<Long> orderIds) {
        return this.list(new QueryWrapper<ProduceOrderMaterialProgress>()
                .in(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME,orderIds));
    }

    @Override
    public ProduceOrderMaterialProgress getByOrderIdAndMaterialId(Long orderId, String materialId) {
        return this.getOne(new QueryWrapper<ProduceOrderMaterialProgress>()
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME,orderId)
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME,materialId));
    }

    @Override
    public boolean isPreparedByOrderId(Long orderId) {
         return this.count(new QueryWrapper<ProduceOrderMaterialProgress>()
                         .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME,orderId)
                .lt(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PROGRESS_PERCENT_NUM_FIELDNAME,100)) == 0;
    }

    @Override
    public Page<ProduceOrderMaterialProgress> innerQuery(Page page, QueryWrapper<ProduceOrderMaterialProgress> eq) {
        return produceOrderMaterialProgressMapper.page(page,eq);
    }

    @Override
    public Page<ProduceOrderMaterialProgress> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, List<Long> searchStatus2, Map<String, String> otherSearch,String searchNoPropread) {
        QueryWrapper<ProduceOrderMaterialProgress> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDNAME,searchStatus)
                        .in(searchStatus2 != null && searchStatus2.size() > 0, DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDNAME,searchStatus2)
                        .gt(!Boolean.valueOf(searchNoPropread),DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME,0)
                        .orderByDesc(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME)

        );
    }

    @Override
    public List<ProduceOrderMaterialProgress> listByMaterialIdCreatedAscNotOver(String materialId) {
        return this.produceOrderMaterialProgressMapper.listByMaterialIdCreatedAscNotOver(materialId);
    }

    @Override
    public void updateInNum(Long id, String afterNum) {
        UpdateWrapper<ProduceOrderMaterialProgress> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.IN_NUM_FIELDNAME,afterNum)
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ID_FIELDNAME,id);
        this.update(updateWrapper);
    }

    @Override
    public void addInNum(Double xiaodanNum, String materialId) {
        Double logNum=xiaodanNum;
        List<ProduceOrderMaterialProgress> xiaodanList = this.listByMaterialIdCreatedAscNotOver(materialId);
        StringBuilder sb = new StringBuilder();
        for (ProduceOrderMaterialProgress progress : xiaodanList){
            if(xiaodanNum ==0.0D){
                break ;
            }
            sb.append(progress.getId()).append(",");
            // 该订单，需要的物料数目
            double needNum = BigDecimalUtil.sub(progress.getPreparedNum(),progress.getInNum()).doubleValue();
            if(xiaodanNum <= needNum){
                // 入库的比需要的少，直接该订单上全部消单
                String afterNum = BigDecimalUtil.add(progress.getInNum(),xiaodanNum+"").toString() ;
                log.info("【进度表】【入库】物料:{},总入库数目:{},循环当前剩余数量:{},该进度ID：{}，入库添加:{},老数目:{},更改后数目:{}"
                        ,materialId,logNum,xiaodanNum,progress.getId(),xiaodanNum,progress.getInNum(),afterNum);
                this.updateInNum(progress.getId(),afterNum);
                xiaodanNum= 0.0d;

                break  ;
            }else{
                // 入库的比需要的多，那就把当前订单的全部消单，然后剩下的给下一个
                // 入库的比需要的少，直接该订单上全部消单
                String afterNum = BigDecimalUtil.add(progress.getInNum(),needNum+"").toString() ;
                log.info("【进度表】【入库】物料:{},总入库数目:{},循环当前剩余数量:{},该进度ID：{}，入库添加:{},老数目:{},更改后数目:{}"
                        ,materialId,logNum,xiaodanNum,progress.getId(),needNum,progress.getInNum(),afterNum);

                this.updateInNum(progress.getId(),afterNum);
                xiaodanNum = BigDecimalUtil.sub(xiaodanNum,needNum).doubleValue();
                continue ;
            }
        }
        // 假如入库有剩余的加到最后一条入库里，不然会出现删除订单会删多的情况
        if(xiaodanNum > 0){
            log.warn("【入库进度表，最终物料数目扔有剩余】.物料{}入库数目:{},最终剩余数目:{}.消单的进度表ID集合:{}",materialId,logNum,xiaodanNum,sb.toString());
            ProduceOrderMaterialProgress theLatest = this.produceOrderMaterialProgressMapper.getByTheLatestByMaterialIdCreatedDesc(materialId);
            if(theLatest==null){
                log.warn("【入库进度表，最终物料数目扔有剩余,并且物料没有进度表进度信息】.物料{}入库数目:{},最终剩余数目:{}.",materialId,logNum,xiaodanNum);
            }else{
                // 补充到最近一个进度表的入库数量里。
                this.updateInNum(theLatest.getId(),BigDecimalUtil.add(theLatest.getInNum(),xiaodanNum+"").toString());
            }

        }
    }

    @Override
    public void subInNum(Double subNum, String materialId) {
        Double logNum=subNum;
        List<ProduceOrderMaterialProgress> subList = this.listByMaterialIdCreatedDescHasInNum(materialId);

        StringBuilder sb = new StringBuilder();
        for (ProduceOrderMaterialProgress progress : subList){
            if(subNum ==0.0D){
                break ;
            }
            sb.append(progress.getId()).append(",");
            // 该订单，需要的物料数目
            double needNum = Double.valueOf(progress.getInNum());
            if(subNum <= needNum){
                // 出库的比需要的少，直接该订单上全部出库(老的入库-现在的出库数目）
                String afterNum = BigDecimalUtil.sub(progress.getInNum(),subNum+"").toString();
                log.info("【进度表】【入库】物料:{},总出库数目:{},循环当前剩余数量:{},该进度ID：{}，老数目:{},出库减少:{},更改后数目:{}"
                        ,materialId,logNum,subNum,progress.getId(),progress.getInNum(),subNum,afterNum);

                this.updateInNum(progress.getId(),afterNum);
                subNum=0.0d;
                break  ;
            }else{
                log.info("【进度表】【入库】物料:{},总出库数目:{},循环当前剩余数量:{},该进度ID：{}，老数目:{},出库减少:{},更改后数目:{}"
                        ,materialId,logNum,subNum,progress.getId(),progress.getInNum(),needNum,0);
                // 出库的比需要的多，那就把当前订单的全部消单，然后剩下的给下一个
                // 出库的比需要的少，直接该订单上全部消单
                this.updateInNum(progress.getId(),"0");
                subNum = BigDecimalUtil.sub(subNum,needNum).doubleValue();
                continue ;
            }
        }

        if(subNum > 0){
            log.warn("【入库进度表，最终物料数目扔有剩余】.物料{}出库数目:{},最终剩余数目:{}.消单的进度表ID集合:{}",materialId,logNum,subNum,sb.toString());
        }
    }

    @Override
    public List<ProduceOrderMaterialProgress> listByMaterialIdCreatedDescHasInNum(String materialId) {
        return this.produceOrderMaterialProgressMapper.listByMaterialIdCreatedDescHasInNum(materialId);
    }

    @Override
    public List<ProduceOrderMaterialProgress> listByOrderIdsAndMaterialId(Long[] orderIds, String materialId) {

        return this.list(new QueryWrapper<ProduceOrderMaterialProgress>()
                .in(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME,orderIds)
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME,materialId)
                .orderByAsc(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME));
    }

    @Override
    public void updateStatus(Long id, Integer complementStatusFieldvalue0) {
        this.update(new UpdateWrapper<ProduceOrderMaterialProgress>()
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ID_FIELDNAME,id)
                .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDNAME,complementStatusFieldvalue0));
    }

    @Override
    public int countByMaterialId(String materialId) {
        return this.count(new QueryWrapper<ProduceOrderMaterialProgress>()
                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME,materialId));
    }

    @Override
    public int countByMaterialIdAndPreparedNumGtInNum(String materialId) {
        return produceOrderMaterialProgressMapper.countByMaterialIdAndPreparedNumGtInNum(materialId);
    }

    @Override
    public List<OrderProductCalVO> listNoInNums() {
        return produceOrderMaterialProgressMapper.listNoInNums();
    }

    @Override
    public List<ProduceOrderMaterialProgress> groupByMaterialIds() {
        return produceOrderMaterialProgressMapper.groupByMaterialIds();
    }

}
