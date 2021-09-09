package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.OrderProductpricePre;
import com.boyi.entity.OrderProductpriceReal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@RequestMapping("/order/productPriceReal")
public class OrderProductpriceRealController extends BaseController {

    @Value("${orderProductPrice.real}")
    private String orderProductPriceRealPath;

    @Transactional
    @PostMapping("/returnValid")
    @PreAuthorize("hasAuthority('order:productPriceReal:returnValid')")
    public ResponseResult returnValid(@RequestBody Long id) {

        orderProductpriceRealService.updateStatusReturn(id);
        return ResponseResult.succ("反审核成功");
    }

    @Transactional
    @PostMapping("/valid")
    @PreAuthorize("hasAuthority('order:productPriceReal:valid')")
    public ResponseResult valid(@RequestBody Long id) {

        OrderProductpriceReal old = orderProductpriceRealService.getById(id);
        orderProductpriceRealService.updateStatusSuccess(id);
        return ResponseResult.succ("审核成功");
    }

    @Transactional
    @PostMapping("/down")
    @PreAuthorize("hasAuthority('order:productPriceReal:down')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        OrderProductpriceReal orderProductpriceReal = orderProductpriceRealService.getById(id);
        String fileName = orderProductpriceReal.getSavePath().replace(orderProductPriceRealPath, "");
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String(fileName.getBytes("ISO8859-1")));
        response.setHeader("filename",fileName );

        FileInputStream fis = new FileInputStream(new File(orderProductpriceReal.getSavePath()));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("删除成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:productPriceReal:del')")
    public ResponseResult delete(@RequestBody Long id) {

        OrderProductpriceReal old = orderProductpriceRealService.getById(id);
        boolean flag = orderProductpriceRealService.removeById(id);
        log.info("删除新成品核算预估表信息,id:{},是否成功：{}",id,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("新成品核算预估删除失败");
        }
        // 删除文件
        File file = new File(old.getSavePath());
        file.delete();
        return ResponseResult.succ("删除成功");
    }


    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:productPriceReal:list')")
    public ResponseResult queryById(Long id) {
        OrderProductpriceReal orderPrice = orderProductpriceRealService.getById(id);
        return ResponseResult.succ(orderPrice);
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('order:productPriceReal:save')")
    public ResponseResult upload(Principal principal, Double price ,
                                 Long preId,
                                 MultipartFile[] files) {
        OrderProductpricePre pre = orderProductpricePreService.getByIdAndStatusSuccess(preId);
        if(pre == null){
            return ResponseResult.fail("该"+preId+"报价编号不存在 审核完成的记录！请检查");
        }
        LocalDateTime now = LocalDateTime.now();
        OrderProductpriceReal orderProductpriceReal = new OrderProductpriceReal();
        orderProductpriceReal.setCreated(now);
        orderProductpriceReal.setUpdated(now);
        orderProductpriceReal.setCreatedUser(principal.getName());
        orderProductpriceReal.setUpdateUser(principal.getName());
        orderProductpriceReal.setStatus(DBConstant.TABLE_ORDER_PRODUCTPRICEPRE.STATUS_FIELDVALUE_1);
        orderProductpriceReal.setCompanyNum(pre.getCompanyNum());
        orderProductpriceReal.setCustomer(pre.getCustomer());
        orderProductpriceReal.setPrice(price);
        orderProductpriceReal.setPreId(preId);

        MultipartFile file = files[0];

        orderProductpriceReal.setUploadName( file.getOriginalFilename());
        try {
            OrderProductpriceReal old = orderProductpriceRealService.getByCustomerAndCompanyNum(orderProductpriceReal.getCustomer(),
                    orderProductpriceReal.getCompanyNum());
            if(old != null){
                return ResponseResult.fail("该客户公司，该货号已存在历史记录!不允许添加");
            }
            orderProductpriceRealService.save(orderProductpriceReal);

            // 1. 存储文件
            String storePath = orderProductPriceRealPath
                    + orderProductpriceReal.getCompanyNum() + "-"
                    + orderProductpriceReal.getCustomer() + "-"
                    + file.getOriginalFilename();
            file.transferTo(new File(storePath));
            // 2. 根据客户公司，公司货号唯一编码，去更新文件路径

            orderProductpriceRealService.updateFilePathByCompanyNumAndCustomer(orderProductpriceReal.getCompanyNum()
                    , orderProductpriceReal.getCustomer(),storePath);
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
    @PreAuthorize("hasAuthority('order:productPriceReal:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<OrderProductpriceReal> pageData = null;
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

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = orderProductpriceRealService.page(getPage(), new QueryWrapper<OrderProductpriceReal>()
                .like(StrUtil.isNotBlank(searchStr)
                        && StrUtil.isNotBlank(searchField), queryField, searchStr));
        return ResponseResult.succ(pageData);
    }

}
