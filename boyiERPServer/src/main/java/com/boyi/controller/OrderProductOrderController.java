package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelImportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.sql.BatchUpdateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-03-25
 */
@RestController
@RequestMapping("/order/productOrder")
@Slf4j
public class OrderProductOrderController extends BaseController {

    @Value("${poi.orderProductOrderImportDemoPath}")
    private String poiImportDemoPath;
    public static final Map<Object,Object> replaceMap = new HashMap<Object,Object>();
    static {
        replaceMap.put("订单",0);
        replaceMap.put("回单",1);
    }

    @Transactional
    @PostMapping("preparedSuccess")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult preparedSuccess(Long id) throws Exception{
        try {

            orderProductOrderService.updatePrepared(id,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_0);
            return ResponseResult.succ("备料完成!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }
    @Transactional
    @PostMapping("preparedNotSuccess")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult preparedNotSuccess(Long id) throws Exception{
        try {

            orderProductOrderService.updatePrepared(id,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
            return ResponseResult.succ("备料解除完成!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /***
     * 根据订单，获取订单，产品组成，用料，进度表信息
     * @param principal
     * @param orderId
     * @return
     * @throws Exception
     */

    @GetMapping("/listOrderConstituentProgress")
    public ResponseResult getByOrderId(Principal principal, Long orderId)throws Exception {
        OrderProductOrder order = orderProductOrderService.getById(orderId);
        ProduceProductConstituent theConstituent = produceProductConstituentService.getValidByNumBrandColor(order.getProductNum(), order.getProductBrand(), order.getProductColor());
        if(theConstituent==null){
            return ResponseResult.fail("没有审核通过的产品组成结构信息，请确认!");
        }
        List<ProduceProductConstituentDetail> theConsitituentDetails = produceProductConstituentDetailService.listByForeignId(theConstituent.getId());

        List<ProduceOrderMaterialProgress> theProgress = produceOrderMaterialProgressService.listByOrderId(order.getId());
        HashMap<String, ProduceOrderMaterialProgress> theMaterialIdAndProgress = new HashMap<>();

        if(theProgress!=null && theProgress.size()>0){
            for (ProduceOrderMaterialProgress progress : theProgress){
                theMaterialIdAndProgress.put(progress.getMaterialId(),progress);
            }
        }

        String orderNumber = order.getOrderNumber();
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        // 计算数目 * 每个物料的用量
        for (ProduceProductConstituentDetail item : theConsitituentDetails){
            HashMap<String, Object> calTheMap = new HashMap<>();
            BaseMaterial material = baseMaterialService.getById(item.getMaterialId());
           /* // 查看该物料，最近的供应商价目，
            List<BaseSupplierMaterial> theSupplierPrices = baseSupplierMaterialService.myList(new QueryWrapper<BaseSupplierMaterial>()
                    .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, item.getMaterialId())
                    .gt(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME, LocalDate.now())
            );
            ArrayList<Map<String, Object>> supplierPrices = new ArrayList<>();
            calTheMap.put("suppliers",supplierPrices);
            for (BaseSupplierMaterial obj:theSupplierPrices){
                HashMap<String, Object> supplierPrice = new HashMap<>();
                supplierPrice.put("supplierName",obj.getSupplierName());
                supplierPrice.put("price",obj.getPrice());
                supplierPrice.put("startDate",obj.getStartDate());
                supplierPrice.put("endDate",obj.getEndDate());
                supplierPrices.add(supplierPrice);
            }*/
            calTheMap.put("orderNumber",orderNumber);
            calTheMap.put("dosage",item.getDosage());
            calTheMap.put("materialId",material.getId());
            calTheMap.put("materialName",material.getName());
            ProduceOrderMaterialProgress dbProgress = theMaterialIdAndProgress.get(material.getId());

            calTheMap.put("calNum",dbProgress == null|| dbProgress.getCalNum()==null || dbProgress.getCalNum().isEmpty() ?Double.valueOf(item.getDosage()) * Double.valueOf(orderNumber) : dbProgress.getCalNum());
            calTheMap.put("materialUnit",material.getUnit());
            calTheMap.put("preparedNum",dbProgress==null?0:dbProgress.getPreparedNum());
            calTheMap.put("comment",dbProgress==null?"":dbProgress.getComment());
            calTheMap.put("addNum",0);
            calTheMap.put("prepared",order.getPrepared());
            double thePercent = Double.valueOf(calTheMap.get("preparedNum").toString())*100 / Double.valueOf(calTheMap.get("calNum").toString());
            if(thePercent > 100){
                thePercent = 100;
            }
            calTheMap.put("progressPercent",(int)thePercent);

            result.add(calTheMap);
        }

        return ResponseResult.succ(result);
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('order:productOrder:import')")
    public ResponseResult upload(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<OrderProductOrder> utils = new ExcelImportUtil<OrderProductOrder>(OrderProductOrder.class);
        List<OrderProductOrder> orderProductOrders = null;
        try (InputStream fis = file.getInputStream();){
            orderProductOrders = utils.readExcel(fis, 1, 0,18,replaceMap);
            log.info("解析的excel数据:{}",orderProductOrders);


            if(orderProductOrders == null || orderProductOrders.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();
            for (OrderProductOrder order: orderProductOrders){
                LocalDateTime now = LocalDateTime.now();
                order.setCreated(now);
                order.setUpdated(now);
                order.setCreatedUser(principal.getName());
                order.setUpdatedUser(principal.getName());
                order.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_1);
                order.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
                ids.add(order.getOrderNum());
            }
            List<OrderProductOrder> exist = orderProductOrderService.list(new QueryWrapper<OrderProductOrder>()
                    .in(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME,ids));
            if(exist != null && !exist.isEmpty()){
                for (OrderProductOrder existOne:exist){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","订单号："+existOne.getOrderNum()+"已存在");
                    errorMsgs.add(errorMsg);
                }
                return ResponseResult.succ(errorMsgs);
            }

            orderProductOrderService.saveBatch(orderProductOrders);
        }
        catch (Exception e) {
            if( e instanceof  DuplicateKeyException){
                return ResponseResult.fail("订单号重复！");
            }
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }

        return ResponseResult.succ("上传成功");
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String("产品订单导入模板".getBytes("ISO8859-1")));
        response.setHeader("filename","产品订单导入模板" );

        FileInputStream fis = new FileInputStream(new File(poiImportDemoPath));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("下载成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:productOrder:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            boolean flag = orderProductOrderService.removeByIds(Arrays.asList(ids));

            log.info("删除产品订单表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品订单删除失败");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:productOrder:list')")
    public ResponseResult queryById(Long id) {
        OrderProductOrder orderProductOrder = orderProductOrderService.getById(id);
        return ResponseResult.succ(orderProductOrder);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:productOrder:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody OrderProductOrder orderProductOrder)
            throws Exception{

        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_2);
        try {

            orderProductOrderService.updateById(orderProductOrder);

            log.info("产品订单模块-更新内容:{}",orderProductOrder);

            return ResponseResult.succ("编辑成功");
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("订单号不能重复!");
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody OrderProductOrder orderProductOrder)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        orderProductOrder.setCreated(now);
        orderProductOrder.setUpdated(now);
        orderProductOrder.setCreatedUser(principal.getName());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_2);
        orderProductOrder.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
        try {

            orderProductOrderService.save(orderProductOrder);

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",orderProductOrder.getId());
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("订单号不能重复!");
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取产品订单 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('order:productOrder:list')")
    public ResponseResult list( String searchField, String searchStatus, String searchStatus2,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<OrderProductOrder> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("productNum")) {
                queryField = "product_num";
            }
            else if (searchField.equals("productBrand")) {
                queryField = "product_brand";

            }else {
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
                    if (searchField.equals("productNum")) {
                        queryField = "product_num";
                    }
                    else if (oneField.equals("productBrand")) {
                        theQueryField = "product_brand";

                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
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
        List<Long> searchStatusList2 = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus2)){
            String[] split = searchStatus2.split(",");
            for (String statusVal : split){
                searchStatusList2.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList2.size() == 0){
            return ResponseResult.fail("备料状态不能为空");
        }

        pageData = orderProductOrderService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,searchStatusList2,queryMap);

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_2);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_1);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('order:productOrder:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<OrderProductOrder> lists = new ArrayList<>();

        for (Long id : ids){
            OrderProductOrder orderProductOrder = new OrderProductOrder();
            orderProductOrder.setUpdated(LocalDateTime.now());
            orderProductOrder.setUpdatedUser(principal.getName());
            orderProductOrder.setId(id);
            orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0);
            lists.add(orderProductOrder);

        }
        orderProductOrderService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('order:productOrder:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('order:productOrder:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        // 假如有进度表关联了，不能反审核了。
        List<ProduceOrderMaterialProgress> progresses = produceOrderMaterialProgressService.listByOrderId(id);
        if(progresses!=null && progresses.size() > 0){
            return ResponseResult.fail("已有物料报备，无法反审核!");
        }
        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_3);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("反审核成功");
    }

}
