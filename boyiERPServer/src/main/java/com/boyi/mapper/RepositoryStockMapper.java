package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryStock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 库存表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
@Repository
public interface RepositoryStockMapper extends BaseMapper<RepositoryStock> {

    String querySql = "select m.id material_id,m.name material_name,m.unit unit,m.specs specs,s.num num," +
            " (select max(rbdd.price_date) from repository_buyin_document_detail rbdd where rbdd.material_id = m.id) latest_price_date," +
            " (select max(rpm.pick_date) from repository_pick_material_detail rpmd,repository_pick_material rpm where rpmd.material_id = m.id" +
            " and rpm.id = rpmd.document_id ) latest_pick_date" +
            "" +
            " from" +
            "             base_material m ,repository_stock s " +
            "             where m.id = s.material_id and s.num != 0" +
            " ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryStock> page(Page page, @Param("ew") Wrapper queryWrapper);

    @Select("select * from repository_stock s where (s.material_id like '01.01.%' or s.material_id like '01.02.%') and s.num!=0    ")
    List<RepositoryStock> listBy01MaterialIds();

}
