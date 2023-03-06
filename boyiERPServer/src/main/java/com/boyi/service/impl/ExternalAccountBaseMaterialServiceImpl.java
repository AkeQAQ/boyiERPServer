package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountBaseMaterial;
import com.boyi.entity.ExternalAccountBaseMaterial;
import com.boyi.mapper.ExternalAccountBaseMaterialMapper;
import com.boyi.service.ExternalAccountBaseMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountBaseMaterialServiceImpl extends ServiceImpl<ExternalAccountBaseMaterialMapper, ExternalAccountBaseMaterial> implements ExternalAccountBaseMaterialService {

    @Override
    public Integer countByGroupCode(String groupCode){
        return this.count(new QueryWrapper<ExternalAccountBaseMaterial>().eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME, groupCode));
    }

    @Override
    public Page<ExternalAccountBaseMaterial> pageByGroupCode(Page page, String searchStr) {
        return this.page(page,new QueryWrapper<ExternalAccountBaseMaterial>().eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,searchStr));
    }
    @Override
    public Page<ExternalAccountBaseMaterial> pageBySearch(Page page,String queryField, String searchStr) {
        return this.page(page, new QueryWrapper<ExternalAccountBaseMaterial>()
                .like(StrUtil.isNotBlank(searchStr), queryField, searchStr)
                .orderByDesc(DBConstant.TABLE_BASE_MATERIAL.CREATED_FIELDNAME));
    }

    @Override
    public List<ExternalAccountBaseMaterial> listSame(String name, String unit, String specs, String groupCode) {
        return this.list(new QueryWrapper<ExternalAccountBaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME, unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.SPECS_FIELDNAME, specs)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode));
    }

    @Override
    public List<ExternalAccountBaseMaterial> listSameExcludSelf(String name, String unit, String specs, String groupCode, String id) {
        return this.list(new QueryWrapper<ExternalAccountBaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME,unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.SPECS_FIELDNAME, specs)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode)
                .ne(DBConstant.TABLE_BASE_MATERIAL.ID,id));
    }

    @Override
    public List<ExternalAccountBaseMaterial> getLowWarningLines() {
        return this.list(new QueryWrapper<ExternalAccountBaseMaterial>()
                .isNotNull(DBConstant.TABLE_BASE_MATERIAL.LOW_WARNING_LINE_FIELDNAME));
    }

    @Override
    public void updateNull(ExternalAccountBaseMaterial baseMaterial) {
        this.update(new UpdateWrapper<ExternalAccountBaseMaterial>()
                .set(DBConstant.TABLE_BASE_MATERIAL.LOW_WARNING_LINE_FIELDNAME,null)
                .eq(DBConstant.TABLE_BASE_MATERIAL.ID,baseMaterial.getId()));
    }

    @Override
    public List<ExternalAccountBaseMaterial> listSame(String name, String unit, String groupCode) {
        return this.list(new QueryWrapper<ExternalAccountBaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME, unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode));
    }

    @Override
    public List<ExternalAccountBaseMaterial> listSameExcludSelf(String name, String unit, String groupCode, String id) {
        return this.list(new QueryWrapper<ExternalAccountBaseMaterial>()
                .eq(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, name)
                .eq(DBConstant.TABLE_BASE_MATERIAL.UNIT_FIELDNAME,unit)
                .eq(DBConstant.TABLE_BASE_MATERIAL.GROUP_CODE_FIELDNAME,groupCode)
                .ne(DBConstant.TABLE_BASE_MATERIAL.ID,id));
    }

    @Override
    public void updateNullWithField(ExternalAccountBaseMaterial baseMaterial ,String videoUrlFieldname) {
        this.update(new UpdateWrapper<ExternalAccountBaseMaterial>()
                .set(videoUrlFieldname,null)
                .eq(DBConstant.TABLE_BASE_MATERIAL.ID,baseMaterial.getId()));
    }
}
