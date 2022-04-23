package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BaseSupplierMaterialCopy;
import com.boyi.entity.RepositoryBuyinDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单据表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Repository

public interface RepositoryBuyinDocumentMapper extends BaseMapper<RepositoryBuyinDocument> {

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
            "                        repository_buyin_document doc , " +
            "                        repository_buyin_document_detail docD, " +
            "                        base_supplier sup, " +
            "                        base_material m " +
            " " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id " +
            ") t " +
            "left join base_supplier_material sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date order by id desc,detail_id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";

    // 蜘蛛王核算用户，特殊SQL
    String queryZZWSql = "" +
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
            "                        repository_buyin_document doc , " +
            "                        repository_buyin_document_detail docD, " +
            "                        base_supplier sup, " +
            "                        base_material m " +
            " " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id " +
            ") t " +
            "left join base_supplier_material_copy sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date order by id desc,detail_id desc";
    String wrapperZZWSql = "SELECT * from ( " + queryZZWSql + " ) AS q ${ew.customSqlSegment}";

    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryBuyinDocument> page(Page page, @Param("ew") Wrapper queryWrapper);

    // 蜘蛛王核算用户，特殊SQL
    /**
     * 分页查询ZZW
     */
    @Select(wrapperZZWSql)
    Page<RepositoryBuyinDocument> pageZZW(Page page, @Param("ew") Wrapper queryWrapper);


    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<RepositoryBuyinDocument> list(@Param("ew") Wrapper queryWrapper);

    // 蜘蛛王核算用户，特殊SQL
    /**
     * 普通查询
     */
    @Select(wrapperZZWSql)
    List<RepositoryBuyinDocument> listZZW(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    RepositoryBuyinDocument one(@Param("ew") Wrapper queryWrapper);


    @Select("select count(1) from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where rbd.status = 0 and rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId} and rbdd.price_date between #{startDate} and #{endDate}")
    Integer getSupplierMaterialPassBetweenDate(BaseSupplierMaterial baseSupplierMaterial);

    // 蜘蛛王核算用户，特殊SQL

    @Select("select count(1) from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where rbd.status = 0 and rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId} and rbdd.price_date between #{startDate} and #{endDate}")
    Integer getSupplierMaterialCopyPassBetweenDate(BaseSupplierMaterialCopy baseSupplierMaterial);

    @Select("select sum(num) from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where  rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId}")
    Double getSumNumBySupplierIdAndMaterialId(@Param("supplierId") String supplierId,@Param("materialId")String materialId);

    @Select("select rbd.id id, rbd.status status,rbdd.material_id material_id,rbdd.radio_num num from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where rbd.source_type = 1 and rbd.id = rbdd.document_id " +
            " and rbd.buy_in_date between #{startDate} and #{endDate}")
    List<RepositoryBuyinDocument> getListFromOrderBetweenDate(@Param("startDate")LocalDate startDate, @Param("endDate")LocalDate endDate);

    @Select("" +
            " select CONVERT(" +
            " (" +
            " select IFNULL(sum(num),0) from " +
            " " +
            " repository_buyin_document rbd," +
            " repository_buyin_document_detail rbdd" +
            " where rbd.id = rbdd.document_id" +
            " and rbd.buy_in_date >= #{startDate} and rbd.buy_in_date <= #{endDate}"+
            " and rbdd.material_id=#{materialId}" +
            "" +
            " ) -" +
            " (" +
            " select IFNULL(sum(num),0)from " +
            "" +
            " repository_buyout_document rbd," +
            " repository_buyout_document_detail rbdd" +
            " where rbd.id = rbdd.document_id" +
            " and rbd.buy_out_date >= #{startDate} and rbd.buy_out_date <= #{endDate}"+
            " and rbdd.material_id=#{materialId}" +
            "" +
            ")"+
            ",DECIMAL(9,2)) as num"

    )
    RepositoryBuyinDocument getNetInFromOrderBetweenDate(@Param("startDate")LocalDate startD,@Param("endDate") LocalDate endD,@Param("materialId")String materialId);
}
