package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ExternalAccountBaseSupplier;
import com.boyi.entity.ExternalAccountBaseSupplierGroup;
import com.boyi.entity.HisProduceBatchProgress;
import com.boyi.entity.ProduceBatchProgress;
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
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@RestController
@RequestMapping("/externalAccount/baseData/supplier")
@Slf4j
public class ExternalAccountBaseSupplierController extends BaseController {

    @Value("${poi.eaBaseDataSupplierDemoPath}")
    private String poiDemoPath;

    /**
     * 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public void export(HttpServletResponse response, String searchField, String searchStr) {

        Page<ExternalAccountBaseSupplier> pageData = null;
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = externalAccountBaseSupplierService.page(page);
        } else {
            String queryField = "";
            if (searchField.equals("id")) {
                queryField = "id";
            }
            else if (searchField.equals("groupCode")) {
                queryField = "group_code";
            } else if (searchField.equals("subId")) {
                queryField = "sub_id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = externalAccountBaseSupplierService.pageBySearch(page, queryField, searchStr);
        }

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(ExternalAccountBaseSupplier.class,1,0).export("null","null",response,fis,pageData.getRecords(),"报表.xlsx",new HashMap<String,String>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult getSearchAllData() {
        List<ExternalAccountBaseSupplier> baseSuppliers = externalAccountBaseSupplierService.list();

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        baseSuppliers.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+obj.getName() ).put("id", obj.getId()).put("name", obj.getName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 获取供应商 分页全部数据
     */
    @GetMapping("/listByGroupCode")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<ExternalAccountBaseSupplier> pageData = null;
        if(searchStr.equals("全部")){
            pageData = externalAccountBaseSupplierService.page(getPage(),new QueryWrapper<ExternalAccountBaseSupplier>());
        }else {
            pageData = externalAccountBaseSupplierService.pageByGroupCode(getPage(),searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取供应商 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<ExternalAccountBaseSupplier> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = externalAccountBaseSupplierService.page(getPage());
        } else {
            String queryField = "";
            if (searchField.equals("id")) {
                queryField = "id";
            }
            else if (searchField.equals("groupCode")) {
                queryField = "group_code";
            } else if (searchField.equals("subId")) {
                queryField = "sub_id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = externalAccountBaseSupplierService.pageBySearch(getPage(), queryField, searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询供应商
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult queryById(String id) {
        ExternalAccountBaseSupplier ExternalAccountBaseSupplier = externalAccountBaseSupplierService.getById(id);
        return ResponseResult.succ(ExternalAccountBaseSupplier);
    }

    /**
     * 新增供应商
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody ExternalAccountBaseSupplier baseSupplier) {
        LocalDateTime now = LocalDateTime.now();
        baseSupplier.setCreated(now);
        baseSupplier.setUpdated(now);
        baseSupplier.setCreatedUser(principal.getName());
        baseSupplier.setUpdateUser(principal.getName());

        ExternalAccountBaseSupplierGroup group = externalAccountBaseSupplierGroupService.getByCode(baseSupplier.getGroupCode());

        baseSupplier.setSubId(group.getAutoSubId());

        baseSupplier.setId(baseSupplier.getGroupCode()+"."+baseSupplier.getSubId());

        try {
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId()+1);
            externalAccountBaseSupplierGroupService.updateById(group);

            externalAccountBaseSupplierService.save(baseSupplier);

            HashMap<String, Object> returnMap = new HashMap<>();
            returnMap.put("subId",baseSupplier.getSubId());
            returnMap.put("id",baseSupplier.getId());
            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",returnMap);
        } catch (DuplicateKeyException e) {
            log.error("供应商，插入异常",e);
            throw new RuntimeException("唯一编码重复!");
        }
    }


    /**
     * 修改供应商
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody ExternalAccountBaseSupplier baseSupplier) {
        baseSupplier.setUpdated(LocalDateTime.now());
        baseSupplier.setUpdateUser(principal.getName());
        try {

            // 1. 查询以前的信息
            ExternalAccountBaseSupplier oldOne = externalAccountBaseSupplierService.getById(baseSupplier.getId());
            if( !oldOne.getName().equals(baseSupplier.getName())){
                // 2. 先查询是否有被价目表审核完成的引用，有则不能修改，
                int count = externalAccountBaseSupplierMaterialService.countSuccessBySupplierId(baseSupplier.getId());
                if (count > 0) {
                    log.info("供应商ID[{}]不能修改，存在{}个 审核完成的 采购价目记录", baseSupplier.getId(), count);
                    return ResponseResult.fail("供应商ID[" + baseSupplier.getId() + "]不能修改，存在" + count + "个 审核完成的 采购价目记录");
                }
            }

            externalAccountBaseSupplierService.updateById(baseSupplier);
            log.info("供应商ID[{}]更新成功，old{},new:{}.",baseSupplier.getId(),oldOne,baseSupplier);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException("唯一编码重复!");
        }catch (Exception e){
            throw new RuntimeException("其他异常!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult delete(@RequestBody String[] ids) {

        int count = externalAccountRepositoryBuyinDocumentService.countBySupplierId(ids);

        if(count > 0){
            return ResponseResult.fail("请先删除"+count+"条对应入库记录!");
        }

        int count2 = externalAccountBaseSupplierMaterialService.countBySupplierId(ids);

        if(count2 > 0){
            return ResponseResult.fail("请先删除"+count2+"条对应价目记录!");
        }

        externalAccountBaseSupplierService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
