package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ExternalAccountRepositoryStock;
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
import java.util.Map;

/**
 * <p>
 * 库存表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@RestController
@RequestMapping("/externalAccount/repository/stock")
@Slf4j
public class ExternalAccountRepositoryStockController extends BaseController {
    @Value("${poi.eaRepositoryStockDemoPath}")
    private String poiDemoPath;

    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    public void export(HttpServletResponse response, String searchStr, String searchField) {
        Page<ExternalAccountRepositoryStock> pageData = null;
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
        pageData = externalAccountRepositoryStockService.pageBySearch(page,queryField,searchField,searchStr);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(ExternalAccountRepositoryStock.class,1,0).export(null,null,response,fis,pageData.getRecords(),"报表.xlsx", new HashMap<>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取库存 分页全部数据
     */
    @GetMapping("/list")
    public ResponseResult list(String searchStr, String searchField) {
        Page<ExternalAccountRepositoryStock> pageData = null;
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
            pageData = externalAccountRepositoryStockService.pageBySearch(getPage(),queryField,searchField,searchStr);

        }catch (PersistenceException e){
            return ResponseResult.fail("物料编码请不要输入中文");
        }


        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        return ResponseResult.succ(pageData);
    }

}
