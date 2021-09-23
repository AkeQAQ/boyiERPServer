package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryPickMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Repository
public interface RepositoryPickMaterialMapper extends BaseMapper<RepositoryPickMaterial> {

    String querySql = "" +
            "select  doc.id id, " +
            "        doc.pick_date , " +
            "        doc.pick_user , " +
            "        doc.status, " +
            "        dep.name department_name, " +
            "        dep.id department_id, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.specs , " +
            "        docD.num" +
            " from " +
            "                        repository_pick_material doc , " +
            "                        repository_pick_material_detail docD, " +
            "                        base_department dep, " +
            "                        base_material m " +
            " " +
            "            where doc.department_id = dep.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id order by id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryPickMaterial> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<RepositoryPickMaterial> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    RepositoryPickMaterial one(@Param("ew") Wrapper queryWrapper);

    @Select("select sum(num)  from" +
            " repository_pick_material pm," +
            "              repository_pick_material_detail pmd," +
            "              base_department bd" +
            " where pm.id = pmd.document_id" +
            " and pm.department_id = bd.id" +
            " and pmd.material_id = #{materialId}" +
            " and bd.id = #{departmentId}" )
    Double countByDepartmentAndMaterial(Long departmentId, String materialId);
}
