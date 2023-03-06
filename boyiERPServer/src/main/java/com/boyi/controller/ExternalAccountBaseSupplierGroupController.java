package com.boyi.controller;


import com.boyi.common.dto.BaseSupplierGroupDto;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ExternalAccountBaseSupplierGroup;
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
 * @since 2023-03-04
 */
@RestController
@RequestMapping("/externalAccount/baseData/supplierGroup")
@Slf4j
public class ExternalAccountBaseSupplierGroupController extends BaseController {

    /**
     *  新增供应商分组
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult save(@Validated @RequestBody ExternalAccountBaseSupplierGroup ExternalAccountBaseSupplierGroup) {
        LocalDateTime now = LocalDateTime.now();
        ExternalAccountBaseSupplierGroup.setCreated(now);
        ExternalAccountBaseSupplierGroup.setUpdated(now);

        externalAccountBaseSupplierGroupService.save(ExternalAccountBaseSupplierGroup);
        return ResponseResult.succ("新增成功");
    }

    /**
     *  修改供应商分组
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult update(Principal principal, @Validated @RequestBody ExternalAccountBaseSupplierGroup ExternalAccountBaseSupplierGroup) {
        ExternalAccountBaseSupplierGroup.setUpdated(LocalDateTime.now());
        externalAccountBaseSupplierGroupService.updateById(ExternalAccountBaseSupplierGroup);
        log.info("操作人:[{}],修改菜单,update content:{}",principal.getName(),ExternalAccountBaseSupplierGroup);
        return ResponseResult.succ("编辑成功");
    }


    /**
     *  根据id 删除菜单
     */
    @GetMapping("/delById")
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult delById(Principal principal,Long id,String groupCode) {
        List<ExternalAccountBaseSupplierGroup> groups = externalAccountBaseSupplierGroupService.getListByParentId(id);
        if (groups!=null && groups.size() > 0) {
            return ResponseResult.fail("请先删除子分组");
        }

        // 假如该子分组下面有物料信息，则不能删除
        Integer count = externalAccountBaseSupplierService.countByGroupCode(groupCode);
        if(count > 0){
            return ResponseResult.fail("该分组有供应商信息，请先删除!");
        }


        boolean flag = externalAccountBaseSupplierGroupService.removeById(id);
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
    @PreAuthorize("hasAuthority('externalAccount:baseData:supplier:list')")
    public ResponseResult listValide() {
        List<ExternalAccountBaseSupplierGroup> list = externalAccountBaseSupplierGroupService.list();

        List<BaseSupplierGroupDto> BaseSupplierGroupDtos = buildTree(list);

        return ResponseResult.succ(BaseSupplierGroupDtos);
    }

    /**
     * 把list转成树形结构的数据
     */
    public List<BaseSupplierGroupDto> buildTree(List<ExternalAccountBaseSupplierGroup> groups) {

        List<BaseSupplierGroupDto> returnGroups = new ArrayList<>();
        for (ExternalAccountBaseSupplierGroup materialGroup : groups) {
            BaseSupplierGroupDto dto = new BaseSupplierGroupDto();
            dto.setParentId(materialGroup.getParentId());
            dto.setId(materialGroup.getId());
            dto.setCode(materialGroup.getCode());
            dto.setLabel(materialGroup.getCode() + "(" + materialGroup.getName() + ")");
            dto.setName(materialGroup.getName());
            // 先寻找各自的孩子
            for (ExternalAccountBaseSupplierGroup e : groups) {
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
