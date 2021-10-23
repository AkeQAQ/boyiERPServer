package com.boyi.controller;


import com.alibaba.fastjson.JSONObject;
import com.boyi.common.utils.JwtUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dataAnalysis/online")
public class AnalysisOnlineController extends BaseController {

    /**
     * 获取在线人数
     */
    @GetMapping("/onlineNum")
    @PreAuthorize("hasAuthority('dataAnalysis:online:list')")
    public ResponseResult onlineNum(Principal principal) {
        int size = HeartController.onlineMap.size();
        JSONObject json = new JSONObject();
        ArrayList<String> onlineUserName = new ArrayList<>();
        json.put("onlineNum",size);
        for (Map.Entry<String,Long> entry : HeartController.onlineMap.entrySet()){
            String key = entry.getKey();
            String[] split = key.split(HeartController.KEY_SPERATOR);
            onlineUserName.add(split[0]);
        }
        json.put("onlineDetail",onlineUserName);
        return ResponseResult.succ(json);
    }

}
