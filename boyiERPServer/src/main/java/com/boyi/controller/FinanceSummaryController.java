package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.fileFilter.MaterialPicFileFilter;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.common.utils.FileUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.formula.functions.Finance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
 * @since 2023-03-06
 */
@RestController
@RequestMapping("/finance/supplierSummary")
@Slf4j
public class FinanceSummaryController extends BaseController {

    @Value("${picture.financePayShoesPath}")
    private String financePayShoesPath;
    private final String  picPrefix = "financeSupplierSummaryPic-";

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();


    @Value("${poi.financeSummaryDemoPath}")
    private String poiDemoPath;


    /**
     * 修改拿走状态
     */
    @GetMapping("/updateStatus")
    @PreAuthorize("hasAuthority('finance:summary:valid')")
    public ResponseResult updateStatus(Principal principal,Long id,Integer status) {
        FinanceSummary old = financeSummaryService.getById(id);
        old.setStatus(status);
        old.setUpdated(LocalDateTime.now());
        old.setUpdatedUser(principal.getName());
        String str ="";
        if(status.equals(DBConstant.TABLE_FINANCE_SUMMARY.STATUS_FIELDVALUE_0)){
            str = "已结账";
            old.setSettleDate(LocalDate.now());

        }else {
            str = "未结账";
            old.setSettleDate(null);
        }
        financeSummaryService.updateById(old);
        return ResponseResult.succ("修改结账状态成功!目前改成: "+str);
    }

    /**
     * 锁单据
     */
    @GetMapping("/updateRoundDown")
    @PreAuthorize("hasAuthority('finance:summary:update')")
    public ResponseResult updateRoundDown(Principal principal,Double roundDown,Long id) {
        FinanceSummary fs = financeSummaryService.getById(id);
        BigDecimal oldRounDown = fs.getRoundDown();
        if(oldRounDown==null){
            oldRounDown = new BigDecimal("0");
        }

        // 对老的进行应付货款进行加减 (
        if(roundDown==null){
            fs.setRoundDown(new BigDecimal("0"));

        }else{
            fs.setRoundDown(new BigDecimal(roundDown));
        }

        BigDecimal newRoundDown = fs.getRoundDown();


        fs.setNeedPayAmount(BigDecimalUtil.sub(fs.getNeedPayAmount().toString(),oldRounDown.toString()).add(   newRoundDown));


        LocalDateTime now = LocalDateTime.now();
        fs.setUpdated(now);
        fs.setUpdatedUser(principal.getName());
        financeSummaryService.updateById(fs);
        return ResponseResult.succ("保存成功");
    }


    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('finance:summary:list')")
    public void export(HttpServletResponse response, String searchField, String status,String payStatus,
                       String searchStartDate,String searchEndDate,
                       String searchStartSettleDate,String searchEndSettleDate,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<FinanceSummary> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("id")) {
                queryField = "id";

            }

