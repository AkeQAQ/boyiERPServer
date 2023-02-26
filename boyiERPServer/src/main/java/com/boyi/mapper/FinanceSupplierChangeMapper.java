package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierChange;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.FinanceSupplierPayshoes;
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
 * @since 2023-02-25
 */
@Repository
public interface FinanceSupplierChangeMapper extends BaseMapper<FinanceSupplierChange> {

    String querySql = "select fsc.*,fscd.material_id,bm.name material_name,bm.unit,fscd.num,fscd.change_price,fscd.change_amount,bs.name supplier_name,fscd.comment" +
            "             from finance_supplier_change fsc,finance_supplier_change_details fscd,base_supplier bs,base_material bm" +
            "             where fsc.id = fscd.change_id" +
            "             and fsc.supplier_id = bs.id and fscd.material_id=bm.id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSupplierChange> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSupplierChange> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSupplierChange one(@Param("ew") Wrapper queryWrapper);
}
