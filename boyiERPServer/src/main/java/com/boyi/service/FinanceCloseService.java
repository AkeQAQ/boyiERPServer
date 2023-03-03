package com.boyi.service;

import com.boyi.entity.FinanceClose;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 仓库关账模块 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-02
 */
public interface FinanceCloseService extends IService<FinanceClose> {

    FinanceClose listLatestOne();
}
