package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.OrderBuyorderDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.OrderBuyorderDocument;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 订单模块-采购订单单据表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Repository
public interface OrderBuyorderDocumentMapper extends BaseMapper<OrderBuyorderDocument> {

    String querySql = "" +
            "select t.*,(sm.price * num) amount ,sm.price price from (" +
            "select  doc.id id, " +
            "        doc.order_date , " +
            "        sup.name supplier_name, " +
            "       sup.id supId, " +
            "        doc.status, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        docD.done_date , " +
            "        docD.order_seq , " +

            "        docD.num from " +
            "                        order_buyorder_document doc , " +
            "                        order_buyorder_document_detail docD, " +
            "                        base_supplier sup, " +
            "                        base_material m " +
            " " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id " +
            ") t " +
            "left join base_supplier_material sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.order_date between  sm.start_date and sm.end_date order by id desc,order_seq desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<OrderBuyorderDocument> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<OrderBuyorderDocument> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    OrderBuyorderDocument one(@Param("ew") Wrapper queryWrapper);


    @Select("select count(1) from order_buyorder_document rbd," +
            "              order_buyorder_document_detail rbdd" +
            " where rbd.status = 0 and rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId} and rbd.order_date between #{startDate} and #{endDate}")
    Integer getBySupplierMaterial(BaseSupplierMaterial baseSupplierMaterial);
}
