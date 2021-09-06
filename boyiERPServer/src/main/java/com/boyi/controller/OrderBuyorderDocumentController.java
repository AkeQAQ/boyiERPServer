package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.OrderBuyorderDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 订单模块-采购订单单据表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Slf4j
@RestController
@RequestMapping("/order/buyOrder")
public class OrderBuyorderDocumentController extends BaseController {

    @Value("${poi.orderBuyOrderDemoPath}")
    private String poiDemoPath;


    @Transactional
    @PostMapping("/push")
    @PreAuthorize("hasAuthority('order:buyOrder:push')")
    public ResponseResult push(Principal principal,@RequestBody RepositoryBuyinDocument repositoryBuyinDocument,Long id) {
        // 1. 获取该订单的全部信息，
        OrderBuyorderDocument orderDoc = orderBuyorderDocumentService.getById(id);

        // 2.封装入库的信息
        repositoryBuyinDocument.setSupplierId(orderDoc.getSupplierId());
        repositoryBuyinDocument.setOrderId(orderDoc.getId());
        LocalDateTime now = LocalDateTime.now();
        repositoryBuyinDocument.setCreated(now);
        repositoryBuyinDocument.setUpdated(now);
        repositoryBuyinDocument.setCreatedUser(principal.getName());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_0);
        // 订单下推入库，priceDate = orderDate
        repositoryBuyinDocument.setPriceDate(orderDoc.getOrderDate());

        // 3. 先查询该供应商，该单号是否已经有记录，有则不能插入
        int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNum(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId());
        if (exitCount > 0) {
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }
        // 4. 插入记录到入库表以及详情表
        repositoryBuyinDocumentService.save(repositoryBuyinDocument);

        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(id);

        ArrayList<RepositoryBuyinDocumentDetail> detailArrayList = new ArrayList<>();
        for (OrderBuyorderDocumentDetail item : details){
            RepositoryBuyinDocumentDetail detail = new RepositoryBuyinDocumentDetail();
            detail.setMaterialId(item.getMaterialId());
            detail.setDocumentId(repositoryBuyinDocument.getId());
            detail.setNum(item.getNum());
            detail.setComment(item.getComment());
            detail.setSupplierId(item.getSupplierId());
            detail.setOrderSeq(item.getOrderSeq());
            detailArrayList.add(detail);

            repositoryStockService.addNumBySupplierIdAndMaterialId(detail.getMaterialId()
                    ,detail.getNum());
        }

        repositoryBuyinDocumentDetailService.saveBatch(detailArrayList);

