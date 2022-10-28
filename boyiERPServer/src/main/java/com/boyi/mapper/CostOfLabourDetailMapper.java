package com.boyi.mapper;

import com.boyi.entity.CostOfLabourDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.CostOfLabourProcesses;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
@Repository
public interface CostOfLabourDetailMapper extends BaseMapper<CostOfLabourDetail> {

    @Select("select count(1) from cost_of_labour col," +
            "              cost_of_labour_detail cold" +
            " where  col.id = cold.foreign_id and cold.cost_of_labour_processes_id = #{id} " +
            " and  col.price_date between #{startDate} and #{endDate}")
    Integer countByProcessesIdBetweenDate(CostOfLabourProcesses one);
}
