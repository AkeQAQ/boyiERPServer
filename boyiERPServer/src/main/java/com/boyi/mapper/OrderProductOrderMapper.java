package com.boyi.mapper;

import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.AnalysisProductOrderVO;
import com.boyi.entity.OrderProductOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.service.ProduceOrderMaterialProgressService;
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
 * @since 2022-03-25
 */
@Repository
public interface OrderProductOrderMapper extends BaseMapper<OrderProductOrder> {

    @Select("<script>" +
            "select t3.*,CONVERT(t3.order_number,DECIMAL(6)) * CONVERT(t3.dosage,DECIMAL(12,6)) cal_num ,pomp.prepared_num from " +
            " " +
            " ( " +
            " select " +
            " t1.id order_id,t1.order_num order_num ,t1.order_number,t1.product_num,t1.product_brand,t1.product_color,t2.material_id,t2.materialName,t2.materialUnit,t2.dosage from " +
            " ( " +
            " select * from order_product_order opo " +
            " where id in " +
            "<foreach collection='orderIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> "  +
            "" +
            "" +
            " ) t1," +
            "" +
            " (" +
            " select ppc.product_num,ppc.product_brand,ppc.product_color,ppcd.material_id,ppcd.dosage,bm.unit materialUnit,bm.name materialName from " +
            " produce_product_constituent ppc," +
            " produce_product_constituent_detail ppcd ," +
            " base_material bm" +
            " where ppc.status=0 and  ppc.id = ppcd.constituent_id and ppcd.material_id = bm.id " +
            " )t2" +
            " where t1.product_num = t2.product_num " +
            " and t1.product_brand = t2.product_brand" +
            " ) t3 left join " +
            " produce_order_material_progress pomp " +
            " on t3.order_id = pomp.order_id and t3.material_id = pomp.material_id order by t3.order_num,t3.material_id  " +
            "</script>")
    List<OrderProductOrder> listBatchMaterialsByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Select("<script>" +
            " select product_num,product_brand from " +
            " order_product_order where  id in"+
            " <foreach collection='orderIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> "  +
            " group by product_num,product_brand"+
            " </script>")
    List<OrderProductOrder> listProductNumBrand(@Param("orderIds")  List<Long> orderIds);

    @Select(" select in_num,pomp.id id " +
            " from" +
            " order_product_order opo ," +
            " produce_order_material_progress pomp " +
            " where opo.id = pomp.order_id" +
            " and opo.product_num = #{productNum} and opo.product_brand = #{productBrand} and material_id = #{materialId}" +
            " ")
    List<ProduceOrderMaterialProgress> listByProductNumBrandAndProgressMaterialId(@Param("productNum")String productNum,
                                                                                  @Param("productBrand") String productBrand,
                                                                                  @Param("materialId") String materialId);

    @Select("select t.product_num, t.sum  from (" +
            " select product_num ,sum(order_number) sum from order_product_order  where order_type!=#{orderType} and created >= #{searchStartDate} and created <= #{searchEndDate}" +
            " group by product_num " +
            " ) t order by sum desc")
    List<AnalysisProductOrderVO> listGroupByProductNum(@Param("orderType") Integer orderType, @Param("searchStartDate")String searchStartDate, @Param("searchEndDate")String searchEndDate);

    @Select("select order_num,order_number,created from order_product_order where order_type!=#{orderType} and created >= #{searchStartDate} and created <= #{searchEndDate}  ")
    List<AnalysisProductOrderVO> listByDate(@Param("orderType") Integer orderType, @Param("searchStartDate")String searchStartDate, @Param("searchEndDate")String searchEndDate);

    @Select("select t.product_brand, t.sum  from (" +
            " select product_brand ,sum(order_number) sum from order_product_order  where order_type!=#{orderType} and created >= #{searchStartDate} and created <= #{searchEndDate}" +
            " group by product_brand " +
            " ) t order by sum desc")
    List<AnalysisProductOrderVO> listGroupByProductBrand(@Param("orderType") Integer orderType, @Param("searchStartDate")String searchStartDate, @Param("searchEndDate")String searchEndDate);

    @Select("select product_num,count(DISTINCT product_brand) sum from order_product_order where order_type!=#{orderType} and created >= #{searchStartDate} and created <= #{searchEndDate} group by product_num  order by count(DISTINCT product_brand) desc ")
    List<AnalysisProductOrderVO> listGroupByMostProductNum(@Param("orderType") Integer orderType, @Param("searchStartDate")String searchStartDate, @Param("searchEndDate")String searchEndDate);

    @Select(" " +
            " select opo.order_num,opo.customer_num,opo.product_num,opo.product_brand,opo.product_color,opo.order_number,opo.product_region,opo.comment,ppcd.material_id,bm.name material_name,cast(ppcd.dosage * opo.order_number as DECIMAL (14,5)) need_num from  " +
            " order_product_order opo,produce_product_constituent ppc,produce_product_constituent_detail ppcd,base_material bm  " +
            "  where order_type != 2 and order_num not in( " +
            " select order_num from " +
            " produce_batch  " +
            " ) " +
            " and opo.product_num = ppc.product_num " +
            " and opo.product_brand = ppc.product_brand " +
            " and ppc.id = ppcd.constituent_id " +
            " and ppcd.material_id like '01.%' " +
            " and ppcd.material_id = bm.id " +
            " order by opo.id desc")
    List<OrderProductCalVO> calNoProductOrders();
}
