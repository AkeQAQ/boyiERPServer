package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.fileFilter.MaterialPicFileFilter;
import com.boyi.common.utils.*;
import com.boyi.common.vo.RealDosageVO;
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

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
@RestController
@RequestMapping("/produce/productConstituent")
@Slf4j
public class ProduceProductConstituentController extends BaseController {
    @Value("${boyi.toEmail}")
    private String toEmail;

    @Value("${boyi.csEmails}")
    private String csEmails;

    @Value("${poi.realDosageDemoPath}")
    private String poiDemoPath;

    @Value("${picture.constituentPath}")
    private String pictureConstituentPath;
    private final String  picPrefix = "produceProductConstituentPic-";
    private final String  videoPrefix = "produceProductConstituentVideo-";


    @RequestMapping(value = "/removeVideoPath", method = RequestMethod.GET)
    public ResponseResult removeVideoPath(String id) {
        if(id==null || id.isEmpty()){
            return ResponseResult.fail("没有ID");
        }
        ProduceProductConstituent ppc = produceProductConstituentService.getById(id);
        String fileName = ppc.getVideoUrl();

        // 删除视频
        File delFile = new File(pictureConstituentPath, fileName);
        if(delFile.exists()){
            delFile.delete();
        }else{
            return ResponseResult.fail("文件["+fileName+"] 不存在,无法删除");
        }
        produceProductConstituentService.updateNullWithField(ppc,DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.VIDEO_URL_FIELDNAME);
        return ResponseResult.succ("删除视频成功!");
    }



