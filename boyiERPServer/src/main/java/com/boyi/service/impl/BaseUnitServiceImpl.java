package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseUnit;
import com.boyi.entity.SysMenu;
import com.boyi.mapper.BaseUnitMapper;
import com.boyi.service.BaseUnitService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 基础模块-计量单位管理 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-19
 */
@Service
public class BaseUnitServiceImpl extends ServiceImpl<BaseUnitMapper, BaseUnit> implements BaseUnitService {

}
