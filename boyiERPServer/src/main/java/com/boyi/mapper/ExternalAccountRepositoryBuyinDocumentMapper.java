package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单据表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Repository
public interface ExternalAccountRepositoryBuyinDocumentMapper extends BaseMapper<ExternalAccountRepositoryBuyinDocument> {

    String querySql = "" +
            "select t.*,(sm.price * num) amount ,sm.price price from (" +
            "select  doc.id id, " +
            "        doc.buy_in_date , " +
            "        doc.supplier_document_num , " +
            "        doc.status, " +
            "        doc.source_type, " +
            "        doc.updated , " +
            "        doc.updated_user , " +
            "        sup.name supplier_name, " +
            "        sup.id supId, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.big_unit , " +
            "        docD.order_seq," +
            "        docD.num," +
            "        docD.order_id , " +
            "        docD.id detail_id , " +
            "        docD.price_date  " +
            " from " +
            "                        external_account_repository_buyin_document doc , " +
            "                        external_account_repository_buyin_document_detail docD, " +
            "                        external_account_base_supplier sup, " +
            "                        external_account_base_material m " +
            " " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id " +
            ") t " +
            "left join external_account_base_supplier_material sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date order by id desc,detail_id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";


    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ExternalAccountRepositoryBuyinDocument> page(Page page, @Param("ew") Wrapper queryWrapper);


    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ExternalAccountRepositoryBuyinDocument> list(@Param("ew") Wrapper queryWrapper);


    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ExternalAccountRepositoryBuyinDocument one(@Param("ew") Wrapper queryWrapper);


    @Select("select count(1) from external_account_repository_buyin_document rbd," +
            "              external_account_repository_buyin_document_detail rbdd" +
            " where rbd.status = 0 and rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId} and rbdd.price_date between #{startDate} and #{endDate}")
    Integer getSupplierMaterialPassBetweenDate(ExternalAccountBaseSupplierMaterial baseSupplierMaterial);

    @Select("select sum(num) from external_account_repository_buyin_document rbd," +
            "              external_account_repository_buyin_document_detail rbdd" +
            " where  rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId}")
    Double getSumNumBySupplierIdAndMaterialId(@Param("supplierId") String supplierId,@Param("materialId")String materialId);

    @Select("select rbd.id id, rbd.status status,rbdd.material_id material_id,rbdd.radio_num num from external_account_repository_buyin_document rbd," +
            "              external_account_repository_buyin_document_detail rbdd" +
            " where rbd.source_type = 1 and rbd.id = rbdd.document_id " +
            " and rbd.buy_in_date between #{startDate} and #{endDate}")
    List<ExternalAccountRepositoryBuyinDocument> getListFromOrderBetweenDate(@Param("startDate") LocalDate startDate, @Param("endDate")LocalDate endDate);

    @Select("" +
            " select CONVERT(" +
            " (" +
            " select IFNULL(sum(num),0) from " +
            " " +
            " external_account_repository_buyin_document rbd," +
            " external_account_repository_buyin_document_detail rbdd" +
            " where rbd.id = rbdd.document_id" +
            " and rbd.buy_in_date >= #{startDate} and rbd.buy_in_date < #{endDate}"+
            " and rbdd.material_id=#{materialId}" +
            "" +
            " ) " +
            ",DECIMAL(9,2)) as num"

    )
    ExternalAccountRepositoryBuyinDocument getNetInFromOrderBetweenDate(@Param("startDate")LocalDate startD,@Param("endDate") LocalDate endD,@Param("materialId")String materialId);


    @Select("select bs2.name supplier_name,t3.sum from ( " +
            " select supplier_id,sum(amount) sum from (" +
            "" +
            " select t.supplier_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " external_account_repository_buyin_document rbd ," +
            " external_account_repository_buyin_document_detail rbdd," +
            " external_account_base_supplier bs  ," +
            " external_account_base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +
            " ) t" +
            " left join external_account_base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by supplier_id "+
            " ) t3 ,external_account_base_supplier bs2 " +
            " where t3.supplier_id = bs2.id and sum is not null order by sum desc")
    List<AnalysisMaterailVO> listSupplierAmountPercent(@Param("searchStartDate") String searchStartDate, @Param("searchEndDate") String searchEndDate);

    @Select("select bs2.name supplier_name,t3.sum from ( " +
            " select supplier_id,sum(amount) sum from (" +
            "" +
            " select t.supplier_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " external_account_repository_buyin_document rbd ," +
            " external_account_repository_buyin_document_detail rbdd," +
            " external_account_base_supplier bs  ," +
            " external_account_base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and bs.group_code = #{searchField}"+
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +
            " ) t" +
            " left join external_account_base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by supplier_id "+
            " ) t3 ,external_account_base_supplier bs2 " +
            " where t3.supplier_id = bs2.id and sum is not null order by sum desc")
    List<AnalysisMaterailVO> listSupplierAmountPercentBySupType(@Param("searchStartDate") String searchStartDate,@Param("searchEndDate")  String searchEndDate,@Param("searchField") String searchField);

    @Select("" +
            " select bm2.name material_name,t3.sum from (" +
            " select material_id,sum(amount) sum from (" +
            " select t.material_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " external_account_repository_buyin_document rbd ," +
            " external_account_repository_buyin_document_detail rbdd," +
            " external_account_base_supplier bs  ," +
            " external_account_base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +

            " ) t" +
            " left join external_account_base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by material_id " +
            " ) t3 ,external_account_base_material bm2 " +
            " where t3.material_id = bm2.id and t3.sum is not  null  order by sum desc")
    List<AnalysisMaterailVO> listMaterialAmountPercent(@Param("searchStartDate")String searchStartDate, @Param("searchEndDate") String searchEndDate);

    @Select("" +
            " select bm2.name material_name,t3.sum from (" +
            " select material_id,sum(amount) sum from (" +
            " select t.material_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " external_account_repository_buyin_document rbd ," +
            " external_account_repository_buyin_document_detail rbdd," +
            " external_account_base_supplier bs  ," +
            " external_account_base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and bm.group_code = #{searchField}"+
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +

            " ) t" +
            " left join external_account_base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by material_id " +
            " ) t3 ,external_account_base_material bm2 " +
            " where t3.material_id = bm2.id and t3.sum is not  null  order by sum desc")
    List<AnalysisMaterailVO> listMaterialAmountPercentByMaterialType(@Param("searchStartDate")String searchStartDate, @Param("searchEndDate")String searchEndDate,@Param("searchField") String searchField);

    @Select("" +
            " select rbdd.material_id, cast( sum( rbdd.num )  as decimal(14,5)) totalNum  from " +
            " external_account_repository_buyin_document rbd," +
            " external_account_repository_buyin_document_detail rbdd" +
            " where " +
            " rbd.id = rbdd.document_id " +
            " and rbd.buy_in_date > #{endDate}" +
            " group by rbdd.material_id")
    List<ExternalAccountRepositoryBuyinDocument> listGTEndDate(@Param("endDate") String endDate);
}
