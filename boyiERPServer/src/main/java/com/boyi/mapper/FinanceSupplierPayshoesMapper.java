package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierPayshoes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.ProduceProductConstituent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-02-24
 */
@Repository
public interface FinanceSupplierPayshoesMapper extends BaseMapper<FinanceSupplierPayshoes> {

    String querySql = "select fsp.*,fspd.customer_num,fspd.pay_amount,fspd.pay_number,fspd.pay_type,bs.name supplier_name" +
            " from finance_supplier_payshoes fsp,finance_supplier_payshoes_details fspd,base_supplier bs" +
            " where fsp.id = fspd.pay_shoes_id" +
            " and fsp.supplier_id = bs.id ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSupplierPayshoes> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSupplierPayshoes> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSupplierPayshoes one(@Param("ew") Wrapper queryWrapper);

    @Select("select m.supplier_id,sum(md.pay_amount) total_amount from finance_supplier_payshoes m," +
            " finance_supplier_payshoes_details md " +
            " where m.id = md.pay_shoes_id" +
            " and m.pay_date >=#{startDate} and m.pay_date <= #{endDate}" +
            " group by m.supplier_id")
    List<FinanceSupplierPayshoes> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,@Param("endDate") LocalDate endDateTime);
}
