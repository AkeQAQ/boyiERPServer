package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.BaseSupplierService;
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
 * 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Slf4j
@RestController
@RequestMapping("/baseData/supplier")
public class BaseSupplierController extends BaseController {

    @Value("${poi.baseDataSupplierDemoPath}")
    private String poiDemoPath;

    /**
     * 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public void export(HttpServletResponse response, String searchField, String searchStr) {

        Page<BaseSupplier> pageData = null;
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = baseSupplierService.page(page);
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
            pageData = baseSupplierService.pageBySearch(page, queryField, searchStr);
        }

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(BaseSupplier.class,1,0).export("null","null",response,fis,pageData.getRecords(),"报表.xlsx",new HashMap<String,String>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult getSearchAllData() {
        List<BaseSupplier> baseSuppliers = baseSupplierService.list();

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
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<BaseSupplier> pageData = null;
        if(searchStr.equals("全部")){
            pageData = baseSupplierService.page(getPage(),new QueryWrapper<BaseSupplier>());
        }else {
            pageData = baseSupplierService.pageByGroupCode(getPage(),searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取供应商 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<BaseSupplier> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = baseSupplierService.page(getPage());
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
            pageData = baseSupplierService.pageBySearch(getPage(), queryField, searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询供应商
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult queryById(String id) {
        BaseSupplier BaseSupplier = baseSupplierService.getById(id);
        return ResponseResult.succ(BaseSupplier);
    }

    /**
     * 新增供应商
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:supplier:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseSupplier baseSupplier) {
        LocalDateTime now = LocalDateTime.now();
        baseSupplier.setCreated(now);
        baseSupplier.setUpdated(now);
        baseSupplier.setCreatedUser(principal.getName());
        baseSupplier.setUpdateUser(principal.getName());

        BaseSupplierGroup group = baseSupplierGroupService.getByCode(baseSupplier.getGroupCode());

        baseSupplier.setSubId(group.getAutoSubId());

        baseSupplier.setId(baseSupplier.getGroupCode()+"."+baseSupplier.getSubId());

        try {
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId()+1);
            baseSupplierGroupService.updateById(group);

            baseSupplierService.save(baseSupplier);

            HashMap<String, Object> returnMap = new HashMap<>();
            returnMap.put("subId",baseSupplier.getSubId());
            returnMap.put("id",baseSupplier.getId());
            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",returnMap);
        } catch (DuplicateKeyException e) {
            log.error("供应商，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     * 修改供应商
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:supplier:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseSupplier baseSupplier) {
        baseSupplier.setUpdated(LocalDateTime.now());
        baseSupplier.setUpdateUser(principal.getName());
        try {

            // 1. 查询以前的信息
            BaseSupplier oldOne = baseSupplierService.getById(baseSupplier.getId());
            if( !oldOne.getName().equals(baseSupplier.getName())){
                // 2. 先查询是否有被价目表审核完成的引用，有则不能修改，
                int count = baseSupplierMaterialService.countSuccessBySupplierId(baseSupplier.getId());
                if (count > 0) {
                    log.info("供应商ID[{}]不能修改，存在{}个 审核完成的 采购价目记录", baseSupplier.getId(), count);
                    return ResponseResult.fail("供应商ID[" + baseSupplier.getId() + "]不能修改，存在" + count + "个 审核完成的 采购价目记录");
                }
            }
            // 2. 查询生产进度表
            List<ProduceBatchProgress> progresses = produceBatchProgressService.listBySupplierId(baseSupplier.getId());

            if(progresses!=null && !progresses.isEmpty()){
                return ResponseResult.fail("供应商ID[" + baseSupplier.getId() + "]不能修改，存在" + progresses.size() + "个生产序号进度表");
            }

            // 2. 查询生产进度表
            List<HisProduceBatchProgress> hisProgresses = hisProduceBatchProgressService.listBySupplierId(baseSupplier.getId());

            if(hisProgresses!=null && !hisProgresses.isEmpty()){
                return ResponseResult.fail("供应商ID[" + baseSupplier.getId() + "]不能修改，存在" + hisProgresses.size() + "个历史生产序号进度表");
            }

            baseSupplierService.updateById(baseSupplier);
            log.info("供应商ID[{}]更新成功，old{},new:{}.",baseSupplier.getId(),oldOne,baseSupplier);
            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:supplier:del')")
    public ResponseResult delete(@RequestBody String[] ids) {

        int count = repositoryBuyinDocumentService.countBySupplierId(ids);

        if(count > 0){
            return ResponseResult.fail("请先删除"+count+"条对应入库记录!");
        }

        int count2 = baseSupplierMaterialService.countBySupplierId(ids);

        if(count2 > 0){
            return ResponseResult.fail("请先删除"+count2+"条对应价目记录!");
        }

        //判断采购订单，是否有该物料
        int orderBuyorderCount = orderBuyorderDocumentDetailService.countBySupplierId(ids);
        if(orderBuyorderCount > 0){
            return ResponseResult.fail("请先删除"+orderBuyorderCount+"条对应采购订单信息!");
        }

        // 2. 查询生产进度表
        List<ProduceBatchProgress> progresses = produceBatchProgressService.listBySupplierIds(ids);

        if(progresses!=null && !progresses.isEmpty()){
            return ResponseResult.fail("供应商ID[" + Arrays.asList(ids).toString() + "]不能删除，存在" + progresses.size() + "个生产序号进度表");
        }

        // 2. 查询历史生产进度表
        List<HisProduceBatchProgress> hisProgresses = hisProduceBatchProgressService.listBySupplierIds(ids);

        if(hisProgresses!=null && !hisProgresses.isEmpty()){
            return ResponseResult.fail("供应商ID[" + Arrays.asList(ids).toString() + "]不能删除，存在" + hisProgresses.size() + "个历史生产序号进度表");
        }

        baseSupplierService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
