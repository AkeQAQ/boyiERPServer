package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.EmailUtils;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.common.utils.ThreadUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.OrderBeforeProductionProgress;
import com.boyi.entity.OrderBeforeProductionProgressDetail;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.entity.RepositoryBuyinDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-08-27
 */
@Slf4j
@RestController
@RequestMapping("/order/beforeProductionProgress")
public class OrderBeforeProductionProgressController extends BaseController {

    @Value("${boyi.orderBeforeProductionProgress.sureCustomerRequired.sureCustomerRequiredEmail}")
    private String sureCustomerRequiredEmail;

    @Value("${boyi.orderBeforeProductionProgress.sureCustomerRequired.sureCustomerRequiredEmails}")
    private String[] sureCustomerRequiredEmails;

    @Value("${boyi.orderBeforeProductionProgress.sureShoes.sureShoesEmail}")
    private String sureShoesEmail;
    @Value("${boyi.orderBeforeProductionProgress.sureShoes.sureShoesEmails}")
    private String[] sureShoesEmails;

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:list')")
    public ResponseResult list(String searchField, String searchStartDate, String searchEndDate, String searchType, @RequestBody Map<String,Object> params) {

        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("productNum")) {
                queryField = "product_num";
            }
            else if (searchField.equals("productBrand")) {
                queryField = "product_brand";

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
                    if (oneField.equals("productNum")) {
                        theQueryField = "product_num";
                    }
                    else if (oneField.equals("productBrand")) {
                        theQueryField = "product_brand";

                    }else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
        List<String> searchStatusList = new ArrayList<String>();
        if(StringUtils.isNotBlank(searchType)){
            String[] split = searchType.split(",");
            for (String statusVal : split){
                searchStatusList.add(statusVal);
            }
        }

        if(searchStatusList.size() == 0){
            return ResponseResult.fail("类型不能为空");
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        Page<OrderBeforeProductionProgress> pageData = orderBeforeProductionProgressService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);
        List<OrderBeforeProductionProgress> records = pageData.getRecords();
        for (int i = 0; i < records.size(); i++) {
            OrderBeforeProductionProgress obpp = records.get(i);
            obpp.setCurrentIndex(9999);
            List<OrderBeforeProductionProgressDetail> details = orderBeforeProductionProgressDetailService.listByForeignId(obpp.getId());
            obpp.setDetails(details);
            for (int j = 0; j < details.size(); j++) {
                OrderBeforeProductionProgressDetail detail = details.get(j);
                if(detail.getIsCurrent().equals(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_0)){
                    obpp.setCurrentIndex(j);
                }
            }
        }


        return ResponseResult.succ(pageData);
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody OrderBeforeProductionProgress orderBeforeProductionProgress) {
        LocalDateTime now = LocalDateTime.now();
        orderBeforeProductionProgress.setCreated(now);
        orderBeforeProductionProgress.setCreatedUser(principal.getName());
        orderBeforeProductionProgress.setStatus(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.STATUS_FIELDVALUE_1);


        try {

            // 校验是否有重复的工厂型号和品牌
            orderBeforeProductionProgressService.save(orderBeforeProductionProgress);

            // 生成3个进度表子记录
            List<OrderBeforeProductionProgressDetail> lists = new ArrayList<>();

            OrderBeforeProductionProgressDetail detail = new OrderBeforeProductionProgressDetail();
            detail.setForeignId(orderBeforeProductionProgress.getId());
            detail.setTypeId(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.TYPE_ID_FIELDVALUE_10);
            detail.setCreated(now);
            detail.setCreatedUser(principal.getName());
            detail.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_0);
            lists.add(detail);

            OrderBeforeProductionProgressDetail detail2 = new OrderBeforeProductionProgressDetail();
            detail2.setForeignId(orderBeforeProductionProgress.getId());
            detail2.setTypeId(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.TYPE_ID_FIELDVALUE_20);
            detail2.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_1);
            lists.add(detail2);


