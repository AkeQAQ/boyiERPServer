package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.CostOfLabour;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.RepositoryReturnMaterial;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
@Repository

public interface CostOfLabourMapper extends BaseMapper<CostOfLabour> {

    String querySql = "" +
            "select col.*, ppc.product_num product_num, ppc.product_brand product_brand,colt.type_name cost_of_labour_type_name " +
            ",colp.processes_name ,colp.pieces_price * cold.pieces cal_pieces_price,colp.low_price,cold.real_price,cold.reason reason " +
            "from cost_of_labour col , " +
            "produce_product_constituent ppc , " +
            "cost_of_labour_detail cold, " +
            "cost_of_labour_type colt, " +
            "cost_of_labour_processes colp  " +
            "where col.produce_product_constituent_id = ppc.id  " +
            "and col.cost_of_labour_type_id = colt.id " +
            "and col.id = cold.foreign_id " +
            "and cold.cost_of_labour_processes_id = colp.ID " +
            "order by col.id desc,cold.id desc ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<CostOfLabour> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<CostOfLabour> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    CostOfLabour one(@Param("ew") Wrapper queryWrapper);

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
            "  where  id in"+
            " <foreach collection='batchIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> "  +
            " </script>")
    void updateBatchIdAppendYearById(@Param("year")int year,@Param("batchIds") List<String> batchIds);

    @Select("select rrmd.material_id, cast( sum( rrmd.num )  as decimal(14,5)) totalNum  from " +
            " repository_return_material rrm," +
            " repository_return_material_detail rrmd" +
            " where " +
            " rrm.id = rrmd.document_id " +
            " and rrm.return_date > #{endDate}" +
            " group by rrmd.material_id")
    List<RepositoryReturnMaterial> listGTEndDate(@Param("endDate") String endDate);

}
