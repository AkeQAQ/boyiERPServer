package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceProductConstituent;
import com.boyi.entity.ProduceTechnologyBom;
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
 * @since 2023-04-27
 */
@Repository

public interface ProduceTechnologyBomMapper extends BaseMapper<ProduceTechnologyBom> {

    String query2Sql = "" +
            " select  ppc.*,bm.name material_name " +
            " from produce_technology_bom ppc,produce_technology_bom_detail ppcd,base_material bm" +
            " where ppc.id = ppcd.constituent_id and ppcd.material_id = bm.id";
    String wrapper2Sql = "SELECT * from ( " + query2Sql + " ) AS q ${ew.customSqlSegment}";

    @Select(wrapper2Sql)
    Page<ProduceTechnologyBom> page2(Page page, @Param("ew") Wrapper queryWrapper);


    String querySql = "" +
            "select ppc.* " +
            " from produce_technology_bom ppc ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceTechnologyBom> page(Page page, @Param("ew") Wrapper queryWrapper);

}
