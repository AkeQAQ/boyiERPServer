package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryInOutDetail;
import com.boyi.mapper.ExternalAccountRepositoryInOutDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物料收发明细 前端控制器
 * </p>
 *
 * @author sunke
 */
@Slf4j
@RestController
@RequestMapping("/externalAccount/repository/inOutDetail")
public class ExternalAccountRepositoryInOutDetailController extends BaseController {
    @Autowired
    private ExternalAccountRepositoryInOutDetailMapper externalAccountRepositoryInOutDetailMapper;

    @Value("${poi.repositoryInOutDemoPath}")
    private String poiDemoPath;
    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('externalAccount:repository:inOutDetail:list')")
    public void export(HttpServletResponse response, String searchStartDate, String searchEndDate,String searchField,String searchStatus,@RequestBody Map<String,Object> params) {

        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField .equals("")) {
            if (searchField.equals("materialName")) {
                queryField = "material_name";
            }
        }
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        searchStatusList.add(-1L);

        LocalDate startDate = LocalDate.parse(searchStartDate);
        LocalDate endDate = LocalDate.parse(searchEndDate);
        Page page2 = getPage();
        if(page2.getSize()==10 && page2.getCurrent() == 1){
            page2.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        Page<RepositoryInOutDetail> page = externalAccountRepositoryInOutDetailMapper.page(page2, new QueryWrapper<RepositoryBuyinDocument>().
                like(StrUtil.isNotBlank(searchStr)
                        && StrUtil.isNotBlank(searchField), queryField, searchStr)
                .in("status",searchStatusList), startDate, endDate);
        List<RepositoryInOutDetail> records = page.getRecords();
        for (int i = 0; i < records.size(); i++) {
            RepositoryInOutDetail current = records.get(i);
            Integer typeOrder = current.getTypeOrder();
            if(typeOrder == 1){// 1:期初，2：采购入库，3:生产领料，4：采购退料，5：生产退料，6：盘点
                current.setAfterNum(current.getStartNum());
                continue;
            }
            RepositoryInOutDetail lastOne = records.get(i - 1); //TODO 分页情况下，点击其他页存在BUG：该分页第一个不是期初，该行报错
            if(typeOrder == 2 || typeOrder == 5 || typeOrder == 6){
                Double afterNum = lastOne.getAfterNum();
                if(afterNum == null){
                    afterNum = 0D;
                }
                current.setAfterNum(afterNum+current.getAddNum());
            }else if(typeOrder == 3 || typeOrder == 4){
                Double afterNum = lastOne.getAfterNum();
                if(afterNum == null){
                    afterNum = 0D;
                }
                current.setAfterNum(afterNum - current.getSubNum());
            }
        }


        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryInOutDetail.class,1,0).export(null,null,response,fis,records,"报表.xlsx", DBConstant.TABLE_REPOSITORY_INOUT_DETAIL.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('externalAccount:repository:inOutDetail:list')")
    public ResponseResult list(String searchStr, String searchStartDate, String searchEndDate,String searchField,String searchStatus) {
        if(StringUtils.isBlank(searchStartDate) || StringUtils.isBlank(searchEndDate)){
            return ResponseResult.fail("时间区间不能为空");
        }

        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
             if (searchField.equals("materialName")) {
                queryField = "material_name";
            }
             else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        searchStatusList.add(-1L);

        LocalDate startDate = LocalDate.parse(searchStartDate);
        LocalDate endDate = LocalDate.parse(searchEndDate);
        Page<RepositoryInOutDetail> page = externalAccountRepositoryInOutDetailMapper.page(getPage(), new QueryWrapper<RepositoryBuyinDocument>().
                like(StrUtil.isNotBlank(searchStr)
                        && StrUtil.isNotBlank(searchField), queryField, searchStr)
                .in("status",searchStatusList), startDate, endDate);
        List<RepositoryInOutDetail> records = page.getRecords();
        for (int i = 0; i < records.size(); i++) {
            RepositoryInOutDetail current = records.get(i);
            Integer typeOrder = current.getTypeOrder();
            if(typeOrder == 1){// 1:期初，2：采购入库，3:生产领料，4：采购退料，5：生产退料，6：盘点
                current.setAfterNum(current.getStartNum());
                continue;
            }
            RepositoryInOutDetail lastOne = records.get(i - 1); //TODO 分页情况下，点击其他页存在BUG：该分页第一个不是期初，该行报错
            if(typeOrder == 2 || typeOrder == 5 || typeOrder == 6){
                Double afterNum = lastOne.getAfterNum();
                if(afterNum == null){
                    afterNum = 0D;
                }
                current.setAfterNum(afterNum+current.getAddNum());
            }else if(typeOrder == 3 || typeOrder == 4){
                Double afterNum = lastOne.getAfterNum();
                if(afterNum == null){
                    afterNum = 0D;
                }
                current.setAfterNum(afterNum - current.getSubNum());
            }
        }
        return ResponseResult.succ(page);
    }


}
