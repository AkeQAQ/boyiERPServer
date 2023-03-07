package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierFine;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.FinanceSupplierRoundDown;
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
 * @since 2023-02-26
 */
@Repository
public interface FinanceSupplierFineMapper extends BaseMapper<FinanceSupplierFine> {

    String querySql = "SELECT fsd.*,bs.name supplier_name FROM finance_supplier_fine fsd,base_supplier bs where fsd.supplier_id = bs.id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSupplierFine> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSupplierFine> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSupplierFine one(@Param("ew") Wrapper queryWrapper);

    @Select("select fsf.supplier_id,sum(fsf.fine_amount) total_amount from finance_supplier_fine fsf " +
            " where fsf.fine_date >=#{startDate} and fsf.fine_date <= #{endDate} " +
            " group by fsf.supplier_id")
    List<FinanceSupplierFine> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,
                                                                @Param("endDate")LocalDate endDateTime);
}
