package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.dto.BaseSupplierGroupDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplierGroup;
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
 * 基础模块-供应商分组表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-22
 */
@Slf4j
@RestController
@RequestMapping("/baseData/supplierGroup")
public class BaseSupplierGroupController extends BaseController {

    /**
     *  新增供应商分组
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:supplier:save')")
    public ResponseResult save(@Validated @RequestBody BaseSupplierGroup BaseSupplierGroup) {
        LocalDateTime now = LocalDateTime.now();
        BaseSupplierGroup.setCreated(now);
        BaseSupplierGroup.setUpdated(now);

        baseSupplierGroupService.save(BaseSupplierGroup);
        return ResponseResult.succ("新增成功");
    }

    /**
     *  修改供应商分组
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:supplier:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseSupplierGroup BaseSupplierGroup) {
        BaseSupplierGroup.setUpdated(LocalDateTime.now());
        baseSupplierGroupService.updateById(BaseSupplierGroup);
        log.info("操作人:[{}],修改菜单,update content:{}",principal.getName(),BaseSupplierGroup);
        return ResponseResult.succ("编辑成功");
    }


    /**
     *  根据id 删除菜单
     */
    @GetMapping("/delById")
    @PreAuthorize("hasAuthority('baseData:supplier:del')")
    public ResponseResult delById(Principal principal,Long id,String groupCode) {
        List<BaseSupplierGroup> groups = baseSupplierGroupService.getListByParentId(id);
        if (groups!=null && groups.size() > 0) {
            return ResponseResult.fail("请先删除子分组");
        }

        // 假如该子分组下面有物料信息，则不能删除
        Integer count = baseSupplierService.countByGroupCode(groupCode);
        if(count > 0){
            return ResponseResult.fail("该分组有供应商信息，请先删除!");
        }


        boolean flag = baseSupplierGroupService.removeById(id);
        if(flag){
            log.info("操作人:[{}],删除供应商分组id:{}",principal.getName(), id);
            return ResponseResult.succ("删除成功");
        }else {
            return ResponseResult.fail("删除失败");

        }
    }

    /**
     * 获取分组全部有效数据
     */
    @PostMapping("/listValide")
    @PreAuthorize("hasAuthority('baseData:supplier:list')")
    public ResponseResult listValide() {
        List<BaseSupplierGroup> list = baseSupplierGroupService.list();

        List<BaseSupplierGroupDto> BaseSupplierGroupDtos = buildTree(list);

        return ResponseResult.succ(BaseSupplierGroupDtos);
    }

    /**
     * 把list转成树形结构的数据
     */
    public List<BaseSupplierGroupDto> buildTree(List<BaseSupplierGroup> groups) {

        List<BaseSupplierGroupDto> returnGroups = new ArrayList<>();
        for (BaseSupplierGroup materialGroup : groups) {
            BaseSupplierGroupDto dto = new BaseSupplierGroupDto();
            dto.setParentId(materialGroup.getParentId());
            dto.setId(materialGroup.getId());
            dto.setCode(materialGroup.getCode());
            dto.setLabel(materialGroup.getCode() + "(" + materialGroup.getName() + ")");
            dto.setName(materialGroup.getName());
            // 先寻找各自的孩子
            for (BaseSupplierGroup e : groups) {
                if (e.getParentId() == materialGroup.getId()) {
                    BaseSupplierGroupDto child = new BaseSupplierGroupDto();
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

        BaseSupplierGroupDto dto = new BaseSupplierGroupDto();
        dto.setId(0L);
        dto.setCode("全部");
        dto.setLabel("全部");
        dto.setChildren(returnGroups);

        List<BaseSupplierGroupDto> returnRootGroups = new ArrayList<>();
        returnRootGroups.add(dto);
        return returnRootGroups;
    }
}
