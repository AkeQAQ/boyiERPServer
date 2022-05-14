package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/push")
    @PreAuthorize("hasAuthority('order:buyOrder:push')")
    public ResponseResult push(Principal principal, @RequestBody RepositoryBuyinDocument repositoryBuyinDocument, Long[] orderDetailIds, Long id) {

        boolean validIsClose = validIsClose(repositoryBuyinDocument.getBuyInDate());
        if (!validIsClose) {
            return ResponseResult.fail("日期请设置在关账日之后.");
        }

        List<OrderBuyorderDocumentDetail> details = null;
        if (id != -1) {
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            details = orderBuyorderDocumentDetailService.listByDocumentId(id);
        } else {
            details = orderBuyorderDocumentDetailService.listByIds(Arrays.asList(orderDetailIds));
            for (OrderBuyorderDocumentDetail detail : details){
                String user = locks.get(detail.getDocumentId());
                if(StringUtils.isNotBlank(user)){
                    return ResponseResult.fail("单据"+detail.getDocumentId()+"被["+user+"]占用");
                }
            }
        }

        List<Long> detailIds = new ArrayList<>();
        String supplierId = details.get(0).getSupplierId();
        // 已经是下推过的，则不能执行
        for (OrderBuyorderDocumentDetail detail : details) {
            if (detail.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0) {
                return ResponseResult.fail("请选择未下推的订单进行下推！！！");
            }
            if (!detail.getSupplierId().equals(supplierId)) {
                return ResponseResult.fail("请选择同供应商进行下推！！！");
            }
            detailIds.add(detail.getId());
        }
        if (id != null) {
            orderDetailIds = detailIds.toArray(new Long[detailIds.size()]);
        }

        repositoryBuyinDocument.setSupplierId(supplierId);
        // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
        int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNum(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId());
        if (exitCount > 0) {
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }

        // 假如没有价格也不能下推
        for (OrderBuyorderDocumentDetail item : details) {
            BaseSupplierMaterial successPrice = baseSupplierMaterialService.getSuccessPrice(item.getSupplierId(), item.getMaterialId(), item.getOrderDate());
            if(successPrice == null || successPrice.getPrice()==null || successPrice.getPrice()==0D){
                return ResponseResult.fail("供应商:"+item.getSupplierId()+",物料:"+item.getMaterialId()+",没有价格，不能下推");
            }
        }

        try {
            // 2.封装入库单据表的信息
            LocalDateTime now = LocalDateTime.now();
            repositoryBuyinDocument.setCreated(now);
            repositoryBuyinDocument.setUpdated(now);
            repositoryBuyinDocument.setCreatedUser(principal.getName());
            repositoryBuyinDocument.setUpdatedUser(principal.getName());
            repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
            repositoryBuyinDocument.setSourceType(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_1);

            repositoryBuyinDocumentService.save(repositoryBuyinDocument);

            ArrayList<RepositoryBuyinDocumentDetail> detailArrayList = new ArrayList<>();
            for (OrderBuyorderDocumentDetail item : details) {
                // 3. 存入库表
                RepositoryBuyinDocumentDetail detail = new RepositoryBuyinDocumentDetail();
                detail.setMaterialId(item.getMaterialId());
                detail.setDocumentId(repositoryBuyinDocument.getId());
                detail.setNum(item.getNum());
                detail.setRadioNum(item.getRadioNum());

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

            // 6. 增加库存
            for (RepositoryBuyinDocumentDetail detail : detailArrayList) {
                repositoryStockService.addNumByMaterialId(detail.getMaterialId()
                        , detail.getRadioNum());
            }
            return ResponseResult.succ("下推入库成功");
        } catch (Exception e) {
            log.error("发生error:",e);
            throw new RuntimeException(e.getMessage()); // 事务默认需要RuntimeException，但是我们要求有Exception异常也要回滚
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:buyOrder:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        // 先查询，假如有状态已下推的，不能删除
        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(ids[0]);
        List<Long> removeIds = new ArrayList<>();
        for (OrderBuyorderDocumentDetail detail : details) {
            if (detail.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0) {
                return ResponseResult.fail("该采购订单存在已下推的记录。删除失败!");
            }
            removeIds.add(detail.getId());
        }

        try {
            boolean flag = orderBuyorderDocumentService.removeByIds(Arrays.asList(ids));

            log.info("删除采购订单表信息,ids:{},是否成功：{}", ids, flag ? "成功" : "失败");
            if (!flag) {
                return ResponseResult.fail("采购订单删除失败");
            }

            boolean flagDetail = orderBuyorderDocumentDetailService.removeByIds(removeIds);
            log.info("删除采购订单表详情信息,document_id:{},是否成功：{}", ids, flagDetail ? "成功" : "失败");

            if (!flagDetail) {
                return ResponseResult.fail("采购订单详情表没有删除成功!");
            }
            return ResponseResult.succ("删除成功");
        } catch (Exception e) {
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        OrderBuyorderDocument orderBuyorderDocument = orderBuyorderDocumentService.getById(id);

        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(orderBuyorderDocument.getSupplierId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (OrderBuyorderDocumentDetail detail : details) {
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setBigUnit(material.getBigUnit());
            detail.setUnitRadio(material.getUnitRadio());
            detail.setSpecs(material.getSpecs());

            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(), material.getId(), detail.getOrderDate());

            if (one != null) {
                detail.setPrice(one.getPrice());
                double amount = detail.getPrice() * detail.getNum();
                detail.setAmount(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                totalAmount += amount;
            }

            totalNum += detail.getNum();
        }

        orderBuyorderDocument.setTotalNum(totalNum);
        orderBuyorderDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

        orderBuyorderDocument.setSupplierName(supplier.getName());

        orderBuyorderDocument.setRowList(details);
        return ResponseResult.succ(orderBuyorderDocument);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:buyOrder:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody OrderBuyorderDocument orderBuyorderDocument) {

        if (orderBuyorderDocument.getRowList() == null || orderBuyorderDocument.getRowList().size() == 0) {
            return ResponseResult.fail("物料信息不能为空");
        }

        orderBuyorderDocument.setUpdated(LocalDateTime.now());
        orderBuyorderDocument.setUpdatedUser(principal.getName());

        try {
            //2. 先删除老的，再插入新的
            ArrayList<Long> updateIds = new ArrayList<>();// 存放未下推的详情。
            ArrayList<OrderBuyorderDocumentDetail> updateDetails = new ArrayList<>();

            int pushCount = 0;
            ArrayList<Long> nowIds = new ArrayList<>();// 删除的IDS
            for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()) {
                nowIds.add(item.getId());
                // 只有状态是 1|| null（新增的），未下推的，才能编辑。
                if (item.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1) {
                    updateIds.add(item.getId());
                    updateDetails.add(item);
                } else {
                    pushCount++;
                }
            }
            ArrayList<Long> removeIds = new ArrayList<>();// 删除的IDS
            List<OrderBuyorderDocumentDetail> oldDetails = orderBuyorderDocumentDetailService.listByDocumentId(orderBuyorderDocument.getId());
            for (OrderBuyorderDocumentDetail item : oldDetails) {
                if (!nowIds.contains(item.getId())) {
                    removeIds.add(item.getId());
                }
            }

            if (updateIds.size() == 0 && removeIds.size() == 0) {
                return ResponseResult.succ("公共部分更新成功，但详情无更新。");
            }
            // 没有下推过的，更新供应商，采购日期信息。
            if (pushCount == 0) {
                orderBuyorderDocumentService.updateById(orderBuyorderDocument);
            } else {
                // 有推送过的，以之前数据库的未标准。
                orderBuyorderDocument = orderBuyorderDocumentService.getById(orderBuyorderDocument.getId());
            }

            orderBuyorderDocumentDetailService.removeByIds(updateIds);
            orderBuyorderDocumentDetailService.removeByIds(removeIds);
            for (OrderBuyorderDocumentDetail item : updateDetails) {
                item.setId(null);
                item.setDocumentId(orderBuyorderDocument.getId());
                item.setSupplierId(orderBuyorderDocument.getSupplierId());
                item.setStatus(item.getStatus());
                item.setOrderDate(orderBuyorderDocument.getOrderDate());
                item.setRadioNum(item.getNum() * item.getUnitRadio());
            }
            orderBuyorderDocumentDetailService.saveBatch(updateDetails);
            log.info("采购订单模块-更新内容:{}", orderBuyorderDocument);

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常", e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:buyOrder:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody OrderBuyorderDocument orderBuyorderDocument) {
        LocalDateTime now = LocalDateTime.now();
        orderBuyorderDocument.setCreated(now);
        orderBuyorderDocument.setUpdated(now);
        orderBuyorderDocument.setCreatedUser(principal.getName());
        orderBuyorderDocument.setUpdatedUser(principal.getName());
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1);
        try {
            orderBuyorderDocumentService.save(orderBuyorderDocument);

            for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()) {
                item.setDocumentId(orderBuyorderDocument.getId());
                item.setSupplierId(orderBuyorderDocument.getSupplierId());
                item.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1);
                item.setOrderDate(orderBuyorderDocument.getOrderDate());
                item.setRadioNum(item.getNum() * item.getUnitRadio());
            }

            orderBuyorderDocumentDetailService.saveBatch(orderBuyorderDocument.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",orderBuyorderDocument.getId());
        } catch (Exception e) {
            log.error("采购订单单，插入异常", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取采购订单 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('order:buyOrder:export')")
    public void export(HttpServletResponse response,String searchDocNum,  String searchField,String searchStatus, String searchStartDate, String searchEndDate,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<OrderBuyorderDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            } else if (searchField.equals("materialName")) {
                queryField = "material_name";

            } else if (searchField.equals("id")) {
                queryField = "id";

            } else {
            }
        } else {

        }

        Map<String, String> queryMap = new HashMap<>();
        if(manySearchArr!=null && manySearchArr.size() > 0){
            for (int i = 0; i < manySearchArr.size(); i++) {
                Map<String, String> theOneSearch = manySearchArr.get(i);
                String oneField = theOneSearch.get("selectField");
                String oneStr = theOneSearch.get("searchStr");
                String theQueryField = null;
                if (StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            throw new RuntimeException("状态不能为空");
        }

        log.info("搜索字段:{},对应ID:{}", searchField, ids);
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = orderBuyorderDocumentService.innerQueryByManySearch(page, searchField, queryField, searchStr, searchStatusList, searchStartDate, searchEndDate,queryMap,StringUtils.isBlank(searchDocNum)?null:searchDocNum.split(","));

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);) {
            new ExcelExportUtil(OrderBuyorderDocument.class, 1, 0).export(null,null,response, fis, pageData.getRecords(), "报表.xlsx", DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.", e);
        }
    }

    /**
     * 获取采购订单 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult list(String searchDocNum, String searchField,String searchStatus, String searchStartDate, String searchEndDate
                               ,@RequestBody Map<String,Object> params) {

        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<OrderBuyorderDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            } else if (searchField.equals("materialName")) {
                queryField = "material_name";

            } else if (searchField.equals("id")) {
                queryField = "id";

            }else if (searchField.equals("price")) {
                queryField = "price";

            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }

        Map<String, String> queryMap = new HashMap<>();
        if(manySearchArr!=null && manySearchArr.size() > 0){
            for (int i = 0; i < manySearchArr.size(); i++) {
                Map<String, String> theOneSearch = manySearchArr.get(i);
                String oneField = theOneSearch.get("selectField");
                String oneStr = theOneSearch.get("searchStr");
                String theQueryField = null;
                if (StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    }else if (oneField.equals("price")) {
                        theQueryField = "price";
                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            return ResponseResult.fail("状态不能为空");
        }

        log.info("搜索字段:{},对应ID:{}", searchField, ids);
        pageData = orderBuyorderDocumentService.innerQueryByManySearch(getPage(), searchField, queryField, searchStr,searchStatusList, searchStartDate, searchEndDate,queryMap,StringUtils.isBlank(searchDocNum)?null:searchDocNum.split(","));

        Map<String,Double> allPageTotalAmountAndNum = orderBuyorderDocumentService.getAllPageTotalAmount( searchField, queryField, searchStr,searchStatusList, searchStartDate, searchEndDate,queryMap,StringUtils.isBlank(searchDocNum)?null:searchDocNum.split(","));
        Double sumAmount = allPageTotalAmountAndNum.get("sumAmount");
        Double sumNum = allPageTotalAmountAndNum.get("sumNum");

        return ResponseResult.succ(ResponseResult.SUCCESS_CODE,sumAmount+"_"+sumNum,pageData);

    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('order:buyOrder:valid')")
    public ResponseResult statusPass(Principal principal, Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        OrderBuyorderDocument orderBuyorderDocument = new OrderBuyorderDocument();
        orderBuyorderDocument.setUpdated(LocalDateTime.now());
        orderBuyorderDocument.setUpdatedUser(principal.getName());
        orderBuyorderDocument.setId(id);
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_0);
        orderBuyorderDocumentService.updateById(orderBuyorderDocument);
        log.info("仓库模块-审核通过内容:{}", orderBuyorderDocument);

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
