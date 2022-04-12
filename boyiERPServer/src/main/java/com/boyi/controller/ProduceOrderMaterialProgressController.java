package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
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
import java.util.*;

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

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:complementPrepare:del')")
    public ResponseResult del(@RequestBody Long[] ids) throws Exception{
        try {
            List<ProduceOrderMaterialProgress> dels = produceOrderMaterialProgressService.listByIds(Arrays.asList(ids));
            for (ProduceOrderMaterialProgress progress : dels){
                if(Double.valueOf(progress.getInNum()) > 0.0D){
                    return ResponseResult.fail("已存在入库，不能删除");
                }
            }

            boolean flag = produceOrderMaterialProgressService.removeByIds(Arrays.asList(ids));

            log.info("删除补数备料进度表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品订单删除失败");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("complementValid")
    @PreAuthorize("hasAuthority('order:complementPrepare:valid')")
    public ResponseResult complementReValid(Long id) throws Exception{
        try {
            ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getById(id);
            if(!old.getComplementStatus().equals(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_1)){
                return ResponseResult.fail("补数备料状态不对，已修改，请刷新!");
            }

            produceOrderMaterialProgressService.updateStatus(id,DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_0);
            return ResponseResult.succ("补数备料审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("complementReValid")
    @PreAuthorize("hasAuthority('order:complementPrepare:valid')")
    public ResponseResult complementValid(Long id) throws Exception{
        try {
            ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getById(id);
            if(!old.getComplementStatus().equals(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("补数备料状态不对，已修改，请刷新!");
            }

            produceOrderMaterialProgressService.updateStatus(id,DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_1);
            return ResponseResult.succ("补数备料反审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
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
                materialProgresses.setComplementStatus(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_1);
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
                // 假如比已入库数量少，也不行
                if(Double.valueOf(preparedNum.doubleValue()) < Double.valueOf(old.getInNum())){
                    throw new RuntimeException("备料数目不能小于已入库数目");
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
     * 新增,修改备料
     */
    @PostMapping("/saveBatch")
    @Transactional
    public ResponseResult saveBatch(Principal principal,Long[] orderIds, @Validated @RequestBody List<ProduceOrderMaterialProgress> materialProgresses) {
        LocalDateTime now = LocalDateTime.now();
        try {
            // 订单排序
                for (ProduceOrderMaterialProgress materialAndAddNumMsg : materialProgresses) {
                    String currentMaterialId = materialAndAddNumMsg.getMaterialId();

                    List<Map<String, Object>> details = materialAndAddNumMsg.getDetails();
                    String addNum = materialAndAddNumMsg.getAddNums();
                    if(addNum == null || StringUtils.isBlank(addNum)){
                        continue;
                    }
                    Double addNumDouble = Double.valueOf(addNum);
                    if(addNumDouble < 0.0D){
                        throw new RuntimeException("备料数目不能为负数");
                    }

                    // 该物料，有这几个订单关联
                    for (int i = 0; i < details.size(); i++) {
                        Map<String, Object> theOrder = details.get(i);
                        Long orderId = ((Number) theOrder.get("orderId")).longValue();
                        Double calNum = Double.valueOf( theOrder.get("calNum").toString());// 应备数量
                        Double preparedNum = Double.valueOf(theOrder.get("preparedNum").toString()) ; // 已备数量

                        ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getByOrderIdAndMaterialId(orderId, currentMaterialId);
                        if (old == null) {

                            ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                            progress.setOrderId(orderId);
                            progress.setMaterialId(currentMaterialId);
                            progress.setCreated(now);
                            progress.setUpdated(now);
                            progress.setCreatedUser(principal.getName());
                            progress.setUpdatedUser(principal.getName());
                            progress.setCalNum(calNum+""); // 设置传进来的计算数量

                            // 假如备料比一个物料计算的多，则补到该物料所需，剩下的循环继续加
                            if(addNumDouble > calNum){
                                // 假如是最后一个的话，全部数量加到该订单物料上
                                if(i == details.size() -1){
                                    progress.setPreparedNum(addNumDouble+"");
                                    addNumDouble = 0.0d;
                                }else{
                                    progress.setPreparedNum(calNum+"");
                                    addNumDouble = BigDecimalUtil.sub(addNumDouble+"",calNum+"").doubleValue();
                                }

                            }else{
                                progress.setPreparedNum(addNumDouble+"");
                                addNumDouble=0.0d;
                            }

                            double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();
                            progress.setProgressPercent((int)thePercent);
                            produceOrderMaterialProgressService.save(progress);
                        } else {
                            // 假如老的存在，则查询需要补多少，补了之后循环后续

                            ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                            progress.setUpdated(now);
                            progress.setUpdatedUser(principal.getName());
                            progress.setOrderId(orderId);
                            progress.setMaterialId(currentMaterialId);
                            progress.setCalNum(calNum+""); // 设置传进来的计算数量

                            double needNum = BigDecimalUtil.sub(calNum, preparedNum).doubleValue();// 仍需 补的数量

                            if(addNumDouble > needNum){
                                // 假如是最后一个的话，全部数量加到该订单物料上
                                if(i == details.size() -1){
                                    progress.setPreparedNum(BigDecimalUtil.add(old.getPreparedNum(),addNumDouble+"").toString()); // 补满所需数量
                                    addNumDouble = 0.0d;
                                }else{
                                    progress.setPreparedNum(calNum+""); // 补满所需数量
                                    addNumDouble = BigDecimalUtil.sub(addNumDouble,needNum).doubleValue();
                                }

                            }else{
                                progress.setPreparedNum(BigDecimalUtil.add(preparedNum+"",addNumDouble+"").toString());
                                addNumDouble=0.0d;
                            }

                            double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();

                            progress.setProgressPercent((int)thePercent);

                            UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                            updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, progress.getPreparedNum())
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PROGRESS_PERCENT_NUM_FIELDNAME,progress.getProgressPercent())
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                                    .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME, orderId)
                                    .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME, old.getMaterialId());

                            produceOrderMaterialProgressService.update(updateW);
                        }

                    }

            }

            return ResponseResult.succ("备料成功");
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

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

                    double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(process.getPreparedNum()), 100).doubleValue(),Double.valueOf(process.getCalNum())).doubleValue();
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
                    double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(process.getPreparedNum()), 100).doubleValue(),Double.valueOf(process.getCalNum())).doubleValue();

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
            /*boolean flag = produceOrderMaterialProgressService.isPreparedByOrderId(orderId);
            if(flag){
                orderProductOrderService.updatePrepared(orderId,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_0);
            }else{
                orderProductOrderService.updatePrepared(orderId,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
            }*/
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