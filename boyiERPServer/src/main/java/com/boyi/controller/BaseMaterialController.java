package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.fileFilter.CraftPicFileFilter;
import com.boyi.common.fileFilter.MaterialPicFileFilter;
import com.boyi.common.utils.FileUtils;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.service.BaseMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Slf4j
@RestController
@RequestMapping("/baseData/material")
public class BaseMaterialController extends BaseController {
    @Value("${picture.craftPath}")
    private String pictureCraftPath;
    private final String  picPrefix = "baseDataMaterial-";

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
     *  // 用于盘点的物料信息加库存信息
     * @return
     */
    @PostMapping("/loadTableSearchMaterialDetailAllWithStock")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult loadTableSearchMaterialDetailAllWithStock() {
        QueryWrapper<BaseMaterial> validStatus = new QueryWrapper<BaseMaterial>().isNull(DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDNAME);

        List<BaseMaterial> baseMaterials = baseMaterialService.list(validStatus);
        List<String> ids = new ArrayList<>();
        for (BaseMaterial baseMaterial : baseMaterials) {
            ids.add(baseMaterial.getId());
        }
        List<RepositoryStock> stocks = repositoryStockService.listByMaterialIds(ids);

        HashMap<String, Double> stockNum = new HashMap<>();
        for (RepositoryStock stock : stocks) {
            stockNum.put(stock.getMaterialId(), stock.getNum());
        }

        ArrayList<Map<Object, Object>> returnList = new ArrayList<>();
        baseMaterials.forEach(obj -> {
            Double num = stockNum.get(obj.getId());
            Map<Object, Object> returnMap = MapUtil.builder().put(
                            "value", obj.getId() + " : " + obj.getName())
                    .put("id", obj.getId())
                    .put("obj", obj)
                    .put("stockNum", num == null ? 0D : num)
                    .map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    /**
     * 用于增量表格搜索输入建议框的数据
     */
    @PostMapping("/loadTableSearchMaterialDetailAll")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult loadTableSearchMaterialDetailAll() {
        QueryWrapper<BaseMaterial> validStatus = new QueryWrapper<BaseMaterial>().isNull(DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDNAME);

        List<BaseMaterial> baseMaterials = baseMaterialService.list(validStatus);

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
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult getSearchAllData() {
        QueryWrapper<BaseMaterial> validStatus = new QueryWrapper<BaseMaterial>().isNull(DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDNAME);
        List<BaseMaterial> baseMaterials = baseMaterialService.list(validStatus);

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
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult listByGroupCode(String searchStr) {
        Page<BaseMaterial> pageData = null;
        if (searchStr.equals("全部")) {
            pageData = baseMaterialService.page(getPage(), new QueryWrapper<BaseMaterial>());
        } else {
            pageData = baseMaterialService.pageByGroupCode(getPage(), searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 获取物料 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<BaseMaterial> pageData = null;
        if (searchField == "") {
            log.info("未选择搜索字段，全查询");
            pageData = baseMaterialService.page(getPage());
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
            pageData = baseMaterialService.pageBySearch(getPage(), queryField, searchStr);
        }
        return ResponseResult.succ(pageData);
    }

    /**
     * 查询物料
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('baseData:material:list')")
    public ResponseResult queryById(String id) {
        BaseMaterial baseMaterial = baseMaterialService.getById(id);
        return ResponseResult.succ(baseMaterial);
    }

    /**
     * 新增物料
     */
    @Transactional
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('baseData:material:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        if(baseMaterial.getSpecs()==null){
            baseMaterial.setSpecs("");
        }
        baseMaterial.setCreated(now);
        baseMaterial.setUpdated(now);
        baseMaterial.setCreatedUser(principal.getName());
        baseMaterial.setUpdateUser(principal.getName());

        // 需要先判断，同名称，同规格，同基本单位是否存在
        List<BaseMaterial> list = baseMaterialService.listSame(
                baseMaterial.getName(),
                baseMaterial.getUnit(),
                baseMaterial.getSpecs(),
                baseMaterial.getGroupCode());

        if (list != null && list.size() > 0) {
            return ResponseResult.fail("存在同名称，同规格，同单位的物料!请检查!");
        }
        if(!baseMaterial.getGroupCode().contains(".")){
            return ResponseResult.fail("一级分组不允许建物料,只允许在二级分组中建立");
        }

        BaseMaterialGroup group = baseMaterialGroupService.getByCode(baseMaterial.getGroupCode());

        if (baseMaterial.getSubId() == null || baseMaterial.getSubId().isEmpty()) {
            baseMaterial.setSubId(group.getAutoSubId() + "");
            baseMaterial.setId(group.getCode() + "." + group.getAutoSubId());
            // 先自增该分组的ID
            group.setAutoSubId(group.getAutoSubId() + 1);
            baseMaterialGroupService.updateById(group);
        }

        try {

            // 再保存
            baseMaterialService.save(baseMaterial);

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
    @PreAuthorize("hasAuthority('baseData:material:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody BaseMaterial baseMaterial) {
        baseMaterial.setUpdated(LocalDateTime.now());
        baseMaterial.setUpdateUser(principal.getName());

        // 需要先判断，同名称，同规格，同基本单位是否存在
        List<BaseMaterial> list = baseMaterialService.listSameExcludSelf(baseMaterial.getName(),
                baseMaterial.getUnit(),
                baseMaterial.getSpecs(),
                baseMaterial.getGroupCode(),
                baseMaterial.getId());

        if (list != null && list.size() > 0) {
            return ResponseResult.fail("存在同名称，同规格，同单位的物料!请检查!");
        }
        try {


            // 1. 查询以前的信息
            BaseMaterial oldOne = baseMaterialService.getById(baseMaterial.getId());

            // 2. 先查询是否有被价目表审核完成的引用，有则不能修改，
            int count = baseSupplierMaterialService.countSuccessByMaterialId(baseMaterial.getId());

            if (count > 0) {
                if(baseMaterial.getLowWarningLine()==null ){
                    baseMaterialService.updateNull(baseMaterial);
                }
                if(baseMaterial.getLowWarningLine() != null){
                    BaseMaterial bm = new BaseMaterial();
                    bm.setId(baseMaterial.getId());
                    bm.setLowWarningLine(baseMaterial.getLowWarningLine());
                    baseMaterialService.updateById(bm);
                }

                log.info("物料ID[{}]不能修改，存在{}个 审核完成的 采购价目记录", baseMaterial.getId(), count);
                return ResponseResult.fail("物料ID[" + baseMaterial.getId() + "]不能修改，存在" + count + "个 审核完成的 采购价目记录");
            }

            // 3. 有入库,退料，领料记录的，不能修改系数
            int buyInCount = repositoryBuyinDocumentDetailService.count(new QueryWrapper<RepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int buyOutCount = repositoryBuyoutDocumentDetailService.count(new QueryWrapper<RepositoryBuyoutDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int pickCount = repositoryPickMaterialDetailService.count(new QueryWrapper<RepositoryPickMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int returnCount = repositoryReturnMaterialDetailService.count(new QueryWrapper<RepositoryReturnMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));
            int orderCount = orderBuyorderDocumentDetailService.count(new QueryWrapper<OrderBuyorderDocumentDetail>().eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, baseMaterial.getId()));

            if(oldOne.getUnitRadio() != baseMaterial.getUnitRadio() && (buyInCount>0 ||buyOutCount>0||pickCount>0||returnCount>0||orderCount>0)){
                if(baseMaterial.getLowWarningLine()==null ){
                    baseMaterialService.updateNull(baseMaterial);
                }
                if(baseMaterial.getLowWarningLine() != null){
                    BaseMaterial bm = new BaseMaterial();
                    bm.setId(baseMaterial.getId());
                    bm.setLowWarningLine(baseMaterial.getLowWarningLine());
                    baseMaterialService.updateById(bm);
                }
                return ResponseResult.fail("物料ID[" + baseMaterial.getId() + "]不能修改系数，存在:" + buyInCount + "个采购入库记录,"+ buyOutCount + "个采购退料记录,"+ pickCount + "个生产领料记录,"+ returnCount + "个生产退料记录,"+ orderCount + "个采购订单记录");
            }

            baseMaterialService.updateById(baseMaterial);
            if(baseMaterial.getLowWarningLine()==null){
                baseMaterialService.updateNull(baseMaterial);
            }
            log.info("物料ID[{}]更新成功，old{},new:{}.", baseMaterial.getId(), oldOne, baseMaterial);

            return ResponseResult.succ("编辑成功");
        } catch (DuplicateKeyException e) {
            log.error("物料，更新异常", e);
            return ResponseResult.fail("唯一编码重复!");
        }
    }

    @PostMapping("/del")
    @PreAuthorize("hasAuthority('baseData:material:del')")
    @Transactional
    public ResponseResult delete(@RequestBody String[] ids) {

        int count = repositoryBuyinDocumentDetailService.countByMaterialId(ids);
        if(count > 0){
            return ResponseResult.fail("请先删除"+count+"条对应入库记录!");
        }

        int count3 = repositoryPickMaterialDetailService.countByMaterialId(ids);
        if(count3 > 0){
            return ResponseResult.fail("请先删除"+count3+"条对应出库记录!");
        }

        int count2 = baseSupplierMaterialService.countByMaterialId(ids);
        if(count2 > 0){
            return ResponseResult.fail("请先删除"+count2+"条对应价目记录!");
        }

        //判断采购订单，是否有该物料
        int orderBuyorderCount = orderBuyorderDocumentDetailService.countByMaterialId(ids);
        if(orderBuyorderCount > 0){
            return ResponseResult.fail("请先删除"+orderBuyorderCount+"条对应采购订单信息!");
        }

        //判断产品组成结构，是否有该物料
        int produceProductCount = produceProductConstituentDetailService.countByMaterialId(ids);
        if(produceProductCount > 0){
            return ResponseResult.fail("请先删除"+produceProductCount+"条对应产品组成信息!");
        }

        baseMaterialService.removeByIds(Arrays.asList(ids));

        // 删除物料之后，要删除该物料的库存记录
        repositoryStockService.removeByMaterialId(ids);

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
    @PreAuthorize("hasAuthority('baseData:material:del')")
    @Transactional
    public ResponseResult stop(@RequestBody String id) {

        UpdateWrapper<BaseMaterial> set = new UpdateWrapper<BaseMaterial>().set(
                DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDNAME,  DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDVALUE_F1)
                .eq(DBConstant.TABLE_BASE_MATERIAL.ID,id);
        baseMaterialService.update(set);
        return ResponseResult.succ("禁用成功");
    }

    @PostMapping("/startBM")
    @PreAuthorize("hasAuthority('baseData:material:del')")
    @Transactional
    public ResponseResult startBM(@RequestBody String id) {
        UpdateWrapper<BaseMaterial> set = new UpdateWrapper<BaseMaterial>().set(
                DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDNAME, DBConstant.TABLE_BASE_MATERIAL.STATUS_FIELDVALUE_NULL)
                .eq(DBConstant.TABLE_BASE_MATERIAL.ID,id);
        baseMaterialService.update(set);
        return ResponseResult.succ("启用成功");
    }
}
