package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.RepositoryClose;

import java.util.List;

public interface RepositoryCloseService extends IService<RepositoryClose> {
    RepositoryClose listLatestOne();

}
