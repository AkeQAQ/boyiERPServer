package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.BaseMaterialGroupDto;
import com.boyi.common.dto.SysMenuDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseMaterialGroup;
import com.boyi.entity.SysMenu;
import com.boyi.entity.SysRoleMenu;
import com.boyi.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 基础模块-物料分组表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-20
 */
@Slf4j
@RestController
@RequestMapping("/baseData/materialGroup")
public class BaseMaterialGroupController extends BaseController {

    /**
     *  新增物料分组
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:material:save')")
    public ResponseResult save(@Validated @RequestBody BaseMaterialGroup baseMaterialGroup) {
        LocalDateTime now = LocalDateTime.now();
        baseMaterialGroup.setCreated(now);
        baseMaterialGroup.setUpdated(now);

        baseMaterialGroupService.save(baseMaterialGroup);
        return ResponseResult.succ("新增成功");
    }

    /**
     *  修改物料分组
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:material:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseMaterialGroup baseMaterialGroup) {

        baseMaterialGroup.setUpdated(LocalDateTime.now());
        baseMaterialGroupService.updateById(baseMaterialGroup);
        log.info("操作人:[{}],修改菜单,update content:{}",principal.getName(),baseMaterialGroup);
        return ResponseResult.succ("编辑成功");
    }


    /**
     *  根据id 删除菜单
     */
    @GetMapping("/delById")
    @PreAuthorize("hasAuthority('baseData:material:del')")
    public ResponseResult delById(Principal principal,Long id,String groupCode) {
        List<BaseMaterialGroup> groups = baseMaterialGroupService.getListByParentId(id);
        if (groups!=null && groups.size() > 0) {
            return ResponseResult.fail("请先删除子分组");
        }

        // 假如该子分组下面有物料信息，则不能删除
        Integer count = baseMaterialService.countByGroupCode(groupCode);
        if(count > 0){
            return ResponseResult.fail("该分组有物料信息，请先删除!");
        }


        boolean flag = baseMaterialGroupService.removeById(id);
        if(flag){
            log.info("操作人:[{}],删除物料分组id:{}",principal.getName(), id);
            return ResponseResult.succ("删除成功");
        }else {
            return ResponseResult.fail("删除失败");

        }
    }

    /**
     * 获取分组全部有效数据
     */
    @PostMapping("/listValide")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult listValide() {
        List<BaseMaterialGroup> list = baseMaterialGroupService.list();

        List<BaseMaterialGroupDto> baseMaterialGroupDtos = buildTree(list);

        return ResponseResult.succ(baseMaterialGroupDtos);
    }

    /**
     * 把list转成树形结构的数据
     */
    public List<BaseMaterialGroupDto> buildTree(List<BaseMaterialGroup> groups) {

        List<BaseMaterialGroupDto> returnGroups = new ArrayList<>();
        for (BaseMaterialGroup materialGroup : groups) {
            BaseMaterialGroupDto dto = new BaseMaterialGroupDto();
            dto.setParentId(materialGroup.getParentId());
            dto.setId(materialGroup.getId());
            dto.setCode(materialGroup.getCode());
            dto.setLabel(materialGroup.getCode() + "(" + materialGroup.getName() + ")");
            dto.setName(materialGroup.getName());
            // 先寻找各自的孩子
            for (BaseMaterialGroup e : groups) {
                if (e.getParentId() == materialGroup.getId()) {
                    BaseMaterialGroupDto child = new BaseMaterialGroupDto();
                    child.setParentId(e.getParentId());
                    child.setId(e.getId());
                    child.setCode(e.getCode());
                    child.setLabel(e.getCode() + "(" + e.getName() + ")");
                    child.setParentCode(dto.getCode());
                    child.setName(e.getName());
                    dto.setName(materialGroup.getName());
                    dto.getChildren().add(child);
                }
            }
            if (materialGroup.getParentId() == 0L) {
                dto.setParentCode("全部");
                returnGroups.add(dto);
            }
        }

        BaseMaterialGroupDto dto = new BaseMaterialGroupDto();
        dto.setId(0L);
        dto.setCode("全部");
        dto.setLabel("全部");
        dto.setChildren(returnGroups);

        List<BaseMaterialGroupDto> returnRootGroups = new ArrayList<>();
        returnRootGroups.add(dto);
        return returnRootGroups;
    }
}
