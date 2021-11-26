package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.entity.SysUser;
import com.boyi.mapper.ProduceReturnShoesMapper;
import com.boyi.service.ProduceReturnShoesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

    @Override
    public Page<ProduceReturnShoes> pageBySearch(Page page, String searchUserName) {
        return this.page(page, new QueryWrapper<ProduceReturnShoes>()
                .like(StrUtil.isNotBlank(searchUserName), DBConstant.TABLE_PRODUCE_RETURN_SHOES.USER_NAME_FIELDNAME, searchUserName));
    }
}
