package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@RestController
@RequestMapping("/externalAccount/baseData/material")
@Slf4j
public class ExternalAccountBaseMaterialController extends BaseController {
    @Value("${picture.eaCraftPath}")
    private String pictureCraftPath;
    private final String  picPrefix = "ExternalAccountBaseDataMaterial-";
    private final String  videoPrefix = "ExternalAccountBaseDataMaterialVideo-";

    @Value("${poi.eaBaseDataMaterialDemoPath}")
    private String poiDemoPath;


    @RequestMapping(value = "/removeVideoPath", method = RequestMethod.GET)
    public ResponseResult removeVideoPath(String id) {
        if(id==null || id.isEmpty()){
            return ResponseResult.fail("没有ID");
        }
        ExternalAccountBaseMaterial bm = externalAccountBaseMaterialService.getById(id);
        String fileName = bm.getVideoUrl();

        // 删除视频
        File delFile = new File(pictureCraftPath, fileName);
        if(delFile.exists()){
            delFile.delete();
        }else{
            return ResponseResult.fail("文件["+fileName+"] 不存在,无法删除");
        }
        externalAccountBaseMaterialService.updateNullWithField(bm, DBConstant.TABLE_EA_BASE_MATERIAL.VIDEO_URL_FIELDNAME);

        return ResponseResult.succ("删除视频成功!");
    }



    @RequestMapping(value = "/uploadVideo", method = RequestMethod.POST)
    public ResponseResult uploadVideo(String id, @RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
        if(id==null || id.isEmpty()){
            return ResponseResult.fail("没有ID");
        }
        // 一个ID只能允许传一个视频
        ExternalAccountBaseMaterial bm = externalAccountBaseMaterialService.getById(id);

        if(bm.getVideoUrl()!=null && !bm.getVideoUrl().isEmpty()){
            return ResponseResult.fail("一个物料，只能允许上传一个视频");
        }

        String path = "";
        for (int i = 0; i < files.length; i++) {
            log.info("文件内容:{}",files[i]);
            MultipartFile file = files[i];
            try (InputStream fis = file.getInputStream();){
                String originalFilename = file.getOriginalFilename();
                String[] split = originalFilename.split("\\.");
                String suffix = split[split.length - 1];// 获取后缀

                String fileName = videoPrefix +id+ "_" + System.currentTimeMillis() + "." + suffix;
                FileUtils.writeFile(fis,pictureCraftPath,fileName);
                path =  fileName;
                bm.setVideoUrl(path);
                externalAccountBaseMaterialService.updateById(bm);
            }catch (Exception e){
                log.error("error:",e);

            }
        }
        return ResponseResult.succ(path);
    }


