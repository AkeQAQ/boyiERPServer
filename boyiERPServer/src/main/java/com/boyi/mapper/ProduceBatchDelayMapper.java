package com.boyi.mapper;

import com.boyi.entity.ProduceBatchDelay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-11-02
 */
@Repository
public interface ProduceBatchDelayMapper extends BaseMapper<ProduceBatchDelay> {

    @Update("update produce_batch_delay set date = null where id = #{id}" )
    void updateDateByField(Long id);
}
