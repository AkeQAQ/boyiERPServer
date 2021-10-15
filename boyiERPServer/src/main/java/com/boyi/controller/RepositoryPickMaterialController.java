package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.common.utils.ExcelImportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 仓库模块-领料模块 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Slf4j
@RestController
@RequestMapping("/repository/pickMaterial")
public class RepositoryPickMaterialController extends BaseController {

    @Value("${poi.repositoryPickMaterialDemoPath}")
    private String poiDemoPath;
    @Value("${poi.repositoryPickMaterialImportDemoPath}")
    private String poiImportDemoPath;


    @Transactional
    @PostMapping("/down")
    @PreAuthorize("hasAuthority('repository:pickMaterial:save')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String("领料导入模板".getBytes("ISO8859-1")));
        response.setHeader("filename","领料导入模板" );

        FileInputStream fis = new FileInputStream(new File(poiImportDemoPath));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("下载成功");
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('repository:pickMaterial:save')")
    public ResponseResult upload(Principal principal, String pickUser, String departmentId, String pickDate , MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: pickUser:{},dep:{},pickdate:{},files:{}",pickUser,departmentId,pickDate,file);

        ExcelImportUtil<PickMaterialExcelVO> utils = new ExcelImportUtil<PickMaterialExcelVO>(PickMaterialExcelVO.class);
        List<PickMaterialExcelVO> pickMaterialExcelVOS = null;
        try (InputStream fis = file.getInputStream();){
            pickMaterialExcelVOS = utils.readExcel(fis, 1, 0);
            log.info("解析的excel数据:{}",pickMaterialExcelVOS);


        if(pickMaterialExcelVOS == null || pickMaterialExcelVOS.size() == 0){
            return ResponseResult.fail("解析内容未空");
        }
            LocalDate date = LocalDate.parse(pickDate);
            boolean validIsClose = validIsClose(date);
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }

        Map<String, Double> map = new HashMap<>();// 一个物料，需要减少的数目
        List<RepositoryPickMaterialDetail> details = new ArrayList<>();

        // 1. 遍历获取一个物料要减少的数目。
        for (PickMaterialExcelVO detail : pickMaterialExcelVOS) {
            Double materialNum = map.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            map.put(detail.getMaterialId(),materialNum+detail.getNum());

            RepositoryPickMaterialDetail theDetail = new RepositoryPickMaterialDetail();
            theDetail.setMaterialId(detail.getMaterialId());
            theDetail.setNum(detail.getNum());

            details.add(theDetail);
        }
        ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
        for(Map.Entry<String,Double> entry : map.entrySet()){
            String materialId = entry.getKey();
            RepositoryStock stock = repositoryStockService.getByMaterialId(materialId);
            HashMap<String, String> errorMsg = new HashMap<>();
            if(stock == null){
                errorMsg.put("content","该物料库存："+materialId+"库存为0!");
                errorMsgs.add(errorMsg);
            }else if(stock.getNum() < entry.getValue()){
                errorMsg.put("content","该物料："+materialId+"出现负库存.明细：库存数量:"+stock.getNum()+"小于要减少的数量:"+entry.getValue()+"不能减库存!");
                errorMsgs.add(errorMsg);
            }
        }
        if(errorMsgs.size() > 0){
            return ResponseResult.succ(errorMsgs);
        }

        // 减少库存
        repositoryStockService.subNumByMaterialId(map);

        RepositoryPickMaterial pickMaterial = new RepositoryPickMaterial();
        pickMaterial.setDepartmentId(Long.valueOf(departmentId));
        pickMaterial.setPickUser(pickUser);
        pickMaterial.setPickDate(date);
        LocalDateTime now = LocalDateTime.now();
        pickMaterial.setCreated(now);
        pickMaterial.setUpdated(now);
        pickMaterial.setCreatedUser(principal.getName());
        pickMaterial.setUpdatedUser(principal.getName());
        pickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_1);

        repositoryPickMaterialService.save(pickMaterial);

        for (RepositoryPickMaterialDetail item : details){
            item.setDocumentId(pickMaterial.getId());
        }

        repositoryPickMaterialDetailService.saveBatch(details);
        } catch (Exception e) {
            log.error("发生错误:",e);
            return ResponseResult.fail("上传失败.",e.getMessage());
        }
        return ResponseResult.succ("上传成功");
        /*LocalDateTime now = LocalDateTime.now();
        OrderProductpricePre orderProductpricePre = new OrderProductpricePre();
        orderProductpricePre.setCreated(now);
        orderProductpricePre.setUpdated(now);
        orderProductpricePre.setCreatedUser(principal.getName());
        orderProductpricePre.setUpdateUser(principal.getName());
        orderProductpricePre.setStatus(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_1);
        orderProductpricePre.setCompanyNum(companyNum);
        orderProductpricePre.setCustomer(customer);
        orderProductpricePre.setPrice(price);
        MultipartFile file = files[0];

        orderProductpricePre.setUploadName( file.getOriginalFilename());
        try {
            OrderProductpricePre old = orderProductpricePreService.getByCustomerAndCompanyNum(orderProductpricePre.getCustomer(),
                    orderProductpricePre.getCompanyNum());
            if(old != null){
                return ResponseResult.fail("该客户公司，该货号已存在历史记录!不允许添加");
            }
            orderProductpricePreService.save(orderProductpricePre);

            // 1. 存储文件
            String storePath = orderProductPricePrePath
                    + orderProductpricePre.getCompanyNum() + "-"
                    + orderProductpricePre.getCustomer() + "-"
                    + file.getOriginalFilename();
            file.transferTo(new File(storePath));
            // 2. 根据客户公司，公司货号唯一编码，去更新文件路径

            orderProductpricePreService.updateFilePathByCompanyNumAndCustomer(orderProductpricePre.getCompanyNum()
                    , orderProductpricePre.getCustomer(),storePath);
            return ResponseResult.succ("新增成功");
        } catch (Exception e) {
            log.error("插入异常", e);
            return ResponseResult.fail(e.getMessage());
        }*/
    }


    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:pickMaterial:del')")
    public ResponseResult delete(@RequestBody Long[] ids)throws Exception {

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<RepositoryPickMaterialDetail> details = repositoryPickMaterialDetailService.listByDocumentId(ids[0]);

        RepositoryPickMaterial repositoryPickMaterial1 = repositoryPickMaterialService.getById(ids[0]);

        Map<String, Double> map = new HashMap<>();// 一个物料，需要添加的数目
        // 1. 遍历获取一个物料要添加的数目。
        for (RepositoryPickMaterialDetail detail : details) {
            Double materialNum = map.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            map.put(detail.getMaterialId(),materialNum+detail.getNum());
        }
        // 2. 领料数目 要求>=退料数目 (金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
        /*for (Map.Entry<String,Double> entry : map.entrySet()) {
            String materialId = entry.getKey();
            Double needAddNum = entry.getValue();// 该单据该物料，需要进行入库的数目

            // 查询该部门，该物料 领料数目.
            Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(repositoryPickMaterial1.getDepartmentId(),
                    materialId);

            // 剩下的该部门，该物料的领料数目
            double calNum = pickCount - needAddNum;

            // 查询该部门，该物料 退料数目
            Double returnCount = repositoryReturnMaterialService.countByDepartmentIdMaterialId(repositoryPickMaterial1.getDepartmentId(),
                    materialId);

            if(calNum < returnCount){
                throw new Exception("该部门:"+repositoryPickMaterial1.getDepartmentId()+",该物料:" +materialId+
                        "(总领料数目:"+pickCount+" - 当前删除数目:"+needAddNum+")="+calNum+" < 退料审核通过的数目:"+returnCount);
            }

        }*/

        // 新增库存
        repositoryStockService.addNumByMaterialIdFromMap(map);

        boolean flag = repositoryPickMaterialService.removeByIds(Arrays.asList(ids));

        log.info("删除领料表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("领料删除失败");
        }

        boolean flagDetail = repositoryPickMaterialDetailService.delByDocumentIds(ids);
        log.info("删除领料表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("领料详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:pickMaterial:list')")
    public ResponseResult queryById(Long id) {
        RepositoryPickMaterial repositoryPickMaterial = repositoryPickMaterialService.getById(id);

        List<RepositoryPickMaterialDetail> details = repositoryPickMaterialDetailService.listByDocumentId(id);

        BaseDepartment department = baseDepartmentService.getById(repositoryPickMaterial.getDepartmentId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (RepositoryPickMaterialDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            totalNum += detail.getNum();
        }


        repositoryPickMaterial.setTotalNum( totalNum);

        repositoryPickMaterial.setDepartmentName(department.getName());

        repositoryPickMaterial.setRowList(details);
        return ResponseResult.succ(repositoryPickMaterial);
    }

    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:pickMaterial:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryPickMaterial repositoryPickMaterial)throws Exception {

        if(repositoryPickMaterial.getRowList() ==null || repositoryPickMaterial.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        repositoryPickMaterial.setUpdated(LocalDateTime.now());
        repositoryPickMaterial.setUpdatedUser(principal.getName());

        try {
            boolean validIsClose = validIsClose(repositoryPickMaterial.getPickDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            Map<String, Double> needSubMap = new HashMap<>();   // 需要减少库存的内容
            Map<String, Double> needAddMap = new HashMap<>();   // 需要增加库存的内容
            Map<String, Double> notUpdateMap = new HashMap<>();   // 不需要更新的内容
            // 校验退料数目(金蝶目前没有判断，因为导入比较麻烦，目前暂时先取消该功能)
            validCompareReturnNumFromUpdate(repositoryPickMaterial, needSubMap,needAddMap,notUpdateMap);
            log.info("需要减少的内容:{},需要添加的内容:{},需要修改的内容:{}",needSubMap,needAddMap,notUpdateMap);

            // 校验库存
            repositoryStockService.validStockNum(needSubMap);

            // 减少库存
            repositoryStockService.subNumByMaterialId(needSubMap);
            // 添加库存
            repositoryStockService.addNumByMaterialIdFromMap(needAddMap);

            //1. 先删除老的，再插入新的
            boolean flag = repositoryPickMaterialDetailService.removeByDocId(repositoryPickMaterial.getId());
            if(flag){
                repositoryPickMaterialService.updateById(repositoryPickMaterial);

                for (RepositoryPickMaterialDetail item : repositoryPickMaterial.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryPickMaterial.getId());
                }

                repositoryPickMaterialDetailService.saveBatch(repositoryPickMaterial.getRowList());
                log.info("领料模块-更新内容:{}",repositoryPickMaterial);
            }else{
                return ResponseResult.fail("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("供应商，更新异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }


    /**
     *  和退料比较，要求领料>=退料
     * @param repositoryPickMaterial
     * @param needSubMap
     * @param needAddMap
     * @param notUpdateMap
     * @throws Exception
     */
    private void validCompareReturnNumFromUpdate(RepositoryPickMaterial repositoryPickMaterial,
                                       Map<String, Double> needSubMap,
                                       Map<String, Double> needAddMap,
                                       Map<String, Double> notUpdateMap) throws Exception{

        // 查询老的详情
        RepositoryPickMaterial old = repositoryPickMaterialService.getById(repositoryPickMaterial.getId());
        Long oldDepartmentId = old.getDepartmentId();
        Long newDepartmentId = repositoryPickMaterial.getDepartmentId();

        // 判断2. 库存能否修改。
        List<RepositoryPickMaterialDetail> oldDetails = repositoryPickMaterialDetailService.listByDocumentId(repositoryPickMaterial.getId());

        // 新的物料数目：
        Map<String, Double> newMap = new HashMap<>();
        for (RepositoryPickMaterialDetail detail : repositoryPickMaterial.getRowList()) {
            Double materialNum = newMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            newMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }

        // 2.  老的物料数目
        Map<String, Double> oldMap = new HashMap<>();
        for (RepositoryPickMaterialDetail detail : oldDetails) {
            Double materialNum = oldMap.get(detail.getMaterialId());
            if(materialNum == null){
                materialNum= 0D;
            }
            oldMap.put(detail.getMaterialId(),materialNum+detail.getNum());
        }

        // 全部的物料
        HashSet<String> set = new HashSet<>();
        set.addAll(newMap.keySet());
        set.addAll(oldMap.keySet());

        // 3. 减少之后的该部门，该物料的领料数目 >= 该供应商，该物料 退料数目
        for (String materialId : set) {
            Double newNum = newMap.get(materialId);
            Double oldNum = oldMap.get(materialId);

            oldNum = oldNum == null? 0D: oldMap.get(materialId);
            newNum = newNum == null? 0D: newMap.get(materialId);

            // 供应商换了，老的领料要减少，新的要增加,老的要看能否减少
            if(!oldDepartmentId.equals(newDepartmentId)){
                // 老的物料里， 数目比 新的物料数目多的,就是要新增库存的，就不需要判断。
                if(oldNum > newNum){
                    needAddMap.put(materialId,oldNum - newNum );//库存新增的数目
                }else if(oldNum < newNum){
                    needSubMap.put(materialId,newNum - oldNum ); // 库存减少的数目
                }else {
                    notUpdateMap.put(materialId,newNum);
                }
                // 假如老的物料不存在的话，就不需要判断
                if(oldNum == 0){
                    continue;
                }
/*
                Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(oldDepartmentId, materialId);

                double calPickNum = pickCount - oldNum;

                Double returnCount = repositoryReturnMaterialService.countByDepartmentIdMaterialId(oldDepartmentId,materialId);

                if(calPickNum < returnCount){
                    throw new Exception("该供应商:"+newDepartmentId+",该物料:" +materialId+
                            "(修改后的领料数目 :"+calPickNum+"将会  < 退料的数目:"+returnCount);
                }*/
            }else{
                // 老的物料里， 数目比 新的物料数目多的,就是要新增库存的，就不需要判断。
                if(oldNum > newNum){
                    needAddMap.put(materialId,oldNum - newNum );//库存新增的数目
                }else if(oldNum < newNum){
                    needSubMap.put(materialId,newNum - oldNum ); // 库存减少的数目
                    continue;
                }else {
                    notUpdateMap.put(materialId,newNum);
                    continue;
                }
                /*
                // 查询历史该供应商，该物料 总领料数目.
                Double pickCount = repositoryPickMaterialService.countByDepartmentIdMaterialId(newDepartmentId, materialId);

                double calPickNum = pickCount - (oldNum-newNum);

                // 查询该供应商，该物料退料数目
                Double returnCount = repositoryReturnMaterialService.countByDepartmentIdMaterialId(newDepartmentId,materialId);

                if(calPickNum < returnCount){
                    throw new Exception("该供应商:"+newDepartmentId+",该物料:" +materialId+
                            "(修改后的领料数目 :"+calPickNum+"将会  < 退料的数目:"+returnCount);

                }*/
            }

        }
    }

    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:pickMaterial:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryPickMaterial repositoryPickMaterial)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        repositoryPickMaterial.setCreated(now);
        repositoryPickMaterial.setUpdated(now);
        repositoryPickMaterial.setCreatedUser(principal.getName());
        repositoryPickMaterial.setUpdatedUser(principal.getName());
        repositoryPickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_1);
        try {

            boolean validIsClose = validIsClose(repositoryPickMaterial.getPickDate());
            if(!validIsClose){
                return ResponseResult.fail("日期请设置在关账日之后.");
            }

            Map<String, Double> map = new HashMap<>();// 一个物料，需要减少的数目
            // 1. 遍历获取一个物料要减少的数目。
            for (RepositoryPickMaterialDetail detail : repositoryPickMaterial.getRowList()) {
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

            repositoryPickMaterialService.save(repositoryPickMaterial);

            for (RepositoryPickMaterialDetail item : repositoryPickMaterial.getRowList()){
                item.setDocumentId(repositoryPickMaterial.getId());
            }

            repositoryPickMaterialDetailService.saveBatch(repositoryPickMaterial.getRowList());

            return ResponseResult.succ("新增成功");
        } catch (DuplicateKeyException e) {
            log.error("领料单，插入异常",e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    /**
     * 获取领料 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:pickMaterial:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryPickMaterial> pageData = null;
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
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryPickMaterialService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryPickMaterial.class,1,0).export(response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取领料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:pickMaterial:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryPickMaterial> pageData = null;
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
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryPickMaterialService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:pickMaterial:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        RepositoryPickMaterial repositoryPickMaterial = new RepositoryPickMaterial();
        repositoryPickMaterial.setUpdated(LocalDateTime.now());
        repositoryPickMaterial.setUpdatedUser(principal.getName());
        repositoryPickMaterial.setId(id);
        repositoryPickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_0);
        repositoryPickMaterialService.updateById(repositoryPickMaterial);
        log.info("仓库模块-领料模块-审核通过内容:{}",repositoryPickMaterial);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:pickMaterial:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        RepositoryPickMaterial old = repositoryPickMaterialService.getById(id);
        boolean validIsClose = validIsClose(old.getPickDate());
        if(!validIsClose){
            return ResponseResult.fail("日期请设置在关账日之后.");
        }

        RepositoryPickMaterial repositoryPickMaterial = new RepositoryPickMaterial();
        repositoryPickMaterial.setUpdated(LocalDateTime.now());
        repositoryPickMaterial.setUpdatedUser(principal.getName());
        repositoryPickMaterial.setId(id);
        repositoryPickMaterial.setStatus(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_1);
        repositoryPickMaterialService.updateById(repositoryPickMaterial);
        log.info("仓库模块-反审核通过内容:{}",repositoryPickMaterial);


        return ResponseResult.succ("反审核成功");
    }

}
