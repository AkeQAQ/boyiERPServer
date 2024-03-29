package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
 * @since 2023-03-05
 */
@Slf4j
@RestController
@RequestMapping("/externalAccount/repositorySendOutGoods")
public class ExternalAccountRepositorySendOutGoodsController extends BaseController {


    public static final Map<Long,String> locks = new ConcurrentHashMap<>();


    /**
     *  获取选中的批量打印的数据
     * @param principal
     * @param ids
     * @return
     */
    @PostMapping("/getBatchPrintByIds")
    public ResponseResult getBatchPrintByIds(Principal principal, @RequestBody Long[] ids) {
        ArrayList<ExternalAccountRepositorySendOutGoods> lists = new ArrayList<>();

        for (Long id : ids){

            ExternalAccountRepositorySendOutGoods repositoryBuyinDocument = externalAccountRepositorySendOutGoodsService.getById(id);

            List<ExternalAccountRepositorySendOutGoodsDetails> details = externalAccountRepositorySendOutGoodsDetailsService.listByDocumentId(id);

            double totalNum = 0d;
            double totalAmount = 0.0d;

            for (ExternalAccountRepositorySendOutGoodsDetails detail : details){


                    double amount = detail.getPrice().multiply(detail.getNum()).doubleValue() ;
                    detail.setAmount(new BigDecimal(amount).setScale(2,   BigDecimal.ROUND_HALF_UP));
                    totalAmount += amount;
                totalNum += detail.getNum().doubleValue();
            }

            repositoryBuyinDocument.setTotalNum( totalNum);
            repositoryBuyinDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue());

            repositoryBuyinDocument.setRowList(details);
            lists.add(repositoryBuyinDocument);
        }
        return ResponseResult.succ(lists);

    }
    
    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult lockById(Principal principal, Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            List<ExternalAccountRepositorySendOutGoods> olds = externalAccountRepositorySendOutGoodsService.listByIds(Arrays.asList(ids));
            for(ExternalAccountRepositorySendOutGoods old : olds){
                String user = locks.get(old.getId());
                if(StringUtils.isNotBlank(user)){
                    return ResponseResult.fail("单据被["+user+"]占用");
                }
            }


            boolean flag = externalAccountRepositorySendOutGoodsService.removeByIds(Arrays.asList(ids));

            log.info("删除成品出库表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("成品出库删除失败");
            }

            externalAccountRepositorySendOutGoodsDetailsService.delByDocumentIds(ids);

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
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        ExternalAccountRepositorySendOutGoods fsp = externalAccountRepositorySendOutGoodsService.getById(id);
        List<ExternalAccountRepositorySendOutGoodsDetails> details = externalAccountRepositorySendOutGoodsDetailsService.listByForeignId(id);
        fsp.setRowList(details);
        return ResponseResult.succ(fsp);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody ExternalAccountRepositorySendOutGoods fsp)
            throws Exception{

        if(fsp.getRowList() ==null || fsp.getRowList().size() ==0){
            return ResponseResult.fail("详情内容不能为空");
        }

        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus( DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_2);
        try {

            //1. 先删除老的，再插入新的
            boolean flag = externalAccountRepositorySendOutGoodsDetailsService.removeByDocId(fsp.getId());

            if(flag){
                externalAccountRepositorySendOutGoodsService.updateById(fsp);

                for (ExternalAccountRepositorySendOutGoodsDetails item : fsp.getRowList()){
                    item.setId(null);
                    item.setSendId(fsp.getId());
                    item.setCreated(LocalDateTime.now());
                    item.setUpdated(LocalDateTime.now());
                }

                externalAccountRepositorySendOutGoodsDetailsService.saveBatch(fsp.getRowList());
                log.info("成品出库模块-更新内容:{}",fsp);
            }else{
                throw new RuntimeException("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        }
        catch (Exception e) {
            log.error("更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产成品出库，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody ExternalAccountRepositorySendOutGoods fsp)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        fsp.setCreated(now);
        fsp.setUpdated(now);
        fsp.setCreatedUser(principal.getName());
        fsp.setUpdateUser(principal.getName());
        fsp.setStatus(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_2);
        try {

            externalAccountRepositorySendOutGoodsService.save(fsp);

            for (ExternalAccountRepositorySendOutGoodsDetails item : fsp.getRowList()){
                item.setSendId(fsp.getId());
                item.setCreated(now);
                item.setUpdated(now);
            }

            externalAccountRepositorySendOutGoodsDetailsService.saveBatch(fsp.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",fsp.getId());
        }
        catch (Exception e) {
            log.error("成品出库单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取成品出库 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult list( String searchField, String searchStatus,
                                String searchStartDate,String searchEndDate,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ExternalAccountRepositorySendOutGoods> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("customerName")) {
                queryField = "customer_name";
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
                    if (oneField.equals("customer_name")) {
                        theQueryField = "customer_name";
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

        pageData = externalAccountRepositorySendOutGoodsService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap,searchStartDate,searchEndDate);

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {
        ExternalAccountRepositorySendOutGoods old = externalAccountRepositorySendOutGoodsService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        ExternalAccountRepositorySendOutGoods fsp = new ExternalAccountRepositorySendOutGoods();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_2);
        externalAccountRepositorySendOutGoodsService.updateById(fsp);
        log.info("财务模块-成品出库模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("提交成功");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        ExternalAccountRepositorySendOutGoods old = externalAccountRepositorySendOutGoodsService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_2 &&
                old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_3
        ){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        ExternalAccountRepositorySendOutGoods fsp = new ExternalAccountRepositorySendOutGoods();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_1);
        externalAccountRepositorySendOutGoodsService.updateById(fsp);
        log.info("财务模块-成品出库模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("撤销通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<ExternalAccountRepositorySendOutGoods> lists = new ArrayList<>();

        for (Long id : ids){
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            ExternalAccountRepositorySendOutGoods old = externalAccountRepositorySendOutGoodsService.getById(id);
            if(old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_2
                    && old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_3){
                return ResponseResult.fail("状态已被修改.请刷新");
            }

            ExternalAccountRepositorySendOutGoods fsp = new ExternalAccountRepositorySendOutGoods();
            fsp.setUpdated(LocalDateTime.now());
            fsp.setUpdateUser(principal.getName());
            fsp.setId(id);
            fsp.setStatus(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_0);
            lists.add(fsp);

        }
        externalAccountRepositorySendOutGoodsService.updateBatchById(lists);
        return ResponseResult.succ("批量审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        ExternalAccountRepositorySendOutGoods old = externalAccountRepositorySendOutGoodsService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_2
                && old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_3){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        ExternalAccountRepositorySendOutGoods fsp = new ExternalAccountRepositorySendOutGoods();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_0);
        externalAccountRepositorySendOutGoodsService.updateById(fsp);
        log.info("财务模块-成品出库模块-审核通过内容:{}",fsp);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('externalAccount:sendOutGoods:list')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        ExternalAccountRepositorySendOutGoods old = externalAccountRepositorySendOutGoodsService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }
        ExternalAccountRepositorySendOutGoods fsp = new ExternalAccountRepositorySendOutGoods();
        fsp.setUpdated(LocalDateTime.now());
        fsp.setUpdateUser(principal.getName());
        fsp.setId(id);
        fsp.setStatus(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS.STATUS_FIELDVALUE_3);
        externalAccountRepositorySendOutGoodsService.updateById(fsp);
        log.info("财务模块-反审核通过内容:{}",fsp);

        return ResponseResult.succ("反审核成功");
    }
}
