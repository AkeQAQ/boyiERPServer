package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.entity.RepositoryStock;
import com.boyi.entity.RepositoryStockHistory;
import com.boyi.mapper.RepositoryStockHistoryMapper;
import com.boyi.mapper.RepositoryStockMapper;
import com.boyi.service.RepositoryStockHistoryService;
import com.boyi.service.RepositoryStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存历史表 服务实现类
 * </p>
 *
 * @author sunke
 */
@Service
public class RepositoryStockHistoryServiceImpl extends ServiceImpl<RepositoryStockHistoryMapper, RepositoryStockHistory> implements RepositoryStockHistoryService {

}