            else {
                 throw new RuntimeException("搜索字段不存在");
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
                    else if (oneField.equals("id")) {
                        theQueryField = "id";

                    }
                    else {
                        throw new RuntimeException("搜索字段不存在");
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(status)){
            String[] split = status.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            throw new RuntimeException("结账状态不能为空");
        }

        List<Long> payStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(payStatus)){
            String[] split = payStatus.split(",");
            for (String statusVal : split){
                payStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(payStatusList.size() == 0){
            throw new RuntimeException("结清状态不能为空");
        }

        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = financeSummaryService.innerQueryByManySearch(page,searchField,queryField,searchStr,searchStatusList,payStatusList,queryMap,searchStartDate,searchEndDate,searchStartSettleDate,searchEndSettleDate);
        for(FinanceSummary fs : pageData.getRecords()){
            fs.setOtherTotalAmount(fs.getPayShoesAmount()
                    .add(fs.getTestAmount())
                    .add(fs.getFineAmount())
                    .add(fs.getChangeAmount())
                    .add(fs.getTaxSupplement())
                    .add(fs.getTaxDeduction())
                    .add(fs.getRoundDown()));
            fs.setShowId(fs.getSummaryDate()+"-"+fs.getId());
        }
        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(FinanceSummary.class,1,0).export("id","",response,fis,pageData.getRecords(),"报表.xlsx",
                    DBConstant.TABLE_FINANCE_SUMMARY.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }


    /**
     * 新增月份的对账数据
     */
    @PostMapping("/addNewData")
    @PreAuthorize("hasAuthority('finance:summary:update')")
    public ResponseResult addNewData(Principal principal, String addDate)throws Exception {
        if(addDate==null || addDate.isEmpty()){
            return ResponseResult.fail("没有选择需要生成的月份！");
        }
        // 根据年月的参数，生成该月份的临界点
        String[] split = addDate.split("-");

        LocalDateTime now = LocalDateTime.now();
        LocalDate localDate = LocalDate.of(Integer.valueOf(split[0]), Integer.valueOf(split[1]), 1);

        LocalDate startDateTime = LocalDate.of(localDate.getYear(), localDate.getMonth(), 1);

        LocalDate endDate = localDate.plusMonths(1);
        LocalDate endDateTime = LocalDate.of(endDate.getYear(), endDate.getMonthValue(), 1 );

        endDateTime = endDateTime.plusDays(-1);
        endDate = endDate.plusDays(-1);

        // 判断该月份是否已经关账,比选择的月份晚1个月之后，要有仓库关账、财务关账的记录存在，否则不能生成
        RepositoryClose rc = repositoryCloseService.listLatestOne();
        boolean rcClose = rc.getCloseDate().isAfter(endDate);
        if(!rcClose){
            if(!rc.getCloseDate().isEqual(endDate)){
                return ResponseResult.fail("该月份["+addDate+"],仓库最近关账日期["+rc.getCloseDate()+"],汇总月份需要比关账日期的月份小至少一月!! ");
            }
        }
        FinanceClose fc = financeCloseService.listLatestOne();
        boolean fcClose = fc.getCloseDate().isAfter(endDate);
        if(!fcClose){
            if(!fc.getCloseDate().isEqual(endDate)) {
                return ResponseResult.fail("该月份["+addDate+"],财务最近关账日期["+fc.getCloseDate()+"],汇总月份需要比关账日期的月份小至少一月!! ");
            }
        }


        // 查看数据库是否已经有该月份数据
        Integer count = financeSummaryService.countByDate(addDate);
        if(count >0){
            return ResponseResult.fail("该月份["+addDate+"],已经有["+count+"]条数据!不能再生成");
        }


        log.info("选择月份：{}，startDateTime:{},endDateTime:{}",addDate,startDateTime,endDateTime);


        // 1. 获取该日期段之间的 全部采购入库、采购退料的供应商净入库金额
        Map<String, BigDecimal> supplier_amount_buyIn= getBuyInAmount(startDateTime,endDateTime);

        Map<String, BigDecimal> supplier_amount_buyOut= getBuyOutAmount(startDateTime,endDateTime);

        // 2. 获取该日期段之间的，供应商赔鞋金额
        Map<String, BigDecimal> supplier_amount_payShoes= getPayShoesAmount(startDateTime,endDateTime);

        // 3. 获取该日期段之间的，供应商罚款金额
        Map<String, BigDecimal> supplier_amount_fine= getFineAmount(startDateTime,endDateTime);

        // 4. 获取该日期段之间的，供应商检测费金额
        Map<String, BigDecimal> supplier_amount_test= getTestAmount(startDateTime,endDateTime);

        // 5. 获取该日期段之间的，供应商补税点金额
        Map<String, BigDecimal> supplier_amount_taxSupplement= getTaxSAmount(startDateTime,endDateTime);

        // 6. 获取该日期段之间的，供应商扣税点金额
        Map<String, BigDecimal> supplier_amount_taxDeduction= getTaxDAmount(startDateTime,endDateTime);

        // 7. 获取该日期段之间的，供应商调整金额
        Map<String, BigDecimal> supplier_amount_change= getChangeAmount(startDateTime,endDateTime);


        Set<String> allSupplier = new TreeSet<>();
        allSupplier.addAll(supplier_amount_buyIn.keySet());
        allSupplier.addAll(supplier_amount_buyOut.keySet());

        allSupplier.addAll(supplier_amount_payShoes.keySet());
        allSupplier.addAll(supplier_amount_fine.keySet());
        allSupplier.addAll(supplier_amount_test.keySet());
        allSupplier.addAll(supplier_amount_taxSupplement.keySet());
        allSupplier.addAll(supplier_amount_taxDeduction.keySet());
        allSupplier.addAll(supplier_amount_change.keySet());
        log.info("supplier_amount_buyIn:{},size:{}",supplier_amount_buyIn,supplier_amount_buyIn.size());
        log.info("supplier_amount_buyOut:{},size:{}",supplier_amount_buyOut,supplier_amount_buyOut.size());
        log.info("supplier_amount_payShoes:{},size:{}",supplier_amount_payShoes,supplier_amount_payShoes.size());
        log.info("supplier_amount_fine:{},size:{}",supplier_amount_fine,supplier_amount_fine.size());
        log.info("supplier_amount_test:{},size:{}",supplier_amount_test,supplier_amount_test.size());
        log.info("supplier_amount_taxSupplement:{},size:{}",supplier_amount_taxSupplement,supplier_amount_taxSupplement.size());
        log.info("supplier_amount_taxDeduction:{},size:{}",supplier_amount_taxDeduction,supplier_amount_taxDeduction.size());
        log.info("supplier_amount_change:{},size:{}",supplier_amount_change);

        log.info("allSupplier:{}",allSupplier);


        ArrayList<FinanceSummary> lists = new ArrayList<>();
        for(String supplierId : allSupplier){
            // 生成对账单信息。
            FinanceSummary fs = new FinanceSummary();
            fs.setSummaryDate(addDate);
            fs.setSupplierId(supplierId);

            BigDecimal buyIn = supplier_amount_buyIn.get(supplierId);
            if(buyIn==null){
                buyIn=new BigDecimal(0);
            }
            BigDecimal buyOut = supplier_amount_buyOut.get(supplierId);
            if(buyOut==null){
                buyOut=new BigDecimal(0);
            }
            fs.setBuyInAmount(buyIn);// 采购入库 显示付货款
            fs.setBuyOutAmount(BigDecimalUtil.mul(buyOut.toString(),"-1")); // 采购退料 显示扣货款
            fs.setBuyNetInAmount(buyIn.subtract(buyOut));

            BigDecimal payShoes = supplier_amount_payShoes.get(supplierId);
            fs.setPayShoesAmount(payShoes==null ? new BigDecimal(0):payShoes);
            fs.setPayShoesAmount(BigDecimalUtil.mul(fs.getPayShoesAmount().toString(),"-1"));//显示扣货款

            BigDecimal fine = supplier_amount_fine.get(supplierId);
            fs.setFineAmount(fine==null ? new BigDecimal(0):fine);
            fs.setFineAmount(BigDecimalUtil.mul(fs.getFineAmount().toString(),"-1"));//显示扣货款

            BigDecimal test = supplier_amount_test.get(supplierId);
            fs.setTestAmount(test==null ? new BigDecimal(0):test);
            fs.setTestAmount(BigDecimalUtil.mul(fs.getTestAmount().toString(),"-1"));//显示扣货款

            BigDecimal taxS = supplier_amount_taxSupplement.get(supplierId);
            fs.setTaxSupplement(taxS==null ? new BigDecimal(0):taxS);

            BigDecimal taxD = supplier_amount_taxDeduction.get(supplierId);
            fs.setTaxDeduction(taxD==null ? new BigDecimal(0):taxD);
            fs.setTaxDeduction(BigDecimalUtil.mul(fs.getTaxDeduction().toString(),"-1"));//显示扣货款

            BigDecimal change = supplier_amount_change.get(supplierId);
            fs.setChangeAmount(change==null ? new BigDecimal(0):change);

            BigDecimal needPayAmount = new BigDecimal(0);

            BigDecimal next = needPayAmount.add(fs.getBuyNetInAmount())
                    .add(fs.getPayShoesAmount())
                    .add(fs.getFineAmount())
                    .add(fs.getTestAmount())
                    .add(fs.getTaxSupplement())
                    .add(fs.getTaxDeduction())
                    .add(fs.getChangeAmount());
            fs.setStatus(DBConstant.TABLE_FINANCE_SUMMARY.STATUS_FIELDVALUE_1);
            fs.setCreated(now);
            fs.setUpdated(now);
            fs.setCreatedUser(principal.getName());
            fs.setUpdatedUser(principal.getName());

            fs.setNeedPayAmount(next);
            fs.setRoundDown(new BigDecimal("0"));

            // 应付0的不生成
            if(next.doubleValue() ==0){
                continue;
            }

            lists.add(fs);
        }
        if(lists.size() >0){
            financeSummaryService.saveBatch(lists);
        }else{
            ResponseResult.fail("该月份:"+addDate+",没有数据可生成!");
        }

        return ResponseResult.succ("生成成功！");
    }

    private Map<String, BigDecimal> getChangeAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<FinanceSupplierChange> outs = financeSupplierChangeService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(FinanceSupplierChange out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getTaxDAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<FinanceSupplierTaxDeduction> outs = financeSupplierTaxDeductionService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(FinanceSupplierTaxDeduction out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getTaxSAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<FinanceSupplierTaxSupplement> outs = financeSupplierTaxSupplementService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(FinanceSupplierTaxSupplement out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getTestAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<FinanceSupplierTest> outs = financeSupplierTestService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(FinanceSupplierTest out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getFineAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<FinanceSupplierFine> outs = financeSupplierFineService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(FinanceSupplierFine out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getPayShoesAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<FinanceSupplierPayshoes> outs = financeSupplierPayshoesService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(FinanceSupplierPayshoes out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getBuyOutAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<RepositoryBuyoutDocument> outs = repositoryBuyoutDocumentService.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(RepositoryBuyoutDocument out : outs){
            returnMap.put(out.getSupplierId(),new BigDecimal(out.getTotalAmount()));
        }
        return returnMap;
    }

    private Map<String, BigDecimal> getBuyInAmount(LocalDate startDateTime, LocalDate endDateTime) {
        List<RepositoryBuyinDocument> ins = repositoryBuyinDocumentService.getSupplierTotalAmountBetweenDate(startDateTime, endDateTime);
        HashMap<String, BigDecimal> returnMap = new HashMap<>();
        for(RepositoryBuyinDocument in : ins){
            returnMap.put(in.getSupplierId(),new BigDecimal(in.getTotalAmount()));
        }
        return returnMap;
    }


    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('finance:summary:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('finance:summary:list')")
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

        FinanceSummary ppc = financeSummaryService.getById(id);
        ppc.setPicUrl(null);
        financeSummaryService.updateById(ppc);
        return ResponseResult.succ("删除成功");
    }

    @RequestMapping(value = "/uploadPic", method = RequestMethod.POST)
    public ResponseResult uploadFile(Long id, MultipartFile[] files) {
        if(id==null ){
            return ResponseResult.fail("没有ID，请先保存记录");
        }
        FinanceSummary ppc = financeSummaryService.getById(id);
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
                financeSummaryService.updateById(ppc);
            }catch (Exception e){
                log.error("报错..",e);
            }
        }
        return ResponseResult.succ("");
    }


    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('finance:summary:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSummary fsp = financeSummaryService.getById(id);
        BaseSupplier bs = baseSupplierService.getById(fsp.getSupplierId());
        fsp.setSupplierName(bs.getName());
        List<FinanceSummaryDetails> details = financeSummaryDetailsService.listByForeignId(id);
        fsp.setRowList(details);
        return ResponseResult.succ(fsp);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('finance:summary:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody FinanceSummary fsp)
            throws Exception{

        if(fsp.getRowList() ==null || fsp.getRowList().size() ==0){
            return ResponseResult.fail("详情内容不能为空");
        }

        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdatedUser(principal.getName());
        try {

            //1. 先删除老的，再插入新的
            financeSummaryDetailsService.removeByDocId(fsp.getId());

            financeSummaryService.updateById(fsp);

            for (FinanceSummaryDetails item : fsp.getRowList()){
                item.setId(null);
                item.setSummaryId(fsp.getId());
                //新增物料，赋值初始数值
                item.setCreated(LocalDateTime.now());
                item.setUpdated(LocalDateTime.now());
                item.setCreatedUser(principal.getName());
                item.setUpdatedUser(principal.getName());
            }

            financeSummaryDetailsService.saveBatch(fsp.getRowList());
            log.info("对账模块-更新内容:{}",fsp);

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
     * 获取对账 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('finance:summary:list')")
    public ResponseResult list( String searchField, String status,String payStatus,
                                String searchStartDate,String searchEndDate,
                                String searchStartSettleDate,String searchEndSettleDate,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<FinanceSummary> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            }
            else if (searchField.equals("id")) {
                queryField = "id";

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
                    else if (oneField.equals("id")) {
                        theQueryField = "id";

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
        if(StringUtils.isNotBlank(status)){
            String[] split = status.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            return ResponseResult.fail("结账状态不能为空");
        }

        List<Long> payStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(payStatus)){
            String[] split = payStatus.split(",");
            for (String statusVal : split){
                payStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(payStatusList.size() == 0){
            return ResponseResult.fail("结清状态不能为空");
        }

        pageData = financeSummaryService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,payStatusList,queryMap,searchStartDate,searchEndDate,searchStartSettleDate,searchEndSettleDate);

        for(FinanceSummary fs : pageData.getRecords()){
            fs.setOtherTotalAmount(fs.getPayShoesAmount()
                    .add(fs.getTestAmount())
                    .add(fs.getFineAmount())
                    .add(fs.getChangeAmount())
                    .add(fs.getTaxSupplement())
                    .add(fs.getTaxDeduction())
                    .add(fs.getRoundDown()));
        }

        return ResponseResult.succ(pageData);
    }

}