            OrderBeforeProductionProgressDetail detail3 = new OrderBeforeProductionProgressDetail();
            detail3.setForeignId(orderBeforeProductionProgress.getId());
            detail3.setTypeId(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.TYPE_ID_FIELDVALUE_30);
            detail3.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_1);
            lists.add(detail3);

            orderBeforeProductionProgressDetailService.saveBatch(lists);

        } catch (DuplicateKeyException de){
            return ResponseResult.fail("货号，品牌不能重复!");
        }
        catch (Exception e) {
            log.error("产前沟通确认进度表，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
        return ResponseResult.succ("新增成功");
    }
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:list')")
    public ResponseResult queryById(Long id) {
        OrderBeforeProductionProgress obj = orderBeforeProductionProgressService.getById(id);
        return ResponseResult.succ(obj);
    }


    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:update')")
    public ResponseResult update(Principal principal,@Validated @RequestBody OrderBeforeProductionProgress progress) {
        progress.setUpdated(LocalDateTime.now());
        progress.setUpdatedUser(principal.getName());

        try{
            orderBeforeProductionProgressService.updateById(progress);

        } catch (DuplicateKeyException de){
            return ResponseResult.fail("货号，品牌不能重复!");
        } catch (Exception e) {
            log.error("产前沟通确认进度表，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
        return ResponseResult.succ("编辑成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        orderBeforeProductionProgressService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:valid')")
    @Transactional
    public ResponseResult statusPass(Principal principal,Long id) {

        OrderBeforeProductionProgress old = orderBeforeProductionProgressService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.STATUS_FIELDVALUE_1){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        OrderBeforeProductionProgress obj = new OrderBeforeProductionProgress();
        obj.setUpdated(LocalDateTime.now());
        obj.setUpdatedUser(principal.getName());
        obj.setId(id);
        obj.setStatus(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.STATUS_FIELDVALUE_0);
        orderBeforeProductionProgressService.updateById(obj);
        log.info("订单产前进度表模块-审核通过内容:{}",obj);

        List<OrderBeforeProductionProgressDetail> details = orderBeforeProductionProgressDetailService.listByForeignId(id);
        LocalDateTime now = LocalDateTime.now();

        // 确认进度，设置最后时间
        OrderBeforeProductionProgressDetail detail = details.get(0);
        detail.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_1);
        detail.setUpdated(now);
        detail.setUpdatedUser(principal.getName());

        // 客户要求，设置开始时间
        OrderBeforeProductionProgressDetail detail2 = details.get(1);
        detail2.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_0);
        detail2.setCreated(now);
        detail2.setCreatedUser(principal.getName());
        // 审核即：确认订单
        orderBeforeProductionProgressDetailService.updateBatchById(details);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:valid')")
    @Transactional
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {

        OrderBeforeProductionProgress old = orderBeforeProductionProgressService.getById(id);
        if(old.getStatus()!=DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.STATUS_FIELDVALUE_0){
            return ResponseResult.fail("状态已被修改.请刷新");
        }

        OrderBeforeProductionProgress obj = new OrderBeforeProductionProgress();
        obj.setUpdated(LocalDateTime.now());
        obj.setUpdatedUser(principal.getName());
        obj.setId(id);
        obj.setStatus(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_PROGRESS.STATUS_FIELDVALUE_1);
        orderBeforeProductionProgressService.updateById(obj);

        List<OrderBeforeProductionProgressDetail> details = orderBeforeProductionProgressDetailService.listByForeignId(id);
        LocalDateTime now = LocalDateTime.now();
        OrderBeforeProductionProgressDetail detail = details.get(0);
        detail.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_0);
        detail.setUpdated(now);
        detail.setUpdatedUser(principal.getName());
        OrderBeforeProductionProgressDetail detail2 = details.get(1);
        detail2.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_1);
        detail2.setUpdated(now);
        detail2.setUpdatedUser(principal.getName());
        // 审核即：确认订单
        orderBeforeProductionProgressDetailService.updateBatchById(details);

        log.info("订单产前进度表模块-反审核通过内容:{}",obj);
        return ResponseResult.succ("反审核通过");

    }


    /**
     * 获取details详情
     */
    @GetMapping("/getDetailsById")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:list')")
    public ResponseResult getDetailsById(Principal principal,String id)throws Exception {

        List<OrderBeforeProductionProgressDetail> details = orderBeforeProductionProgressDetailService.listByForeignId(Long.valueOf(id));

        return ResponseResult.succ(details);

    }

    /**
     * 获取detail详情
     */
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:valid')")
    public ResponseResult getDetailById(Principal principal,String id,Integer detailIndex)throws Exception {

        List<OrderBeforeProductionProgressDetail> details = orderBeforeProductionProgressDetailService.listByForeignId(Long.valueOf(id));

        OrderBeforeProductionProgressDetail detail = details.get(detailIndex);

        return ResponseResult.succ(detail);

    }

    /**
     * 当前步骤，确认 detail step 阶段内容
     */
    @PostMapping("/sureCustomerRequired")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:list')")
    public ResponseResult sureCustomerRequired(Principal principal,@RequestBody OrderBeforeProductionProgressDetail detail )throws Exception {

        detail.setUpdated(LocalDateTime.now());
        detail.setUpdatedUser(principal.getName());
        detail.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_1);
        orderBeforeProductionProgressDetailService.updateById(detail);
        // 当前确认，给下一个步骤状态设置为0
        List<OrderBeforeProductionProgressDetail> details = orderBeforeProductionProgressDetailService.listByForeignId(detail.getForeignId());

        OrderBeforeProductionProgress obpp = orderBeforeProductionProgressService.getById(detail.getForeignId());


        for (int i = 0; i < details.size(); i++) {
            OrderBeforeProductionProgressDetail obj = details.get(i);
            if(obj.getTypeId() > detail.getTypeId() ){
                obj.setIsCurrent(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.PROGRESS_FIELDVALUE_0);
                obj.setCreated(LocalDateTime.now());
                obj.setCreatedUser(principal.getName());
                orderBeforeProductionProgressDetailService.updateById(obj);
                // 发邮件
                if(detail.getTypeId().equals(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.TYPE_ID_FIELDVALUE_20)){
                    ThreadUtils.executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EmailUtils.sendMail(EmailUtils.MODULE_ORDER_BEFORE_PRODUCTION_PROGRESS_SURECUSTOMERREQUIRED_NAME
                                                +"【"+obpp.getProductNum()+"】"+"【"+obpp.getProductBrand()+"】"
                                        ,sureCustomerRequiredEmail,sureCustomerRequiredEmails,detail.getContent()==null?"":detail.getContent());
                            } catch (MessagingException e) {
                                log.error("error",e);
                            }
                        }
                    });

                }
                break;
            }
            if(i== details.size() -1){
                if(detail.getTypeId().equals(DBConstant.TABLE_ORDER_BEFORE_PRODUCT_DETAIL_PROGRESS.TYPE_ID_FIELDVALUE_30)){
                    ThreadUtils.executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EmailUtils.sendMail(EmailUtils.MODULE_ORDER_BEFORE_PRODUCTION_PROGRESS_SURECUSTOMER_NAME
                                                +"【"+obpp.getProductNum()+"】"+"【"+obpp.getProductBrand()+"】"
                                        ,sureShoesEmail,sureShoesEmails,detail.getContent()==null?"":detail.getContent());
                            } catch (MessagingException e) {
                                log.error("error",e);
                            }
                        }
                    });

                }
            }


        }


        return ResponseResult.succ(detail);

    }

    /**
     *  保存
     */
    @PostMapping("/sureCustomerRequiredSave")
    @PreAuthorize("hasAuthority('order:beforeProductionProgress:valid')")
    public ResponseResult sureCustomerRequiredSave(Principal principal,@RequestBody OrderBeforeProductionProgressDetail detail )throws Exception {

        detail.setUpdated(LocalDateTime.now());
        detail.setUpdatedUser(principal.getName());
        orderBeforeProductionProgressDetailService.updateById(detail);

        return ResponseResult.succ(detail);

    }


}
