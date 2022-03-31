package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-03-26
 */
@RestController
@RequestMapping("/produce/orderMaterialProgress")
@Slf4j
public class ProduceOrderMaterialProgressController extends BaseController {

    public static void main(String[] args) {
        BigDecimal preparedNum = new BigDecimal("1").add(new BigDecimal("2"));
        System.out.println(preparedNum);
    }

    @GetMapping("/queryByComplementId")
    @PreAuthorize("hasAuthority('order:complementPrepare:list')")
    public ResponseResult queryByComplementId(Long id ) {
        ProduceOrderMaterialProgress progress = produceOrderMaterialProgressService.getById(id);
        BaseMaterial bm = baseMaterialService.getById(progress.getMaterialId());
        progress.setMaterialName(bm.getName());
        return ResponseResult.succ(progress);
    }

    /**
     * 查看进度表信息
     */
    @PostMapping("/complementList")
    @PreAuthorize("hasAuthority('order:complementPrepare:list')")
    public ResponseResult list( String searchField, @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceOrderMaterialProgress> pageData = null;
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


        pageData = produceOrderMaterialProgressService.complementInnerQueryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);

        return ResponseResult.succ(pageData);
    }

    /***
     *  补数新增，修改
     * @param principal
     * @param materialProgresses
     * @return
     */
    @PostMapping("/complementSave")
    @PreAuthorize("hasAuthority('order:complementPrepare:save')")
    public ResponseResult complementSave(Principal principal, @Validated @RequestBody ProduceOrderMaterialProgress materialProgresses) {
        LocalDateTime now = LocalDateTime.now();

        try {

            if (materialProgresses.getId() == null) {
                materialProgresses.setCreated(now);
                materialProgresses.setUpdated(now);
                materialProgresses.setCreatedUser(principal.getName());
                materialProgresses.setUpdatedUser(principal.getName());
                materialProgresses.setPreparedNum(materialProgresses.getAddNum());
                if(Double.valueOf(Double.valueOf(materialProgresses.getAddNum())) < 0.0D){
                    throw new RuntimeException("备料数目不能为负数");
                }
                produceOrderMaterialProgressService.save(materialProgresses);
            } else {
                ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getById(materialProgresses.getId());
                BigDecimal preparedNum = new BigDecimal(old.getPreparedNum()==null?"0":old.getPreparedNum()).add(new BigDecimal(materialProgresses.getAddNum()));

                materialProgresses.setPreparedNum(preparedNum.toString());
                if(Double.valueOf(preparedNum.doubleValue()) < 0.0D){
                    throw new RuntimeException("备料数目不能为负数");
                }
                UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, materialProgresses.getPreparedNum())
                        .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                        .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                        .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMMENT_FIELDNAME,materialProgresses.getComment())
                        .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ID_FIELDNAME, old.getId());
                produceOrderMaterialProgressService.update(updateW);
            }
            return ResponseResult.succ("备料成功");
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


    /**
     * 查看进度表信息
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('dataAnalysis:orderProgress:list')")
    public ResponseResult list( String searchField, String searchStatus, String searchStatus2,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceOrderMaterialProgress> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("productNum")) {
                queryField = "product_num";
            }
            else if (searchField.equals("productBrand")) {
                queryField = "product_brand";

            }
            else if (searchField.equals("materialName")) {
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
                    if (oneField.equals("productNum")) {
                        theQueryField = "product_num";
                    }
                    else if (oneField.equals("productBrand")) {
                        theQueryField = "product_brand";

                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            return ResponseResult.fail("状态不能为空");
        }
        List<Long> searchStatusList2 = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus2)){
            String[] split = searchStatus2.split(",");
            for (String statusVal : split){
                searchStatusList2.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList2.size() == 0){
            return ResponseResult.fail("备料状态不能为空");
        }

        pageData = produceOrderMaterialProgressService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,searchStatusList2,queryMap);



        return ResponseResult.succ(pageData);
    }

    /**
     * 新增入库
     */
    @PostMapping("/save")
    @Transactional
    public ResponseResult save(Principal principal,Long orderId, @Validated @RequestBody List<ProduceOrderMaterialProgress> materialProgresses) {
        LocalDateTime now = LocalDateTime.now();
        try {

            for (ProduceOrderMaterialProgress process : materialProgresses) {
//                if((process.getAddNum() == null || Double.valueOf(process.getAddNum()) == 0 ) &&( process.getComment()==null || process.getComment().isEmpty())){
//                    continue;
//                }
                process.setOrderId(orderId);
                ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getByOrderIdAndMaterialId(orderId, process.getMaterialId());

                if (old == null) {
                    if(Double.valueOf(process.getAddNum()) < 0.0D){
                        throw new RuntimeException("备料数目不能为负数");
                    }
                    process.setCreated(now);
                    process.setUpdated(now);
                    process.setCreatedUser(principal.getName());
                    process.setUpdatedUser(principal.getName());
                    process.setPreparedNum(process.getAddNum());
                    double thePercent = Double.valueOf(process.getPreparedNum())*100 / Double.valueOf(process.getCalNum());
                    process.setProgressPercent((int)thePercent);
                    produceOrderMaterialProgressService.save(process);
                } else {
                    process.setUpdated(now);
                    process.setUpdatedUser(principal.getName());
                    BigDecimal preparedNum = new BigDecimal(old.getPreparedNum()).add(new BigDecimal(process.getAddNum()));
                    if(Double.valueOf(preparedNum.doubleValue()) < 0.0D){
                        throw new RuntimeException("备料数目不能为负数");
                    }

                    process.setPreparedNum(preparedNum.toString());
                    double thePercent = Double.valueOf(process.getPreparedNum())*100 / Double.valueOf(process.getCalNum());
                    process.setProgressPercent((int)thePercent);

                    UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                    updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, process.getPreparedNum())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PROGRESS_PERCENT_NUM_FIELDNAME,process.getProgressPercent())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMMENT_FIELDNAME,process.getComment())
                            .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME, orderId)
                            .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME, old.getMaterialId());

                    produceOrderMaterialProgressService.update(updateW);
                }
            }
            // 判断该物料是否全部进度已齐？是则修改订单的进度状态
            boolean flag = produceOrderMaterialProgressService.isPreparedByOrderId(orderId);
            if(flag){
                orderProductOrderService.updatePrepared(orderId,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_0);
            }else{
                orderProductOrderService.updatePrepared(orderId,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
            }
            return ResponseResult.succ("备料成功");
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    @GetMapping("/getByOrderId")
    public ResponseResult getByOrderId(Principal principal, Long orderId)throws Exception {

        List<ProduceOrderMaterialProgress> lists = produceOrderMaterialProgressService.listByOrderId(orderId);

        // 根据物料编码返回
        Map<String, String> theMaterialIdAndPreparedNum = new HashMap<>();
        for (ProduceOrderMaterialProgress obj : lists){
            theMaterialIdAndPreparedNum.put(obj.getMaterialId(),obj.getPreparedNum());
        }
        return ResponseResult.succ(theMaterialIdAndPreparedNum);
    }


}