    @RequestMapping(value = "/uploadVideo", method = RequestMethod.POST)
    public ResponseResult uploadVideo(String id,@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
        if(id==null || id.isEmpty()){
            return ResponseResult.fail("没有ID");
        }
        // 一个ID只能允许传一个视频
        ProduceProductConstituent ppc = produceProductConstituentService.getById(id);

        if(ppc.getVideoUrl()!=null && !ppc.getVideoUrl().isEmpty()){
            return ResponseResult.fail("一个组成结构，只能允许上传一个视频");
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
                FileUtils.writeFile(fis,pictureConstituentPath,fileName);
                path =  fileName;
                ppc.setVideoUrl(path);
                produceProductConstituentService.updateById(ppc);
            }catch (Exception e){
                log.error("error:",e);

            }
        }
        return ResponseResult.succ(path);
    }


    @RequestMapping(value = "/getPicturesById", method = RequestMethod.GET)
    public ResponseResult getPicturesById( String id) {
        // 根据ID 查询照片的路径和名字
        File directory = new File(pictureConstituentPath);
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
    public ResponseResult delPic(String fileName,String id) {
        File delFile = new File(pictureConstituentPath, fileName);
        if(delFile.exists()){
            delFile.delete();
        }else{
            return ResponseResult.fail("文件["+fileName+"] 不存在,无法删除");
        }

        ProduceProductConstituent ppc = produceProductConstituentService.getById(id);
        produceProductConstituentService.updateNullWithField(ppc,DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.PIC_URL_FIELDNAME);
        return ResponseResult.succ("删除成功");
    }

    @RequestMapping(value = "/uploadPic", method = RequestMethod.POST)
    public ResponseResult uploadFile(String id, MultipartFile[] files) {
        if(id==null || id.isEmpty()){
            return ResponseResult.fail("没有ID");
        }
        ProduceProductConstituent ppc = produceProductConstituentService.getById(id);
        for (int i = 0; i < files.length; i++) {
            log.info("文件内容:{}",files[i]);
            MultipartFile file = files[i];
            try (InputStream fis = file.getInputStream();){
                String originalFilename = file.getOriginalFilename();
                String[] split = originalFilename.split("\\.");
                String suffix = split[split.length - 1];// 获取后缀

                String s = picPrefix + id + "_" + System.currentTimeMillis() + "." + suffix;
                FileUtils.writeFile(fis,pictureConstituentPath,s);
                ppc.setPicUrl(s);
                produceProductConstituentService.updateById(ppc);
            }catch (Exception e){

            }
        }
        return ResponseResult.succ("");
    }


    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult getSearchAllData() {
        List<ProduceProductConstituent> produceProductConstituents = produceProductConstituentService.list();
        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        produceProductConstituents.forEach(obj ->{
            String showStr = obj.getProductNum() + obj.getProductBrand();
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+showStr ).put("id", obj.getId()).put("name", showStr).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    @PostMapping("/exportAllRealDosage")
    @PreAuthorize("hasAuthority('produce:productConstituent:queryRealDosage')")
    public void exportAllRealDosage(HttpServletResponse response) {
        List<RealDosageVO> lists = produceProductConstituentService.listRealDosage();

        // 由于目前实际发皮出现一种现象： 一个批次号，本来是用A皮料，但部分改成B皮料进行发皮，所以导致A用料和B用料的应发都对不上。
        // 目前解决方案：在一个批次号领料中同时存在 用料相同的的领料记录，则进行合并（物料合并，用料合并）

        Map<String, List<RealDosageVO>> batchDosage_picks = new HashMap<>();

        //  需要合并处理的数据
        Set<String> needMergeKeys = new HashSet<>();
        Map<String,Boolean> needMergeRemoveKeys = new HashMap<String,Boolean>();// 重复的，是否跳过第一条，可以删除后续的标识

        for(RealDosageVO vo : lists) {
            String batchId = vo.getBatchId();
            String planDosage = vo.getPlanDosage();
            String key = batchId+"_"+planDosage;
            List<RealDosageVO> oneBatch_sameDosages = batchDosage_picks.get(key);

            // 同批次号，同用料的记录
            if(oneBatch_sameDosages==null){
                oneBatch_sameDosages = new ArrayList<>();
                oneBatch_sameDosages.add(vo);
                batchDosage_picks.put(key,oneBatch_sameDosages);
            }else{
                // 有同批次号，同用料的多个领料记录
                oneBatch_sameDosages.add(vo);
                needMergeKeys.add(key);
                needMergeRemoveKeys.put(key,false);
            }
        }
        // 将有同批次号，同用料的金额合并
        for(String key : needMergeKeys){
            List<RealDosageVO> realDosageVOS = batchDosage_picks.get(key);
            RealDosageVO first = realDosageVOS.get(0);
            for (int i = 1; i < realDosageVOS.size(); i++) {
                RealDosageVO current = realDosageVOS.get(i);
                first.setMaterialId(first.getMaterialId()+"(合并"+ current.getMaterialId()+")");
                first.setMaterialName(first.getMaterialName()+"(合并"+current.getMaterialName()+")");
                first.setNum(BigDecimalUtil.add(first.getNum(),current.getNum()).toString());
                first.setReturnNum(BigDecimalUtil.add(first.getReturnNum(),current.getReturnNum()).toString());
                first.setRealDosage(BigDecimalUtil.add(first.getRealDosage(),current.getRealDosage()).toString());
//                first.setCaiduanPlanPickNum(BigDecimalUtil.add(first.getCaiduanPlanPickNum(),current.getCaiduanPlanPickNum()).toString());

            }
        }

        // 将合并后的从数组移除
        for (int i = 0; i < lists.size(); i++) {
            RealDosageVO vo = lists.get(i);
            String batchId = vo.getBatchId();
            String planDosage = vo.getPlanDosage();
            String key = batchId+"_"+planDosage;
            if(needMergeKeys.contains(key)){
                if(needMergeRemoveKeys.get(key)){
                    lists.remove(i--);
                }else{
                    needMergeRemoveKeys.put(key,true);
                }
            }
        }


        HashMap<String, String> materialSum = new HashMap<>();
        HashMap<String, String> materialCount = new HashMap<>();

        // 一款，一品牌，一个物料的计划出库总和
        HashMap<String, String> materialSumByCaiduanPickNumSum = new HashMap<>();
        // 一款，一品牌，一个物料的出库总和
        HashMap<String, String> materialSumByNum = new HashMap<>();

        // 根据物料进行分组，对实际用料进行平均求值,
        for(RealDosageVO vo : lists){
            String key = vo.getProductNum() + "_" + vo.getProductBrand() + "_" + vo.getMaterialId();
            String sum = materialSum.get(key);

            String sum2 = materialSumByCaiduanPickNumSum.get(key);
            if(sum2 == null){
                materialSumByCaiduanPickNumSum.put(key,vo.getCaiduanPlanPickNum());
            }else{
                materialSumByCaiduanPickNumSum.put(key,BigDecimalUtil.add(sum2,vo.getCaiduanPlanPickNum()).toString());
            }

            String sum3 = materialSumByNum.get(key);
            if(sum3 == null){
                materialSumByNum.put(key,vo.getNum());
            }else{
                materialSumByNum.put(key,BigDecimalUtil.add(sum3,vo.getNum()).toString());
            }

            String netUse = BigDecimalUtil.sub(vo.getNum(), vo.getReturnNum()).toString();
            if(sum == null){
                materialSum.put(key,netUse);
            }else{
                materialSum.put(key,BigDecimalUtil.add(sum,netUse).toString());
            }
            String count = materialCount.get(key);
            if(count == null){
                materialCount.put(key,vo.getBatchNum());
            }else{
                materialCount.put(key,BigDecimalUtil.add(count,vo.getBatchNum()).toString());
            }
        }


        Set<String> isSet = new HashSet<>();
        // 求出均值
        for(RealDosageVO vo : lists) {
            String key = vo.getProductNum() + "_" + vo.getProductBrand() + "_" + vo.getMaterialId();

            if(!isSet.contains(key)){
                vo.setAvgDosage(BigDecimalUtil.div(materialSum.get(key),materialCount.get(key)).toString());
                vo.setCaiduanPlanPickNumSum(materialSumByCaiduanPickNumSum.get(key));
                vo.setNumSum(materialSumByNum.get(key));
                isSet.add(key);
            }

        }

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RealDosageVO.class,1,0).export("","",response,fis,lists,"报表.xlsx",null);
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }


    @GetMapping("/queryRealDosageById")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult queryRealDosageById(Long id) {

        List<RealDosageVO> lists = produceProductConstituentService.listRealDosageById(id);

        HashMap<String, String> materialSum = new HashMap<>();
        HashMap<String, String> materialCount = new HashMap<>();

        // 由于目前实际发皮出现一种现象： 一个批次号，本来是用A皮料，但部分改成B皮料进行发皮，所以导致A用料和B用料的应发都对不上。
        // 目前解决方案：在一个批次号领料中同时存在 用料相同的的领料记录，则进行合并（物料合并，用料合并）

        Map<String, List<RealDosageVO>> batchDosage_picks = new HashMap<>();

        //  需要合并处理的数据
        Set<String> needMergeKeys = new HashSet<>();
        Map<String,Boolean> needMergeRemoveKeys = new HashMap<String,Boolean>();// 重复的，是否跳过第一条，可以删除后续的标识

        for(RealDosageVO vo : lists) {
            String batchId = vo.getBatchId();
            String planDosage = vo.getPlanDosage();
            String key = batchId+"_"+planDosage;
            List<RealDosageVO> oneBatch_sameDosages = batchDosage_picks.get(key);

            // 同批次号，同用料的记录
            if(oneBatch_sameDosages==null){
                oneBatch_sameDosages = new ArrayList<>();
                oneBatch_sameDosages.add(vo);
                batchDosage_picks.put(key,oneBatch_sameDosages);
            }else{
                // 有同批次号，同用料的多个领料记录
                oneBatch_sameDosages.add(vo);
                needMergeKeys.add(key);
                needMergeRemoveKeys.put(key,false);
            }
        }
        // 将有同批次号，同用料的金额合并
        for(String key : needMergeKeys){
            List<RealDosageVO> realDosageVOS = batchDosage_picks.get(key);
            RealDosageVO first = realDosageVOS.get(0);
            for (int i = 1; i < realDosageVOS.size(); i++) {
                RealDosageVO current = realDosageVOS.get(i);
                first.setMaterialId(first.getMaterialId()+"(合并"+ current.getMaterialId()+")");
                first.setMaterialName(first.getMaterialName()+"(合并"+current.getMaterialName()+")");
                first.setNum(BigDecimalUtil.add(first.getNum(),current.getNum()).toString());
                first.setReturnNum(BigDecimalUtil.add(first.getReturnNum(),current.getReturnNum()).toString());
                first.setRealDosage(BigDecimalUtil.add(first.getRealDosage(),current.getRealDosage()).toString());
            }
        }

        // 将合并后的从数组移除
        for (int i = 0; i < lists.size(); i++) {
            RealDosageVO vo = lists.get(i);
            String batchId = vo.getBatchId();
            String planDosage = vo.getPlanDosage();
            String key = batchId+"_"+planDosage;
            if(needMergeKeys.contains(key)){
                if(needMergeRemoveKeys.get(key)){
                    lists.remove(i--);
                }else{
                    needMergeRemoveKeys.put(key,true);
                }
            }
        }




        // 根据物料进行分组，对实际用料进行平均求值,
        for(RealDosageVO vo : lists){
            String key = vo.getProductNum() + "_" + vo.getProductBrand() + "_" + vo.getMaterialId();
            String sum = materialSum.get(key);

            String netUse = BigDecimalUtil.sub(vo.getNum(), vo.getReturnNum()).toString();
            if(sum == null){
                materialSum.put(key,netUse);
            }else{
                materialSum.put(key,BigDecimalUtil.add(sum,netUse).toString());
            }
            String count = materialCount.get(key);
            if(count == null){
                materialCount.put(key,vo.getBatchNum());
            }else{
                materialCount.put(key,BigDecimalUtil.add(count,vo.getBatchNum()).toString());
            }
        }
        // 求出均值
        for(RealDosageVO vo : lists) {
            String key = vo.getProductNum() + "_" + vo.getProductBrand() + "_" + vo.getMaterialId();
            vo.setAvgDosage(BigDecimalUtil.div(materialSum.get(key),materialCount.get(key)).toString());
        }
            return ResponseResult.succ(lists);
    }


    /**
     * 计算用料
     */
    /*@GetMapping("/calNumByBrandNumColor")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    @Transactional
    public ResponseResult calNumById(Principal principal,String productNum,String productBrand,String productColor, Long orderNumber)throws Exception {
        if(StringUtils.isBlank(productNum) || StringUtils.isBlank(productBrand) ||StringUtils.isBlank(productColor) ){
            return ResponseResult.fail("公司货号，品牌，颜色不能有空");
        }
        try {

            ProduceProductConstituent byNumBrandColor = produceProductConstituentService.getByNumBrandColor(productNum, productBrand);
            if(byNumBrandColor == null){
                return ResponseResult.fail("产品组成结构没有公司货号["+productNum+"],品牌["+productBrand+"] 对应的信息");
            }
            List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(byNumBrandColor.getId());

            ArrayList<Map<String, Object>> result = new ArrayList<>();
            // 计算数目 * 每个物料的用量
            for (ProduceProductConstituentDetail item : details){
                HashMap<String, Object> calTheMap = new HashMap<>();
                BaseMaterial material = baseMaterialService.getById(item.getMaterialId());
                // 查看该物料，最近的供应商价目，
                List<BaseSupplierMaterial> theSupplierPrices = baseSupplierMaterialService.myList(new QueryWrapper<BaseSupplierMaterial>()
                        .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, item.getMaterialId())
                        .gt(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME, LocalDate.now())
                );
                ArrayList<Map<String, Object>> supplierPrices = new ArrayList<>();
                calTheMap.put("suppliers",supplierPrices);
                for (BaseSupplierMaterial obj:theSupplierPrices){
                    HashMap<String, Object> supplierPrice = new HashMap<>();
                    supplierPrice.put("supplierName",obj.getSupplierName());
                    supplierPrice.put("price",obj.getPrice());
                    supplierPrice.put("startDate",obj.getStartDate());
                    supplierPrice.put("endDate",obj.getEndDate());
                    supplierPrices.add(supplierPrice);
                }
                calTheMap.put("materialId",material.getId());

                calTheMap.put("materialName",material.getName());
                double theOneCalNum = Double.valueOf(item.getDosage()) * orderNumber;
                calTheMap.put("calNum",theOneCalNum);
                calTheMap.put("materialUnit",material.getUnit());

                result.add(calTheMap);
            }

            return ResponseResult.succ(result);
        }

        catch (Exception e) {
            log.error("产品组成结构单，计算异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }
*/

    /**
     * 计算用料
     */
    @GetMapping("/calNumById")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    @Transactional
    public ResponseResult calNumById(Principal principal,Long id, Long calNum)throws Exception {
        try {
            List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(id);

            ArrayList<Map<String, Object>> result = new ArrayList<>();
            // 计算数目 * 每个物料的用量
            for (ProduceProductConstituentDetail item : details){
                HashMap<String, Object> calTheMap = new HashMap<>();
                BaseMaterial material = baseMaterialService.getById(item.getMaterialId());
                // 查看该物料，最近的供应商价目，
                List<BaseSupplierMaterial> theSupplierPrices = baseSupplierMaterialService.myList(new QueryWrapper<BaseSupplierMaterial>()
                        .eq(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.MATERIAL_ID_FIELDNAME, item.getMaterialId())
                        .gt(DBConstant.TABLE_BASE_SUPPLIER_MATERIAL.END_DATE_FIELDNAME, LocalDate.now())
                );
                ArrayList<Map<String, Object>> supplierPrices = new ArrayList<>();
                calTheMap.put("suppliers",supplierPrices);
                for (BaseSupplierMaterial obj:theSupplierPrices){
                    HashMap<String, Object> supplierPrice = new HashMap<>();
                    supplierPrice.put("supplierName",obj.getSupplierName());
                    supplierPrice.put("price",obj.getPrice());
                    supplierPrice.put("startDate",obj.getStartDate());
                    supplierPrice.put("endDate",obj.getEndDate());
                    supplierPrices.add(supplierPrice);
                }

                calTheMap.put("materialName",material.getName());
                double theOneCalNum = Double.valueOf(item.getDosage()) * calNum;
                calTheMap.put("calNum",theOneCalNum);
                calTheMap.put("materialUnit",material.getUnit());

                result.add(calTheMap);
            }

            return ResponseResult.succ(result);
        }

        catch (Exception e) {
            log.error("产品组成结构单，计算异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:productConstituent:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            List<ProduceProductConstituent> olds = produceProductConstituentService.listByIds(Arrays.asList(ids));
            for(ProduceProductConstituent old : olds){
                // 假如有照片和视频的，也不能反审核
                if( old.getPicUrl()!=null || !old.getPicUrl().isEmpty() || old.getVideoUrl()!=null || !old.getVideoUrl().isEmpty()){
                    return ResponseResult.fail(old.getId()+" 编号,已有照片和视频资料，不能反，请先删除照片和视频");
                }
            }


            boolean flag = produceProductConstituentService.removeByIds(Arrays.asList(ids));

            log.info("删除产品组成结构表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品组成结构删除失败");
            }

             produceProductConstituentDetailService.delByDocumentIds(ids);

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
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult queryById(Long id) {
        ProduceProductConstituent productConstituent = produceProductConstituentService.getById(id);
        List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignId(id);

        LocalDate localDate = LocalDate.now().plusDays(-300);

        for (ProduceProductConstituentDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            // 假如是采购进度备料进度，入库数量>0的记录数量，则标注为不可以修改
            List<ProduceOrderMaterialProgress> list = orderProductOrderService.listByProductNumBrandAndProgressMaterialId(productConstituent.getProductNum(),
                    productConstituent.getProductBrand(),detail.getMaterialId());
            Boolean canChange = true;
            for(ProduceOrderMaterialProgress progress: list){
                String inNum = progress.getInNum();
                if(inNum !=null && !inNum.isEmpty() && Double.valueOf(inNum).doubleValue() > 0D){
                    canChange = false;
                }
            }
            detail.setCanChange(canChange);

            // 假如是true的，并且是01编码开头物料，
            // 还要看该品牌、该工厂货号对应的订单，对应的批次号有没有出现在最近300内的领料记录里，存在则设置fasle
            if(detail.getCanChange() && material.getId().startsWith("01.")){
                Long count = produceProductConstituentService.countPickMaterialRows(productConstituent.getProductNum(),
                        productConstituent.getProductBrand(),detail.getMaterialId(),localDate);
                if(count !=null && count > 0){
                    detail.setCanChange(false);
                }
            }

        }

        productConstituent.setRowList(details);
        return ResponseResult.succ(productConstituent);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:productConstituent:update')")
    @Transactional
    public ResponseResult update(Principal principal,boolean specialAddFlag, @Validated @RequestBody ProduceProductConstituent productConstituent)
            throws Exception{

        if(productConstituent.getRowList() ==null || productConstituent.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        // 查看核算表，有没有该货号的记录，没有则不允许录入
        String productNum = productConstituent.getProductNum();
        StringBuilder sb2 = new StringBuilder(productNum);
        String substring = sb2.substring(3);

        List<OrderProductpricePre> lists = orderProductpricePreService.listByLikeProductNum(substring);

        if(lists==null ||lists.size() == 0){
            return ResponseResult.fail("核算没有该货号:["+substring+"],信息，无法录入!");
        }

        // 1. 假如比老的有多新增01.的物料，邮件通知
        List<ProduceProductConstituentDetail> oldDetails = produceProductConstituentDetailService.listByForeignId(productConstituent.getId());

        Set<String> oldMaterialIds = new HashSet<>();

        Map<String, ProduceProductConstituentDetail> oldDetailsObj = new HashMap<>();


        for(ProduceProductConstituentDetail old : oldDetails){
            oldMaterialIds.add(old.getMaterialId());
            oldDetailsObj.put(old.getMaterialId(),old);
        }

        Set<String> materialIds = new HashSet<>();
        Boolean flagSend = false;
        StringBuilder sb = new StringBuilder("产品组成ID:").append(productConstituent.getId())
                .append(",")
                .append(productConstituent.getProductNum())
                .append(":")
                .append(productConstituent.getProductBrand());

        for (ProduceProductConstituentDetail detail: productConstituent.getRowList()){
            if(materialIds.contains(detail.getMaterialId())){
                return ResponseResult.fail("物料编码"+detail.getMaterialId()+"重复");
            }
            materialIds.add(detail.getMaterialId());
            if(!oldMaterialIds.contains(detail.getMaterialId()) && detail.getMaterialId().startsWith("01.")){
                flagSend = true;
                sb.append(",新增01分组物料:").append(detail.getMaterialId()).append("-").append(detail.getMaterialName()).append("<br>");
            }
        }

        // 老物料被删除了的列表
        Set<String> delMaterialIds = new HashSet<>();
        delMaterialIds.addAll(oldMaterialIds);
        delMaterialIds.removeAll(materialIds);


        if(flagSend){
            ThreadUtils.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EmailUtils.sendMail(EmailUtils.MODULE_ADDNEW_MATERIAL_NAME,toEmail, csEmails.split(","),sb.toString());
                    } catch (MessagingException e) {
                        log.error("error",e);
                    }
                }
            });
        }


        productConstituent.setUpdated(LocalDateTime.now());
        productConstituent.setUpdatedUser(principal.getName());
        if(!specialAddFlag){
            productConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_2);
        }
        try {

            //1. 先删除老的，再插入新的
            boolean flag = produceProductConstituentDetailService.removeByDocId(productConstituent.getId());
            if(flag){
                produceProductConstituentService.updateById(productConstituent);

                for (ProduceProductConstituentDetail item : productConstituent.getRowList()){
                    item.setId(null);
                    item.setConstituentId(productConstituent.getId());
                    ProduceProductConstituentDetail theOneMaterial = oldDetailsObj.get(item.getMaterialId());
                    // 老的存在，则赋值创建时间等字段，
                    if(theOneMaterial!=null){
                        item.setCreated(theOneMaterial.getCreated());
                        item.setUpdated(LocalDateTime.now());
                        item.setCreatedUser(theOneMaterial.getCreatedUser());
                        item.setUpdatedUser(principal.getName());
                    }
                    //新增物料，赋值初始数值
                    else{
                        item.setCreated(LocalDateTime.now());
                        item.setUpdated(LocalDateTime.now());
                        item.setCreatedUser(principal.getName());
                        item.setUpdatedUser(principal.getName());
                    }
                }

                produceProductConstituentDetailService.saveBatch(productConstituent.getRowList());
                log.info("产品组成结构模块-更新内容:{}",productConstituent);
            }else{
                throw new RuntimeException("操作失败，期间detail删除失败");
            }

            log.info("【补充物料】，组成结构ID：{},删除掉的物料列表：{}",productConstituent.getId(),delMaterialIds);
            HashSet<Long> removeIds = new HashSet<>();
            // 老物料被修改的，要删除对应进度表记录
            for(String delMaterialId:delMaterialIds){

                List<ProduceOrderMaterialProgress> list = orderProductOrderService.listByProductNumBrandAndProgressMaterialId(productConstituent.getProductNum(),
                        productConstituent.getProductBrand(), delMaterialId);

                for(ProduceOrderMaterialProgress progress : list){
                    if(progress.getInNum()!=null && Double.valueOf(progress.getInNum()).doubleValue() > 0D){
                        log.error("逻辑出现漏洞，能删除品牌{},货号{},物料{}有入库数量的采购进度表.",productConstituent.getProductNum(),
                                productConstituent.getProductBrand(),delMaterialId);
                        return ResponseResult.fail("【异常情况，请通知管理员】物料编码"+delMaterialId+",在进度表ID:{"+progress.getId()+"}中存在入库记录{"+progress.getId()+"}");
                    }
                    removeIds.add(progress.getId());
                }
            }
            if(!removeIds.isEmpty()){
                produceOrderMaterialProgressService.removeByIds(removeIds);
                log.info("【补充物料】【删除修改物料的进度表内容】,物料{},进度表ID：{}",delMaterialIds,removeIds);
            }

            return ResponseResult.succ("编辑成功");
        }
        catch (DuplicateKeyException de){
            throw new RuntimeException("货号，品牌不能重复!");
        }
        catch (Exception e) {
            log.error("供应商，更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产产品组成结构，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:productConstituent:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody ProduceProductConstituent productConstituent)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        productConstituent.setCreated(now);
        productConstituent.setUpdated(now);
        productConstituent.setCreatedUser(principal.getName());
        productConstituent.setUpdatedUser(principal.getName());
        productConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_2);
        try {
            HashSet<String> materialIds = new HashSet<>();
            for (ProduceProductConstituentDetail detail: productConstituent.getRowList()){
                if(materialIds.contains(detail.getMaterialId())){
                    return ResponseResult.fail("物料编码"+detail.getMaterialId()+"重复");
                }
                materialIds.add(detail.getMaterialId());
            }

            // 查看核算表，有没有该货号的记录，没有则不允许录入
            String productNum = productConstituent.getProductNum();
            StringBuilder sb = new StringBuilder(productNum);
            String substring = sb.substring(3);

            List<OrderProductpricePre> lists = orderProductpricePreService.listByLikeProductNum(substring);

            if(lists==null ||lists.size() == 0){
                return ResponseResult.fail("核算没有该货号:["+substring+"],信息，无法录入!");
            }

            produceProductConstituentService.save(productConstituent);

            for (ProduceProductConstituentDetail item : productConstituent.getRowList()){
                item.setConstituentId(productConstituent.getId());
                item.setCreated(now);
                item.setUpdated(now);
                item.setCreatedUser(principal.getName());
                item.setUpdatedUser(principal.getName());
                item.setCanShowPrint("0");
            }

            produceProductConstituentDetailService.saveBatch(productConstituent.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",productConstituent.getId());
        }
        catch (DuplicateKeyException de){
            throw new RuntimeException("货号，品牌不能重复!");
        }
        catch (Exception e) {
            log.error("产品组成结构单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取产品组成结构 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('produce:productConstituent:list')")
    public ResponseResult list( String searchField, String searchStatus,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceProductConstituent> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("productNum")) {
                queryField = "product_num";
            }
            else if (searchField.equals("productBrand")) {
                queryField = "product_brand";

            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";

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
                    if (oneField.equals("productNum")) {
                        theQueryField = "product_num";
                    }
                    else if (oneField.equals("productBrand")) {
                        theQueryField = "product_brand";

                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

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
            return ResponseResult.fail("状态不能为空");
        }
        if(((queryMap.containsKey("material_name")&&!queryMap.get("material_name").isEmpty())) || (queryField.equals("material_name")&&!searchStr.isEmpty())){
            pageData = produceProductConstituentService.innerQueryByManySearchWithDetailField(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);
        }else{
            pageData = produceProductConstituentService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);
        }
/*
        for(ProduceProductConstituent ppc: pageData.getRecords()){
            List<ProduceProductConstituentDetail> details = produceProductConstituentDetailService.listByForeignIdAnd1101MaterialId(ppc.getId());
            if(details==null || details.isEmpty()){
                ppc.setCaiduanForeignPriceStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.caiduanForeignPriceStatus_FIELDVALUE_1);

            }else{
                boolean isAllHasPrice = true;
                String[] materialIds = new String[details.size()];
                for (int i = 0; i < details.size(); i++) {
                    materialIds[i]=details.get(i).getMaterialId();
                }

                // 看下是否有价格
                int count = baseSupplierMaterialService.countByMaterialId(materialIds);
                if(count != details.size()){
                    isAllHasPrice=false;
                }
                if(isAllHasPrice){
                    ppc.setCaiduanForeignPriceStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.caiduanForeignPriceStatus_FIELDVALUE_0);
                }else{
                    ppc.setCaiduanForeignPriceStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT_DETIAL.caiduanForeignPriceStatus_FIELDVALUE_2);
                }

            }
        }*/



        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('produce:productConstituent:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {

        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_2);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-产品组成结构模块-审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('produce:productConstituent:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_1);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-产品组成结构模块-审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<ProduceProductConstituent> lists = new ArrayList<>();

        for (Long id : ids){
            ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
            produceProductConstituent.setUpdated(LocalDateTime.now());
            produceProductConstituent.setUpdatedUser(principal.getName());
            produceProductConstituent.setId(id);
            produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_0);
            lists.add(produceProductConstituent);

        }
        produceProductConstituentService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_0);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-产品组成结构模块-审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        // 假如有进度表关联了，不能反审核了。
        ProduceProductConstituent old = produceProductConstituentService.getById(id);
        List<OrderProductOrder> orders = orderProductOrderService.getByNumBrand(old.getProductNum(),old.getProductBrand());
        if(orders != null && orders.size() > 0){
            HashSet<Long> orderIds = new HashSet<>();
            // 去查询是否有该订单号的进度表
            for (OrderProductOrder order : orders){
                orderIds.add(order.getId());
            }
            List<ProduceOrderMaterialProgress> processes = produceOrderMaterialProgressService.listByOrderIds(orderIds);
            if(processes!=null && processes.size() > 0){
                return ResponseResult.fail("已有物料报备，无法反审核!");

            }
        }



        ProduceProductConstituent produceProductConstituent = new ProduceProductConstituent();
        produceProductConstituent.setUpdated(LocalDateTime.now());
        produceProductConstituent.setUpdatedUser(principal.getName());
        produceProductConstituent.setId(id);
        produceProductConstituent.setStatus(DBConstant.TABLE_PRODUCE_PRODUCT_CONSTITUENT.STATUS_FIELDVALUE_3);
        produceProductConstituentService.updateById(produceProductConstituent);
        log.info("生产模块-反审核通过内容:{}",produceProductConstituent);

        return ResponseResult.succ("反审核成功");
    }

    public static void main(String[] args) {
        Set<String> s1 = new HashSet<>();
        Set<String> s2 = new HashSet<>();
        Set<String> s3 = new HashSet<>();

        s1.add("1");
        s1.add("2");

        s2.add("1");
        s2.add("3");

        s3.addAll(s1);

        s3.removeAll(s2);
        System.out.println(s3);
    }

}
