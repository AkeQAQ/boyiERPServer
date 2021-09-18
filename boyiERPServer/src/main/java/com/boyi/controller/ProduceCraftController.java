package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ProduceCraft;
import com.boyi.entity.ProduceCraft;
import com.boyi.entity.SpreadDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 工艺单
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Slf4j
@RestController
@RequestMapping("/produce/craft")
public class ProduceCraftController extends BaseController {


    /**
     *
     */
    @GetMapping("/getOneExcel")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public String getOneExcel(Long id) {
        ProduceCraft produceCraft = produceCraftService.getById(id);
        return produceCraft.getExcelJson();
    }

    /**
     * 查询最终详情内容
     */
    @GetMapping("/queryRealById")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult queryRealById(Long id) {
        ProduceCraft produceCraft = produceCraftService.getById(id);
        if(produceCraft.getRealJson() == null || produceCraft.getRealJson().isEmpty()){
            produceCraft.setRealJson(produceCraft.getExcelJson());
        }
        return ResponseResult.succ(produceCraft);
    }

    /**
     * 保存最终
     */
    @PostMapping("/setStreadReal")
    @PreAuthorize("hasAuthority('produce:craft:real')")
    public ResponseResult setStreadReal(Principal principal,@Validated @RequestBody ProduceCraft produceCraft) {
        LocalDateTime now = LocalDateTime.now();
        produceCraft.setUpdated(now);
        produceCraft.setUpdateUser(principal.getName());

        produceCraft.setLastUpdateDate(now);
        produceCraft.setLastUpdateUser(principal.getName());

        try {
            produceCraftService.updateById(produceCraft);
            return ResponseResult.succ("保存最終成功");
        } catch (Exception e) {
            log.error("修改异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 获取工艺单模板
     */
    @GetMapping("/getStreadDemo")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult getStreadDemo() {
        try {
            SpreadDemo dbObj = spreadDemoService.getByType(DBConstant.TABLE_SPREAD_DEMO.TYPE_GYD_FIELDVALUE_1);
            return ResponseResult.succ(dbObj);
        } catch (Exception e) {
            log.error("设置模板异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 设置工艺单模板
     */
    @PostMapping("/setStreadDemo")
    @PreAuthorize("hasAuthority('produce:craft:save')")
    public ResponseResult setStreadDemo(@Validated @RequestBody SpreadDemo spreadDemo) {
        try {
            SpreadDemo dbObj = spreadDemoService.getByType(DBConstant.TABLE_SPREAD_DEMO.TYPE_GYD_FIELDVALUE_1);
            if(dbObj == null ){
                spreadDemo.setType(DBConstant.TABLE_SPREAD_DEMO.TYPE_GYD_FIELDVALUE_1);
                spreadDemoService.save(spreadDemo);
            }else {
                dbObj.setDemoJson(spreadDemo.getDemoJson());
                spreadDemoService.updateById(dbObj);
            }
            return ResponseResult.succ("设置模板成功");
        } catch (Exception e) {
            log.error("设置模板异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:craft:update')")
    public ResponseResult update(Principal principal,@Validated @RequestBody ProduceCraft produceCraft) {
        LocalDateTime now = LocalDateTime.now();
        produceCraft.setUpdated(now);
        produceCraft.setUpdateUser(principal.getName());
        produceCraft.setStatus(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_1);
        produceCraft.setDevLastUpdateDate(now);
        produceCraft.setDevLastUpdateUser(principal.getName());
        try {
            produceCraftService.updateById(produceCraft);
            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("修改异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:craft:save')")
    public ResponseResult save(Principal principal,@Validated @RequestBody ProduceCraft produceCraft) {
        LocalDateTime now = LocalDateTime.now();
        produceCraft.setCreated(now);
        produceCraft.setUpdated(now);
        produceCraft.setCreatedUser(principal.getName());
        produceCraft.setUpdateUser(principal.getName());
        produceCraft.setStatus(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_1);
        produceCraft.setDevLastUpdateDate(now);
        produceCraft.setDevLastUpdateUser(principal.getName());

        try {
            ProduceCraft old = produceCraftService.getByCustomerAndCompanyNum(produceCraft.getCustomer(),
                    produceCraft.getCompanyNum());
            if(old != null){
                return ResponseResult.fail("该客户公司，该货号已存在历史记录!不允许添加");
            }
            produceCraftService.save(produceCraft);
            return ResponseResult.succ("新增成功");
        } catch (Exception e) {
            log.error("插入异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }


    @Transactional
    @PostMapping("/returnValid")
    @PreAuthorize("hasAuthority('produce:craft:returnValid')")
    public ResponseResult returnValid(Principal principal,@RequestBody Long id) {

        produceCraftService.updateStatusReturn(principal.getName(),id);
        return ResponseResult.succ("反审核成功");
    }

    @Transactional
    @PostMapping("/returnRealValid")
    @PreAuthorize("hasAuthority('produce:craft:returnRealValid')")
    public ResponseResult returnRealValid(Principal principal,@RequestBody Long id) {

        produceCraftService.updateStatusReturnReal(principal.getName(),id);
        return ResponseResult.succ("反审核成功");
    }

    @Transactional
    @PostMapping("/valid")
    @PreAuthorize("hasAuthority('produce:craft:valid')")
    public ResponseResult valid(Principal principal,@RequestBody Long id) {

        produceCraftService.updateStatusSuccess(principal.getName(),id);
        return ResponseResult.succ("审核成功");
    }

    @Transactional
    @PostMapping("/realValid")
    @PreAuthorize("hasAuthority('produce:craft:realValid')")
    public ResponseResult realValid(Principal principal,@RequestBody Long id) {
        produceCraftService.updateStatusFinal(principal.getName(),id);
        return ResponseResult.succ("审核成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:craft:del')")
    public ResponseResult delete(@RequestBody Long id) {

        boolean flag = produceCraftService.removeById(id);
        log.info("删除工艺单信息,id:{},是否成功：{}",id,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("工艺单删除失败");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult queryById(Long id) {
        ProduceCraft produceCraft = produceCraftService.getById(id);
        return ResponseResult.succ(produceCraft);
    }

    /**
     * 获取工艺单 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<ProduceCraft> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("companyNum")) {
                queryField = "company_num";
            }
            else if (searchField.equals("customer")) {
                queryField = "customer";

            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = produceCraftService.page(getPage(), new QueryWrapper<ProduceCraft>()
                .like(StrUtil.isNotBlank(searchStr)
                        && StrUtil.isNotBlank(searchField), queryField, searchStr));
        return ResponseResult.succ(pageData);
    }

}
