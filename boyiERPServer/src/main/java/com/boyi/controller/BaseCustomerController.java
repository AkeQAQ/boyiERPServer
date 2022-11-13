package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseCustomer;
import com.boyi.entity.BaseDepartment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-11-13
 */
@RestController
@RequestMapping("/baseData/customer")
@Slf4j
public class BaseCustomerController extends BaseController {


    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('baseData:customer:list')")
    public ResponseResult getSearchAllData() {
        List<BaseCustomer> customers = baseCustomerService.list();

        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        customers.forEach(obj ->{
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getName() ).put("name", obj.getName()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取客户 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:customer:list')")
    public ResponseResult list(String searchName) {
        Page<BaseCustomer> pageData = baseCustomerService.pageBySearch(getPage(),searchName);
        return ResponseResult.succ(pageData);
    }


    /**
     *  新增客户
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:customer:save')")
    public ResponseResult save(@Validated @RequestBody BaseCustomer baseCustomer) {
        LocalDateTime now = LocalDateTime.now();
        baseCustomer.setCreated(now);
        baseCustomer.setUpdated(now);
        baseCustomerService.save(baseCustomer);
        return ResponseResult.succ("新增成功");
    }
    /**
     *  查询客户
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:customer:list')")
    public ResponseResult queryById(Long id) {
        BaseCustomer baseCustomer = baseCustomerService.getById(id);
        return ResponseResult.succ(baseCustomer);
    }

    /**
     *  修改客户
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('baseData:customer:update')")
    public ResponseResult update(@Validated @RequestBody BaseCustomer baseCustomer) {
        baseCustomer.setUpdated(LocalDateTime.now());
        try {
            baseCustomerService.updateById(baseCustomer);
            return ResponseResult.succ("编辑成功");
        }catch (DuplicateKeyException e){
            return ResponseResult.fail("用户名重复!");
        }catch (DataIntegrityViolationException e){
            return ResponseResult.fail("被其他表数据外键关联，无法修改");
        }

    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:customer:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        try{
            baseCustomerService.removeByIds(Arrays.asList(ids));
        }catch (DataIntegrityViolationException e){
            return ResponseResult.fail("被其他表数据外键关联，无法删除");
        }

        return ResponseResult.succ("删除成功");
    }
}
