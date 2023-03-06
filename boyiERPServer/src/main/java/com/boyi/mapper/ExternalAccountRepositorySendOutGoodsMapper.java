package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountRepositorySendOutGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.ExternalAccountRepositorySendOutGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-03-05
 */
@Repository
public interface ExternalAccountRepositorySendOutGoodsMapper extends BaseMapper<ExternalAccountRepositorySendOutGoods> {

    String querySql = "select fsc.*,fscd.send_id,fscd.product_num,fscd.num,fscd.price,fscd.amount,fscd.product_name,fscd.unit" +
            "             from external_account_repository_send_out_goods fsc,external_account_repository_send_out_goods_details fscd" +
            "             where fsc.id = fscd.send_id" +
            "            ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ExternalAccountRepositorySendOutGoods> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ExternalAccountRepositorySendOutGoods> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ExternalAccountRepositorySendOutGoods one(@Param("ew") Wrapper queryWrapper);
}
