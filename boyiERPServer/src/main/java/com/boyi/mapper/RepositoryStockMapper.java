package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryStock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

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

    String querySql = "select m.id material_id,m.name material_name,m.unit unit,m.specs specs," +
            "s.num num from" +
            " base_material m ," +
            "(select material_id,sum(num) num from repository_stock  group by material_id) s " +
            " where m.id = s.material_id ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryStock> page(Page page, @Param("ew") Wrapper queryWrapper);
}
