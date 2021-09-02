package com.boyi.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryBuyinDocumentDetail;
import com.boyi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;

public class BaseController {
    public DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public HttpServletRequest req;


    @Autowired
    public SysUserService sysUserService;

    @Autowired
    public SysRoleService sysRoleService;

    @Autowired
    public SysMenuService sysMenuService;

    @Autowired
    public SysUserRoleService sysUserRoleService;

    @Autowired
    public SysRoleMenuService sysRoleMenuService;

    @Autowired
    public BaseMaterialGroupService baseMaterialGroupService;

    @Autowired
    public BaseMaterialService baseMaterialService;

    @Autowired
    public BaseSupplierGroupService baseSupplierGroupService;

    @Autowired
    public BaseSupplierService baseSupplierService;

    @Autowired
    public BaseDepartmentService baseDepartmentService;

    @Autowired
    public BaseSupplierMaterialService baseSupplierMaterialService;

    @Autowired
    public RepositoryBuyinDocumentService repositoryBuyinDocumentService;

    @Autowired
    public RepositoryBuyinDocumentDetailService repositoryBuyinDocumentDetailService;

    @Autowired
    public RepositoryStockService repositoryStockService;

    /**
     * 获取页面
     * @return
     */
    public Page getPage() {
        int current = ServletRequestUtils.getIntParameter(req, "currentPage", 1);
        int size = ServletRequestUtils.getIntParameter(req, "pageSize", 10);

        return new Page(current, size);
    }
}
