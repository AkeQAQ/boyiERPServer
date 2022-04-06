package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
@RestController
@RequestMapping("/produce/productConstituent")
@Slf4j
public class ProduceProductConstituentController extends BaseController {



    /**
     * 计算用料
     */
    @GetMapping("/calNumByBrandNumColor")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    @Transactional
    public ResponseResult calNumById(Principal principal,String productNum,String productBrand,String productColor, Long orderNumber)throws Exception {
        if(StringUtils.isBlank(productNum) || StringUtils.isBlank(productBrand) ||StringUtils.isBlank(productColor) ){
            return ResponseResult.fail("公司货号，品牌，颜色不能有空");
        }
        try {

            ProduceProductConstituent byNumBrandColor = produceProductConstituentService.getByNumBrandColor(productNum, productBrand, productColor);
            if(byNumBrandColor == null){
                return ResponseResult.fail("产品组成结构没有公司货号["+productNum+"],品牌["+productBrand+"],颜色["+productColor+"] 对应的信息");
            }
            List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(byNumBrandColor.getId());

            ArrayList<Map<String, Object>> result = new ArrayList<>();
            // 计算数目 * 每个物料的用量
            for (ProduceProductConstituentDetail item : details){
                HashMap<String, Object> calTheMap = new HashMap<>();
                BaseMaterial material = baseMaterialService.getById(item.getMaterialId());
                // 查看该物料，最近的供应商价目，
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
                }
                calTheMap.put("materialId",material.getId());

                calTheMap.put("materialName",material.getName());
                double theOneCalNum = Double.valueOf(item.getDosage()) * orderNumber;
                calTheMap.put("calNum",theOneCalNum);
                calTheMap.put("materialUnit",material.getUnit());

                result.add(calTheMap);
            }

            return ResponseResult.succ(result);
        }

        catch (Exception e) {
            log.error("产品组成结构单，计算异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 计算用料
     */
    @GetMapping("/calNumById")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    @Transactional
    public ResponseResult calNumById(Principal principal,Long id, Long calNum)throws Exception {
        try {
            List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(id);

            ArrayList<Map<String, Object>> result = new ArrayList<>();
            // 计算数目 * 每个物料的用量
            for (ProduceProductConstituentDetail item : details){
                HashMap<String, Object> calTheMap = new HashMap<>();
                BaseMaterial material = baseMaterialService.getById(item.getMaterialId());
                // 查看该物料，最近的供应商价目，
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
                }

                calTheMap.put("materialName",material.getName());
                double theOneCalNum = Double.valueOf(item.getDosage()) * calNum;
                calTheMap.put("calNum",theOneCalNum);
                calTheMap.put("materialUnit",material.getUnit());

                result.add(calTheMap);
            }

            return ResponseResult.succ(result);
        }

        catch (Exception e) {
            log.error("产品组成结构单，计算异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:productConstituent:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            boolean flag = produceProductConstituentService.removeByIds(Arrays.asList(ids));

            log.info("删除产品组成结构表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品组成结构删除失败");
            }
             produceProductConstituentDetailService.delByDocumentIds(ids);

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
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult queryById(Long id) {
        ProduceProductConstituent productConstituent = produceProductConstituentService.getById(id);
        List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(id);

        for (ProduceProductConstituentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());
        }

        productConstituent.setRowList(details);
        return ResponseResult.succ(productConstituent);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:productConstituent:update')")
    @Transactional
    public ResponseResult update(Principal principal,boolean specialAddFlag, @Validated @RequestBody ProduceProductConstituent productConstituent)
            throws Exception{

        if(productConstituent.getRowList() ==null || productConstituent.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }
        HashSet<String> materialIds = new HashSet<>();
        for (ProduceProductConstituentDetail detail: productConstituent.getRowList()){
            if(materialIds.contains(detail.getMaterialId())){
                return ResponseResult.fail("物料编码"+detail.getMaterialId()+"重复");
            }
            materialIds.add(detail.getMaterialId());
        }

        productConstituent.setUpdated(LocalDateTime.now());
        productConstituent.setUpdatedUser(principal.getName());
        if(!specialAddFlag){
            productConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_2);
        }
        try {

            //1. 先删除老的，再插入新的
            boolean flag = produceProductConstituentDetailService.removeByDocId(productConstituent.getId());
            if(flag){
                produceProductConstituentService.updateById(productConstituent);

                for (ProduceProductConstituentDetail item : productConstituent.getRowList()){
                    item.setId(null);
                    item.setConstituentId(productConstituent.getId());
                }

                produceProductConstituentDetailService.saveBatch(productConstituent.getRowList());
                log.info("产品组成结构模块-更新内容:{}",productConstituent);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("货号，品牌，颜色不能重复!");
        }
        catch (Exception e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产产品组成结构，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:productConstituent:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody ProduceProductConstituent productConstituent)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        productConstituent.setCreated(now);
        productConstituent.setUpdated(now);
        productConstituent.setCreatedUser(principal.getName());
        productConstituent.setUpdatedUser(principal.getName());
        productConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_2);
        try {
            HashSet<String> materialIds = new HashSet<>();
            for (ProduceProductConstituentDetail detail: productConstituent.getRowList()){
                if(materialIds.contains(detail.getMaterialId())){
                    return ResponseResult.fail("物料编码"+detail.getMaterialId()+"重复");
                }
                materialIds.add(detail.getMaterialId());
            }


            produceProductConstituentService.save(productConstituent);

            for (ProduceProductConstituentDetail item : productConstituent.getRowList()){
                item.setConstituentId(productConstituent.getId());
            }

            produceProductConstituentDetailService.saveBatch(productConstituent.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",productConstituent.getId());
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("货号，品牌，颜色不能重复!");
        }
        catch (Exception e) {
            log.error("产品组成结构单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取产品组成结构 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult list( String searchField, String searchStatus,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceProductConstituent> pageData = null;
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
                    if (oneField.equals("productNum")) {
                        theQueryField = "product_num";
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
        pageData = produceProductConstituentService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('produce:productConstituent:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {

        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_2);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-产品组成结构模块-审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('produce:productConstituent:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_1);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-产品组成结构模块-审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<ProduceProductConstituent> lists = new ArrayList<>();

        for (Long id : ids){
            ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
            produceProductConstituent.setUpdated(LocalDateTime.now());
            produceProductConstituent.setUpdatedUser(principal.getName());
            produceProductConstituent.setId(id);
            produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_0);
            lists.add(produceProductConstituent);

        }
        produceProductConstituentService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_0);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-产品组成结构模块-审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        // 假如有进度表关联了，不能反审核了。
        ProduceProductConstituent old = produceProductConstituentService.getById(id);
        List<OrderProductOrder> orders = orderProductOrderService.getByNumBrandColor(old.getProductNum(),old.getProductBrand(),old.getProductColor());
        if(orders != null && orders.size() > 0){
            HashSet<Long> orderIds = new HashSet<>();
            // 去查询是否有该订单号的进度表
            for (OrderProductOrder order : orders){
                orderIds.add(order.getId());
            }
            List<ProduceOrderMaterialProgress> processes = produceOrderMaterialProgressService.listByOrderIds(orderIds);
            if(processes!=null && processes.size() > 0){
                return ResponseResult.fail("已有物料报备，无法反审核!");

            }
        }


        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_3);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-反审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("反审核成功");
    }

}
