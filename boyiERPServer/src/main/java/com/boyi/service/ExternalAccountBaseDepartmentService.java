package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountBaseDepartment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 基础模块-部门管理 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountBaseDepartmentService extends IService<ExternalAccountBaseDepartment> {

    Page<ExternalAccountBaseDepartment> pageBySearch(Page page, String searchName);

}
