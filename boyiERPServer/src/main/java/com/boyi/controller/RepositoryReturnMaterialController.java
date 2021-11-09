package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 仓库模块-退料模块 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Slf4j
@RestController
@RequestMapping("/repository/returnMaterial")
public class RepositoryReturnMaterialController extends BaseController {

    @Value("${poi.repositoryReturnMaterialDemoPath}")
    private String poiDemoPath;

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('repository:returnMaterial:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('repository:returnMaterial:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }


    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:returnMaterial:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        try {

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryReturnMaterialDetail> details = repositoryReturnMaterialDetailService.listByDocumentId(ids[0]);

        Map<String, Double> map = new HashMap<>();// 一个物料，需要减少的数目
        // 1. 遍历获取一个物料要添加的数目。
        for (RepositoryReturnMaterialDetail detail : details) {
            Double materialNum = map.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            map.put(detail.getMaterialId(),materialNum+detail.getNum());
        }

        // 校验库存
        repositoryStockService.validStockNum(map);
        // 减少库存
        repositoryStockService.subNumByMaterialId(map);

        boolean flag = repositoryReturnMaterialService.removeByIds(Arrays.asList(ids));

        log.info("删除退料表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("退料删除失败");
        }

        boolean flagDetail = repositoryReturnMaterialDetailService.delByDocumentIds(ids);
        log.info("删除退料表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("退料详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:returnMaterial:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        RepositoryReturnMaterial repositoryReturnMaterial = repositoryReturnMaterialService.getById(id);

        List<RepositoryReturnMaterialDetail> details = repositoryReturnMaterialDetailService.listByDocumentId(id);

        BaseDepartment department = baseDepartmentService.getById(repositoryReturnMaterial.getDepartmentId());

//        Double totalNum = 0D;

        for (RepositoryReturnMaterialDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

//            totalNum += detail.getNum();
        }
//        repositoryReturnMaterial.setTotalNum( totalNum);

        repositoryReturnMaterial.setDepartmentName(department.getName());

        repositoryReturnMaterial.setRowList(details);
        return ResponseResult.succ(repositoryReturnMaterial);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:returnMaterial:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryReturnMaterial repositoryReturnMaterial)
        throws Exception{

        if(repositoryReturnMaterial.getRowList() ==null || repositoryReturnMaterial.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        try {
            boolean validIsClose = validIsClose(repositoryReturnMaterial.getReturnDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            Map<String, Double> needSubMap = new HashMap<>();   // 需要减少库存的内容
            Map<String, Double> needAddMap = new HashMap<>();   // 需要增加库存的内容
            Map<String, Double> notUpdateMap = new HashMap<>();   // 不需要更新的内容
            // 校验退料数目(金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
            validComparePickNum(repositoryReturnMaterial, needSubMap,needAddMap,notUpdateMap);
            log.info("需要减少的内容:{},需要添加的内容:{},需要修改的内容:{}",needSubMap,needAddMap,notUpdateMap);

            // 校验库存
            repositoryStockService.validStockNum(needSubMap);

            // 减少库存
            repositoryStockService.subNumByMaterialId(needSubMap);
            // 添加库存
            repositoryStockService.addNumByMaterialIdFromMap(needAddMap);

            //1. 先删除老的，再插入新的
            boolean flag = repositoryReturnMaterialDetailService.removeByDocId(repositoryReturnMaterial.getId());
            if(flag){
                repositoryReturnMaterialService.updateById(repositoryReturnMaterial);

                for (RepositoryReturnMaterialDetail item : repositoryReturnMaterial.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryReturnMaterial.getId());
                }

                repositoryReturnMaterialDetailService.saveBatch(repositoryReturnMaterial.getRowList());
                log.info("退料模块-更新内容:{}",repositoryReturnMaterial);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *  要求：该部门，该物料，退料数目<= 该部门，该物料的领料数目
     * @param repositoryReturnMaterial
     * @param needSubMap
     * @param needAddMap
     * @param notUpdateMap
     */
    private void validComparePickNum(RepositoryReturnMaterial repositoryReturnMaterial, Map<String, Double> needSubMap
            , Map<String, Double> needAddMap, Map<String, Double> notUpdateMap)throws Exception {
        // 查询老的详情
        RepositoryReturnMaterial old = repositoryReturnMaterialService.getById(repositoryReturnMaterial.getId());
        Long oldDepartmentId = old.getDepartmentId();
        Long newDepartmentId = repositoryReturnMaterial.getDepartmentId();

        // 判断2. 库存能否修改。
        List<RepositoryReturnMaterialDetail> oldDetails = repositoryReturnMaterialDetailService.listByDocumentId(
                repositoryReturnMaterial.getId());

        // 新的物料数目：
        Map<String, Double> newMap = new HashMap<>();
        for (RepositoryReturnMaterialDetail detail : repositoryReturnMaterial.getRowList()) {
            Double materialNum = newMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            newMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }

        // 2.  老的物料数目
        Map<String, Double> oldMap = new HashMap<>();
        for (RepositoryReturnMaterialDetail detail : oldDetails) {
            Double materialNum = oldMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            oldMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }
        Set<String> set = new HashSet<>();
        set.addAll(oldMap.keySet());
        set.addAll(newMap.keySet());

        // 3. 该物料的领料数目 >= 该供应商，该物料 退料数目
        for (String materialId : set) {
            Double newNum = newMap.get(materialId) == null ? 0D : newMap.get(materialId);
            Double oldNum = oldMap.get(materialId) == null ? 0D : oldMap.get(materialId);

            if(!oldDepartmentId.equals(newDepartmentId)){
                // 老退料>新退料，退料将变少，不需要校验。同时，库存要减少
                if (oldNum > newNum) {
                    needSubMap.put(materialId, oldNum - newNum);//需要减少库存的数目
                } else if (oldNum < newNum) {
                    needAddMap.put(materialId, newNum - oldNum); // 需要新增库存的数目
                } else {
                    notUpdateMap.put(materialId, newNum);
                }
                // 假如部门换了，老的部门少了退料数目，不需要管。新的部门新增了退料，需要判断
                if(newNum==0){
                    // 新的物料不存在，不需要判断
                    continue;
                }
/*
                Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(newDepartmentId, materialId);

                Double returnCount = repositoryReturnMaterialService.countByDepartmentIdMaterialId(newDepartmentId,materialId);
                double calReturnNum = returnCount + newNum;

                if(pickCount < calReturnNum){
                    throw new Exception("该供应商:"+newDepartmentId+",该物料:" +materialId+
                            "(领料数目 :"+pickCount+"将会  < 修改后的退料的数目:"+calReturnNum);
                }*/
            }else{

                // 老退料>新退料，退料将变少，不需要校验。同时，库存要减少
                if (oldNum > newNum) {
                    needSubMap.put(materialId, oldNum - newNum);//需要减少库存的数目
                    continue;
                } else if (oldNum < newNum) {
                    needAddMap.put(materialId, newNum - oldNum); // 需要新增库存的数目
                } else {
                    notUpdateMap.put(materialId, newNum);
                    continue;
                }
                /*
                Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(newDepartmentId, materialId);


                // 查询该供应商，该物料退料数目
                Double returnCount = repositoryReturnMaterialService.countByDepartmentIdMaterialId(newDepartmentId, materialId);

                double calReturnNum = returnCount + (newNum - oldNum);

                if (pickCount < calReturnNum) {
                    throw new Exception("该供应商:" + newDepartmentId + ",该物料:" + materialId +
                            "(领料数目 :" + pickCount + "将会  < 修改后的退料的数目:" + calReturnNum);

                }*/
            }

        }
    }


    /**
     * 生产退料，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:returnMaterial:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryReturnMaterial repositoryReturnMaterial)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        repositoryReturnMaterial.setCreated(now);
        repositoryReturnMaterial.setUpdated(now);
        repositoryReturnMaterial.setCreatedUser(principal.getName());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_2);
        try {

            boolean validIsClose = validIsClose(repositoryReturnMaterial.getReturnDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            // 1. 根据单据ID 获取该单据的全部详情信息，
            Map<String, Double> map = new HashMap<>();// 一个物料，需要添加库存的数目

            // 1. 遍历获取一个物料要添加的数目。
            for (RepositoryReturnMaterialDetail detail : repositoryReturnMaterial.getRowList()) {
                Double materialNum = map.get(detail.getMaterialId());
                if(materialNum == null){
                    materialNum= 0D;
                }
                map.put(detail.getMaterialId(),materialNum+detail.getNum());
            }

            // 2. 该部门，该物料 退料不能 > 该部门，该物料领料通过的 总和 (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
           /* for (Map.Entry<String,Double> entry : map.entrySet()) {
                String materialId = entry.getKey();
                Double needAddNum = entry.getValue();// 该单据该物料，需要退料进行入库的数目
                // 查询该部门，该物料 总领料数目.
                Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(repositoryReturnMaterial.getDepartmentId(),
                        materialId);
                Double returnNum = repositoryReturnMaterialService.countByDepartmentIdMaterialId(repositoryReturnMaterial.getDepartmentId(),
                        materialId);
                double calReturnNum = returnNum + needAddNum;

                if(calReturnNum > pickCount ){
                    throw new Exception("该部门:"+repositoryReturnMaterial.getDepartmentId()+",该物料:" +materialId+
                            " 新增后的退料数目:"+calReturnNum+" > 领料总数目:"+pickCount);
                }
            }*/
            // 添加库存
            repositoryStockService.addNumByMaterialIdFromMap(map);

            repositoryReturnMaterialService.save(repositoryReturnMaterial);

            for (RepositoryReturnMaterialDetail item : repositoryReturnMaterial.getRowList()){
                item.setDocumentId(repositoryReturnMaterial.getId());
            }

            repositoryReturnMaterialDetailService.saveBatch(repositoryReturnMaterial.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",repositoryReturnMaterial.getId());
        } catch (Exception e) {
            log.error("退料单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取退料 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('repository:returnMaterial:list')")
    public ResponseResult list( String searchField, String searchStartDate, String searchEndDate,String searchStatus,
                               @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<RepositoryReturnMaterial> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("departmentName")) {
                queryField = "department_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

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
                    if (searchField.equals("departmentName")) {
                        queryField = "department_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    } else {
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
        pageData = repositoryReturnMaterialService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);
        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('repository:returnMaterial:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {
        RepositoryReturnMaterial old = repositoryReturnMaterialService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setId(id);
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_2);
        repositoryReturnMaterialService.updateById(repositoryReturnMaterial);
        log.info("仓库模块-退料模块-审核通过内容:{}",repositoryReturnMaterial);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('repository:returnMaterial:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {
        RepositoryReturnMaterial old = repositoryReturnMaterialService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_2
                &&
                old.getStatus()!=DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setId(id);
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_1);
        repositoryReturnMaterialService.updateById(repositoryReturnMaterial);
        log.info("仓库模块-退料模块-审核通过内容:{}",repositoryReturnMaterial);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('repository:returnMaterial:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<RepositoryReturnMaterial> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            RepositoryReturnMaterial old = repositoryReturnMaterialService.getById(id);
            if(old.getStatus() != DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_2 &&
                    old.getStatus() != DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_3
            ){
                return ResponseResult.fail("单据编号:"+id+"状态不正确，无法审核通过");
            }
            RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
            repositoryReturnMaterial.setUpdated(LocalDateTime.now());
            repositoryReturnMaterial.setUpdatedUser(principal.getName());
            repositoryReturnMaterial.setId(id);
            repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_0);
            lists.add(repositoryReturnMaterial);

        }
        repositoryReturnMaterialService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:returnMaterial:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        RepositoryReturnMaterial old = repositoryReturnMaterialService.getById(id);
        if(old.getStatus() != DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_2 &&
                old.getStatus() != DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setId(id);
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_0);
        repositoryReturnMaterialService.updateById(repositoryReturnMaterial);
        log.info("仓库模块-退料模块-审核通过内容:{}",repositoryReturnMaterial);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:returnMaterial:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        RepositoryReturnMaterial old = repositoryReturnMaterialService.getById(id);
        boolean validIsClose = validIsClose(old.getReturnDate());
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }
        if(old.getStatus()!=DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        List<RepositoryReturnMaterialDetail> details = repositoryReturnMaterialDetailService.listByDocumentId(id);
        // 1. 遍历更新 一个物料对应的库存数量
        repositoryStockService.subNumReturnMaterialId(details);
        RepositoryReturnMaterial repositoryReturnMaterial = new RepositoryReturnMaterial();
        repositoryReturnMaterial.setUpdated(LocalDateTime.now());
        repositoryReturnMaterial.setUpdatedUser(principal.getName());
        repositoryReturnMaterial.setId(id);
        repositoryReturnMaterial.setStatus(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_3);
        repositoryReturnMaterialService.updateById(repositoryReturnMaterial);
        log.info("仓库模块-反审核通过内容:{}",repositoryReturnMaterial);


        return ResponseResult.succ("反审核成功");
    }

    /**
     * 获取领料 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:returnMaterial:export')")
    public void export(HttpServletResponse response,String searchField, String searchStartDate, String searchEndDate,String searchStatus,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<RepositoryReturnMaterial> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("departmentName")) {
                queryField = "department_name";
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

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
                    if (searchField.equals("departmentName")) {
                        queryField = "department_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryReturnMaterialService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryReturnMaterial.class,1,0).export("id","SCTL",response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

}
