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
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2023-04-27
 */
@RestController
@RequestMapping("/produce/technologyBOM")
@Slf4j
public class ProduceTechnologyBomController extends BaseController {
    @Value("${boyi.toEmail}")
    private String toEmail;

    @Value("${boyi.csEmails}")
    private String csEmails;


    @GetMapping("/updateNumAndBrand")
    @PreAuthorize("hasAuthority('produce:technologyBOM:valid')")
    public ResponseResult updateNumAndBrand(Long id) {
        ProduceTechnologyBom productConstituent = produceTechnologyBomService.getById(id);

        return ResponseResult.succ(productConstituent);
    }


    /*@RequestMapping(value = "/updateNumBrandSubmit", method = RequestMethod.POST)
    @Transactional
    @PreAuthorize("hasAuthority('produce:technologyBOM:valid')")
    public ResponseResult updateNumBrandSubmit(Principal principal, @RequestBody ProduceTechnologyBom ppc) {
        ProduceTechnologyBom old = produceTechnologyBomService.getById(ppc.getId());

        if(old.getProductBrand().equals(ppc.getProductBrand()) && old.getProductNum().equals(ppc.getProductNum())){
            return ResponseResult.fail("没有任何修改，不修改!");
        }
        // 1. 允许修改的，把老的货号、品牌的订单信息，都一并改成新的货号、品牌、

        List<OrderProductOrder> oldOrders = orderProductOrderService.listByMBomId(ppc.getId());
        for(OrderProductOrder order : oldOrders){
            order.setProductNum(ppc.getProductNum());
            order.setProductBrand(ppc.getProductBrand());
        }
        if(!oldOrders.isEmpty()){
            orderProductOrderService.updateBatchById(oldOrders);
        }


        old.setProductNum(ppc.getProductNum());
        old.setProductBrand(ppc.getProductBrand());
        old.setUpdated(LocalDateTime.now());
        old.setUpdatedUser(principal.getName());
        produceProductConstituentService.updateById(old);

        return ResponseResult.succ("修改货号、品牌成功！");
    }
*/

    /**
     * 获取全部
     */
    @PostMapping("/getSearchAllData")
    @PreAuthorize("hasAuthority('produce:technologyBOM:list')")
    public ResponseResult getSearchAllData() {
        List<ProduceTechnologyBom> produceProductConstituents = produceTechnologyBomService.list();
        ArrayList<Map<Object,Object>> returnList = new ArrayList<>();
        produceProductConstituents.forEach(obj ->{
            String showStr = obj.getProductNum() + obj.getProductBrand();
            Map<Object, Object> returnMap = MapUtil.builder().put("value",obj.getId()+" : "+showStr ).put("id", obj.getId()).put("name", showStr).map();
            returnList.add(returnMap);
        });
        return ResponseResult.succ(returnList);
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:technologyBOM:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            boolean flag = produceTechnologyBomService.removeByIds(Arrays.asList(ids));

            log.info("删除产品组成结构表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品组成结构删除失败");
            }

            produceTechnologyBomDetailService.delByDocumentIds(ids);

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
    @PreAuthorize("hasAuthority('produce:technologyBOM:list')")
    public ResponseResult queryById(Long id) {
        ProduceTechnologyBom productConstituent = produceTechnologyBomService.getById(id);
        List<ProduceTechnologyBomDetail> details = produceTechnologyBomDetailService.listByForeignId(id);

        for (ProduceTechnologyBomDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());

            String supplierId = detail.getSupplierId();
            if(supplierId!=null && !supplierId.isEmpty()){
                BaseSupplier bs = baseSupplierService.getById(supplierId);
                detail.setSupplierName(bs.getName());

            }


        }

