package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseDepartment;
import com.boyi.mapper.BaseDepartmentMapper;
import com.boyi.service.BaseDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 基础模块-部门管理 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-22
 */
@Service
public class BaseDepartmentServiceImpl extends ServiceImpl<BaseDepartmentMapper, BaseDepartment> implements BaseDepartmentService {

    @Override
    public Page<BaseDepartment> pageBySearch(Page page, String searchName) {
        return this.page(page, new QueryWrapper<BaseDepartment>()
                .like(StrUtil.isNotBlank(searchName), DBConstant.TABLE_BASE_DEPARTMENT.NAME_FIELDNAME, searchName));
    }
}
