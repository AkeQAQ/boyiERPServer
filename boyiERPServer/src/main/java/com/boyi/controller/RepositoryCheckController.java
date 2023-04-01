package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.EmailUtils;
import com.boyi.common.utils.ThreadUtils;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/repository/check")
public class RepositoryCheckController extends BaseController {

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('repository:check:del')")
    public ResponseResult delete(@RequestBody Long[] ids)throws Exception {

        try {

            for(Long id : ids){

                List<RepositoryCheckDetail> oldDetails = repositoryCheckDetailService.listByDocumentId(id);

                Map<String, Double> needSubMap = new HashMap<>();

                // 把库存 进行调整，进行+-
                for (RepositoryCheckDetail item : oldDetails) {
                    Double materialNum = needSubMap.get(item.getMaterialId());
                    if(materialNum == null){
                        materialNum= 0D;
                    }
                    BigDecimal mul = BigDecimalUtil.mul(-1.0D, item.getChangeNum()); // 取反：+1 其实就是要变成-1
                    needSubMap.put(item.getMaterialId(), BigDecimalUtil.add(materialNum,mul.doubleValue()).doubleValue());
                }
                repositoryStockService.addNumByMaterialIdFromMap(needSubMap); //  带了+- 号，所以还是用加法
            }


        boolean flag = repositoryCheckService.removeByIds(Arrays.asList(ids));

        log.info("删除盘点表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
        if(!flag){
            return ResponseResult.fail("盘点删除失败");
        }

        boolean flagDetail = repositoryCheckDetailService.delByDocumentIds(ids);
        log.info("删除盘点表详情信息,document_id:{},是否成功：{}",ids,flagDetail?"成功":"失败");

        if(!flagDetail){
            return ResponseResult.fail("盘点详情表没有删除成功!");
        }
        return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('repository:check:list')")
    public ResponseResult queryById(Long id) {
        RepositoryCheck repositoryCheck = repositoryCheckService.getById(id);

        List<RepositoryCheckDetail> details = repositoryCheckDetailService.listByDocumentId(id);

        for (RepositoryCheckDetail detail : details){
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setSpecs(material.getSpecs());
        }

        repositoryCheck.setRowList(details);
        return ResponseResult.succ(repositoryCheck);
    }

    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('repository:check:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody RepositoryCheck repositoryCheck)throws Exception {

        if(repositoryCheck.getRowList() ==null || repositoryCheck.getRowList().size() ==0){
            return ResponseResult.fail("物料信息不能为空");
        }

        String yearAndMonth = repositoryCheck.getCheckDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<RepositoryCheck> list = repositoryCheckService.list(new QueryWrapper<RepositoryCheck>().ne(DBConstant.TABLE_REPOSITORY_CHECK.ID_FIELDNAME,repositoryCheck.getId()).likeRight(DBConstant.TABLE_REPOSITORY_CHECK.CHECK_DATE_FIELDNAME, yearAndMonth));

        if(list.size() > 0){
            return ResponseResult.fail("已存在该月盘点，请确认");
        }

        repositoryCheck.setUpdated(LocalDateTime.now());
        repositoryCheck.setUpdatedUser(principal.getName());

        try {

            List<RepositoryCheckDetail> oldDetails = repositoryCheckDetailService.listByDocumentId(repositoryCheck.getId());

            Map<String, Double> needSubMap = new HashMap<>();

            // 把库存 进行调整，进行+-
            for (RepositoryCheckDetail item : oldDetails) {
                Double materialNum = needSubMap.get(item.getMaterialId());
                if(materialNum == null){
                    materialNum= 0D;
                }
                BigDecimal mul = BigDecimalUtil.mul(-1.0D, item.getChangeNum()); // 取反：+1 其实就是要变成-1
                needSubMap.put(item.getMaterialId(), BigDecimalUtil.add(materialNum,mul.doubleValue()).doubleValue());
            }

            repositoryStockService.addNumByMaterialIdFromMap(needSubMap); //  带了+- 号，所以还是用加法

            Map<String, Double> needAddMap = new HashMap<>();

            //1. 先删除老的，再插入新的
            boolean flag = repositoryCheckDetailService.removeByDocId(repositoryCheck.getId());
            if(flag){
                repositoryCheckService.updateById(repositoryCheck);

                HashMap<String, Double> needValidMap = new HashMap<>();

                for (RepositoryCheckDetail item : repositoryCheck.getRowList()){
                    item.setId(null);
                    item.setDocumentId(repositoryCheck.getId());

                    Double materialNum = needAddMap.get(item.getMaterialId());
                    if(materialNum == null){
                        materialNum= 0D;
                    }

                    if(item.getChangeNum() < 0){
                        needValidMap.put(item.getMaterialId(),Math.abs(item.getChangeNum()));
                    }

                    needAddMap.put(item.getMaterialId(), BigDecimalUtil.add(materialNum,item.getChangeNum()).doubleValue());
                }

                repositoryStockService.validStockNum(needValidMap);


                repositoryCheckDetailService.saveBatch(repositoryCheck.getRowList());
                repositoryStockService.addNumByMaterialIdFromMap(needAddMap);

                log.info("盘点模块-更新内容:{}",repositoryCheck);
            }else{
                throw new RuntimeException("操作失败，期间detail删除失败");
            }

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常",e);
            log.error("报错.",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:check:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody RepositoryCheck repositoryCheck)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        repositoryCheck.setCreated(now);
        repositoryCheck.setUpdated(now);
        repositoryCheck.setCreatedUser(principal.getName());
        repositoryCheck.setUpdatedUser(principal.getName());
        repositoryCheck.setStatus(DBConstant.TABLE_REPOSITORY_CHECK.STATUS_FIELDVALUE_1);
        String yearAndMonth = repositoryCheck.getCheckDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<RepositoryCheck> list = repositoryCheckService.list(new QueryWrapper<RepositoryCheck>().likeRight(DBConstant.TABLE_REPOSITORY_CHECK.CHECK_DATE_FIELDNAME, yearAndMonth));

        if(list.size() > 0){
            return ResponseResult.fail("已存在该月盘点，请确认");
        }
        try {


            repositoryCheckService.save(repositoryCheck);

            for (RepositoryCheckDetail item : repositoryCheck.getRowList()){
                item.setDocumentId(repositoryCheck.getId());
            }

            repositoryCheckDetailService.saveBatch(repositoryCheck.getRowList());

            Map<String, Double> needAddMap = new HashMap<>();

            HashMap<String, Double> needValidMap = new HashMap<>();


            // 把库存 进行调整，进行+-
            for (RepositoryCheckDetail item : repositoryCheck.getRowList()) {
                Double materialNum = needAddMap.get(item.getMaterialId());
                if(materialNum == null){
                    materialNum= 0D;
                }
                if(item.getChangeNum()<0){
                    needValidMap.put(item.getMaterialId(),item.getChangeNum());
                }
                needAddMap.put(item.getMaterialId(), BigDecimalUtil.add(materialNum,item.getChangeNum()).doubleValue());
            }

            repositoryStockService.validStockNum(needValidMap);

            repositoryStockService.addNumByMaterialIdFromMap(needAddMap);

            return ResponseResult.succ("新增成功");
        } catch (Exception e) {
            log.error("盘点单，插入异常",e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 获取盘点 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:check:list')")
    public ResponseResult list(String searchStr, String searchField, String searchStartDate, String searchEndDate) {
        Page<RepositoryCheck> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
             if (searchField.equals("materialName")) {
                queryField = "material_name";

            }else if (searchField.equals("id")) {
                queryField = "id";

            } else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }else {

        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        pageData = repositoryCheckService.innerQueryBySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate);
        return ResponseResult.succ(pageData);
    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('repository:check:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        LocalDateTime now2 = LocalDateTime.now();
        LocalDate now = LocalDate.now();
        RepositoryCheck repositoryCheck = new RepositoryCheck();
        repositoryCheck.setUpdated(now2);
        repositoryCheck.setUpdatedUser(principal.getName());
        repositoryCheck.setId(id);
        repositoryCheck.setStatus(DBConstant.TABLE_REPOSITORY_CHECK.STATUS_FIELDVALUE_0);
        repositoryCheckService.updateById(repositoryCheck);

        ThreadUtils.executorService.submit(() -> {
            try {


                // 盘点时，生成该月份截止的大皮物料废库存数量（库存-未投数量-投产未领），废库存金额（以最近单价）。

                List<RepositoryStock> stocks = repositoryStockService.listBy01MaterialIds();

                HashMap<String, RepositoryStock> materialIds = new HashMap<>();

                StringBuilder sb = new StringBuilder();
                for(RepositoryStock stock : stocks){
                    materialIds.put(stock.getMaterialId(),stock);
                    stock.setNeedNum("0");
                    stock.setNoInNum("0");
                    stock.setNoPickNum("0");
                }
                if(!materialIds.isEmpty()){

                    // 获取未投产应需用量
                    List<OrderProductCalVO> noProductionNums = orderProductOrderService.calNoProductOrdersWithMaterialIds(materialIds.keySet());
                    HashMap<String, String> noProductionGroupNums = new HashMap<>();
                    HashMap<String, List<OrderProductCalVO>> noProductionGroupDetails = new HashMap<>();

                    for(OrderProductCalVO vo : noProductionNums){
                        String materialId = vo.getMaterialId();
                        String theSum = noProductionGroupNums.get(materialId);
                        if(theSum==null || theSum.isEmpty()){
                            noProductionGroupNums.put(materialId,vo.getNeedNum());
                            ArrayList<OrderProductCalVO> orderProductCalVOS = new ArrayList<>();
                            orderProductCalVOS.add(vo);
                            noProductionGroupDetails.put(materialId,orderProductCalVOS);
                        }else{
                            noProductionGroupNums.put(materialId, BigDecimalUtil.add(theSum,vo.getNeedNum()).toString());
                            List<OrderProductCalVO> orderProductCalVOS = noProductionGroupDetails.get(materialId);
                            orderProductCalVOS.add(vo);
                        }
                    }

                    for(Map.Entry<String,String> entry : noProductionGroupNums.entrySet()){
                        String materialId = entry.getKey();
                        String needNum = entry.getValue();
                        RepositoryStock stock = materialIds.get(materialId);
                        stock.setNeedNum(needNum);
                    }

                    // 获取投产未领数量
                    List<RepositoryStock> noPickMaterials = orderProductOrderService.listNoPickMaterialsWithMaterialIds(materialIds.keySet());
                    HashMap<String, String> noPickGroupNums = new HashMap<>();
                    HashMap<String, List<RepositoryStock>> noPickDetails = new HashMap<>();

                    for(RepositoryStock vo : noPickMaterials){
                        String materialId = vo.getMaterialId();
                        String theSum = noPickGroupNums.get(materialId);
                        if(theSum==null || theSum.isEmpty()){
                            noPickGroupNums.put(materialId,vo.getNum()+"");
                            ArrayList<RepositoryStock> objs = new ArrayList<>();
                            objs.add(vo);
                            noPickDetails.put(materialId,objs);
                        }else{
                            noPickGroupNums.put(materialId, BigDecimalUtil.add(theSum,vo.getNum()+"").toString());
                            List<RepositoryStock> objs = noPickDetails.get(materialId);
                            objs.add(vo);
                        }
                    }

                    for(Map.Entry<String,String> entry : noPickGroupNums.entrySet()){
                        String materialId = entry.getKey();
                        String needNum = entry.getValue();
                        RepositoryStock stock = materialIds.get(materialId);
                        stock.setNoPickNum(needNum);
                    }

                    // 假如库存-未投-投产未领 >0 的，进行保存


                    List<RepositoryStockLost> stockLists = new ArrayList<>();
                    Double totalAmount = 0.0D;
                    Double totalNum = 0.0D;

                    for(RepositoryStock stock : stocks) {
                        Double stockNum = stock.getNum();
                        String needNum = stock.getNeedNum();
                        String noPickNum = stock.getNoPickNum();

                        RepositoryStockLost lost = new RepositoryStockLost();
                        lost.setMaterialId(stock.getMaterialId());
                        lost.setNum(stock.getNum());
                        lost.setNeedNum(new BigDecimal(stock.getNeedNum()));
                        lost.setNoPickNum(new BigDecimal(stock.getNoPickNum()));

                        BeanUtils.copyProperties(stock,lost);
                        double lostNum = BigDecimalUtil.sub(stockNum + "", needNum).subtract(new BigDecimal(noPickNum)).doubleValue();
                        if(lostNum<=0){
                            continue;
                        }

                        lost.setCreated(now2);
                        lost.setUpdated(now2);
                        lost.setCreatedDate(now);
                        lost.setLostNum(new BigDecimal(lostNum).setScale(2,   BigDecimal.ROUND_HALF_UP));
                        BaseSupplierMaterial bsm = baseSupplierMaterialService.getSuccessPriceByLatestPrice(stock.getMaterialId());
                        if(bsm!=null){
                            lost.setLatestPrice(new BigDecimal(bsm.getPrice()));
                        }else{
                            lost.setLatestPrice(new BigDecimal("0"));
                        }
                        BaseMaterial bm = baseMaterialService.getById(stock.getMaterialId());
                        sb.append("物料ID："+stock.getMaterialId()+",物料名称:"+bm.getName()+",库存数量:["+lost.getNum()+"],未投数量:["+lost.getNeedNum()+"],未领数量:["+lost.getNoPickNum()+"],废库存数量:["+lost.getLostNum()+"],最近单价:["+lost.getLatestPrice()+"] <br>");
                        totalAmount = BigDecimalUtil.add(totalAmount,BigDecimalUtil.mul(lost.getNum(),lost.getLatestPrice().doubleValue()).doubleValue()).doubleValue();
                        totalNum = BigDecimalUtil.add(totalNum,lost.getNum()).doubleValue();
                        stockLists.add(lost);
                    }
                    sb.append(",总废库存数量:["+totalNum+"],总金额:["+totalAmount+"]");

                    if(!stockLists.isEmpty()){
                        repositoryStockLostService.saveBatch(stockLists);
                    }

                }
                EmailUtils.sendMail(EmailUtils.MODULE_LOST_MATERIAL_NAME,
                        "244454526@qq.com",new String[]{}, sb.toString());
            }catch (Exception e){
                log.error("发生异常.",e);
            }
        });


        log.info("仓库模块-盘点模块-审核通过内容:{}",repositoryCheck);

        return ResponseResult.succ("审核通过");
    }


    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('repository:check:returnValid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {

        RepositoryCheck repositoryCheck = new RepositoryCheck();
        repositoryCheck.setUpdated(LocalDateTime.now());
        repositoryCheck.setUpdatedUser(principal.getName());
        repositoryCheck.setId(id);
        repositoryCheck.setStatus(DBConstant.TABLE_REPOSITORY_CHECK.STATUS_FIELDVALUE_1);
        repositoryCheckService.updateById(repositoryCheck);
        log.info("仓库模块-反审核通过内容:{}",repositoryCheck);


        return ResponseResult.succ("反审核成功");
    }

}
