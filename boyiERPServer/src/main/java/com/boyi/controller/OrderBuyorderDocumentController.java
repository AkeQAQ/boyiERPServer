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
import java.sql.Array;
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
    public ResponseResult push(Principal principal,@RequestBody RepositoryBuyinDocument repositoryBuyinDocument,Long[] orderDetailIds) {
        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByIds(Arrays.asList(orderDetailIds));
        String supplierId = details.get(0).getSupplierId();
        // 已经是下推过的，则不能执行
        for (OrderBuyorderDocumentDetail detail: details){
            if(detail.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0){
                return ResponseResult.fail("请选择未下推的订单进行下推！！！");
            }
            if(!detail.getSupplierId().equals(supplierId)){
                return ResponseResult.fail("请选择同供应商进行下推！！！");
            }
        }

        repositoryBuyinDocument.setSupplierId(supplierId);
        // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
        int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNum(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId());
        if (exitCount > 0) {
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }

        // 2.封装入库单据表的信息
        LocalDateTime now = LocalDateTime.now();
        repositoryBuyinDocument.setCreated(now);
        repositoryBuyinDocument.setUpdated(now);
        repositoryBuyinDocument.setCreatedUser(principal.getName());
        repositoryBuyinDocument.setUpdatedUser(principal.getName());
        repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        repositoryBuyinDocument.setSourceType(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_1);

        repositoryBuyinDocumentService.save(repositoryBuyinDocument);

        ArrayList<RepositoryBuyinDocumentDetail> detailArrayList = new ArrayList<>();
        for (OrderBuyorderDocumentDetail item:details){
            // 3. 存入库表
            RepositoryBuyinDocumentDetail detail = new RepositoryBuyinDocumentDetail();
            detail.setMaterialId(item.getMaterialId());
            detail.setDocumentId(repositoryBuyinDocument.getId());
            detail.setNum(item.getNum());
            detail.setComment(item.getComment());
            detail.setSupplierId(item.getSupplierId());
            detail.setOrderSeq(item.getOrderSeq());
            detail.setOrderId(item.getDocumentId());
            detail.setPriceDate(item.getOrderDate());
            detail.setOrderDetailId(item.getId());

            detailArrayList.add(detail);

        }

        // 4. 存储入库详情
        repositoryBuyinDocumentDetailService.saveBatch(detailArrayList);

        // 5. 修改该订单详情 状态
        orderBuyorderDocumentDetailService.statusSuccess(orderDetailIds);
        return ResponseResult.succ("下推入库成功");

    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:buyOrder:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        // 先查询，假如有状态已下推的，不能删除
        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(ids[0]);
        List<Long> removeIds = new ArrayList<>();
        for (OrderBuyorderDocumentDetail detail:details){
            if(detail.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0){
                return ResponseResult.fail("该采购订单存在已下推的记录。删除失败!");
            }
            removeIds.add(detail.getId());
        }

        boolean flag = orderBuyorderDocumentService.removeByIds(Arrays.asList(ids));

        log.info("删除采购订单表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("采购订单删除失败");
        }

        boolean flagDetail = orderBuyorderDocumentDetailService.removeByIds(removeIds);
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
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(),material.getId(),detail.getOrderDate());

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
            ArrayList<Long> removeIds = new ArrayList<>();// 存放未下推的详情。
            ArrayList<OrderBuyorderDocumentDetail> removeDetails = new ArrayList<>();

            int pushCount = 0;

            for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()) {
                // 只有状态是 1|| null（新增的），未下推的，才能编辑。
                if(item.getStatus()==DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1)
                {
                    removeIds.add(item.getId());
                    removeDetails.add(item);
                }else {
                    pushCount++;
                }
            }

            if(removeIds.size() == 0){
                return ResponseResult.succ("公共部分更新成功，但详情无更新。");
            }
            // 没有下推过的，才能更新供应商，采购日期信息。
            if(pushCount == 0){
                orderBuyorderDocumentService.updateById(orderBuyorderDocument);
            }else {
                // 有推送过的，以之前数据库的未标准。
                orderBuyorderDocument = orderBuyorderDocumentService.getById(orderBuyorderDocument.getId());
            }

            boolean flag = orderBuyorderDocumentDetailService.removeByIds(removeIds);
            if(flag){
                for (OrderBuyorderDocumentDetail item : removeDetails){
                    item.setId(null);
                    item.setDocumentId(orderBuyorderDocument.getId());
                    item.setSupplierId(orderBuyorderDocument.getSupplierId());
                    item.setStatus(item.getStatus() );
                    item.setOrderDate(orderBuyorderDocument.getOrderDate());
                }
                orderBuyorderDocumentDetailService.saveBatch(removeDetails);
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
                item.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1);
                item.setOrderDate(orderBuyorderDocument.getOrderDate());
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
