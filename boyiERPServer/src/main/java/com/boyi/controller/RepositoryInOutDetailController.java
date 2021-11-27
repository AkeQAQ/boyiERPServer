package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.mapper.RepositoryInOutDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>
 * 物料收发明细 前端控制器
 * </p>
 *
 * @author sunke
 */
@Slf4j
@RestController
@RequestMapping("/repository/inOutDetail")
public class RepositoryInOutDetailController extends BaseController {
    @Autowired
    private RepositoryInOutDetailMapper repositoryInOutDetailMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:inOutDetail:list')")
    public ResponseResult list(String searchStr, String searchStartDate, String searchEndDate,String searchField,String searchStatus) {
        if(StringUtils.isBlank(searchStartDate) || StringUtils.isBlank(searchEndDate)){
            return ResponseResult.fail("时间区间不能为空");
        }

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
        Page<RepositoryInOutDetail> page = repositoryInOutDetailMapper.page(getPage(), new QueryWrapper<RepositoryBuyinDocument>().
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
