package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplier;
import com.boyi.entity.FinanceSupplierTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
 * @since 2023-02-26
 */
@Slf4j
@RestController
@RequestMapping("/finance/supplierTest")
public class FinanceSupplierTestController extends BaseController {


    public static final Map<Long,String> locks = new ConcurrentHashMap<>();

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('finance:test:list')")
    public ResponseResult lockById(Principal principal, Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('finance:test:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('finance:test:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            List<FinanceSupplierTest> olds = financeSupplierTestService.listByIds(Arrays.asList(ids));
            for(FinanceSupplierTest old : olds){
                String user = locks.get(old.getId());
                if(StringUtils.isNotBlank(user)){
                    return ResponseResult.fail("单据被["+user+"]占用");
                }
            }


            boolean flag = financeSupplierTestService.removeByIds(Arrays.asList(ids));

            log.info("删除检测费表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("检测费删除失败");
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
    @PreAuthorize("hasAuthority('finance:test:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierTest fsp = financeSupplierTestService.getById(id);
        BaseSupplier bs = baseSupplierService.getById(fsp.getSupplierId());
        fsp.setSupplierName(bs.getName());
        return ResponseResult.succ(fsp);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('finance:test:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody FinanceSupplierTest fsp)
            throws Exception{

        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus( DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_2);
        try {
            financeSupplierTestService.updateById(fsp);

            return ResponseResult.succ("编辑成功");
        }
        catch (Exception e) {
            log.error("更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产检测费，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('finance:test:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody FinanceSupplierTest fsp)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        fsp.setCreated(now);
        fsp.setUpdated(now);
        fsp.setCreatedUser(principal.getName());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_2);
        try {

            financeSupplierTestService.save(fsp);

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",fsp.getId());
        }catch (DuplicateKeyException de){
            return ResponseResult.fail("单据不能重复!");
        }
        catch (Exception e) {
            log.error("检测费单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取检测费 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('finance:test:list')")
    public ResponseResult list( String searchField, String searchStatus,
                                String searchStartDate,String searchEndDate,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<FinanceSupplierTest> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
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

        pageData = financeSupplierTestService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap,searchStartDate,searchEndDate);

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('finance:test:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {
        FinanceSupplierTest old = financeSupplierTestService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierTest fsp = new FinanceSupplierTest();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_2);
        financeSupplierTestService.updateById(fsp);
        log.info("财务模块-检测费模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("提交成功");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('finance:test:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        FinanceSupplierTest old = financeSupplierTestService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_2 &&
                old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        FinanceSupplierTest fsp = new FinanceSupplierTest();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_1);
        financeSupplierTestService.updateById(fsp);
        log.info("财务模块-检测费模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("撤销通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('finance:test:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<FinanceSupplierTest> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            FinanceSupplierTest old = financeSupplierTestService.getById(id);
            if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_2
                    && old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_3){
                return ResponseResult.fail("状态已被修改.请刷新");
            }

            FinanceSupplierTest fsp = new FinanceSupplierTest();
            fsp.setUpdated(LocalDateTime.now());
            fsp.setUpdateUser(principal.getName());
            fsp.setId(id);
            fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_0);
            lists.add(fsp);

        }
        financeSupplierTestService.updateBatchById(lists);
        return ResponseResult.succ("批量审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('finance:test:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierTest old = financeSupplierTestService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierTest fsp = new FinanceSupplierTest();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_0);
        financeSupplierTestService.updateById(fsp);
        log.info("财务模块-检测费模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('finance:test:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        FinanceSupplierTest old = financeSupplierTestService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        FinanceSupplierTest fsp = new FinanceSupplierTest();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_3);
        financeSupplierTestService.updateById(fsp);
        log.info("财务模块-反审核通过内容:{}",fsp);

        return ResponseResult.succ("反审核成功");
    }
}
