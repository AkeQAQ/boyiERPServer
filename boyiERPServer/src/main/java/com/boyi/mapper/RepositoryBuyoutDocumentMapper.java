package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryBuyoutDocument;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购退料单据表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Repository

public interface RepositoryBuyoutDocumentMapper extends BaseMapper<RepositoryBuyoutDocument> {

    String querySql = "" +
            "select t.*,(sm.price * num) amount ,sm.price price from (" +
            "select  doc.id id, " +
            "        doc.buy_out_date , " +
            "        doc.status, " +
            "        doc.updated , " +
            "        doc.updated_user , " +
            "        sup.name supplier_name, " +
            "        sup.id supId, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.big_unit , " +
            "        docD.num, " +
            "        docD.id detail_id, " +
            "        docD.price_date  " +
            " from " +
            "                        repository_buyout_document doc , " +
            "                        repository_buyout_document_detail docD, " +
            "                        base_supplier sup, " +
            "                        base_material m " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id "+
            ") t " +
            "left join base_supplier_material sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date order by id desc,detail_id desc ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryBuyoutDocument> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<RepositoryBuyoutDocument> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    RepositoryBuyoutDocument one(@Param("ew") Wrapper queryWrapper);



    @Select("select sum(num) from repository_buyout_document rbd," +
            "              repository_buyout_document_detail rbdd" +
            " where rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId}")
    Double getSumNumBySupplierIdAndMaterialId(@Param("supplierId") String supplierId,@Param("materialId")String materialId);

    @Select("select rbdd.material_id, cast( sum( rbdd.num )  as decimal(14,5)) totalNum  from " +
            " repository_buyout_document rbd," +
            " repository_buyout_document_detail rbdd" +
            " where " +
            " rbd.id = rbdd.document_id " +
            " and rbd.buy_out_date > #{endDate}" +
            " group by rbdd.material_id")
    List<RepositoryBuyoutDocument> listGTEndDate(@Param("endDate") String endDate);

    @Select("select t2.supplier_id,sum(material_amount) total_amount from " +
            " (" +
            "" +
            " select t1.supplier_id,t1.material_id,t1.price_date,t1.total_num,bsm.price," +
            "  (cast(t1.total_num as decimal(11,4))*(cast(bsm.price as decimal(11,4)) ) )   material_amount from" +
            "" +
            " (" +
            " select rbd.supplier_id,rbdd.material_id,rbdd.price_date,sum(rbdd.num) total_num from repository_buyout_document rbd," +
            " repository_buyout_document_detail rbdd " +
            " where rbd.id = rbdd.document_id" +
            " and rbd.buy_out_date >= #{startDate} and rbd.buy_out_date <= #{endDate}" +
            " group by rbd.supplier_id,rbdd.material_id,rbdd.price_date" +
            " ) t1,base_supplier_material bsm " +
            " where t1.supplier_id = bsm.supplier_id" +
            " and t1.material_id = bsm.material_id" +
            " and t1.price_date >= bsm.start_date " +
            " and t1.price_date <= bsm.end_date" +
            " ) t2 group by t2.supplier_id ")
    List<RepositoryBuyoutDocument> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,
                                                                     @Param("endDate")LocalDate endDateTime);
}
