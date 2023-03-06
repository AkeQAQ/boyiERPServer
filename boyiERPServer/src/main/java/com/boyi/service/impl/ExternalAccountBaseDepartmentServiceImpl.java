package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.ExternalAccountBaseDepartment;
import com.boyi.mapper.ExternalAccountBaseDepartmentMapper;
import com.boyi.service.ExternalAccountBaseDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 基础模块-部门管理 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountBaseDepartmentServiceImpl extends ServiceImpl<ExternalAccountBaseDepartmentMapper, ExternalAccountBaseDepartment> implements ExternalAccountBaseDepartmentService {

    @Override
    public Page<ExternalAccountBaseDepartment> pageBySearch(Page page, String searchName) {
        return this.page(page, new QueryWrapper<ExternalAccountBaseDepartment>()
                .like(StrUtil.isNotBlank(searchName), DBConstant.TABLE_EA_BASE_DEPARTMENT.NAME_FIELDNAME, searchName));
    }
}
