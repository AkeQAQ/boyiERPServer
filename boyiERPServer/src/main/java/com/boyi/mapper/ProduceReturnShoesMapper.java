package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceBatch;
import com.boyi.entity.ProduceReturnShoes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
@Repository
public interface ProduceReturnShoesMapper extends BaseMapper<ProduceReturnShoes> {
    String querySql =
            " select prs.*,bd.name department_name " +
                    "  from " +
                    "             produce_return_shoes prs left join base_department bd on prs.department_id = bd.id" +
                    "            order by prs.created desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";

    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceReturnShoes> page(Page page, @Param("ew") Wrapper queryWrapper);

}
