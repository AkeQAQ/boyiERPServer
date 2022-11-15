package com.boyi.controller.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryClose;
import com.boyi.service.*;
import com.boyi.service.impl.BaseSupplierMaterialServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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

    @Autowired
    public OrderBuyorderDocumentService orderBuyorderDocumentService;

    @Autowired
    public OrderBuyorderDocumentDetailService orderBuyorderDocumentDetailService;

    @Autowired
    public RepositoryPickMaterialService repositoryPickMaterialService;

    @Autowired
    public RepositoryPickMaterialDetailService repositoryPickMaterialDetailService;

    @Autowired
    public RepositoryReturnMaterialService repositoryReturnMaterialService;

    @Autowired
    public RepositoryReturnMaterialDetailService repositoryReturnMaterialDetailService;


    @Autowired
    public RepositoryBuyoutDocumentService repositoryBuyoutDocumentService;

    @Autowired
    public RepositoryBuyoutDocumentDetailService repositoryBuyoutDocumentDetailService;

    @Autowired
    public OrderProductpricePreService orderProductpricePreService;


    @Autowired
    public ProduceCraftService produceCraftService;

    @Autowired
    public SpreadDemoService spreadDemoService;

    @Autowired
    public RepositoryCheckService repositoryCheckService;

    @Autowired
    public RepositoryCheckDetailService repositoryCheckDetailService;

    @Autowired
    public RepositoryCloseService repositoryCloseService;

    @Autowired
    public AnalysisRequestService analysisRequestService;

    @Autowired
    public TagService tagService;

    @Autowired
    public ProduceReturnShoesService produceReturnShoesService;

    @Autowired
    public BuyMaterialSupplierService buyMaterialSupplierService;

    @Autowired
    public ProduceProductConstituentService produceProductConstituentService;

    @Autowired
    public ProduceProductConstituentDetailService produceProductConstituentDetailService;

    @Autowired
    public BaseSupplierMaterialCopyService baseSupplierMaterialCopyService;

    @Autowired
    public OrderProductOrderService orderProductOrderService;

    @Autowired
    public ProduceOrderMaterialProgressService produceOrderMaterialProgressService;

    @Autowired
    public ProduceBatchService produceBatchService;

    @Autowired
    public OrderBeforeProductionProgressService orderBeforeProductionProgressService;


    @Autowired
    public OrderBeforeProductionProgressDetailService orderBeforeProductionProgressDetailService;

    @Autowired
    public CostOfLabourTypeService costOfLabourTypeService;

    @Autowired
    public CostOfLabourProcessesService costOfLabourProcessesService;

    @Autowired
    public CostOfLabourService costOfLabourService;

    @Autowired
    public CostOfLabourDetailService costOfLabourDetailService;

    @Autowired
    public ProduceBatchProgressService produceBatchProgressService;

    @Autowired
    public ProduceBatchDelayService produceBatchDelayService;

    @Autowired
    public HisProduceBatchProgressService hisProduceBatchProgressService;

    @Autowired
    public HisProduceBatchDelayService hisProduceBatchDelayService;

    @Autowired
    public BaseCustomerService baseCustomerService;

    @Autowired
    public BaseMaterialSameGroupService baseMaterialSameGroupService;

    @Autowired
    public BaseMaterialSameGroupDetailService baseMaterialSameGroupDetailService;

    /**
     * 获取页面
     * @return
     */
    public Page getPage() {
        int current = ServletRequestUtils.getIntParameter(req, "currentPage", 1);
        int size = ServletRequestUtils.getIntParameter(req, "pageSize", 10);

        return new Page(current, size);
    }

    public boolean validIsClose(LocalDate buyInDate) {
        RepositoryClose close = repositoryCloseService.listLatestOne();
        return buyInDate.isAfter(close.getCloseDate());
    }
}
