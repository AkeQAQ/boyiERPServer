package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.BaseMaterialSameGroup;
import com.boyi.entity.BaseMaterialSameGroupDetail;
import com.boyi.entity.ProduceOrderMaterialProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-11-14
 */
@RestController
@RequestMapping("/baseData/materialSameGroup")
@Slf4j
public class BaseMaterialSameGroupController extends BaseController {


    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:materialSameGroup:list')")
    public ResponseResult queryById(Long id ) {
        BaseMaterialSameGroup group = baseMaterialSameGroupService.getById(id);
        List<BaseMaterialSameGroupDetail> details = baseMaterialSameGroupDetailService.listByGroupId(group.getId());
        for(BaseMaterialSameGroupDetail detail : details){
            BaseMaterial bm = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(bm.getName());
        }
        group.setDetails(details);
        return ResponseResult.succ(group);
    }

    /**
     * 查看进度表信息
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('baseData:materialSameGroup:list')")
    public ResponseResult list( String searchField, @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<BaseMaterialSameGroup> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("materialName")) {
                queryField = "material_name";
            }
            else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }
        Map<String, String> queryMap = new HashMap<>();
        if(manySearchArr!=null && manySearchArr.size() > 0){
            for (int i = 0; i < manySearchArr.size(); i++) {
                Map<String, String> theOneSearch = manySearchArr.get(i);
                String oneField = theOneSearch.get("selectField");
                String oneStr = theOneSearch.get("searchStr");
                String theQueryField = null;
                if (StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("materialName")) {
                        theQueryField = "material_name";
                    }else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);


//        pageData = produceOrderMaterialProgressService.complementInnerQueryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);
        pageData = baseMaterialSameGroupService.queryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);

        return ResponseResult.succ(pageData);
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:materialSameGroup:del')")
    public ResponseResult del(@RequestBody Long[] ids) throws Exception{
        try {
            List<BaseMaterialSameGroup> dels = baseMaterialSameGroupService.listByIds(Arrays.asList(ids));
            for (BaseMaterialSameGroup group : dels){
                baseMaterialSameGroupDetailService.removeByGroupId(group.getId());
            }

            baseMaterialSameGroupService.removeByIds(Arrays.asList(ids));

            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /***
     *
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:materialSameGroup:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseMaterialSameGroup baseMaterialSameGroup) {
        LocalDateTime now = LocalDateTime.now();
        try {
            if(baseMaterialSameGroup.getDetails().size()<2){
                throw new RuntimeException("替代物料至少要存在2个");
            }

            if (baseMaterialSameGroup.getId() == null) {
                baseMaterialSameGroup.setCreated(now);
                baseMaterialSameGroup.setUpdated(now);
                baseMaterialSameGroup.setCreatedUser(principal.getName());
                baseMaterialSameGroup.setUpdateUser(principal.getName());
                baseMaterialSameGroup.setStatus(DBConstant.TABLE_BASE_MATERIAL_SAME_GROUP.STATUS_FIELDVALUE_1);
                baseMaterialSameGroupService.save(baseMaterialSameGroup);


                for(BaseMaterialSameGroupDetail detail : baseMaterialSameGroup.getDetails()){
                    detail.setGroupId(baseMaterialSameGroup.getId());
                    detail.setCreated(now);
                    detail.setUpdated(now);
                    detail.setCreatedUser(principal.getName());
                    detail.setUpdateUser(principal.getName());
                }
                baseMaterialSameGroupDetailService.saveBatch(baseMaterialSameGroup.getDetails());
            } else {
                BaseMaterialSameGroup old = baseMaterialSameGroupService.getById(baseMaterialSameGroup.getId());
                baseMaterialSameGroupDetailService.removeByGroupId(old.getId());

                for(BaseMaterialSameGroupDetail detail : baseMaterialSameGroup.getDetails()){
                    detail.setGroupId(old.getId());
                    detail.setCreated(now);
                    detail.setUpdated(now);
                    detail.setCreatedUser(principal.getName());
                    detail.setUpdateUser(principal.getName());
                }
                baseMaterialSameGroupDetailService.saveBatch(baseMaterialSameGroup.getDetails());

            }
            return ResponseResult.succ("成功");
        }catch (DuplicateKeyException e) {
            if(baseMaterialSameGroup.getId()==null){
                throw new RuntimeException("组名不能重复");
            }else{
                throw new RuntimeException("一个物料只能存在一个组中，请确认!");
            }
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


}
