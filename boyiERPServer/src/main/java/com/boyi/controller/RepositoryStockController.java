package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryStock;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 库存表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
@Slf4j
@RestController
@RequestMapping("/repository/stock")
public class RepositoryStockController extends BaseController {
    @Value("${poi.repositoryStockDemoPath}")
    private String poiDemoPath;

    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:stock:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField) {
        Page<RepositoryStock> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
             if (searchField.equals("materialName")) {
                queryField = "material_name";
            }else if (searchField.equals("materialId")) {
                 queryField = "material_id";
             }
        }
        Page page = getPage();
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = repositoryStockService.pageBySearch(page,queryField,searchField,searchStr);

        // 库存数量为0的过滤.
        List<RepositoryStock> records = pageData.getRecords();
        ArrayList<RepositoryStock> newRecords = new ArrayList<>();
        for (RepositoryStock stock : records){
            if(stock.getNum() != 0){
                newRecords.add(stock);
            }
        }
        pageData.setRecords(newRecords);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryStock.class,1,0).export(null,null,response,fis,pageData.getRecords(),"报表.xlsx", new HashMap<>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取库存 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:stock:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<RepositoryStock> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
             if (searchField.equals("materialName")) {
                queryField = "material_name";
            }else if (searchField.equals("materialId")) {
                 queryField = "material_id";
             }
             else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }
        try {
            pageData = repositoryStockService.pageBySearch(getPage(),queryField,searchField,searchStr);

        }catch (PersistenceException e){
            return ResponseResult.fail("物料编码请不要输入中文");
        }


        // 库存数量为0的过滤.
        List<RepositoryStock> records = pageData.getRecords();
        ArrayList<RepositoryStock> newRecords = new ArrayList<>();
        for (RepositoryStock stock : records){
            if(stock.getNum() != 0){
                newRecords.add(stock);
            }
        }

        pageData.setRecords(newRecords);
        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        return ResponseResult.succ(pageData);
    }


}
