package com.boyi.controller;

import cn.hutool.core.map.MapUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.SysUser;
import com.boyi.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class TestController extends BaseController {
    @Autowired
    SysUserService sysUserService;

    @RequestMapping("/test")
    public Object test() {
        try {
            List<SysUser> list = sysUserService.list();
            log.info("test{}", list);
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @RequestMapping("/test/pass")
    public Object testPass() {
        try {

            // 密码加密
            String pass = bCryptPasswordEncoder.encode("111111");
            // 密码验证
            boolean matches = bCryptPasswordEncoder.matches("111111", pass);
            return ResponseResult.succ(
                    MapUtil.builder()
                            .put("pass", pass)
                            .put("marches", matches)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
