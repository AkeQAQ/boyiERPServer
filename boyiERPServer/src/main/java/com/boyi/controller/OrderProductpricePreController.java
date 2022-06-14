package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.style.Align;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.LuckeySheetPOIUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 新产品成本核算-报价 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Slf4j
@RestController
@RequestMapping("/order/productPricePre")
public class OrderProductpricePreController extends BaseController {

    @Value("${orderProductPrice.pre}")
    private String orderProductPricePrePath;

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('order:productPricePre:list')")
    public void export(HttpServletResponse response, @RequestBody LuckySheetExportRequestDTO param) {
        String str = null;
        log.info("导出核算excel:",param);
//        if(param instanceof  JSONObject){
             str = param.getExceldatas().replace("&#xA;", "\\r\\n");//去除luckysheet中 &#xA 的换行
//        }else{
//             str = ((JSONArray)param).getJSONObject(0).get("exceldatas").toString().replace("&#xA;", "\\r\\n");//去除luckysheet中 &#xA 的换行
//        }

        LuckeySheetPOIUtils.exportLuckySheetXlsx(str,response,"");
    }


    /**
     * 查询实际报价详情内容
     */
    @GetMapping("/queryRealById")
    @PreAuthorize("hasAuthority('order:productPricePre:list')")
    public ResponseResult queryRealById(Long id) {
        OrderProductpricePre orderPrice = orderProductpricePreService.getById(id);
        if(orderPrice.getRealJson() == null || orderPrice.getRealJson().isEmpty()){
            orderPrice.setRealJson(orderPrice.getExcelJson());
        }
        return ResponseResult.succ(orderPrice);
    }

