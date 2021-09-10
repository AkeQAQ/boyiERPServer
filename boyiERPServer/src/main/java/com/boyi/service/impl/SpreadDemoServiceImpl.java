package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.SpreadDemo;
import com.boyi.mapper.BaseDepartmentMapper;
import com.boyi.mapper.SpreadDemoMapper;
import com.boyi.service.BaseDepartmentService;
import com.boyi.service.SpreadDemoService;
import org.springframework.stereotype.Service;

@Service
public class SpreadDemoServiceImpl extends ServiceImpl<SpreadDemoMapper, SpreadDemo> implements SpreadDemoService {
    @Override
    public SpreadDemo getByType(Integer type) {
        return this.getOne(new QueryWrapper<SpreadDemo>().eq(DBConstant.TABLE_SPREAD_DEMO.TYPE_FIELDNAME,type));
    }
}
