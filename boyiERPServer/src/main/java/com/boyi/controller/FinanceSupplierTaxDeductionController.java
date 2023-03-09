package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.fileFilter.MaterialPicFileFilter;
import com.boyi.common.utils.FileUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplier;
import com.boyi.entity.FinanceSupplierPayshoes;
import com.boyi.entity.FinanceSupplierTaxDeduction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-02-27
 */
@Slf4j
@RestController
@RequestMapping("/finance/supplierTaxDeduction")
public class FinanceSupplierTaxDeductionController extends BaseController {


    @Value("${picture.financePayShoesPath}")
    private String financePayShoesPath;

    private final String  picPrefix = "financeSupplierTaxDeductionPic-";

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();


    @RequestMapping(value = "/getPicturesById", method = RequestMethod.GET)
    public ResponseResult getPicturesById(Long id) {
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

        FinanceSupplierTaxDeduction ppc = financeSupplierTaxDeductionService.getById(id);
        financeSupplierTaxDeductionService.updateNullWithField(ppc, DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.PIC_URL_FIELDNAME);
        return ResponseResult.succ("删除成功");
    }

    @RequestMapping(value = "/uploadPic", method = RequestMethod.POST)
    public ResponseResult uploadFile(Long id, MultipartFile[] files) {
        if(id==null ){
            return ResponseResult.fail("没有ID");
        }
        FinanceSupplierTaxDeduction ppc = financeSupplierTaxDeductionService.getById(id);
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
                financeSupplierTaxDeductionService.updateById(ppc);
            }catch (Exception e){
                log.error("报错..",e);
            }
        }
        return ResponseResult.succ("");
    }

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('finance:taxDeduction:list')")
    public ResponseResult lockById(Principal principal, Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('finance:taxDeduction:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('finance:taxDeduction:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            List<FinanceSupplierTaxDeduction> olds = financeSupplierTaxDeductionService.listByIds(Arrays.asList(ids));
            for(FinanceSupplierTaxDeduction old : olds){
                String user = locks.get(old.getId());
                if(StringUtils.isNotBlank(user)){
                    return ResponseResult.fail("单据被["+user+"]占用");
                }
                // 假如有照片也不能反审核
                if( (old.getPicUrl()!=null && !old.getPicUrl().isEmpty()) ){
                    return ResponseResult.fail(old.getId()+" 编号,已有照片不能反审核，请先删除照片");
                }
            }

            boolean flag = financeSupplierTaxDeductionService.removeByIds(Arrays.asList(ids));

            log.info("删除扣税点表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("扣税点删除失败");
            }

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
    @PreAuthorize("hasAuthority('finance:taxDeduction:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierTaxDeduction fsp = financeSupplierTaxDeductionService.getById(id);
        BaseSupplier bs = baseSupplierService.getById(fsp.getSupplierId());
        fsp.setSupplierName(bs.getName());
        return ResponseResult.succ(fsp);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('finance:taxDeduction:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody FinanceSupplierTaxDeduction fsp)
            throws Exception{

        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        if(!fsp.getStatus().equals(0)){
            fsp.setStatus( DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_2);
        }
        try {
            if(fsp.getPayDate()==null){
                financeSupplierTaxDeductionService.updateNullWithField(fsp,DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.PAY_DATE_FIELDNAME);
            }
            financeSupplierTaxDeductionService.updateById(fsp);

            return ResponseResult.succ("编辑成功");
        }
        catch (Exception e) {
            log.error("更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产扣税点，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('finance:taxDeduction:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody FinanceSupplierTaxDeduction fsp)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        fsp.setCreated(now);
        fsp.setUpdated(now);
        fsp.setCreatedUser(principal.getName());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_2);
        try {

            financeSupplierTaxDeductionService.save(fsp);

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",fsp.getId());
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("单据不能重复!");
        }
        catch (Exception e) {
            log.error("扣税点单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取扣税点 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('finance:taxDeduction:list')")
    public ResponseResult list( String searchField, String searchStatus,String payStatus,
                                String searchStartDate,String searchEndDate,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<FinanceSupplierTaxDeduction> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }else if (searchField.equals("company")) {
                queryField = "company";
            }else if (searchField.equals("documentNum")) {
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
                    }else if (oneField.equals("company")) {
                        theQueryField = "company";
                    }else if (oneField.equals("documentNum")) {
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

        List<Long> searchPayStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(payStatus)){
            String[] split = payStatus.split(",");
            for (String statusVal : split){
                searchPayStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchPayStatusList.size() == 0){
            return ResponseResult.fail("付款状态不能为空");
        }

        pageData = financeSupplierTaxDeductionService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap,searchStartDate,searchEndDate,searchPayStatusList);

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('finance:taxDeduction:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {
        FinanceSupplierTaxDeduction old = financeSupplierTaxDeductionService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierTaxDeduction fsp = new FinanceSupplierTaxDeduction();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_2);
        financeSupplierTaxDeductionService.updateById(fsp);
        log.info("财务模块-扣税点模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("提交成功");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('finance:taxDeduction:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        FinanceSupplierTaxDeduction old = financeSupplierTaxDeductionService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_2 &&
                old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        FinanceSupplierTaxDeduction fsp = new FinanceSupplierTaxDeduction();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_1);
        financeSupplierTaxDeductionService.updateById(fsp);
        log.info("财务模块-扣税点模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("撤销通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('finance:taxDeduction:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<FinanceSupplierTaxDeduction> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            FinanceSupplierTaxDeduction old = financeSupplierTaxDeductionService.getById(id);
            if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_2
                    && old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_3){
                return ResponseResult.fail("状态已被修改.请刷新");
            }

            FinanceSupplierTaxDeduction fsp = new FinanceSupplierTaxDeduction();
            fsp.setUpdated(LocalDateTime.now());
            fsp.setUpdateUser(principal.getName());
            fsp.setId(id);
            fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_0);
            lists.add(fsp);

        }
        financeSupplierTaxDeductionService.updateBatchById(lists);
        return ResponseResult.succ("批量审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('finance:taxDeduction:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierTaxDeduction old = financeSupplierTaxDeductionService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierTaxDeduction fsp = new FinanceSupplierTaxDeduction();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_0);
        financeSupplierTaxDeductionService.updateById(fsp);
        log.info("财务模块-扣税点模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('finance:taxDeduction:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierTaxDeduction old = financeSupplierTaxDeductionService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierTaxDeduction fsp = new FinanceSupplierTaxDeduction();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDVALUE_3);
        financeSupplierTaxDeductionService.updateById(fsp);
        log.info("财务模块-反审核通过内容:{}",fsp);

        return ResponseResult.succ("反审核成功");
    }
}