    /**
     * 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public void export(HttpServletResponse response, String searchField, String searchStr) {
        Page<ExternalAccountBaseMaterial> pageData = null;
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = externalAccountBaseMaterialService.page(page);
        } else {
            String queryField = "";
            if (searchField.equals("id")) {
                queryField = "id";
            } else if (searchField.equals("groupCode")) {
                queryField = "group_code";
            } else if (searchField.equals("subId")) {
                queryField = "sub_id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            } else {
                return ;
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = externalAccountBaseMaterialService.pageBySearch(page, queryField, searchStr);
        }
        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(ExternalAccountBaseMaterial.class,1,0).export("null","null",response,fis,pageData.getRecords(),"报表.xlsx",new HashMap<String,String>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    @RequestMapping(value = "/getPicturesById", method = RequestMethod.GET)
    public ResponseResult getPicturesById( String id) {
        // 根据ID 查询照片的路径和名字
        File directory = new File(pictureCraftPath);
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
    public ResponseResult uploadFile(String id, MultipartFile[] files) {
        for (int i = 0; i < files.length; i++) {
            log.info("文件内容:{}",files[i]);
            MultipartFile file = files[i];
            try (InputStream fis = file.getInputStream();){
                String originalFilename = file.getOriginalFilename();
                String[] split = originalFilename.split("\\.");
                String suffix = split[split.length - 1];// 获取后缀

                FileUtils.writeFile(fis,pictureCraftPath,picPrefix+id+"_"+System.currentTimeMillis()+"."+suffix);
            }catch (Exception e){

            }
        }
        return ResponseResult.succ("");
    }

    /**
     * 用于增量表格搜索输入建议框的数据
     */
    @PostMapping("/loadTableSearchMaterialDetailAll")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult loadTableSearchMaterialDetailAll() {
        QueryWrapper<ExternalAccountBaseMaterial> validStatus = new QueryWrapper<ExternalAccountBaseMaterial>().isNull(DBConstant.TABLE_EA_BASE_MATERIAL.STATUS_FIELDNAME);

        List<ExternalAccountBaseMaterial> baseMaterials = externalAccountBaseMaterialService.list(validStatus);

        ArrayList<Map<Object, Object>> returnList = new ArrayList<>();
        baseMaterials.forEach(obj -> {
            Map<Object, Object> returnMap = MapUtil.builder().put(
                            "value", obj.getId() + " : " + obj.getName())
                    .put("id", obj.getId())
                    .put("obj", obj)
                    .map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 获取全部数据
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult getSearchAllData() {
        QueryWrapper<ExternalAccountBaseMaterial> validStatus = new QueryWrapper<ExternalAccountBaseMaterial>().isNull(DBConstant.TABLE_EA_BASE_MATERIAL.STATUS_FIELDNAME);
        List<ExternalAccountBaseMaterial> baseMaterials = externalAccountBaseMaterialService.list(validStatus);

        ArrayList<Map<Object, Object>> returnList = new ArrayList<>();
        baseMaterials.forEach(obj -> {
            Map<Object, Object> returnMap = MapUtil.builder().put("value", obj.getId() + " : " + obj.getName()).put("id", obj.getId()).put("name", obj.getName())
                    .put("unit", obj.getUnit()).put("bigUnit", obj.getBigUnit()).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }


    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/listByGroupCode")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<ExternalAccountBaseMaterial> pageData = null;
        if (searchStr.equals("全部")) {
            pageData = externalAccountBaseMaterialService.page(getPage(), new QueryWrapper<ExternalAccountBaseMaterial>());
        } else {
            pageData = externalAccountBaseMaterialService.pageByGroupCode(getPage(), searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<ExternalAccountBaseMaterial> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = externalAccountBaseMaterialService.page(getPage());
        } else {
            String queryField = "";
            if (searchField.equals("id")) {
                queryField = "id";
            } else if (searchField.equals("groupCode")) {
                queryField = "group_code";
            } else if (searchField.equals("subId")) {
                queryField = "sub_id";
            } else if (searchField.equals("name")) {
                queryField = "name";
            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
            log.info("搜索字段:{},查询内容:{}", searchField, searchStr);
            pageData = externalAccountBaseMaterialService.pageBySearch(getPage(), queryField, searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询物料
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult queryById(String id) {
        ExternalAccountBaseMaterial baseMaterial = externalAccountBaseMaterialService.getById(id);
        return ResponseResult.succ(baseMaterial);
    }

    /**
     * 新增物料
     */
    @Transactional
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult save(Principal principal, @Validated @RequestBody ExternalAccountBaseMaterial baseMaterial) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if(baseMaterial.getSpecs()==null){
            baseMaterial.setSpecs("");
        }
        baseMaterial.setCreated(now);
        baseMaterial.setUpdated(now);
        baseMaterial.setCreatedUser(principal.getName());
        baseMaterial.setUpdateUser(principal.getName());

        // 需要先判断，同名称，同规格，同基本单位是否存在
        List<ExternalAccountBaseMaterial> list = externalAccountBaseMaterialService.listSame(
                baseMaterial.getName(),
                baseMaterial.getUnit(),
                baseMaterial.getGroupCode());



        if (list != null && list.size() > 0) {
            return ResponseResult.fail("存在同名称，同规格，同单位的物料!请检查!");
        }
        if(!baseMaterial.getGroupCode().contains(".")){
            return ResponseResult.fail("一级分组不允许建物料,只允许在二级分组中建立");
        }

        ExternalAccountBaseMaterialGroup group = externalAccountBaseMaterialGroupService.getByCode(baseMaterial.getGroupCode());

        if (baseMaterial.getSubId() == null || baseMaterial.getSubId().isEmpty()) {
            baseMaterial.setSubId(group.getAutoSubId() + "");
            baseMaterial.setId(group.getCode() + "." + group.getAutoSubId());
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId() + 1);
            externalAccountBaseMaterialGroupService.updateById(group);
        }

        try {

            // 再保存
            externalAccountBaseMaterialService.save(baseMaterial);

            HashMap<String, Object> returnMap = new HashMap<>();
            returnMap.put("subId",baseMaterial.getSubId());
            returnMap.put("id",baseMaterial.getId());
            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",returnMap);
        } catch (DuplicateKeyException e) {
            log.error("物料，插入异常", e);
            throw new Exception("唯一编码重复!");
        }
    }


    /**
     * 修改物料
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    public ResponseResult update(Principal principal, @Validated @RequestBody ExternalAccountBaseMaterial baseMaterial) {
        baseMaterial.setUpdated(LocalDateTime.now());
        baseMaterial.setUpdateUser(principal.getName());

        // 需要先判断，同名称，同规格，同基本单位是否存在
        List<ExternalAccountBaseMaterial> list = externalAccountBaseMaterialService.listSameExcludSelf(baseMaterial.getName(),
                baseMaterial.getUnit(),
                baseMaterial.getGroupCode(),
                baseMaterial.getId());

        if (list != null && list.size() > 0) {
            return ResponseResult.fail("存在同名称，同规格，同单位的物料!请检查!");
        }
        try {


            // 1. 查询以前的信息
            ExternalAccountBaseMaterial oldOne = externalAccountBaseMaterialService.getById(baseMaterial.getId());


            // 2. 先查询是否有被价目表审核完成的引用，有则不能修改，
            int count = externalAccountBaseSupplierMaterialService.countSuccessByMaterialId(baseMaterial.getId());

            if (count > 0) {
                if(baseMaterial.getLowWarningLine()==null ){
                    externalAccountBaseMaterialService.updateNull(baseMaterial);
                }
                if(baseMaterial.getLowWarningLine() != null){
                    ExternalAccountBaseMaterial bm = new ExternalAccountBaseMaterial();
                    bm.setId(baseMaterial.getId());
                    bm.setLowWarningLine(baseMaterial.getLowWarningLine());
                    externalAccountBaseMaterialService.updateById(bm);
                }

                log.info("物料ID[{}]不能修改，存在{}个 审核完成的 采购价目记录", baseMaterial.getId(), count);
                return ResponseResult.fail("物料ID[" + baseMaterial.getId() + "]不能修改，存在" + count + "个 审核完成的 采购价目记录");
            }

            // 3. 有入库,退料，领料记录的，不能修改系数
            int buyInCount = externalAccountRepositoryBuyinDocumentDetailService.count(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int pickCount = externalAccountRepositoryPickMaterialDetailService.count(new QueryWrapper<ExternalAccountRepositoryPickMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));

            if(oldOne.getUnitRadio() != baseMaterial.getUnitRadio() && (buyInCount>0 ||pickCount>0)){
                if(baseMaterial.getLowWarningLine()==null ){
                    externalAccountBaseMaterialService.updateNull(baseMaterial);
                }
                if(baseMaterial.getLowWarningLine() != null){
                    ExternalAccountBaseMaterial bm = new ExternalAccountBaseMaterial();
                    bm.setId(baseMaterial.getId());
                    bm.setLowWarningLine(baseMaterial.getLowWarningLine());
                    externalAccountBaseMaterialService.updateById(bm);
                }
                return ResponseResult.fail("物料ID[" + baseMaterial.getId() + "]不能修改系数，存在:" + buyInCount + "个采购入库记录,"+ pickCount + "个生产领料记录,");
            }

            externalAccountBaseMaterialService.updateById(baseMaterial);
            if(baseMaterial.getLowWarningLine()==null){
                externalAccountBaseMaterialService.updateNull(baseMaterial);
            }
            log.info("物料ID[{}]更新成功，old{},new:{}.", baseMaterial.getId(), oldOne, baseMaterial);

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("物料，更新异常", e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    @Transactional
    public ResponseResult delete(@RequestBody String[] ids) {

        int count = externalAccountRepositoryBuyinDocumentDetailService.countByMaterialId(ids);
        if(count > 0){
            return ResponseResult.fail("请先删除"+count+"条对应入库记录!");
        }

        int count3 = externalAccountRepositoryPickMaterialDetailService.countByMaterialId(ids);
        if(count3 > 0){
            return ResponseResult.fail("请先删除"+count3+"条对应出库记录!");
        }

        int count2 = externalAccountBaseSupplierMaterialService.countByMaterialId(ids);
        if(count2 > 0){
            return ResponseResult.fail("请先删除"+count2+"条对应价目记录!");
        }

        externalAccountBaseMaterialService.removeByIds(Arrays.asList(ids));

        // 删除物料之后，要删除该物料的库存记录
        externalAccountRepositoryStockService.removeByMaterialId(ids);

        // 删除对应的图片
        File delFile = new File(pictureCraftPath);
        List<String> delIds = Arrays.asList(ids);

        if(delFile.exists()&& delFile.isDirectory()){
            for (String id : delIds){
                MaterialPicFileFilter craftPicFileFilter = new MaterialPicFileFilter(picPrefix+id);
                File[] files = delFile.listFiles(craftPicFileFilter);
                for (File file : files){
                    file.delete();
                }
            }

        }else{
            return ResponseResult.fail("搜索目录["+pictureCraftPath+"] 不存在,无法搜索IDS：["+picPrefix+delIds.toString()+"]进行删除图片");
        }

        return ResponseResult.succ("删除成功");
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    @Transactional
    public ResponseResult stop(@RequestBody String id) {

        UpdateWrapper<ExternalAccountBaseMaterial> set = new UpdateWrapper<ExternalAccountBaseMaterial>().set(
                        DBConstant.TABLE_EA_BASE_MATERIAL.STATUS_FIELDNAME,  DBConstant.TABLE_EA_BASE_MATERIAL.STATUS_FIELDVALUE_F1)
                .eq(DBConstant.TABLE_EA_BASE_MATERIAL.ID,id);
        externalAccountBaseMaterialService.update(set);
        return ResponseResult.succ("禁用成功");
    }

    @PostMapping("/startBM")
    @PreAuthorize("hasAuthority('externalAccount:baseData:material:list')")
    @Transactional
    public ResponseResult startBM(@RequestBody String id) {
        UpdateWrapper<ExternalAccountBaseMaterial> set = new UpdateWrapper<ExternalAccountBaseMaterial>().set(
                        DBConstant.TABLE_EA_BASE_MATERIAL.STATUS_FIELDNAME, DBConstant.TABLE_EA_BASE_MATERIAL.STATUS_FIELDVALUE_NULL)
                .eq(DBConstant.TABLE_EA_BASE_MATERIAL.ID,id);
        externalAccountBaseMaterialService.update(set);
        return ResponseResult.succ("启用成功");
    }
}
