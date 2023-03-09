package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.CostOfLabourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
@RestController
@RequestMapping("/costOfLabour")
@Slf4j
public class CostOfLabourController extends BaseController {


    public static final Map<Long,String> locks = new ConcurrentHashMap<>();

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        try {

            boolean flag = costOfLabourService.removeByIds(Arrays.asList(ids));

            log.info("删除工价表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("工价删除失败");
            }

            boolean flagDetail = costOfLabourDetailService.removeByForeignIds(ids);
            log.info("删除工价表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

            if(!flagDetail){
                return ResponseResult.fail("工价详情表没有删除成功!");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        CostOfLabour old = costOfLabourService.getById(id);
        ProduceProductConstituent ppc = produceProductConstituentService.getById(old.getProduceProductConstituentId());
        old.setProductNum(ppc.getProductNum()+ppc.getProductBrand());

        CostOfLabourType type = costOfLabourTypeService.getById(old.getCostOfLabourTypeId());
        old.setCostOfLabourTypeName(type.getTypeName());

        List<CostOfLabourDetail> details = costOfLabourDetailService.listByForeignId(id);

        old.setRowList(details);
        for (CostOfLabourDetail detail : details){
            CostOfLabourProcesses processes = costOfLabourProcessesService.getById(detail.getCostOfLabourProcessesId());
            detail.setProcessesName(processes.getProcessesName());
            detail.setLowPrice(processes.getLowPrice()+"");
            detail.setPiecesPrice(processes.getPiecesPrice()+"");

            BigDecimal picPrice = BigDecimalUtil.mul(detail.getPieces() ==null?0:detail.getPieces().doubleValue(), processes.getPiecesPrice().doubleValue());

            detail.setCalPrice(picPrice.doubleValue() > processes.getLowPrice().doubleValue() ? picPrice.toString():processes.getLowPrice().toString());
        }
        old.setRowList(details);
        return ResponseResult.succ(old);
    }



    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody CostOfLabour costOfLabour)
            throws Exception{

        if(costOfLabour.getRowList() ==null || costOfLabour.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        costOfLabour.setUpdated(LocalDateTime.now());
        costOfLabour.setUpdateUser(principal.getName());
        costOfLabour.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
        try {

            //1. 先删除老的，再插入新的
            boolean flag = costOfLabourDetailService.removeByForeignId(costOfLabour.getId());
            if(flag){

                costOfLabourService.updateById(costOfLabour);

                for (CostOfLabourDetail item : costOfLabour.getRowList()){
                    item.setId(null);
                    item.setForeignId(costOfLabour.getId());
                }

                costOfLabourDetailService.saveBatch(costOfLabour.getRowList());
                log.info("工价模块-更新内容:{}",costOfLabour);
            }else{
                throw new RuntimeException("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody CostOfLabour costOfLabour)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        costOfLabour.setCreated(now);
        costOfLabour.setUpdated(now);
        costOfLabour.setCreatedUser(principal.getName());
        costOfLabour.setUpdateUser(principal.getName());
        costOfLabour.setStatus(DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_2);
        try {

            costOfLabourService.save(costOfLabour);

            for (CostOfLabourDetail item : costOfLabour.getRowList()){
                item.setForeignId(costOfLabour.getId());
            }

            costOfLabourDetailService.saveBatch(costOfLabour.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",costOfLabour.getId());
        } catch (Exception e) {
            log.error("工价单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取工价 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:list')")
    public ResponseResult list( String searchField, String searchStartDate, String searchEndDate,String searchStatus,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<CostOfLabour> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("produceProductConstituentId")) {
                queryField = "produce_product_constituent_id";
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
                    if (oneField.equals("produceProductConstituentId")) {
                        theQueryField = "produce_product_constituent_id";
                    }
                    else {
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

        pageData = costOfLabourService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);
        List<CostOfLabour> records = pageData.getRecords();
        HashMap<Long, String> totalPrices = new HashMap<>();
        HashMap<Long, OrderProductpricePre> productNumBrandAndPrePrices = new HashMap<>();

        for(CostOfLabour col : records){
            String totalPrice = totalPrices.get(col.getId());
            if(totalPrice == null){
                totalPrices.put(col.getId(),col.getRealPrice());
            }else{
                totalPrices.put(col.getId(),BigDecimalUtil.add(totalPrice,col.getRealPrice()).toString());
            }

            // 获取核算价格
            OrderProductpricePre pre = productNumBrandAndPrePrices.get(col.getId());
            if(pre == null ){
               pre= orderProductpricePreService.getByCustomerAndCompanyNumSimple(col.getProductBrand(),col.getProductNum());
               if(pre==null){
                   //去除前3个
                   StringBuilder sb = new StringBuilder(col.getProductNum());
                   StringBuilder nextStr = sb.delete(0, 3);
                   pre= orderProductpricePreService.getByCustomerAndCompanyNumSimple(col.getProductBrand(),nextStr.toString());
               }
               productNumBrandAndPrePrices.put(col.getId(),pre);
            }
            if(pre==null){
                continue;
            }
            if(col.getCostOfLabourTypeName().equals("裁断")){
                col.setPrePrice(pre.getCaiduanPrice().toString());
            }else if(col.getCostOfLabourTypeName().equals("针车")){
                col.setPrePrice(pre.getZhenchePrice().toString());

            }else if(col.getCostOfLabourTypeName().equals("成型")){
                col.setPrePrice(pre.getCxPrice().toString());
            }
        }
        for(CostOfLabour col : records) {
            col.setTotalPrice(totalPrices.get(col.getId()));
        }
            return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {
        CostOfLabour old = costOfLabourService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        CostOfLabour costOfLabour = new CostOfLabour();
        costOfLabour.setUpdated(LocalDateTime.now());
        costOfLabour.setUpdateUser(principal.getName());
        costOfLabour.setId(id);
        costOfLabour.setStatus(DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_2);
        costOfLabourService.updateById(costOfLabour);
        log.info("模块-工价模块-审核通过内容:{}",costOfLabour);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {
        CostOfLabour old = costOfLabourService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_2
                &&
                old.getStatus()!=DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        CostOfLabour costOfLabour = new CostOfLabour();
        costOfLabour.setUpdated(LocalDateTime.now());
        costOfLabour.setUpdateUser(principal.getName());
        costOfLabour.setId(id);
        costOfLabour.setStatus(DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_1);
        costOfLabourService.updateById(costOfLabour);
        log.info("模块-工价模块-审核通过内容:{}",costOfLabour);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<CostOfLabour> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            CostOfLabour old = costOfLabourService.getById(id);
            if(old.getStatus() != DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_2 &&
                    old.getStatus() != DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_3
            ){
                return ResponseResult.fail("单据编号:"+id+"状态不正确，无法审核通过");
            }
            CostOfLabour costOfLabour = new CostOfLabour();
            costOfLabour.setUpdated(LocalDateTime.now());
            costOfLabour.setUpdateUser(principal.getName());
            costOfLabour.setId(id);
            costOfLabour.setStatus(DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_0);
            lists.add(costOfLabour);

        }
        costOfLabourService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        CostOfLabour old = costOfLabourService.getById(id);
        if(old.getStatus() != DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_2 &&
                old.getStatus() != DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        CostOfLabour costOfLabour = new CostOfLabour();
        costOfLabour.setUpdated(LocalDateTime.now());
        costOfLabour.setUpdateUser(principal.getName());
        costOfLabour.setId(id);
        costOfLabour.setStatus(DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_0);
        costOfLabourService.updateById(costOfLabour);
        log.info("模块-工价模块-审核通过内容:{}",costOfLabour);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        CostOfLabour old = costOfLabourService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        CostOfLabour costOfLabour = new CostOfLabour();
        costOfLabour.setUpdated(LocalDateTime.now());
        costOfLabour.setUpdateUser(principal.getName());
        costOfLabour.setId(id);
        costOfLabour.setStatus(DBConstant.TABLE_COST_OF_LABOUR.STATUS_FIELDVALUE_3);
        costOfLabourService.updateById(costOfLabour);
        log.info("模块-反审核通过内容:{}",costOfLabour);


        return ResponseResult.succ("反审核成功");
    }

    /**
     * 获取领料 分页导出
     */
  /*  @PostMapping("/export")
    @PreAuthorize("hasAuthority('costOfLabour:costOfLabour:export')")
    public void export(HttpServletResponse response, String searchField, String searchStartDate, String searchEndDate, String searchStatus,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<RepositoryReturnMaterial> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
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
                    if (oneField.equals("departmentName")) {
                        theQueryField = "department_name";
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
        Page page = getPage();
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = repositoryReturnMaterialService.innerQueryByManySearch(page,searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryReturnMaterial.class,1,0).export("id","SCTL",response,fis,pageData.getRecords(),"报表.xlsx",DBConstant.TABLE_COST_OF_LABOUR.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }
*/
}
