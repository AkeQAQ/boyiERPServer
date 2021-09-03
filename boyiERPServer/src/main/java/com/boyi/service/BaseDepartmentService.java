package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseDepartment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 基础模块-部门管理 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-22
 */
public interface BaseDepartmentService extends IService<BaseDepartment> {

    Page<BaseDepartment> pageBySearch(Page page, String searchName);
}
