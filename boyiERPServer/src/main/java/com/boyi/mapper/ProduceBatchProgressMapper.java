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
            " pbp.id ,pbp.supplier_id,pbp.supplier_name,pbp.material_id,pbp.material_name," +
            " pbp.send_foreign_product_date,pbp.back_foreign_product_date,pbp.out_date,pbp.produce_batch_id," +
            " colt.type_name cost_of_labour_type_name,colt.id  cost_of_labour_type_id" +
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
}
