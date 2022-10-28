package com.boyi.service;

import com.boyi.entity.CostOfLabourDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.CostOfLabourProcesses;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
public interface CostOfLabourDetailService extends IService<CostOfLabourDetail> {

    boolean removeByForeignId(Long id);

    List<CostOfLabourDetail> listByForeignId(Long id);

    boolean removeByForeignIds(Long[] ids);


    Integer countByProcessesIdBetweenDate(CostOfLabourProcesses one);
}
