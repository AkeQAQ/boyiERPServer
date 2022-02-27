package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BuyMaterialSupplier;
import com.boyi.entity.ProduceReturnShoes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * @since 2022-02-22
 */
@RestController
@RequestMapping("/baseData/buyMaterialSupplier")
@Slf4j
public class BuyMaterialSupplierController extends BaseController {

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('baseData:buyMaterialSupplier:list')")
    public ResponseResult list(String searchField,  @RequestBody Map<String,Object> params) {

        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("materialName")) {
                queryField = "material_name";
            }
            else if (searchField.equals("supplierName")) {
                queryField = "supplier_name";

            }else if (searchField.equals("supplierMaterialId")) {
                queryField = "supplier_material_id";

            } else {
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
                    }
                    else if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";

                    }else if (oneField.equals("supplierMaterialId")) {
                        theQueryField = "supplier_material_id";

                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        Page<BuyMaterialSupplier> pageData = buyMaterialSupplierService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);

        return ResponseResult.succ(pageData);
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:buyMaterialSupplier:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BuyMaterialSupplier buyMaterialSupplier) {
        try{

            LocalDateTime now = LocalDateTime.now();
            buyMaterialSupplier.setCreated(now);
            buyMaterialSupplier.setUpdated(now);
            buyMaterialSupplier.setCreatedUser(principal.getName());

            // 判断是否已经存在该供应商，该供应商物料编码的记录，已经存在则代表已经有关联，不能插入
            BuyMaterialSupplier exist = buyMaterialSupplierService.isExist(buyMaterialSupplier.getSupplierId(),buyMaterialSupplier.getSupplierMaterialId());
            if(exist!=null){
                return ResponseResult.fail("该供应商，该供应商物料编码已经有对应的内部物料["+exist.getInnerMaterialId()+"]关联");
            }
            buyMaterialSupplierService.save(buyMaterialSupplier);

        }catch (DuplicateKeyException e){
            return ResponseResult.fail("请勿重复同物料，同供应商，同供应商物料编码！");
        }
        return ResponseResult.succ("新增成功");
    }
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:buyMaterialSupplier:list')")
    public ResponseResult queryById(Long id) {
        BuyMaterialSupplier buyMaterialSupplier = buyMaterialSupplierService.getById(id);
        return ResponseResult.succ(buyMaterialSupplier);
    }


    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:buyMaterialSupplier:update')")
    public ResponseResult update(Principal principal,@Validated @RequestBody BuyMaterialSupplier buyMaterialSupplier) {
        buyMaterialSupplier.setUpdated(LocalDateTime.now());
        buyMaterialSupplier.setUpdatedUser(principal.getName());


        // 判断是否已经存在该供应商，该供应商物料编码的记录，已经存在则代表已经有关联，不能插入
        BuyMaterialSupplier exist = buyMaterialSupplierService.isExist(buyMaterialSupplier.getSupplierId(),buyMaterialSupplier.getSupplierMaterialId());
        if(exist!=null){
            return ResponseResult.fail("该供应商，该供应商物料编码已经有对应的内部物料["+exist.getInnerMaterialId()+"]关联");
        }
        buyMaterialSupplierService.updateById(buyMaterialSupplier);
        return ResponseResult.succ("编辑成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:buyMaterialSupplier:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        buyMaterialSupplierService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
