package com.boyi.mapper;

import com.boyi.entity.OrderBuyorderDocumentDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 订单模块-采购订单-详情内容 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Repository
public interface OrderBuyorderDocumentDetailMapper extends BaseMapper<OrderBuyorderDocumentDetail> {

    @Select("" +
            " select bm.id material_id,bm.name material_name,bm.specs,bm.unit,bs.name supplier_name from " +
            " (" +
            " select obdd.material_id,obdd.supplier_id from " +
            " (select * from order_buyorder_document_detail where material_id like '11.%'   ) obdd" +
            " left join " +
            " base_supplier_material bsm on" +
            " obdd.material_id = bsm.material_id and" +
            " obdd.supplier_id = bsm.supplier_id and " +
            " obdd.order_date >= bsm.start_date and " +
            " obdd.order_date < bsm.end_date " +
            " where bsm.price is null" +
            " group by obdd.material_id,obdd.supplier_id" +
            " ) t1,base_material bm ,base_supplier bs" +
            " where t1.material_id = bm.id " +
            "  and t1.supplier_id = bs.id " +
            "" +
            " order by bs.name ")
    List<OrderBuyorderDocumentDetail> listNoPriceForeignMaterials();

}
