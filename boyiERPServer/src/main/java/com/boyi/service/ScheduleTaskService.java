package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.AnalysisRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-10-29
 */
public interface ScheduleTaskService  {

    void changeProduceBatchTranService();
    void changeProductOrderAndProgress();
    void configureTasks();

    void addProduceOrderMaterialProgressByNull();

}
