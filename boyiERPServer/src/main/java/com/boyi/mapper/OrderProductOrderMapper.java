package com.boyi.mapper;

import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.entity.AnalysisProductOrderVO;
import com.boyi.entity.OrderProductOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.ProduceOrderMaterialProgress;
import com.boyi.entity.RepositoryStock;
import com.boyi.service.ProduceOrderMaterialProgressService;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            " select t1.*,rs.num stock_num from  " +
            " ( " +
            " select opo.order_num,opo.customer_num,opo.product_num,opo.product_brand,opo.product_color,opo.order_number,opo.product_region,opo.comment,ppcd.material_id,bm.name material_name,cast(ppcd.dosage * opo.order_number as DECIMAL (14,5)) need_num from   " +
            "             order_product_order opo,produce_product_constituent ppc,produce_product_constituent_detail ppcd,base_material bm   " +
            "              where order_type != 2 and order_num not in(  " +
            "             select order_num from  " +
            "             produce_batch   " +
            "             )  " +
            "             and opo.product_num = ppc.product_num  " +
            "             and opo.product_brand = ppc.product_brand  " +
            "             and ppc.id = ppcd.constituent_id  " +
            "             and ppcd.material_id = bm.id  " +
            "             " +
            "             order by opo.id desc " +
            " ) t1 left join repository_stock rs  " +
            " on   t1.material_id = rs.material_id")
    List<OrderProductCalVO> calNoProductOrders();

    @Select("select opo.* from order_product_order opo  " +
            "              where order_type != 2 and order_num not in( " +
            "             select order_num from " +
            "             produce_batch  " +
            "             ) " +
            "             order by opo.id desc")
    List<OrderProductOrder> listNoProduct();

    @Select(" " +
            " select t3.material_id,sum(pickNum) num from  " +
            " ( " +
            " select t1.product_num,t1.product_brand,t2.*,t1.material_id,bm.`name` material_name,t1.dosage,CAST(t2.batch_number * t1.dosage as decimal(8,3)) pickNum  from  " +
            " ( " +
            "  select opo.order_num,opo.product_num,opo.product_brand,ppcd.material_id,ppcd.dosage from  order_product_order opo,produce_product_constituent ppc,produce_product_constituent_detail ppcd  " +
            "  where ppc.id = ppcd.constituent_id and ppcd.material_id like '01.01%' and opo.product_num = ppc.product_num and opo.product_brand = ppc.product_brand  " +
            " )t1 , " +
            " ( " +
            " select pb.batch_id,pb.order_num, " +
            " (IFNULL(size34,0)+IFNULL(size35,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)+IFNULL(size41,0)+IFNULL(size42,0)+IFNULL(size43,0)+IFNULL(size44,0)+IFNULL(size45,0)+IFNULL(size46,0)+IFNULL(size47,0)) batch_number from produce_batch pb  " +
            " where pb.batch_id not in ( " +
            " select batch_id from repository_pick_material rpm " +
            " where rpm.batch_id != '' " +
            " )  " +
            " ) t2 ,base_material bm  " +
            " where   " +
            " t1.order_num = t2.order_num " +
            " and t1.material_id = bm.id   and t2.batch_number > 5 " +
            " ) t3 group by t3.material_id order by num desc")
    List<RepositoryStock> listNoPickMaterials();

    @Select("select sum(opo.order_number*ppcd.dosage) order_number,ppcd.material_id from order_product_order opo ," +
            " produce_product_constituent ppc ," +
            " produce_product_constituent_detail ppcd" +
            " where opo.product_num = ppc.product_num and opo.order_type!=2" +
            " and opo.product_brand = ppc.product_brand" +
            " and ppc.id = ppcd.constituent_id" +
            " and (ppcd.material_id like '04.01%' or ppcd.material_id like '04.04%')" +
            " and (ppc.product_num like '%S%' or ppc.product_num like '%L%'  )    and opo.created>=#{searchStartDate} and opo.created<=#{searchEndDate}" +
            " group by ppcd.material_id")
    List<Map<String, Object>> listBySTXMaterial(@Param("searchStartDate") String searchStartDate,@Param("searchEndDate") String searchEndDate);

    @Select("select opo.product_brand,opo.order_type,sum(opo.order_number) sum from order_product_order opo" +
            " where opo.order_type !=2 " +
            " group by opo.product_brand,opo.order_type")
    List<AnalysisProductOrderVO> listGroupByProductBrandAndOrderType(@Param("searchStartDate") String searchStartDate,@Param("searchEndDate") String searchEndDate);

    @Select("<script> select t1.* from " +
            "            (" +
            "            select ppcd.dosage,opo.order_num,opo.customer_num,opo.product_num,opo.product_brand,opo.product_color,opo.order_number,opo.product_region,opo.comment,ppcd.material_id,bm.name material_name,cast(ppcd.dosage * opo.order_number as DECIMAL (14,1)) need_num from  " +
            "                        order_product_order opo,produce_product_constituent ppc,produce_product_constituent_detail ppcd," +
            "   (select * from base_material where id in <foreach collection='materialIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> )bm  " +
            "                         where order_type != 2 and order_num not in( " +
            "                        select order_num from " +
            "                        produce_batch  " +
            "                        ) " +
            "                        and opo.product_num = ppc.product_num " +
            "                        and ppc.status = 0 " +
            "                        and opo.product_brand = ppc.product_brand " +
            "                        and ppc.id = ppcd.constituent_id " +
            "                        and ppcd.material_id = bm.id " +
            "                       " +
            "                        order by opo.id desc" +
            "            ) t1 </script>")
    List<OrderProductCalVO> calNoProductOrdersWithMaterialIds(@Param("materialIds") Set<String> materialIds);

    @Select("<script> select t3.num,t3.order_num,t3.product_num,t3.product_brand,t3.material_id,t3.material_name,t3.dosage,t3.batch_number,t3.batch_id from  " +
            "             ( " +
            "             select t1.product_num,t1.product_brand,t2.*,t1.material_id,bm.`name` material_name,t1.dosage,CAST(t2.batch_number * t1.dosage as decimal(8,1)) num  from  " +
            "             ( " +
            "              select opo.order_num,opo.product_num,opo.product_brand,ppcd.material_id,ppcd.dosage from  order_product_order opo,produce_product_constituent ppc,produce_product_constituent_detail ppcd  " +
            "              where ppc.id = ppcd.constituent_id and ppcd.material_id in <foreach collection='materialIds' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach> and opo.product_num = ppc.product_num and opo.product_brand = ppc.product_brand and opo.order_type!=2 " +
            "             )t1 , " +
            "             ( " +
            "             select pb.batch_id,pb.order_num, " +
            "             (IFNULL(size34,0)+IFNULL(size35,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)+IFNULL(size41,0)+IFNULL(size42,0)+IFNULL(size43,0)+IFNULL(size44,0)+IFNULL(size45,0)+IFNULL(size46,0)+IFNULL(size47,0)) batch_number from produce_batch pb  " +
            "             where pb.batch_id not in ( " +
            "             select batch_id from repository_pick_material rpm " +
            "             where rpm.batch_id != '' " +
            "             )  " +
            "             ) t2 ,base_material bm  " +
            "             where   " +
            "             t1.order_num = t2.order_num " +
            "             and t1.material_id = bm.id   and t2.batch_number > 5 " +
            "             ) t3  </script>")
    List<RepositoryStock> listNoPickMaterialsWithMaterialIds(@Param("materialIds") Set<String> keySet);

    @Select("select  opo.order_num from order_product_order opo" +
            " where opo.order_type!=2 and substring_index(opo.order_num ,'-',1) >= #{minOrderNum} and substring_index(opo.order_num ,'-',1) <= #{maxOrderNum}")
    List<OrderProductOrder> listByOrderNumWithStartAndEnd(@Param("minOrderNum")Integer minOrderNum,@Param("maxOrderNum") Integer maxOrderNum);

    @Select("" +
            "" +
            " select ppcd.material_id,cast(sum((" +
            " ((IFNULL(pb.size34,0)+IFNULL(pb.size35,0)+IFNULL(pb.size36,0)+IFNULL(pb.size37,0)+IFNULL(pb.size38,0)+IFNULL(pb.size39,0)+IFNULL(pb.size40,0)+IFNULL(pb.size41,0)+IFNULL(pb.size42,0)+IFNULL(pb.size43,0)+IFNULL(pb.size44,0)+IFNULL(pb.size45,0)+IFNULL(pb.size46,0)+IFNULL(pb.size47,0)))" +
            " *ppcd.dosage)) as DECIMAL(12,5)) cal_num from order_product_order opo," +
            " produce_product_constituent ppc," +
            " produce_product_constituent_detail ppcd ," +
            " produce_batch pb " +
            " where opo.product_num = ppc.product_num " +
            " and opo.product_brand = ppc.product_brand" +
            " and ppc.id = ppcd.constituent_id" +
            " and pb.id = #{pbId} and (ppcd.material_id like '04.%' or ppcd.material_id like '06.%') " +
            " and pb.order_num = opo.order_num " +
            " group by ppcd.material_id")
    List<OrderProductOrder> listByOrderNumsWithZCMaterialIds(@Param("pbId")Long pbId);

    @Select("select sum(opo.order_number*ppcd.dosage) order_number,ppcd.material_id from order_product_order opo ," +
            " produce_product_constituent ppc ," +
            " produce_product_constituent_detail ppcd" +
            " where opo.product_num = ppc.product_num and opo.order_type!=2" +
            " and opo.product_brand = ppc.product_brand" +
            " and ppc.id = ppcd.constituent_id" +
            "    and opo.created>=#{searchStartDate} and opo.created<=#{searchEndDate}" +
            " group by ppcd.material_id")
    List<Map<String, Object>> listByCalMaterial(@Param("searchStartDate") String startDate,@Param("searchEndDate") String endDate);
}
