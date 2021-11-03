package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 供应商-物料报价表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
@Repository
public interface BaseSupplierMaterialMapper extends BaseMapper<BaseSupplierMaterial> {
    String querySql = "select bm.name as material_name ,bm.unit as unit,bs.name as supplier_name,bsm.* from base_material bm,base_supplier bs,base_supplier_material bsm" +
            " where bm.id = bsm.material_id and bs.id = bsm.supplier_id order by bsm.created desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<BaseSupplierMaterial> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<BaseSupplierMaterial> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    BaseSupplierMaterial one(@Param("ew") Wrapper queryWrapper);

    @Select("select count(1) from (" +
            "                  select *" +
            "                  from base_supplier_material" +
            "                  where supplier_id = #{supplierId}" +
            "                    and material_id = #{materialId}" +
            "                   and id != #{id}" +
            "              )t where NOT ((end_date < #{startDate}) OR (start_date > #{endDate}))" +
            "")
    int isRigionExcludeSelf(BaseSupplierMaterial baseSupplierMaterial);

    @Select("select count(1) from (" +
            "                  select *" +
            "                  from base_supplier_material" +
            "                  where supplier_id = #{supplierId}" +
            "                    and material_id = #{materialId}" +
            "              )t where NOT ((end_date < #{startDate}) OR (start_date > #{endDate}))" +
            "")
    int isRigion(BaseSupplierMaterial baseSupplierMaterial);
}
