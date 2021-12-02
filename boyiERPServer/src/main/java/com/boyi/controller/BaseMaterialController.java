package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.BaseMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Slf4j
@RestController
@RequestMapping("/baseData/material")
public class BaseMaterialController extends BaseController {

    @PostMapping("/loadTableSearchMaterialDetailAllWithStock")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult loadTableSearchMaterialDetailAllWithStock() {
        List<BaseMaterial> baseMaterials = baseMaterialService.list();
        List<String> ids = new ArrayList<>();
        for (BaseMaterial baseMaterial : baseMaterials) {
            ids.add(baseMaterial.getId());
        }
        List<RepositoryStock> stocks = repositoryStockService.listByMaterialIds(ids);

        HashMap<String, Double> stockNum = new HashMap<>();
        for (RepositoryStock stock : stocks) {
            stockNum.put(stock.getMaterialId(), stock.getNum());
        }

        ArrayList<Map<Object, Object>> returnList = new ArrayList<>();
        baseMaterials.forEach(obj -> {
            Double num = stockNum.get(obj.getId());
            Map<Object, Object> returnMap = MapUtil.builder().put(
                            "value", obj.getId() + " : " + obj.getName())
                    .put("id", obj.getId())
                    .put("obj", obj)
                    .put("stockNum", num == null ? 0D : num)
                    .map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 用于增量表格搜索输入建议框的数据
     */
    @PostMapping("/loadTableSearchMaterialDetailAll")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult loadTableSearchMaterialDetailAll() {
        List<BaseMaterial> baseMaterials = baseMaterialService.list();

        ArrayList<Map<Object, Object>> returnList = new ArrayList<>();
        baseMaterials.forEach(obj -> {
            Map<Object, Object> returnMap = MapUtil.builder().put(
                            "value", obj.getId() + " : " + obj.getName())
                    .put("id", obj.getId())
                    .put("obj", obj)
                    .map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 获取全部数据
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult getSearchAllData() {
        List<BaseMaterial> baseSuppliers = baseMaterialService.list();

        ArrayList<Map<Object, Object>> returnList = new ArrayList<>();
        baseSuppliers.forEach(obj -> {
            Map<Object, Object> returnMap = MapUtil.builder().put("value", obj.getId() + " : " + obj.getName()).put("id", obj.getId()).put("name", obj.getName())
                    .put("unit", obj.getUnit()).put("bigUnit", obj.getBigUnit()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/listByGroupCode")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<BaseMaterial> pageData = null;
        if (searchStr.equals("全部")) {
            pageData = baseMaterialService.page(getPage(), new QueryWrapper<BaseMaterial>());
        } else {
            pageData = baseMaterialService.pageByGroupCode(getPage(), searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<BaseMaterial> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = baseMaterialService.page(getPage());
        } else {
            String queryField = "";
            if (searchField.equals("id")) {
                queryField = "id";
            } else if (searchField.equals("groupCode")) {
                queryField = "group_code";
            } else if (searchField.equals("subId")) {
                queryField = "sub_id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = baseMaterialService.pageBySearch(getPage(), queryField, searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询物料
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult queryById(String id) {
        BaseMaterial baseMaterial = baseMaterialService.getById(id);
        return ResponseResult.succ(baseMaterial);
    }

    /**
     * 新增物料
     */
    @Transactional
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:material:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        baseMaterial.setCreated(now);
        baseMaterial.setUpdated(now);
        baseMaterial.setCreatedUser(principal.getName());
        baseMaterial.setUpdateUser(principal.getName());

        // 需要先判断，同名称，同规格，同基本单位是否存在
        List<BaseMaterial> list = baseMaterialService.listSame(
                baseMaterial.getName(),
                baseMaterial.getUnit(),
                baseMaterial.getSpecs(),
                baseMaterial.getGroupCode());

        if (list != null && list.size() > 0) {
            return ResponseResult.fail("存在同名称，同规格，同单位的物料!请检查!");
        }

        BaseMaterialGroup group = baseMaterialGroupService.getByCode(baseMaterial.getGroupCode());

        if (baseMaterial.getSubId() == null || baseMaterial.getSubId().isEmpty()) {
            baseMaterial.setSubId(group.getAutoSubId() + "");
            baseMaterial.setId(group.getCode() + "." + group.getAutoSubId());
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId() + 1);
            baseMaterialGroupService.updateById(group);
        }

        try {

            // 再保存
            baseMaterialService.save(baseMaterial);

            HashMap<String, Object> returnMap = new HashMap<>();
            returnMap.put("subId",baseMaterial.getSubId());
            returnMap.put("id",baseMaterial.getId());
            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",returnMap);
        } catch (DuplicateKeyException e) {
            log.error("物料，插入异常", e);
            throw new Exception("唯一编码重复!");
        }
    }


    /**
     * 修改物料
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:material:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) {
        baseMaterial.setUpdated(LocalDateTime.now());
        baseMaterial.setUpdateUser(principal.getName());

        // 需要先判断，同名称，同规格，同基本单位是否存在
        List<BaseMaterial> list = baseMaterialService.listSameExcludSelf(baseMaterial.getName(),
                baseMaterial.getUnit(),
                baseMaterial.getSpecs(),
                baseMaterial.getGroupCode(),
                baseMaterial.getId());

        if (list != null && list.size() > 0) {
            return ResponseResult.fail("存在同名称，同规格，同单位的物料!请检查!");
        }
        try {
            // 1. 查询以前的信息
            BaseMaterial oldOne = baseMaterialService.getById(baseMaterial.getId());

            // 2. 先查询是否有被价目表审核完成的引用，有则不能修改，
            int count = baseSupplierMaterialService.countSuccessByMaterialId(baseMaterial.getId());

            if (count > 0) {
                log.info("物料ID[{}]不能修改，存在{}个 审核完成的 采购价目记录", baseMaterial.getId(), count);
                return ResponseResult.fail("物料ID[" + baseMaterial.getId() + "]不能修改，存在" + count + "个 审核完成的 采购价目记录");
            }

            // 3. 有入库,退料，领料记录的，不能修改系数
            int buyInCount = repositoryBuyinDocumentDetailService.count(new QueryWrapper<RepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int buyOutCount = repositoryBuyoutDocumentDetailService.count(new QueryWrapper<RepositoryBuyoutDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int pickCount = repositoryPickMaterialDetailService.count(new QueryWrapper<RepositoryPickMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int returnCount = repositoryReturnMaterialDetailService.count(new QueryWrapper<RepositoryReturnMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int orderCount = orderBuyorderDocumentDetailService.count(new QueryWrapper<OrderBuyorderDocumentDetail>().eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));

            if(oldOne.getUnitRadio() != baseMaterial.getUnitRadio() && (buyInCount>0 ||buyOutCount>0||pickCount>0||returnCount>0||orderCount>0)){
                return ResponseResult.fail("物料ID[" + baseMaterial.getId() + "]不能修改系数，存在:" + buyInCount + "个采购入库记录,"+ buyOutCount + "个采购退料记录,"+ pickCount + "个生产领料记录,"+ returnCount + "个生产退料记录,"+ orderCount + "个采购订单记录");
            }

            baseMaterialService.updateById(baseMaterial);
            log.info("物料ID[{}]更新成功，old{},new:{}.", baseMaterial.getId(), oldOne, baseMaterial);

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("物料，更新异常", e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:material:del')")
    @Transactional
    public ResponseResult delete(@RequestBody String[] ids) {

        int count = repositoryBuyinDocumentDetailService.countByMaterialId(ids);

        if(count > 0){
            return ResponseResult.fail("请先删除"+count+"条对应入库记录!");
        }
        int count2 = baseSupplierMaterialService.countByMaterialId(ids);


        if(count2 > 0){
            return ResponseResult.fail("请先删除"+count2+"条对应价目记录!");
        }

        baseMaterialService.removeByIds(Arrays.asList(ids));

        // 删除物料之后，要删除该物料的库存记录
        repositoryStockService.removeByMaterialId(ids);

        return ResponseResult.succ("删除成功");
    }
}
