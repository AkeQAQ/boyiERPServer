package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountBaseSupplierMaterial;
import com.boyi.entity.ExternalAccountBaseSupplierMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 供应商-物料报价表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Repository
public interface ExternalAccountBaseSupplierMaterialMapper extends BaseMapper<ExternalAccountBaseSupplierMaterial> {
    String querySql = "select bm.name as material_name ,bm.big_unit as unit,bm.specs as specs,bs.name as supplier_name,bsm.* from external_account_base_material bm,external_account_base_supplier bs,external_account_base_supplier_material bsm" +
            " where bm.id = bsm.material_id and bs.id = bsm.supplier_id order by bsm.created desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ExternalAccountBaseSupplierMaterial> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ExternalAccountBaseSupplierMaterial> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ExternalAccountBaseSupplierMaterial one(@Param("ew") Wrapper queryWrapper);

    @Select("select count(1) from (" +
            "                  select *" +
            "                  from external_account_base_supplier_material" +
            "                  where supplier_id = #{supplierId}" +
            "                    and material_id = #{materialId}" +
            "                   and id != #{id}" +
            "              )t where NOT ((end_date < #{startDate}) OR (start_date > #{endDate}))" +
            "")
    int isRigionExcludeSelf(ExternalAccountBaseSupplierMaterial baseSupplierMaterial);

    @Select("select count(1) from (" +
            "                  select *" +
            "                  from external_account_base_supplier_material" +
            "                  where supplier_id = #{supplierId}" +
            "                    and material_id = #{materialId}" +
            "              )t where NOT ((end_date < #{startDate}) OR (start_date > #{endDate}))" +
            "")
    int isRigion(ExternalAccountBaseSupplierMaterial baseSupplierMaterial);

}
