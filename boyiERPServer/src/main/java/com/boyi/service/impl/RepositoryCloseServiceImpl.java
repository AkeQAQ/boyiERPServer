package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.RepositoryClose;
import com.boyi.mapper.BaseDepartmentMapper;
import com.boyi.mapper.RepositoryCloseMapper;
import com.boyi.service.BaseDepartmentService;
import com.boyi.service.RepositoryCloseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryCloseServiceImpl extends ServiceImpl<RepositoryCloseMapper, RepositoryClose> implements RepositoryCloseService {
    @Override
    public RepositoryClose listLatestOne() {
        List<RepositoryClose> lists = this.list(new QueryWrapper<RepositoryClose>().orderByDesc(DBConstant.TABLE_REPOSITORY_CLOSE.CLOSE_DATE_FIELDNAME))
                ;
        if(lists != null  && lists.size() != 0){
            return lists.get(0);
        }
        return null;
    }
}
