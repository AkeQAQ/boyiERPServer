package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceProductConstituent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.RepositoryReturnMaterial;
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
 * @since 2022-03-19
 */
@Repository
public interface ProduceProductConstituentMapper extends BaseMapper<ProduceProductConstituent> {


    String querySql = "" +
            "select  * from produce_product_constituent";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceProductConstituent> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ProduceProductConstituent> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ProduceProductConstituent one(@Param("ew") Wrapper queryWrapper);

    String query2Sql = "" +
            " select  ppc.*,bm.name material_name from produce_product_constituent ppc,produce_product_constituent_detail ppcd,base_material bm" +
            " where ppc.id = ppcd.constituent_id and ppcd.material_id = bm.id";
    String wrapper2Sql = "SELECT * from ( " + query2Sql + " ) AS q ${ew.customSqlSegment}";

    @Select(wrapper2Sql)
    Page<ProduceProductConstituent> page2(Page page, @Param("ew") Wrapper queryWrapper);

}
