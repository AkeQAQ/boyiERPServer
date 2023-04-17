package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierTaxSupplement;
import com.boyi.entity.FinanceSupplierTaxSupplement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-02-27
 */
@Repository
public interface FinanceSupplierTaxSupplementMapper extends BaseMapper<FinanceSupplierTaxSupplement> {

    String querySql = " select t3.* from (" +
            " select t2.*,(t2.total_document_amount - total_document_payed_amount) lost_amount from " +
            " (" +
            " select t.*,fsd.pay_amount,fsd.pay_date," +
            " (" +
            " select IFNULL(sum(pay_amount),0) from finance_summary_details where document_num = t.document_num" +
            " ) total_document_payed_amount," +
            "" +
            " (" +
            " select IFNULL(sum(document_amount),0) from finance_supplier_tax_supplement tsts where tsts.document_num = t.document_num" +
            " ) total_document_amount" +
            "" +
            " from " +
            " (" +
            " SELECT fsd.*,bs.name supplier_name " +
            " FROM finance_supplier_tax_supplement fsd," +
            " base_supplier bs " +
            " where fsd.supplier_id = bs.id" +
            " ) t left join finance_summary_details fsd" +
            " on t.document_num = fsd.document_num" +
            " ) t2 "+
            " ) t3 "
            ;
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<FinanceSupplierTaxSupplement> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<FinanceSupplierTaxSupplement> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    FinanceSupplierTaxSupplement one(@Param("ew") Wrapper queryWrapper);

    @Select("select m.supplier_id,IFNULL( sum(m.tax_supplement_amount) ,0)total_amount from finance_supplier_tax_supplement m" +
            " where m.document_date >=#{startDate} and m.document_date <= #{endDate}" +
            " group by m.supplier_id")
    List<FinanceSupplierTaxSupplement> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,@Param("endDate") LocalDate endDateTime);

    @Select(
            " select * from " +
            " (" +
            " select fsts.document_num ,fsts.document_amount," +
            " (" +
            " select IFNULL( sum(fsd.pay_amount) ,0)from finance_summary_details fsd where fsd.document_num = fsts.document_num" +
            " ) payed_amount " +
            " from finance_supplier_tax_supplement fsts " +
            " where fsts.supplier_id =#{supplierId} " +
            " ) t where (t.document_amount - t.payed_amount) >0")
    List<FinanceSupplierTaxSupplement> listBySupplierIdDocumentNums(@Param("supplierId") String supplierId);
}