    /**
     * 保存实际报价
     */
    @PostMapping("/setStreadReal")
    @PreAuthorize("hasAuthority('order:productPricePre:real')")
    public ResponseResult setStreadReal(Principal principal,@Validated @RequestBody OrderProductpricePre orderProductpricePre) {
        LocalDateTime now = LocalDateTime.now();
        orderProductpricePre.setUpdated(now);
        orderProductpricePre.setUpdateUser(principal.getName());

        orderProductpricePre.setRealPriceLastUpdateDate(now);
        orderProductpricePre.setRealPriceLastUpdateUser(principal.getName());


        try {
            OrderProductpricePre old = orderProductpricePreService.getById(orderProductpricePre.getId());
            Integer ykStatus = calYKStatus(old.getPrice(),orderProductpricePre.getRealPrice());
            orderProductpricePre.setYkStatus(ykStatus);
            orderProductpricePreService.updateById(orderProductpricePre);
            return ResponseResult.succ("保存实际价格成功");
        } catch (Exception e) {
            log.error("修改异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 获取报价模板
     */
    @GetMapping("/getStreadDemo")
    @PreAuthorize("hasAuthority('order:productPricePre:list')")
    public ResponseResult getStreadDemo() {
        try {
            SpreadDemo dbObj = spreadDemoService.getByType(DBConstant.TABLE_SPREAD_DEMO.TYPE_BAOJIA_FIELDVALUE_0);
            return ResponseResult.succ(dbObj);
        } catch (Exception e) {
            log.error("设置模板异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 设置报价模板
     */
    @PostMapping("/setStreadDemo")
    @PreAuthorize("hasAuthority('order:productPricePre:save')")
    public ResponseResult setStreadDemo(@Validated @RequestBody SpreadDemo spreadDemo) {
        try {
            Thread.sleep(3000);
            SpreadDemo dbObj = spreadDemoService.getByType(DBConstant.TABLE_SPREAD_DEMO.TYPE_BAOJIA_FIELDVALUE_0);
            if(dbObj == null ){
                spreadDemo.setType(DBConstant.TABLE_SPREAD_DEMO.TYPE_BAOJIA_FIELDVALUE_0);
                spreadDemoService.save(spreadDemo);
            }else {
                dbObj.setDemoJson(spreadDemo.getDemoJson());
                spreadDemoService.updateById(dbObj);
            }
            return ResponseResult.succ("设置模板成功");
        } catch (Exception e) {
            log.error("设置模板异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:productPricePre:update')")
    public ResponseResult update(Principal principal,@Validated @RequestBody OrderProductpricePre orderProductpricePre) {
        LocalDateTime now = LocalDateTime.now();
        orderProductpricePre.setUpdated(now);
        orderProductpricePre.setUpdateUser(principal.getName());
        orderProductpricePre.setStatus(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_1);
        orderProductpricePre.setPriceLastUpdateDate(now);
        orderProductpricePre.setPriceLastUpdateUser(principal.getName());

        try {
            Integer ykStatus = calYKStatus(orderProductpricePre.getPrice(),orderProductpricePre.getRealPrice());
            orderProductpricePre.setYkStatus(ykStatus);
            orderProductpricePreService.updateById(orderProductpricePre);

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("修改异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }



    /**
     * 新增
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:productPricePre:save')")
    public ResponseResult save(Principal principal,@Validated @RequestBody OrderProductpricePre orderProductpricePre) {
        LocalDateTime now = LocalDateTime.now();
        orderProductpricePre.setCreated(now);
        orderProductpricePre.setUpdated(now);
        orderProductpricePre.setCreatedUser(principal.getName());
        orderProductpricePre.setUpdateUser(principal.getName());
        orderProductpricePre.setStatus(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_1);
        orderProductpricePre.setPriceLastUpdateDate(now);
        orderProductpricePre.setPriceLastUpdateUser(principal.getName());

        try {
            OrderProductpricePre old = orderProductpricePreService.getByCustomerAndCompanyNum(orderProductpricePre.getCustomer(),
                    orderProductpricePre.getCompanyNum());
            if(old != null){
                return ResponseResult.fail("该客户公司，该货号已存在历史记录!不允许添加");
            }
            Integer ykStatus = calYKStatus(orderProductpricePre.getPrice(),orderProductpricePre.getRealPrice());
            orderProductpricePre.setYkStatus(ykStatus);
            orderProductpricePreService.save(orderProductpricePre);
            return ResponseResult.succ("新增成功");
        } catch (Exception e) {
            log.error("插入异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    // 计算盈亏方式
    private Integer calYKStatus(Double price, Double realPrice) {
        if(price ==null || realPrice == null ){
            return DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.YK_STATUS_FIELDVALUE_1;
        }else if(price.doubleValue() == realPrice.doubleValue()){
            return DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.YK_STATUS_FIELDVALUE_0;
        }else if(price > realPrice){
            return DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.YK_STATUS_FIELDVALUE_2;
        }else if(price < realPrice){
            return DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.YK_STATUS_FIELDVALUE_3;
        }else {
            throw new RuntimeException("存在没考虑的情况");
        }
    }


    @PostMapping("/returnValid")
    @PreAuthorize("hasAuthority('order:productPricePre:returnValid')")
    public ResponseResult returnValid(Principal principal,@RequestBody Long id) {

        orderProductpricePreService.updateStatusReturn(principal.getName(),id);
        return ResponseResult.succ("反审核成功");
    }

    @PostMapping("/returnRealValid")
    @PreAuthorize("hasAuthority('order:productPricePre:returnRealValid')")
    public ResponseResult returnRealValid(Principal principal,@RequestBody Long id) {

        orderProductpricePreService.updateStatusReturnReal(principal.getName(),id);
        return ResponseResult.succ("反审核成功");
    }

    @PostMapping("/valid")
    @PreAuthorize("hasAuthority('order:productPricePre:valid')")
    public ResponseResult valid(Principal principal,@RequestBody Long id) {

        orderProductpricePreService.updateStatusSuccess(principal.getName(),id);
        return ResponseResult.succ("审核成功");
    }


    @PostMapping("/realValid")
    @PreAuthorize("hasAuthority('order:productPricePre:realValid')")
    public ResponseResult realValid(Principal principal,@RequestBody Long id) {
        orderProductpricePreService.updateStatusFinal(principal.getName(),id);
        return ResponseResult.succ("审核成功");
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('order:productPricePre:down')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        OrderProductpricePre orderProductpricePre = orderProductpricePreService.getById(id);
        String fileName = orderProductpricePre.getSavePath().replace(orderProductPricePrePath, "");
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String(fileName.getBytes("ISO8859-1")));
        response.setHeader("filename",fileName );

        FileInputStream fis = new FileInputStream(new File(orderProductpricePre.getSavePath()));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("删除成功");
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:productPricePre:del')")
    public ResponseResult delete(@RequestBody Long id) {

        OrderProductpricePre old = orderProductpricePreService.getById(id);
        boolean flag = orderProductpricePreService.removeById(id);
        log.info("删除新成品核算预估表信息,id:{},是否成功：{}",id,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("新成品核算预估删除失败");
        }
        return ResponseResult.succ("删除成功");
    }


    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:productPricePre:list')")
    public ResponseResult queryById(Long id) {
        OrderProductpricePre orderPrice = orderProductpricePreService.getById(id);
        return ResponseResult.succ(orderPrice);
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('order:productPricePre:save')")
    public ResponseResult upload(Principal principal, String companyNum, String customer,Double price ,MultipartFile[] files) {
        LocalDateTime now = LocalDateTime.now();
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
        }
    }

    /**
     * 获取新成品核算预估 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('order:productPricePre:list')")
    public ResponseResult list(String searchStr, String searchField,String ykStatus) {
        Page<OrderProductpricePre> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("companyNum")) {
                queryField = "company_num";
            }
            else if (searchField.equals("customer")) {
                queryField = "customer";

            }
        }
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(ykStatus)){
            String[] split = ykStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            return ResponseResult.fail("盈亏状态不能为空");
        }

        QueryWrapper<OrderProductpricePre> queryWrapper = new QueryWrapper<>();
        pageData = orderProductpricePreService.page(getPage(), queryWrapper
                .like(StrUtil.isNotBlank(searchStr)
                        && StrUtil.isNotBlank(searchField), queryField, searchStr)
                .in(searchStatusList != null && searchStatusList.size() > 0,DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.YK_STATUS_FIELDNAME,searchStatusList)

                .select(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.ID_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COMPANY_NUM_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.COSTOMER_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.PRICE_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.DEAL_PRICE_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.REAL_PRICE_LAST_UPDATE_USER_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.REAL_PRICE_LAST_UPDATE_DATE_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.PRICE_LAST_UPDATE_DATE_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.PRICE_LAST_UPDATE_USER_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.REAL_PRICE_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDNAME,
                        DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.YK_STATUS_FIELDNAME
                        )
                .orderByDesc(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.CREATED_FIELDNAME)
        );
        return ResponseResult.succ(pageData);
    }

}
