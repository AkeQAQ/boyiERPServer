package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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

        pageData = repositoryStockService.pageBySearch(getPage(),queryField,searchField,searchStr);

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
