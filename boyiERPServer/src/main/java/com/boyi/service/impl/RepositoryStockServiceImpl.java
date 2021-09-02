package com.boyi.service.impl;

import com.boyi.entity.RepositoryStock;
import com.boyi.mapper.RepositoryStockMapper;
import com.boyi.service.RepositoryStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
@Service
public class RepositoryStockServiceImpl extends ServiceImpl<RepositoryStockMapper, RepositoryStock> implements RepositoryStockService {
}
