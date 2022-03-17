package com.boyi.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.fileFilter.CraftPicFileFilter;
import com.boyi.common.utils.FileUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ProduceCraft;
import com.boyi.entity.ProduceCraft;
import com.boyi.entity.SpreadDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 工艺单
 * </p>
 *
 * @author sunke
 * @since 2021-09-09
 */
@Slf4j
@RestController
@RequestMapping("/produce/craft")
public class ProduceCraftController extends BaseController {
    @Value("${picture.craftPath}")
    private String pictureCraftPath;


    @RequestMapping(value = "/getPicturesById", method = RequestMethod.GET)
    public ResponseResult getPicturesById( String id) {
        // 根据ID 查询照片的路径和名字
        File directory = new File(pictureCraftPath);
        CraftPicFileFilter craftPicFileFilter = new CraftPicFileFilter(id);
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
    public ResponseResult delPic(String fileName) {
        File delFile = new File(pictureCraftPath, fileName);
        if(delFile.exists()){
            delFile.delete();
        }else{
            return ResponseResult.fail("文件["+fileName+"] 不存在,无法删除");
        }
        return ResponseResult.succ("删除成功");
    }

    @RequestMapping(value = "/uploadPic", method = RequestMethod.POST)
    public ResponseResult uploadFile(String id,MultipartFile[] files) {
        for (int i = 0; i < files.length; i++) {
            log.info("文件内容:{}",files[i]);
            MultipartFile file = files[i];
            try (InputStream fis = file.getInputStream();){
                String originalFilename = file.getOriginalFilename();
                String[] split = originalFilename.split("\\.");
                String suffix = split[split.length - 1];// 获取后缀

                FileUtils.writeFile(fis,pictureCraftPath,id+"_"+System.currentTimeMillis()+"."+suffix);
            }catch (Exception e){

            }
        }
        return ResponseResult.succ("");
    }

    /**
     *
     */
    @GetMapping("/getOneExcel")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public String getOneExcel(Long id) {
        ProduceCraft produceCraft = produceCraftService.getById(id);
        return produceCraft.getExcelJson();
    }

    /**
     * 查询最终详情内容
     */
    @GetMapping("/queryRealById")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult queryRealById(Long id) {
        ProduceCraft produceCraft = produceCraftService.getById(id);
        if(produceCraft.getRealJson() == null || produceCraft.getRealJson().isEmpty()){
            produceCraft.setRealJson(produceCraft.getExcelJson());
        }
        return ResponseResult.succ(produceCraft);
    }

    /**
     * 保存最终
     */
    @PostMapping("/setStreadReal")
    @PreAuthorize("hasAuthority('produce:craft:real')")
    public ResponseResult setStreadReal(Principal principal,@Validated @RequestBody ProduceCraft produceCraft) {
        LocalDateTime now = LocalDateTime.now();
        produceCraft.setUpdated(now);
        produceCraft.setUpdateUser(principal.getName());

        produceCraft.setLastUpdateDate(now);
        produceCraft.setLastUpdateUser(principal.getName());

        try {
            produceCraftService.updateById(produceCraft);
            return ResponseResult.succ("保存最終成功");
        } catch (Exception e) {
            log.error("修改异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 获取工艺单模板
     */
    @GetMapping("/getStreadDemo")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult getStreadDemo() {
        try {
            SpreadDemo dbObj = spreadDemoService.getByType(DBConstant.TABLE_SPREAD_DEMO.TYPE_GYD_FIELDVALUE_1);
            return ResponseResult.succ(dbObj);
        } catch (Exception e) {
            log.error("设置模板异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }

    /**
     * 设置工艺单模板
     */
    @PostMapping("/setStreadDemo")
    @PreAuthorize("hasAuthority('produce:craft:real')")
    public ResponseResult setStreadDemo(@Validated @RequestBody SpreadDemo spreadDemo) {
        try {
            SpreadDemo dbObj = spreadDemoService.getByType(DBConstant.TABLE_SPREAD_DEMO.TYPE_GYD_FIELDVALUE_1);
            if(dbObj == null ){
                spreadDemo.setType(DBConstant.TABLE_SPREAD_DEMO.TYPE_GYD_FIELDVALUE_1);
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
    @PreAuthorize("hasAuthority('produce:craft:update')")
    public ResponseResult update(Principal principal,@Validated @RequestBody ProduceCraft produceCraft) {
        LocalDateTime now = LocalDateTime.now();
        produceCraft.setUpdated(now);
        produceCraft.setUpdateUser(principal.getName());
        produceCraft.setStatus(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_1);
        produceCraft.setDevLastUpdateDate(now);
        produceCraft.setDevLastUpdateUser(principal.getName());
        try {
            produceCraftService.updateById(produceCraft);
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
    @PreAuthorize("hasAuthority('produce:craft:save')")
    public ResponseResult save(Principal principal,@Validated @RequestBody ProduceCraft produceCraft) {
        LocalDateTime now = LocalDateTime.now();
        produceCraft.setCreated(now);
        produceCraft.setUpdated(now);
        produceCraft.setCreatedUser(principal.getName());
        produceCraft.setUpdateUser(principal.getName());
        produceCraft.setStatus(DBConstant.TABLE_PRODUCE_CRAFT.STATUS_FIELDVALUE_1);
        produceCraft.setDevLastUpdateDate(now);
        produceCraft.setDevLastUpdateUser(principal.getName());

        try {
            ProduceCraft old = produceCraftService.getByCustomerAndCompanyNum(produceCraft.getCustomer(),
                    produceCraft.getCompanyNum());
            if(old != null){
                return ResponseResult.fail("该客户公司，该货号已存在历史记录!不允许添加");
            }
            produceCraftService.save(produceCraft);
            return ResponseResult.succ("新增成功");
        } catch (Exception e) {
            log.error("插入异常", e);
            return ResponseResult.fail(e.getMessage());
        }
    }


    @PostMapping("/returnValid")
    @PreAuthorize("hasAuthority('produce:craft:returnValid')")
    public ResponseResult returnValid(Principal principal,@RequestBody Long id) {

        produceCraftService.updateStatusReturn(principal.getName(),id);
        return ResponseResult.succ("反审核成功");
    }

    @PostMapping("/returnRealValid")
    @PreAuthorize("hasAuthority('produce:craft:returnRealValid')")
    public ResponseResult returnRealValid(Principal principal,@RequestBody Long id) {

        produceCraftService.updateStatusReturnReal(principal.getName(),id);
        return ResponseResult.succ("反审核成功");
    }

    @PostMapping("/valid")
    @PreAuthorize("hasAuthority('produce:craft:valid')")
    public ResponseResult valid(Principal principal,@RequestBody Long id) {

        produceCraftService.updateStatusSuccess(principal.getName(),id);
        return ResponseResult.succ("审核成功");
    }

    @PostMapping("/realValid")
    @PreAuthorize("hasAuthority('produce:craft:realValid')")
    public ResponseResult realValid(Principal principal,@RequestBody Long id) {
        produceCraftService.updateStatusFinal(principal.getName(),id);
        return ResponseResult.succ("审核成功");
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:craft:del')")
    public ResponseResult delete(@RequestBody Long id) {

        boolean flag = produceCraftService.removeById(id);
        log.info("删除工艺单信息,id:{},是否成功：{}",id,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("工艺单删除失败");
        }
        // 删除对应图片
        File delFile = new File(pictureCraftPath);
        if(delFile.exists()&& delFile.isDirectory()){
            CraftPicFileFilter craftPicFileFilter = new CraftPicFileFilter(id+"");
            File[] files = delFile.listFiles(craftPicFileFilter);
            for (File file : files){
                file.delete();
            }
        }else{
            return ResponseResult.fail("搜索目录["+pictureCraftPath+"] 不存在,无法搜索ID：["+id+"]进行删除图片");
        }
        return ResponseResult.succ("删除成功");
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult queryById(Long id) {
        ProduceCraft produceCraft = produceCraftService.getById(id);
        return ResponseResult.succ(produceCraft);
    }

    /**
     * 获取工艺单 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('produce:craft:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<ProduceCraft> pageData = null;
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
        pageData = produceCraftService.page(getPage(), new QueryWrapper<ProduceCraft>()
                .like(StrUtil.isNotBlank(searchStr)
                        && StrUtil.isNotBlank(searchField), queryField, searchStr));
        return ResponseResult.succ(pageData);
    }

}
