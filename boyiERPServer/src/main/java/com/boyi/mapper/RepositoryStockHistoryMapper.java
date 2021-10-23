package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryStock;
import com.boyi.entity.RepositoryStockHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 库存历史表 Mapper 接口
 * </p>
 *
 * @author sunke
 */
@Repository
public interface RepositoryStockHistoryMapper extends BaseMapper<RepositoryStockHistory> {

}
