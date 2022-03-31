package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceOrderMaterialProgress;
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
 * @since 2022-03-26
 */
@Repository
public interface ProduceOrderMaterialProgressComplementMapper extends BaseMapper<ProduceOrderMaterialProgress> {

    String querySql = " select pomp.*,bm.name material_name from produce_order_material_progress pomp,base_material bm  where order_id is null and pomp.material_id=bm.id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";


    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceOrderMaterialProgress> page(Page page, @Param("ew") Wrapper queryWrapper);


    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ProduceOrderMaterialProgress> list(@Param("ew") Wrapper queryWrapper);


    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ProduceOrderMaterialProgress one(@Param("ew") Wrapper queryWrapper);
}
