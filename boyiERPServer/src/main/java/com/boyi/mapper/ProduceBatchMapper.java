package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.RepositoryBuyinDocument;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-04-29
 */
@Repository
public interface ProduceBatchMapper extends BaseMapper<ProduceBatch> {

    String querySql =
            " select pb.*,opo.product_num,opo.product_brand from " +
            " produce_batch pb," +
            " order_product_order opo " +
            " where pb.order_num = opo.order_num order by created desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";

    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceBatch> page(Page page, @Param("ew") Wrapper queryWrapper);
}
