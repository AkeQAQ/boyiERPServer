package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterial;
import com.boyi.mapper.BaseMaterialMapper;
import com.boyi.service.BaseMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Service
public class BaseMaterialServiceImpl extends ServiceImpl<BaseMaterialMapper, BaseMaterial> implements BaseMaterialService {


    @Override
    public Integer countByGroupCode(String groupCode){
        return this.count(new QueryWrapper<BaseMaterial>().eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME, groupCode));
    }

    @Override
    public Page<BaseMaterial> pageByGroupCode(Page page, String searchStr) {
        return this.page(page,new QueryWrapper<BaseMaterial>().eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,searchStr));
    }
    @Override
    public Page<BaseMaterial> pageBySearch(Page page,String queryField, String searchStr) {
        return this.page(page, new QueryWrapper<BaseMaterial>()
                .like(StrUtil.isNotBlank(searchStr), queryField, searchStr));
    }

    @Override
    public List<BaseMaterial> listSame(String name, String unit, String specs, String groupCode) {
        return this.list(new QueryWrapper<BaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME, unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.SPECS_FIELDNAME, specs)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode));
    }

    @Override
    public List<BaseMaterial> listSameExcludSelf(String name, String unit, String specs, String groupCode, String id) {
        return this.list(new QueryWrapper<BaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME,unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.SPECS_FIELDNAME, specs)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode)
                .ne(DBConstant.TABLE_BASE_MATERIAL.ID,id));
    }

    @Override
    public List<BaseMaterial> getLowWarningLines() {
        return this.list(new QueryWrapper<BaseMaterial>()
                .isNotNull(DBConstant.TABLE_BASE_MATERIAL.LOW_WARNING_LINE_FIELDNAME));
    }

    @Override
    public void updateNull(BaseMaterial baseMaterial) {
        this.update(new UpdateWrapper<BaseMaterial>()
                .set(DBConstant.TABLE_BASE_MATERIAL.LOW_WARNING_LINE_FIELDNAME,null)
                .eq(DBConstant.TABLE_BASE_MATERIAL.ID,baseMaterial.getId()));
    }

    @Override
    public List<BaseMaterial> listSame(String name, String unit, String groupCode) {
        return this.list(new QueryWrapper<BaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME, unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode));
    }

    @Override
    public List<BaseMaterial> listSameExcludSelf(String name, String unit, String groupCode, String id) {
        return this.list(new QueryWrapper<BaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME,unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode)
                .ne(DBConstant.TABLE_BASE_MATERIAL.ID,id));
    }
}
