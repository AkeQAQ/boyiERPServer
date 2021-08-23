package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.PassWordDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.SysUser;
import com.boyi.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-15
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    /**
     * 获取当前用户信息
     * Principal 标识注入的当前用户
     */
    @GetMapping("/getUserInfo")
    @PreAuthorize("hasAuthority('sysManage:user:list')")
    public ResponseResult getUserInfo(Principal principal) {
        String username = principal.getName();

        SysUser sysUser = sysUserService.getByUsername(username);   // ROLE_Admin,sysManage:user:save

        Map<Object, Object> returnMap = MapUtil.builder().put("id", sysUser.getId()).put("userName", sysUser.getUserName()).map();
        return ResponseResult.succ(returnMap);
    }

    /**
     * 修改密码
     * Principal 标识注入的当前用户
     */
    @PostMapping("/updatePassword")
    public ResponseResult updatePassword(@Validated @RequestBody PassWordDto passWordDto, Principal principal) {
        SysUser sysUser = sysUserService.getByUsername(principal.getName());
        boolean matches = passwordEncoder.matches(passWordDto.getCurrentPass(), sysUser.getPassword());// 匹配密码
        if(!matches){
            return ResponseResult.fail("旧密码不正确");
        }
        sysUser.setPassword(passwordEncoder.encode(passWordDto.getPass()));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);
        return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"修改密码成功",null);
    }

    /**
     * 获取用户 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sysManage:user:list')")
    public ResponseResult list(String searchUserName) {

        Page<SysUser> pageData = sysUserService.page(getPage(), new QueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(searchUserName), "user_name", searchUserName));

        pageData.getRecords().forEach(u -> {
            u.setSysRoles(sysRoleService.listRolesByUserId(u.getId()));
        });

        return ResponseResult.succ(pageData);
    }
    /**
     * 获取用户 的全部角色
     */
    @GetMapping("/queryRolesByUserId")
    @PreAuthorize("hasAuthority('sysManage:user:list')")
    public ResponseResult queryRolesByUserId(Long id) {
        List<Long> userRolesIds = sysUserRoleService.getUserRolesIds(id);

        return ResponseResult.succ(userRolesIds.toArray());
    }


    /**
     *  新增用户
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sysManage:user:save')")
    public ResponseResult save(@Validated @RequestBody SysUser sysUser) {
        LocalDateTime now = LocalDateTime.now();
        sysUser.setCreated(now);
        sysUser.setUpdated(now);
        sysUser.setPassword(passwordEncoder.encode("888888"));
        sysUserService.save(sysUser);
        return ResponseResult.succ("新增成功");
    }
    /**
     *  查询用户
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('sysManage:user:list')")
    public ResponseResult queryById(Long id) {
        SysUser sysUser = sysUserService.getById(id);
        return ResponseResult.succ(sysUser);
    }


    /**
     *  修改用户
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sysManage:user:update')")
    public ResponseResult update(@Validated @RequestBody SysUser sysUser) {
        sysUser.setUpdated(LocalDateTime.now());
        try {
            sysUserService.updateById(sysUser);
            return ResponseResult.succ("编辑成功");
        }catch (DuplicateKeyException e){
            return ResponseResult.fail("用户名重复!");
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('sysManage:user:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        sysUserService.removeByIds(Arrays.asList(ids));
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().in("user_id", ids));

        return ResponseResult.succ("删除成功");
    }

    @Transactional
    @PostMapping("/authority")
    @PreAuthorize("hasAuthority('sysManage:user:authority')")
    public ResponseResult rolePerm(Long userId, @RequestBody Long[] roleIds) {

        List<SysUserRole> userRoles = new ArrayList<>();

        Arrays.stream(roleIds).forEach(r -> {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(r);
            sysUserRole.setUserId(userId);

            userRoles.add(sysUserRole);
        });

        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().eq(DBConstant.TABLE_USER.USER_ID_FIELDNAME, userId));
        sysUserRoleService.saveBatch(userRoles);

        SysUser sysUser = sysUserService.getById(userId);
        // 删除缓存
        sysUserService.clearUserAuthorityInfo(sysUser.getUserName());

        return ResponseResult.succ("修改成功");
    }

    @GetMapping("/resetPass")
    @PreAuthorize("hasAuthority('sysManage:user:resetPass')")
    public ResponseResult resetPass( Long id) {

        SysUser sysUser = sysUserService.getById(id);

        sysUser.setPassword(passwordEncoder.encode("888888"));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);
        return ResponseResult.succ("重置密码成功");
    }
}
