package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.CostOfLabourProcesses;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
 * @since 2022-10-27
 */
@Repository
public interface CostOfLabourProcessesMapper extends BaseMapper<CostOfLabourProcesses> {


    String querySql = "select colt.type_name cost_of_labour_type_name,colp.* from cost_of_labour_processes colp ," +
            " cost_of_labour_type colt " +
            " where colp.cost_of_labour_type_id = colt.id " +
            " order by colp.created desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<CostOfLabourProcesses> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<CostOfLabourProcesses> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    CostOfLabourProcesses one(@Param("ew") Wrapper queryWrapper);

    @Select("select count(1) from (" +
            "                  select *" +
            "                  from cost_of_labour_processes" +
            "                  where cost_of_labour_type_id = #{costOfLabourTypeId} and processes_name=#{processesName}" +
            "                   and id != #{id}" +
            "              )t where NOT ((end_date < #{startDate}) OR (start_date > #{endDate}))" +
            "")
    int isRigionExcludeSelf(CostOfLabourProcesses baseSupplierMaterial);

    @Select("select count(1) from (" +
            "                  select *" +
            "                  from cost_of_labour_processes" +
            "                  where cost_of_labour_type_id = #{costOfLabourTypeId} and processes_name=#{processesName}" +
            "              )t where NOT ((end_date < #{startDate}) OR (start_date > #{endDate}))" +
            "")
    int isRigion(CostOfLabourProcesses baseSupplierMaterial);
}
