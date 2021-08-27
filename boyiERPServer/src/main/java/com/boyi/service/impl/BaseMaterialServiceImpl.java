package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.entity.BaseMaterial;
import com.boyi.mapper.BaseMaterialMapper;
import com.boyi.service.BaseMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
        return this.count(new QueryWrapper<BaseMaterial>().eq("group_code", groupCode));
    }
}
