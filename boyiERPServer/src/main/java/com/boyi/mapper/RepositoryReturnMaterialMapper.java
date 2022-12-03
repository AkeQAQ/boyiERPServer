package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryReturnMaterial;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 仓库模块-退料模块 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Repository
public interface RepositoryReturnMaterialMapper extends BaseMapper<RepositoryReturnMaterial> {

    String querySql = "" +
            "select  doc.id id, " +
            "        doc.return_date , " +
            "        doc.return_user , " +
            "        doc.status, " +
            "        doc.batch_id, " +

            "        dep.name department_name, " +
            "        dep.id department_id, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.specs , " +
            "        docD.num," +
            "        docD.id detail_id," +
            "        docD.reason reason" +

            " from " +
            "                        repository_return_material doc , " +
            "                        repository_return_material_detail docD, " +
            "                        base_department dep, " +
            "                        base_material m " +
            " " +
            "            where doc.department_id = dep.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id order by id desc,detail_id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryReturnMaterial> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<RepositoryReturnMaterial> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    RepositoryReturnMaterial one(@Param("ew") Wrapper queryWrapper);

    @Select("select sum(num)  from" +
            " repository_return_material pm," +
            "              repository_return_material_detail pmd," +
            "              base_department bd" +
            " where pm.id = pmd.document_id" +
            " and pm.department_id = bd.id" +
            " and pmd.material_id = #{materialId}" +
            " and bd.id = #{departmentId}" )
    Double countByDepartmentAndMaterial(@Param("departmentId") Long departmentId,@Param("materialId") String materialId);

    @Update("update repository_return_material set batch_id = null where id = #{id}")
    void updateBatchIdNull(@Param("id")Long id);

    @Update("<script>" +
            " update " +
            "repository_return_material  set batch_id =  CONCAT(#{year},batch_id)" +
            "  where  batch_id in"+
            " <foreach collection='batchIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> "  +
            " </script>")
    void updateBatchIdAppendYearById(@Param("year")String year,@Param("batchIds") List<String> batchIds);

    @Select("select rrmd.material_id, cast( sum( rrmd.num )  as decimal(14,5)) totalNum  from " +
            " repository_return_material rrm," +
            " repository_return_material_detail rrmd" +
            " where " +
            " rrm.id = rrmd.document_id " +
            " and rrm.return_date > #{endDate}" +
            " group by rrmd.material_id")
    List<RepositoryReturnMaterial> listGTEndDate(@Param("endDate") String endDate);

    @Update("" +
            " update " +
            "repository_return_material  set batch_id =  CONCAT(#{year},batch_id)" +
            "  where  batch_id = #{batchId}"+
            " ")
    void updateBatchIdAppendYearByOneId(@Param("year")String year,@Param("batchId") String batchId);

}
