package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterialSameGroup;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.mapper.BaseMaterialSameGroupMapper;
import com.boyi.service.BaseMaterialSameGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-11-14
 */
@Service
public class BaseMaterialSameGroupServiceImpl extends ServiceImpl<BaseMaterialSameGroupMapper, BaseMaterialSameGroup> implements BaseMaterialSameGroupService {

    @Autowired
    public BaseMaterialSameGroupMapper baseMaterialSameGroupMapper;

    @Override
    public Page<BaseMaterialSameGroup> queryByManySearch(Page page, String searchField, String queryField, String searchStr, Map<String, String> otherSearch) {
        QueryWrapper<BaseMaterialSameGroup> queryWrapper = new QueryWrapper<>();

        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return baseMaterialSameGroupMapper.page(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .orderByDesc(DBConstant.TABLE_BASE_MATERIAL_SAME_GROUP.CREATED_FIELDNAME)

        );
    }
}
