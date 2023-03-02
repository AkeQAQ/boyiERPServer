package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.vo.RealDosageVO;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.entity.RepositoryReturnMaterial;
import com.boyi.mapper.ProduceProductConstituentMapper;
import com.boyi.service.ProduceProductConstituentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
@Service
public class ProduceProductConstituentServiceImpl extends ServiceImpl<ProduceProductConstituentMapper, ProduceProductConstituent> implements ProduceProductConstituentService {

    @Autowired
    ProduceProductConstituentMapper produceProductConstituentMapper;

    @Override
    public Page<ProduceProductConstituent> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDNAME,searchStatus)
                        .orderByDesc(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.CREATED_FIELDNAME)

        );
    }

    @Override
    public Page<ProduceProductConstituent> innerQuery(Page page, QueryWrapper<ProduceProductConstituent> eq) {
        return produceProductConstituentMapper.page(page,eq);
    }

    @Override
    public ProduceProductConstituent getValidByNumBrand(String productNum, String productBrand) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_NUM_FIELDNAME,productNum)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_BRAND_FIELDNAME,productBrand)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDNAME,
                        DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_0)
        ;

        return this.getOne(queryWrapper);
    }

    @Override
    public Page<ProduceProductConstituent> innerQueryByManySearchWithDetailField(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return produceProductConstituentMapper.page2(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDNAME,searchStatus)
                        .orderByDesc(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.CREATED_FIELDNAME)

        );
    }

    @Override
    public List<RealDosageVO> listRealDosageById(Long id) {
        return produceProductConstituentMapper.listRealDosageById(id);
    }

    @Override
    public List<RealDosageVO> listRealDosage() {
        return produceProductConstituentMapper.listRealDosage();
    }

    @Override
    public List<RealDosageVO> listRealDosageBetweenDate(String searchStartDate, String searchEndDate) {
        return produceProductConstituentMapper.listRealDosageBetweenDate(searchStartDate,searchEndDate);
    }

    @Override
    public int countProductNum(String productNum) {
        return this.count(new QueryWrapper<ProduceProductConstituent>()
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_NUM_FIELDNAME,productNum));
    }

    @Override
    public Long countPickMaterialRows(String productNum, String productBrand, String materialId, LocalDate localDate) {
        return produceProductConstituentMapper.countPickMaterialRows(productNum,productBrand,materialId,localDate);
    }

    @Override
    public List<ProduceProductConstituent> listDistinctProductNum() {
        return produceProductConstituentMapper.listDistinctProductNum();
    }

    @Override
    public void updateNullWithField(ProduceProductConstituent ppc, String videoUrlFieldname) {
        this.update(new UpdateWrapper<ProduceProductConstituent>()
                .set(videoUrlFieldname,null)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.ID_FIELDNAME,ppc.getId()));
    }

    @Override
    public ProduceProductConstituent getByNumBrand(String productNum, String productBrand) {
        QueryWrapper<ProduceProductConstituent> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_NUM_FIELDNAME,productNum)
                .eq(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PRODUCT_BRAND_FIELDNAME,productBrand);

        return this.getOne(queryWrapper);
    }
}
