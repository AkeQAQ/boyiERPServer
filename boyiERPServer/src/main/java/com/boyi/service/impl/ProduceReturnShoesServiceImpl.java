package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceBatch;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.mapper.ProduceBatchMapper;
import com.boyi.mapper.ProduceReturnShoesMapper;
import com.boyi.service.ProduceReturnShoesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
@Service
public class ProduceReturnShoesServiceImpl extends ServiceImpl<ProduceReturnShoesMapper, ProduceReturnShoes> implements ProduceReturnShoesService {

    @Autowired
    private ProduceReturnShoesMapper produceReturnShoesMapper;

    public Page<ProduceReturnShoes> innerQuery(Page page, QueryWrapper<ProduceReturnShoes> eq) {
        return produceReturnShoesMapper.page(page,eq);
    }

    @Override
    public Page<ProduceReturnShoes> pageBySearch(Page page, String searchUserName) {
        return this.page(page, new QueryWrapper<ProduceReturnShoes>()
                .like(StrUtil.isNotBlank(searchUserName), DBConstant.TABLE_PRODUCE_RETURN_SHOES.USER_NAME_FIELDNAME, searchUserName));
    }

    @Override
    public Page<ProduceReturnShoes> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<String> searchStatusList, Map<String, String> otherSearch) {
        QueryWrapper<ProduceReturnShoes> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper
                        .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_PRODUCE_RETURN_SHOES.RETURN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_PRODUCE_RETURN_SHOES.RETURN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatusList != null && searchStatusList.size() > 0,DBConstant.TABLE_PRODUCE_RETURN_SHOES.DEAL_SITUATION_FIELDNAME,searchStatusList)
                        .orderByDesc(DBConstant.TABLE_PRODUCE_RETURN_SHOES.CREATED_FIELDNAME)
        );
    }

    @Override
    public List<ProduceReturnShoes> listByGroupUserNameAndTypeBetweenDate(String searchStartDate, String searchEndDate) {
        return this.produceReturnShoesMapper.listByGroupUserNameAndTypeBetweenDate(searchStartDate,searchEndDate);
    }

    @Override
    public List<ProduceReturnShoes> listByGroupDepartmentAndTypeBetweenDate(String searchStartDate, String searchEndDate) {
        return this.produceReturnShoesMapper.listByGroupDepartmentAndTypeBetweenDate(searchStartDate,searchEndDate);
    }
}
