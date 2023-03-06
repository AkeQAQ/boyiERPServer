package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountRepositoryPickMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.ExternalAccountRepositoryPickMaterial;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountRepositoryPickMaterialMapper extends BaseMapper<ExternalAccountRepositoryPickMaterial> {

    String querySql = "" +
            "select  doc.id id, " +
            "        doc.pick_date , " +
            "        doc.pick_user , " +
            "        doc.status, " +
            "        doc.produce_doc_num, " +
            "        doc.updated, " +
            "        doc.updated_user, " +
            "        doc.comment, " +
            "        doc.batch_id, " +
            "        dep.name department_name, " +
            "        dep.id department_id, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.specs , " +
            "        docD.num ," +
            "        docD.id detail_id" +
            " from " +
            "                        external_account_repository_pick_material doc , " +
            "                        external_account_repository_pick_material_detail docD, " +
            "                        external_account_base_department dep, " +
            "                        external_account_base_material m " +
            " " +
            "            where doc.department_id = dep.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id order by id desc,detail_id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ExternalAccountRepositoryPickMaterial> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ExternalAccountRepositoryPickMaterial> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ExternalAccountRepositoryPickMaterial one(@Param("ew") Wrapper queryWrapper);

    @Select("select sum(num)  from" +
            " external_account_repository_pick_material pm," +
            "              external_account_repository_pick_material_detail pmd," +
            "              external_account_base_department bd" +
            " where pm.id = pmd.document_id" +
            " and pm.department_id = bd.id" +
            " and pmd.material_id = #{materialId}" +
            " and bd.id = #{departmentId}" )
    Double countByDepartmentAndMaterial(@Param("departmentId") Long departmentId,@Param("materialId") String materialId);

    @Update("update external_account_repository_pick_material set batch_id = null where id = #{id}")
    void updateBatchIdNull(@Param("id")Long id);

    @Update("<script>" +
            " update " +
            "external_account_repository_pick_material  set batch_id =  CONCAT(#{year},batch_id)" +
            "  where  batch_id in"+
            " <foreach collection='batchIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> "  +
            " </script>")
    void updateBatchIdAppendYearById(@Param("year")String year,@Param("batchIds") List<String> batchIds);

    @Select("select rpmd.material_id, cast( sum( rpmd.num )  as decimal(14,5)) totalNum  from " +
            " external_account_repository_pick_material rpm," +
            " external_account_repository_pick_material_detail rpmd" +
            " where " +
            " rpm.id = rpmd.document_id " +
            " and rpm.pick_date > #{endDate}" +
            " group by rpmd.material_id")
    List<ExternalAccountRepositoryPickMaterial> listGTEndDate(@Param("endDate") String endDate);

    @Update("" +
            " update " +
            "external_account_repository_pick_material  set batch_id =  CONCAT(#{year},batch_id)" +
            "  where  batch_id =#{batchId}"+
            "  "  +
            " ")
    void updateBatchIdAppendYearByOneId(@Param("year")String year,@Param("batchId") String batchId);
}
