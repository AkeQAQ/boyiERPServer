package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.RepositoryBuyinDocument;
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
public interface ProduceOrderMaterialProgressMapper extends BaseMapper<ProduceOrderMaterialProgress> {

    String querySql = "" +
            "select pomg.*," +
            "o.order_number,o.status,o.prepared,o.order_num,o.product_num,o.product_brand,o.product_color," +
            "bm.name material_name,bm.unit " +
            " from order_product_order o," +
            " produce_order_material_progress pomg ," +
            " base_material bm" +
            " where o.id = pomg.order_id " +
            " and bm.id = pomg.material_id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";



    @Select("select * from produce_order_material_progress" +
            " pomp " +
            " where pomp.in_num is not null and material_id = #{materialId}" +
            " order by pomp.created desc")
    List<ProduceOrderMaterialProgress> listComplements(@Param("materialId") String materialId);

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

    @Select("" +
            "select * from produce_order_material_progress" +
            " pomp " +
            " where (material_id = #{materialId} and (CAST(pomp.in_num AS DECIMAL(12,5)) < CAST(pomp.prepared_num AS DECIMAL(12,5)))) or (pomp.in_num is null )" +
            " order by pomp.created asc")
    List<ProduceOrderMaterialProgress> listByMaterialIdCreatedAscNotOver(@Param("materialId") String materialId);


    @Select("select * from produce_order_material_progress" +
            " pomp " +
            " where pomp.in_num is not null and material_id = #{materialId}" +
            " order by pomp.created desc")
    List<ProduceOrderMaterialProgress> listByMaterialIdCreatedDescHasInNum(@Param("materialId") String materialId);

    @Select("" +
            "select * from produce_order_material_progress" +
            " pomp " +
            " where material_id = #{materialId}" +
            " order by pomp.created desc limit 1")
    ProduceOrderMaterialProgress getByTheLatestByMaterialIdCreatedDesc(@Param("materialId") String materialId);

}
