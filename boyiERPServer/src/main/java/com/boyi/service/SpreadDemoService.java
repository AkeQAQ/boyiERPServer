package com.boyi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.SpreadDemo;

public interface SpreadDemoService extends IService<SpreadDemo> {
    SpreadDemo getByType(Integer typeBaojiaFieldvalue0);

}