        // 5. 修改订单状态为已完成
        orderBuyorderDocumentService.statusSuccess(id,repositoryBuyinDocument.getSupplierDocumentNum(),repositoryBuyinDocument.getBuyInDate());
        return ResponseResult.succ("下推入库成功");

    }

    @Transactional
    @PostMapping("/returnPush")
    @PreAuthorize("hasAuthority('order:buyOrder:push')")
    public ResponseResult returnPush(Principal principal,Long id)throws Exception {

        RepositoryBuyinDocument repositoryBuyinDocument = repositoryBuyinDocumentService.getByOrderId(id);
        List<RepositoryBuyinDocumentDetail> details = repositoryBuyinDocumentDetailService.listByDocumentId(repositoryBuyinDocument.getId());

        // 1. 修改库存
        repositoryStockService.subNumByMaterialId(details);
        // 2. 根据该订单ID,删除对应的入库单据记录
        repositoryBuyinDocumentService.removeById(repositoryBuyinDocument.getId());
        repositoryBuyinDocumentDetailService.removeByDocId(repositoryBuyinDocument.getId());

        // 2. 修改订单状态为未完成
        orderBuyorderDocumentService.statusNotSuccess(id);

        return ResponseResult.succ("撤销入库成功");

    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:buyOrder:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        boolean flag = orderBuyorderDocumentService.removeByIds(Arrays.asList(ids));

        log.info("删除采购订单表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("采购订单删除失败");
        }

        boolean flagDetail = orderBuyorderDocumentDetailService.delByDocumentIds(ids);
        log.info("删除采购订单表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("采购订单详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult queryById(Long id) {
        OrderBuyorderDocument orderBuyorderDocument = orderBuyorderDocumentService.getById(id);

        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(orderBuyorderDocument.getSupplierId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (OrderBuyorderDocumentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),orderBuyorderDocument.getOrderDate());

            if(one != null){
                detail.setPrice(one.getPrice());
                double amount = detail.getPrice() * detail.getNum();
                detail.setAmount(new BigDecimal(amount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());
                totalAmount += amount;
            }

            totalNum += detail.getNum();
        }

        orderBuyorderDocument.setTotalNum( totalNum);
        orderBuyorderDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());

        orderBuyorderDocument.setSupplierName(supplier.getName());

        orderBuyorderDocument.setRowList(details);
        return ResponseResult.succ(orderBuyorderDocument);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:buyOrder:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody OrderBuyorderDocument orderBuyorderDocument) {

        if(orderBuyorderDocument.getRowList() ==null || orderBuyorderDocument.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        orderBuyorderDocument.setUpdated(LocalDateTime.now());
        orderBuyorderDocument.setUpdatedUser(principal.getName());

        try {
            //2. 先删除老的，再插入新的
            boolean flag = orderBuyorderDocumentDetailService.removeByDocId(orderBuyorderDocument.getId());
            if(flag){
                orderBuyorderDocumentService.updateById(orderBuyorderDocument);

                for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()){
                    item.setId(null);
                    item.setDocumentId(orderBuyorderDocument.getId());
                    item.setSupplierId(orderBuyorderDocument.getSupplierId());
                }

                orderBuyorderDocumentDetailService.saveBatch(orderBuyorderDocument.getRowList());
                log.info("采购订单模块-更新内容:{}",orderBuyorderDocument);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:buyOrder:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody OrderBuyorderDocument orderBuyorderDocument) {
        LocalDateTime now = LocalDateTime.now();
        orderBuyorderDocument.setCreated(now);
        orderBuyorderDocument.setUpdated(now);
        orderBuyorderDocument.setCreatedUser(principal.getName());
        orderBuyorderDocument.setUpdatedUser(principal.getName());
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1);
        try {
            orderBuyorderDocumentService.save(orderBuyorderDocument);

            for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()){
                item.setDocumentId(orderBuyorderDocument.getId());
                item.setSupplierId(orderBuyorderDocument.getSupplierId());
            }

            orderBuyorderDocumentDetailService.saveBatch(orderBuyorderDocument.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("采购订单单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取采购订单 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('order:buyOrder:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<OrderBuyorderDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = orderBuyorderDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(OrderBuyorderDocument.class,1,0).export(response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取采购订单 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<OrderBuyorderDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = orderBuyorderDocumentService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('order:buyOrder:valid')")
    public ResponseResult statusPass(Principal principal,Long id) {

        OrderBuyorderDocument orderBuyorderDocument = new OrderBuyorderDocument();
        orderBuyorderDocument.setUpdated(LocalDateTime.now());
        orderBuyorderDocument.setUpdatedUser(principal.getName());
        orderBuyorderDocument.setId(id);
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_0);
        orderBuyorderDocumentService.updateById(orderBuyorderDocument);
        log.info("仓库模块-审核通过内容:{}",orderBuyorderDocument);

        // 采购订单审核通过之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个物料对应的库存数量
        for (OrderBuyorderDocumentDetail detail : details){
            repositoryStockService.addNumBySupplierIdAndMaterialId(detail.getMaterialId()
                    ,detail.getNum());
        }

        return ResponseResult.succ("审核通过");
    }


   /* *//**
     * 反审核
     *//*
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('order:buyOrder:valid')")
    public ResponseResult statusReturn(Principal principal,Long id) {


        OrderBuyorderDocument OrderBuyorderDocument = new OrderBuyorderDocument();
        OrderBuyorderDocument.setUpdated(LocalDateTime.now());
        OrderBuyorderDocument.setUpdatedUser(principal.getName());
        OrderBuyorderDocument.setId(id);
        OrderBuyorderDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        OrderBuyorderDocumentService.updateById(OrderBuyorderDocument);
        log.info("仓库模块-反审核通过内容:{}",OrderBuyorderDocument);

        // 采购订单反审核之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<OrderBuyorderDocumentDetail> details = OrderBuyorderDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个供应商，一个物料对应的库存数量
        for (OrderBuyorderDocumentDetail detail : details){
            try {
                repositoryStockService.subNumBySupplierIdAndMaterialId(detail.getSupplierId()
                        ,detail.getMaterialId()
                        ,detail.getNum());
            } catch (Exception e) {
                log.error("数据异常",e);
            }
        }

        return ResponseResult.succ("反审核成功");
    }*/
}
