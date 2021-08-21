package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.SysMenuDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseUnit;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysRoleMenu;
import com.boyi.entity.SysUser;
import com.boyi.service.BaseUnitService;
import com.boyi.service.SysMenuService;
import com.boyi.service.SysRoleMenuService;
import com.boyi.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 基础模块-计量单位管理 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-19
 */
@Slf4j
@RestController
@RequestMapping("/baseData/unit")
public class BaseUnitController extends BaseController {

    @Autowired
    BaseUnitService baseUnitService;


    /**
     * 获取计量单位全部数据
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult getSearchAllData() {
        List<BaseUnit> baseUnits = baseUnitService.list();

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        baseUnits.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getName() ).put("code", obj.getCode()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 获取计量单位全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult list() {
        List<BaseUnit> baseUnits = baseUnitService.list();
        return ResponseResult.succ(baseUnits);
    }

    /**
     * 获取有效的计量单位全部数据
     */
    @PostMapping("/listValide")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult listValide() {
        List<BaseUnit> baseUnits = baseUnitService.listValid();
        return ResponseResult.succ(baseUnits);
    }

    /**
     * 新增计量单位
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:unit:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseUnit baseUnit) {
        final String encoding = System.getProperty("file.encoding");
        System.out.println(encoding);
        LocalDateTime now = LocalDateTime.now();
        baseUnit.setCreated(now);
        baseUnit.setUpdated(now);

        baseUnitService.save(baseUnit);
        log.info("操作人:[{}],新增内容:{}", principal.getName(), baseUnit);
        return ResponseResult.succ("新增成功");
    }

    /**
     * 根据id 查询计量单位
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:unit:list')")
    public ResponseResult queryById(Long id) {
        BaseUnit baseUnit = baseUnitService.getById(id);
        return ResponseResult.succ(baseUnit);
    }

    /**
     * 修改计量单位
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:unit:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseUnit baseUnit) {
        baseUnit.setUpdated(LocalDateTime.now());
        baseUnitService.updateById(baseUnit);
        log.info("操作人:[{}],修改计量单位,update content:{}", principal.getName(), baseUnit);
        return ResponseResult.succ("编辑成功");
    }

    /**
     * 根据id 删除计量单位
     */
    @GetMapping("/delById")
    @PreAuthorize("hasAuthority('baseData:unit:del')")
    public ResponseResult delById(Principal principal, Long id) {

        boolean flag = baseUnitService.removeById(id);
        if (flag) {
            log.info("操作人:[{}],删除计量单位id:{}", principal.getName(), id);
            return ResponseResult.succ("删除成功");
        } else {
            return ResponseResult.fail("删除失败");

        }
    }
}
