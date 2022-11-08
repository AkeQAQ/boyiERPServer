package com.boyi.mapper;

import com.boyi.entity.ProduceBatchProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-10-31
 */
@Repository
public interface ProduceBatchProgressMapper extends BaseMapper<ProduceBatchProgress> {

    @Select("select " +
            " pbp.id ,pbp.is_accept,pbp.supplier_id,pbp.supplier_name,pbp.material_id,pbp.material_name," +
            " pbp.send_foreign_product_date,pbp.back_foreign_product_date,pbp.out_date,pbp.produce_batch_id," +
            " colt.type_name cost_of_labour_type_name,colt.id  cost_of_labour_type_id,colt.seq,pb.batch_id batch_id_str" +
            "  from " +
            "             produce_batch pb inner join order_product_order opo on pb.order_num = opo.order_num" +
            "             inner join produce_batch_progress pbp on pb.id=pbp.produce_batch_id" +
            "             left join  cost_of_labour_type colt on colt.id = pbp.cost_of_labour_type_id where pb.id = #{id}" +
            "            order by pbp.id asc")
    List<ProduceBatchProgress> listByBatchId(@Param("id") Long id);

    @Update("update produce_batch_progress set send_foreign_product_date = null where id = #{id}" )
    void updateSendDateByField(@Param("id") Long id);

    @Update("update produce_batch_progress set back_foreign_product_date = null where id = #{id}" )
    void updateBackDateByField(@Param("id") Long id);

    @Update("update produce_batch_progress set out_date = null where id = #{id}" )
    void updateOutDateByField(@Param("id") Long id);

    @Select("select count(1) from produce_batch_progress pbp,produce_batch pb,cost_of_labour_type colt where" +
            " pbp.produce_batch_id = pb.id and pbp.cost_of_labour_type_id = colt.id and pb.batch_id like CONCAT(#{batchIdStr},'%')  and colt.seq = #{seq} and pbp.is_accept=0 and pbp.out_date is not null ")
    Integer countByBatchIdSeqOutDateAccept(@Param("batchIdStr") String batchIdStr,@Param("seq") int seq);

    @Select("select count(1) from produce_batch pb ," +
            " produce_batch_progress pbp " +
            " where pb.id = pbp.produce_batch_id " +
            " and pb.batch_id like concat(#{batchIdStr},'%')" +
            " and pbp.cost_of_labour_type_id = #{coltId} " +
            " and pbp.out_date is not null")
    Integer countByBatchIdStrAndCostOfLabourTypeIdAndOutDateIsNotNull(@Param("batchIdStr") String batchIdStr,@Param("coltId") Long coltId);


    @Select("select distinct(supplier_name) from produce_batch_progress where cost_of_labour_type_id=#{coltId} and supplier_name is not null and created >= #{searchStartDate} and created <= #{searchEndDate}")
    List<Object> listAllSupplierNamesByColtId(@Param("coltId") int coltId,@Param("searchStartDate") String searchStartDate,@Param("searchEndDate") String searchEndDate);

    @Select("" +
            " select pb.batch_id  from produce_batch pb " +
            " where pb.id in" +
            " (" +
            " select pbp.produce_batch_id from produce_batch_progress pbp " +
            " where pbp.cost_of_labour_type_id=#{coltId}  and pbp.supplier_name =#{supplierName} and created >= #{searchStartDate} and created <= #{searchEndDate} " +
            " )")
    List<Object> listProgressesBySupplierNameByColtId(@Param("coltId") int coltId,@Param("searchStartDate") String searchStartDate,@Param("searchEndDate") String searchEndDate,@Param("supplierName") String supplierName);

    @Select("select pbp.send_foreign_product_date,pbp.back_foreign_product_date from produce_batch_progress pbp  where" +
            " pbp.cost_of_labour_type_id=#{coltId}  and  pbp.supplier_name =#{supplierName}" +
            " and created >= #{searchStartDate} and created <= #{searchEndDate} ")
    List<ProduceBatchProgress> listReturnProgressesBySupplierNameByColtId(@Param("coltId") int coltId,@Param("searchStartDate") String searchStartDate,@Param("searchEndDate") String searchEndDate,@Param("supplierName") String supplierName);
}
