package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.utils.JwtUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseDepartment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/common/heart")
public class HeartController extends BaseController {

    @Autowired
    JwtUtils jwtUtils;

    public final static Map<String,Long> onlineMap = new HashMap<String,Long>();
    public final static String KEY_SPERATOR = "_";

    /**
     * 接收心跳的接口
     */
    @GetMapping("/sendHeart")
    public ResponseResult sendHeart(Principal principal, HttpServletRequest request) {
        String jwt = request.getHeader(jwtUtils.getHeader());
        long now = System.currentTimeMillis();
        String remoteIp = request.getRemoteAddr();
        onlineMap.put(remoteIp+":"+principal.getName()+KEY_SPERATOR+jwt,now);
        log.info("【心跳】:key:{},now time:{}",principal.getName()+"_"+jwt, new Date(now));
        return ResponseResult.succ("");
    }

}
