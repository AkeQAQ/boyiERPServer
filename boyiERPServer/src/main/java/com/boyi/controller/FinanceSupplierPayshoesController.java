package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.fileFilter.MaterialPicFileFilter;
import com.boyi.common.utils.*;
import com.boyi.common.vo.RealDosageVO;
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

import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-02-24
 */
@Slf4j
@RestController
@RequestMapping("/finance/supplierPayshoes")
public class FinanceSupplierPayshoesController extends BaseController {

    @Value("${poi.financePayShoesDemoPath}")
    private String poiDemoPath;

    @Value("${picture.financePayShoesPath}")
    private String financePayShoesPath;
    private final String  picPrefix = "financeSupplierPayShoesPic-";

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();


    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('finance:payShoes:save')")
    public void export(HttpServletResponse response, String searchField, String searchStatus,String takeStatus,String payTypeStatus,
                       String searchStartDate,String searchEndDate,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<FinanceSupplierPayshoes> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("customerNum")) {
                queryField = "customer_num";

            }
            else if (searchField.equals("documentNum")) {
                queryField = "document_num";

            }
            else {
                return ;
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
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("customerNum")) {
                        theQueryField = "customer_num";

                    }
                    else if (oneField.equals("documentNum")) {
                        theQueryField = "document_num";

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
            return ;
        }

