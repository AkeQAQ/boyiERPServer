package com.boyi.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.AnalysisRequest;
import com.boyi.service.AnalysisRequestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-10-29
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController extends BaseController {

    /**
     * 获取请求方法耗时统计
     */
    @GetMapping("/requestCast")
//    @PreAuthorize("hasAuthority('dataAnalysis:manage')")
    public ResponseResult onlineNum(Principal principal) {

        List<AnalysisRequest> list = analysisRequestService.list(new QueryWrapper<AnalysisRequest>().select("class_method", "avg(cast) as cast").groupBy("class_method").orderByDesc("cast"));

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        for (AnalysisRequest obj : list){
            legendData.add(obj.getClassMethod());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getClassMethod());
            nameValue.put("value",obj.getCast());
            seriesData.add(nameValue);
        }
        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }
}
