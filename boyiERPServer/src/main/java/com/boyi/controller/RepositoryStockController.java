package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.common.vo.OrderProductCalVO;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryStock;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.util.*;

/**
 * <p>
 * 库存表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
@Slf4j
@RestController
@RequestMapping("/repository/stock")
public class RepositoryStockController extends BaseController {
    @Value("${poi.repositoryStockDemoPath}")
    private String poiDemoPath;

    /**
     * 获取采购入库 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('repository:stock:export')")
    public void export(HttpServletResponse response, String searchStr, String searchField) {
        Page<RepositoryStock> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
             if (searchField.equals("materialName")) {
                queryField = "material_name";
            }else if (searchField.equals("materialId")) {
                 queryField = "material_id";
             }
        }
        Page page = getPage();
        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = repositoryStockService.pageBySearch(page,queryField,searchField,searchStr);

        // 获取全部的物料
        HashMap<String, RepositoryStock> materialIds = new HashMap<>();

        List<RepositoryStock> records = pageData.getRecords();
        for (RepositoryStock stock : records){
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
            // 获取已报未入库数量
            List<OrderProductCalVO> noInNums = produceOrderMaterialProgressService.listNoInNumsWithMaterialIds(materialIds.keySet());
            HashMap<String, String> noInGroupNums = new HashMap<>();
            HashMap<String, List<OrderProductCalVO>> noInDetails = new HashMap<>();

            for(OrderProductCalVO vo : noInNums){
                String materialId = vo.getMaterialId();
                String theSum = noInGroupNums.get(materialId);
                if(theSum==null || theSum.isEmpty()){
                    noInGroupNums.put(materialId,BigDecimalUtil.sub(vo.getPreparedNum(),vo.getInNum()).toString());
                    ArrayList<OrderProductCalVO> objs = new ArrayList<>();
                    objs.add(vo);
                    noInDetails.put(materialId,objs);
                }else{
                    noInGroupNums.put(materialId, BigDecimalUtil.add(theSum,BigDecimalUtil.sub(vo.getPreparedNum(),vo.getInNum()).toString()).toString());
                    List<OrderProductCalVO> objs = noInDetails.get(materialId);
                    objs.add(vo);
                }
            }

            for(Map.Entry<String,String> entry : noInGroupNums.entrySet()){
                String materialId = entry.getKey();
                String needNum = entry.getValue();
                RepositoryStock stock = materialIds.get(materialId);
                stock.setNoInNum(needNum);
            }
        }


        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(RepositoryStock.class,1,0).export(null,null,response,fis,pageData.getRecords(),"报表.xlsx", new HashMap<>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    /**
     * 获取库存 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:stock:list')")
    public ResponseResult list(String searchStr, String searchField) {
        Page<RepositoryStock> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
             if (searchField.equals("materialName")) {
                queryField = "material_name";
            }else if (searchField.equals("materialId")) {
                 queryField = "material_id";
             }
             else {
                return ResponseResult.fail("搜索字段不存在");
            }
        }
        try {
            pageData = repositoryStockService.pageBySearch(getPage(),queryField,searchField,searchStr);

            // 获取全部的物料
            HashMap<String, RepositoryStock> materialIds = new HashMap<>();

            List<RepositoryStock> records = pageData.getRecords();
            for (RepositoryStock stock : records){
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
                    stock.setNoProductionNums(noProductionGroupDetails.get(materialId));
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
                    stock.setNoPickNums(noPickDetails.get(materialId));
                }
                // 获取已报未入库数量
                List<OrderProductCalVO> noInNums = produceOrderMaterialProgressService.listNoInNumsWithMaterialIds(materialIds.keySet());
                HashMap<String, String> noInGroupNums = new HashMap<>();
                HashMap<String, List<OrderProductCalVO>> noInDetails = new HashMap<>();

                for(OrderProductCalVO vo : noInNums){
                    String materialId = vo.getMaterialId();
                    String theSum = noInGroupNums.get(materialId);
                    if(theSum==null || theSum.isEmpty()){
                        noInGroupNums.put(materialId,BigDecimalUtil.sub(vo.getPreparedNum(),vo.getInNum()).toString());
                        ArrayList<OrderProductCalVO> objs = new ArrayList<>();
                        objs.add(vo);
                        noInDetails.put(materialId,objs);
                    }else{
                        noInGroupNums.put(materialId, BigDecimalUtil.add(theSum,BigDecimalUtil.sub(vo.getPreparedNum(),vo.getInNum()).toString()).toString());
                        List<OrderProductCalVO> objs = noInDetails.get(materialId);
                        objs.add(vo);
                    }
                }

                for(Map.Entry<String,String> entry : noInGroupNums.entrySet()){
                    String materialId = entry.getKey();
                    String needNum = entry.getValue();
                    RepositoryStock stock = materialIds.get(materialId);
                    stock.setNoInNum(needNum);
                    stock.setNoInNums(noInDetails.get(materialId));
                }
            }


        }catch (PersistenceException e){
            return ResponseResult.fail("物料编码请不要输入中文");
        }



        // 库存数量为0的过滤.
        /*List<RepositoryStock> records = pageData.getRecords();
        ArrayList<RepositoryStock> newRecords = new ArrayList<>();
        for (RepositoryStock stock : records){
            if(stock.getNum() != 0){
                newRecords.add(stock);
            }
        }

        pageData.setRecords(newRecords);*/
        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        return ResponseResult.succ(pageData);
    }


}