        List<Long> takeStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(takeStatus)){
            String[] split = takeStatus.split(",");
            for (String statusVal : split){
                takeStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(takeStatusList.size() == 0){
            return ;
        }

        List<Long> payTypeStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(payTypeStatus)){
            String[] split = payTypeStatus.split(",");
            for (String statusVal : split){
                payTypeStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(payTypeStatusList.size() == 0){
            return ;
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = financeSupplierPayshoesService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,takeStatusList,payTypeStatusList,queryMap,searchStartDate,searchEndDate);



        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(FinanceSupplierPayshoes.class,1,0).export("id","",response,fis,pageData.getRecords(),"报表.xlsx",
                    DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.statusMap,
                    DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES_DETAILS.payTypeMap,DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.takeStatusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 修改拿走状态
     */
    @GetMapping("/updateTakeStatus")
    @PreAuthorize("hasAuthority('finance:payShoes:update')")
    public ResponseResult updateTakeStatus(Principal principal,Long id,Integer takeStatus) {
        FinanceSupplierPayshoes old = financeSupplierPayshoesService.getById(id);
        old.setTakeStatus(takeStatus);
        old.setUpdated(LocalDateTime.now());
        old.setUpdateUser(principal.getName());
        financeSupplierPayshoesService.updateById(old);
        return ResponseResult.succ("修改未拿/已拿状态成功!");
    }
    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('finance:payShoes:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('finance:payShoes:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @RequestMapping(value = "/getPicturesById", method = RequestMethod.GET)
    public ResponseResult getPicturesById( Long id) {
        // 根据ID 查询照片的路径和名字
        File directory = new File(financePayShoesPath);
        MaterialPicFileFilter craftPicFileFilter = new MaterialPicFileFilter(picPrefix+id);
        File[] files = directory.listFiles(craftPicFileFilter);

        ArrayList<File> files1 = new ArrayList<>();
        if(files!=null && files.length != 0){
            for (int i = 0; i < files.length; i++) {
                files1.add(files[i]);
            }
        }

        Collections.sort(files1, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < files1.size(); i++) {
            File oneFile = files1.get(i);
            String name = oneFile.getName();
            names.add(name);
        }
        return ResponseResult.succ(names);
    }

    @RequestMapping(value = "/delPic", method = RequestMethod.GET)
    public ResponseResult delPic(String fileName,Long id) {
        File delFile = new File(financePayShoesPath, fileName);
        if(delFile.exists()){
            delFile.delete();
        }else{
            return ResponseResult.fail("文件["+fileName+"] 不存在,无法删除");
        }

        FinanceSupplierPayshoes ppc = financeSupplierPayshoesService.getById(id);
        financeSupplierPayshoesService.updateNullWithField(ppc,DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.PIC_URL_FIELDNAME);
        return ResponseResult.succ("删除成功");
    }

    @RequestMapping(value = "/uploadPic", method = RequestMethod.POST)
    public ResponseResult uploadFile(Long id, MultipartFile[] files) {
        if(id==null ){
            return ResponseResult.fail("没有ID");
        }
        FinanceSupplierPayshoes ppc = financeSupplierPayshoesService.getById(id);
        for (int i = 0; i < files.length; i++) {
            log.info("文件内容:{}",files[i]);
            MultipartFile file = files[i];
            try (InputStream fis = file.getInputStream();){
                String originalFilename = file.getOriginalFilename();
                String[] split = originalFilename.split("\\.");
                String suffix = split[split.length - 1];// 获取后缀

                String s = picPrefix + id + "_" + System.currentTimeMillis() + "." + suffix;
                FileUtils.writeFile(fis,financePayShoesPath,s);
                ppc.setPicUrl(s);
                financeSupplierPayshoesService.updateById(ppc);
            }catch (Exception e){
                log.error("报错..",e);
            }
        }
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('finance:payShoes:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            List<FinanceSupplierPayshoes> olds = financeSupplierPayshoesService.listByIds(Arrays.asList(ids));
            for(FinanceSupplierPayshoes old : olds){
                String user = locks.get(old.getId());
                if(StringUtils.isNotBlank(user)){
                    return ResponseResult.fail("单据被["+user+"]占用");
                }
                // 假如有照片也不能反审核
                if( (old.getPicUrl()!=null && !old.getPicUrl().isEmpty()) ){
                    return ResponseResult.fail(old.getId()+" 编号,已有照片不能反审核，请先删除照片");
                }
            }


            boolean flag = financeSupplierPayshoesService.removeByIds(Arrays.asList(ids));

            log.info("删除赔鞋表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("赔鞋删除失败");
            }

            financeSupplierPayshoesDetailsService.delByDocumentIds(ids);

            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('finance:payShoes:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierPayshoes fsp = financeSupplierPayshoesService.getById(id);
        BaseSupplier bs = baseSupplierService.getById(fsp.getSupplierId());
        fsp.setSupplierName(bs.getName());
        List<FinanceSupplierPayshoesDetails> details = financeSupplierPayshoesDetailsService.listByForeignId(id);
        fsp.setRowList(details);
        return ResponseResult.succ(fsp);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('finance:payShoes:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody FinanceSupplierPayshoes fsp)
            throws Exception{

        if(fsp.getRowList() ==null || fsp.getRowList().size() ==0){
            return ResponseResult.fail("详情内容不能为空");
        }

        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus( DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_2);
        try {

            //1. 先删除老的，再插入新的
            boolean flag = financeSupplierPayshoesDetailsService.removeByDocId(fsp.getId());

            if(flag){
                financeSupplierPayshoesService.updateById(fsp);

                for (FinanceSupplierPayshoesDetails item : fsp.getRowList()){
                    item.setId(null);
                    item.setPayShoesId(fsp.getId());
                    //新增物料，赋值初始数值
                    item.setCreated(LocalDateTime.now());
                    item.setUpdated(LocalDateTime.now());
                    item.setCreatedUser(principal.getName());
                    item.setUpdateUser(principal.getName());
                }

                financeSupplierPayshoesDetailsService.saveBatch(fsp.getRowList());
                log.info("赔鞋模块-更新内容:{}",fsp);
            }else{
                throw new RuntimeException("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        }
        catch (DuplicateKeyException de){
            throw new RuntimeException("单据编号不能重复!");
        }
        catch (Exception e) {
            log.error("更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产赔鞋，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('finance:payShoes:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody FinanceSupplierPayshoes fsp)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        fsp.setCreated(now);
        fsp.setUpdated(now);
        fsp.setCreatedUser(principal.getName());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_2);
        try {

            financeSupplierPayshoesService.save(fsp);

            for (FinanceSupplierPayshoesDetails item : fsp.getRowList()){
                item.setPayShoesId(fsp.getId());
                item.setCreated(now);
                item.setUpdated(now);
                item.setCreatedUser(principal.getName());
                item.setUpdateUser(principal.getName());
            }

            financeSupplierPayshoesDetailsService.saveBatch(fsp.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",fsp.getId());
        }
        catch (DuplicateKeyException de){
            throw new RuntimeException("单据编号不能重复!");
        }
        catch (Exception e) {
            log.error("赔鞋单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取赔鞋 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('finance:payShoes:list')")
    public ResponseResult list( String searchField, String searchStatus,String takeStatus,String payTypeStatus,
                                String searchStartDate,String searchEndDate,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<FinanceSupplierPayshoes> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("customerNum")) {
                queryField = "customer_num";

            }
            else if (searchField.equals("documentNum")) {
                queryField = "document_num";

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
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("customerNum")) {
                        theQueryField = "customer_num";

                    }
                    else if (oneField.equals("documentNum")) {
                        theQueryField = "document_num";

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
            return ResponseResult.fail("审核状态不能为空");
        }

        List<Long> takeStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(takeStatus)){
            String[] split = takeStatus.split(",");
            for (String statusVal : split){
                takeStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(takeStatusList.size() == 0){
            return ResponseResult.fail("单据状态不能为空");
        }

        List<Long> payTypeStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(payTypeStatus)){
            String[] split = payTypeStatus.split(",");
            for (String statusVal : split){
                payTypeStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(payTypeStatusList.size() == 0){
            return ResponseResult.fail("类型不能为空");
        }
        pageData = financeSupplierPayshoesService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,takeStatusList,payTypeStatusList,queryMap,searchStartDate,searchEndDate);

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('finance:payShoes:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {
        FinanceSupplierPayshoes old = financeSupplierPayshoesService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierPayshoes fsp = new FinanceSupplierPayshoes();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_2);
        financeSupplierPayshoesService.updateById(fsp);
        log.info("财务模块-赔鞋模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("提交成功");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('finance:payShoes:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        FinanceSupplierPayshoes old = financeSupplierPayshoesService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_2 &&
                old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        FinanceSupplierPayshoes fsp = new FinanceSupplierPayshoes();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_1);
        financeSupplierPayshoesService.updateById(fsp);
        log.info("财务模块-赔鞋模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("撤销通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('finance:payShoes:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<FinanceSupplierPayshoes> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            FinanceSupplierPayshoes old = financeSupplierPayshoesService.getById(id);
            if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_2
                    && old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_3){
                return ResponseResult.fail("状态已被修改.请刷新");
            }

            FinanceSupplierPayshoes fsp = new FinanceSupplierPayshoes();
            fsp.setUpdated(LocalDateTime.now());
            fsp.setUpdateUser(principal.getName());
            fsp.setId(id);
            fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_0);
            lists.add(fsp);

        }
        financeSupplierPayshoesService.updateBatchById(lists);
        return ResponseResult.succ("批量审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('finance:payShoes:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierPayshoes old = financeSupplierPayshoesService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierPayshoes fsp = new FinanceSupplierPayshoes();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_0);
        financeSupplierPayshoesService.updateById(fsp);
        log.info("财务模块-赔鞋模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('finance:payShoes:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierPayshoes old = financeSupplierPayshoesService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierPayshoes fsp = new FinanceSupplierPayshoes();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_PAYSHOES.STATUS_FIELDVALUE_3);
        financeSupplierPayshoesService.updateById(fsp);
        log.info("财务模块-反审核通过内容:{}",fsp);

        return ResponseResult.succ("反审核成功");
    }
}