        productConstituent.setRowList(details);
        return ResponseResult.succ(productConstituent);
    }

    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:technologyBOM:update')")
    @Transactional
    public ResponseResult update(Principal principal,boolean specialAddFlag, @Validated @RequestBody ProduceTechnologyBom productConstituent)
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
        List<ProduceTechnologyBomDetail> oldDetails = produceTechnologyBomDetailService.listByForeignId(productConstituent.getId());

        Set<String> oldMaterialIds = new HashSet<>();

        Map<String, ProduceTechnologyBomDetail> oldDetailsObj = new HashMap<>();

        Map<String, ProduceTechnologyBomDetail> newDetailsObj = new HashMap<>();


        for(ProduceTechnologyBomDetail old : oldDetails){
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

        for (ProduceTechnologyBomDetail detail: productConstituent.getRowList()){
            if(materialIds.contains(detail.getMaterialId())){
                return ResponseResult.fail("物料编码"+detail.getMaterialId()+"重复");
            }
            newDetailsObj.put(detail.getMaterialId(),detail);
            materialIds.add(detail.getMaterialId());
            if(!oldMaterialIds.contains(detail.getMaterialId()) && detail.getMaterialId().startsWith("11.")){
                flagSend = true;
                sb.append(",新增11分组物料:").append(detail.getMaterialId()).append("-").append(detail.getMaterialName()).append("<br>");
            }
        }

        // 老物料被删除了的列表
        Set<String> delMaterialIds = new HashSet<>();
        delMaterialIds.addAll(oldMaterialIds);
        delMaterialIds.removeAll(materialIds);

        // 新加的物料列表，对进度表 进行生成
        Set<String> newMaterialIds = new HashSet<>();
        newMaterialIds.addAll(materialIds);
        newMaterialIds.removeAll(oldMaterialIds);

        if(flagSend){
            ThreadUtils.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EmailUtils.sendMail(EmailUtils.MODULE_ADDNEW_TECHNOLOGY_BOM_NAME,toEmail, csEmails.split(","),sb.toString());
                    } catch (MessagingException e) {
                        log.error("error",e);
                    }
                }
            });
        }

        productConstituent.setUpdated(LocalDateTime.now());
        productConstituent.setUpdatedUser(principal.getName());
        if(!specialAddFlag){
            productConstituent.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_2);
        }
        try {

            //1. 先删除老的，再插入新的
            boolean flag = produceTechnologyBomDetailService.removeByDocId(productConstituent.getId());
            if(flag){
                produceTechnologyBomService.updateById(productConstituent);

                for (ProduceTechnologyBomDetail item : productConstituent.getRowList()){
                    item.setId(null);
                    item.setConstituentId(productConstituent.getId());
                    ProduceTechnologyBomDetail theOneMaterial = oldDetailsObj.get(item.getMaterialId());
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

                produceTechnologyBomDetailService.saveBatch(productConstituent.getRowList());
                log.info("工艺BOM模块-更新内容:{}",productConstituent);
            }else{
                throw new RuntimeException("操作失败，期间detail删除失败");
            }

            log.info("【工艺BOM-补充物料】，组成结构ID：{},删除掉的物料列表：{}",productConstituent.getId(),delMaterialIds);

            return ResponseResult.succ("编辑成功");
        }
        catch (Exception e) {
            log.error("更新异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 生产产品组成结构，库存入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:technologyBOM:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody ProduceTechnologyBom productConstituent)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        productConstituent.setCreated(now);
        productConstituent.setUpdated(now);
        productConstituent.setCreatedUser(principal.getName());
        productConstituent.setUpdatedUser(principal.getName());
        productConstituent.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_2);
        try {
            HashSet<String> materialIds = new HashSet<>();
            for (ProduceTechnologyBomDetail detail: productConstituent.getRowList()){
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

            produceTechnologyBomService.save(productConstituent);

            for (ProduceTechnologyBomDetail item : productConstituent.getRowList()){
                item.setConstituentId(productConstituent.getId());
                item.setCreated(now);
                item.setUpdated(now);
                item.setCreatedUser(principal.getName());
                item.setUpdatedUser(principal.getName());
                item.setCanShowPrint("0");
            }

            produceTechnologyBomDetailService.saveBatch(productConstituent.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",productConstituent.getId());
        }
        catch (Exception e) {
            log.error("工艺BOM，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取产品组成结构 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('produce:technologyBOM:list')")
    public ResponseResult list( String searchField, String searchStatus,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceTechnologyBom> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
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
            pageData = produceTechnologyBomService.innerQueryByManySearchWithDetailField(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);
        }else{
            pageData = produceTechnologyBomService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,queryMap);
        }



        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('produce:technologyBOM:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {

        ProduceTechnologyBom ProduceTechnologyBom = new ProduceTechnologyBom();
        ProduceTechnologyBom.setUpdated(LocalDateTime.now());
        ProduceTechnologyBom.setUpdatedUser(principal.getName());
        ProduceTechnologyBom.setId(id);
        ProduceTechnologyBom.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_2);
        produceTechnologyBomService.updateById(ProduceTechnologyBom);
        log.info("生产模块-工艺BOM模块-审核通过内容:{}",ProduceTechnologyBom);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('produce:technologyBOM:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        ProduceTechnologyBom ProduceTechnologyBom = new ProduceTechnologyBom();
        ProduceTechnologyBom.setUpdated(LocalDateTime.now());
        ProduceTechnologyBom.setUpdatedUser(principal.getName());
        ProduceTechnologyBom.setId(id);
        ProduceTechnologyBom.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_1);
        produceTechnologyBomService.updateById(ProduceTechnologyBom);
        log.info("生产模块-工艺BOM模块-审核通过内容:{}",ProduceTechnologyBom);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('produce:technologyBOM:valid')")
    @Transactional
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<ProduceTechnologyBom> lists = new ArrayList<>();

        for (Long id : ids){
            ProduceTechnologyBom ProduceTechnologyBom = new ProduceTechnologyBom();
            ProduceTechnologyBom.setUpdated(LocalDateTime.now());
            ProduceTechnologyBom.setUpdatedUser(principal.getName());
            ProduceTechnologyBom.setId(id);
            ProduceTechnologyBom.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_0);
            lists.add(ProduceTechnologyBom);

            // 查看订单是否有没选择工艺BOM的，有的则直接关联该款
            List<OrderProductOrder> orders = orderProductOrderService.listByNoTBomByNumBrand(ProduceTechnologyBom.getProductNum(),ProduceTechnologyBom.getProductBrand());

            if(orders!=null && !orders.isEmpty()){
                for(OrderProductOrder opo : orders){
                    opo.setTechnologyBomId(ProduceTechnologyBom.getId());
                }
            }
            orderProductOrderService.updateBatchById(orders);

        }
        produceTechnologyBomService.updateBatchById(lists);



        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('produce:technologyBOM:valid')")
    @Transactional
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        ProduceTechnologyBom ProduceTechnologyBom = new ProduceTechnologyBom();
        ProduceTechnologyBom.setUpdated(LocalDateTime.now());
        ProduceTechnologyBom.setUpdatedUser(principal.getName());
        ProduceTechnologyBom.setId(id);
        ProduceTechnologyBom.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_0);
        produceTechnologyBomService.updateById(ProduceTechnologyBom);

        // 查看订单是否有没选择工艺BOM的，有的则直接关联该款
        List<OrderProductOrder> orders = orderProductOrderService.listByNoTBomByNumBrand(ProduceTechnologyBom.getProductNum(),ProduceTechnologyBom.getProductBrand());

        if(orders!=null && !orders.isEmpty()){
            for(OrderProductOrder opo : orders){
                opo.setTechnologyBomId(ProduceTechnologyBom.getId());
            }
        }
        orderProductOrderService.updateBatchById(orders);

        log.info("生产模块-工艺BOM模块-审核通过内容:{}",ProduceTechnologyBom);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('produce:technologyBOM:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        // 假如订单关联了，不能反审核了。
        List<OrderProductOrder> orders = orderProductOrderService.listByTBomId(id);

        if(orders != null && orders.size() > 0){
            return ResponseResult.fail("已有订单关联，无法反审核!");
        }
        ProduceTechnologyBom ProduceTechnologyBom = new ProduceTechnologyBom();
        ProduceTechnologyBom.setUpdated(LocalDateTime.now());
        ProduceTechnologyBom.setUpdatedUser(principal.getName());
        ProduceTechnologyBom.setId(id);
        ProduceTechnologyBom.setStatus(DBConstant.TABLE_PRODUCE_TECHNOLOGY_BOM.STATUS_FIELDVALUE_3);
        produceTechnologyBomService.updateById(ProduceTechnologyBom);
        log.info("生产模块-反审核通过内容:{}",ProduceTechnologyBom);

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
