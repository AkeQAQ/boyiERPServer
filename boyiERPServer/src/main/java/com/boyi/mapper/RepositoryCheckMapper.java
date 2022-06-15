package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryCheck;
import com.boyi.entity.RepositoryPickMaterial;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryCheckMapper extends BaseMapper<RepositoryCheck> {

    String querySql = "" +
            "select  doc.id id, " +
            "        doc.check_date , " +
            "        doc.check_user , " +
            "        doc.status, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.specs , " +
            "        docD.check_num," +
            "        docD.stock_num," +
            "        docD.change_num" +
            " from " +
            "                        repository_check doc , " +
            "                        repository_check_detail docD, " +
            "                        base_material m " +
            "               where doc.id = docD.document_id and " +
            "                  docD.material_id = m.id order by id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryCheck> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<RepositoryCheck> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    RepositoryCheck one(@Param("ew") Wrapper queryWrapper);

    @Select("select rcd.material_id, cast( sum( rcd.change_num )  as decimal(14,5)) totalNum  from " +
            "            repository_check rc," +
            "             repository_check_detail rcd" +
            "             where " +
            "             rc.id = rcd.document_id " +
            "             and rc.check_date > #{endDate}" +
            "             group by rcd.material_id")
    List<RepositoryCheck> listGtEndDate(@Param("endDate") String endDate);
}
