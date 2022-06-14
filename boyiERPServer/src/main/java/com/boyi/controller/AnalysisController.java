package com.boyi.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.vo.RealDosageVO;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.AnalysisMaterailVO;
import com.boyi.entity.AnalysisProductOrderVO;
import com.boyi.entity.AnalysisRequest;
import com.boyi.entity.OrderProductOrder;
import com.boyi.mapper.RepositoryBuyinDocumentMapper;
import com.boyi.service.AnalysisRequestService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-10-29
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController extends BaseController {



    /**
     * 产品订单，每日订单数目折线图
     */
    @GetMapping("/productOrderDailyNum")
    public ResponseResult productOrderDailyNum(String searchStartDate, String searchEndDate) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }

        List<AnalysisProductOrderVO> list = orderProductOrderService.listByDate(searchStartDate,searchEndDate);

        ArrayList<List<Object>> returnLists = new ArrayList<List<Object>>();

        Map<String, String> theDayOrderNumber = new TreeMap<>();

        HashSet<String> numsets = new HashSet<>();
        numsets.add("1");
        numsets.add("2");
        numsets.add("3");
        numsets.add("4");
        numsets.add("5");
        numsets.add("6");
        numsets.add("7");
        numsets.add("8");
        numsets.add("9");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < list.size(); i++) {

            AnalysisProductOrderVO obj = list.get(i);
            LocalDateTime created = obj.getCreated();
            String ymd = dtf.format(created);
            String orderNum = obj.getOrderNum();
            if(orderNum.contains("-")){
                orderNum = orderNum.split("-")[0];
            }

            // 假如没中文的,就进行归类日期
            if(numsets.contains(orderNum.substring(0,1))) {
                // 假如序号是7位，并且是12开头，并且创建日期的月份是1月，则修改年份为创建日期前一年
                if (orderNum.length() == 7 && orderNum.startsWith("12") && obj.getCreated().getMonthValue() == 1) {
                    LocalDateTime replacedDate = LocalDateTime.of(created.getYear() - 1,Integer.valueOf(obj.getOrderNum().substring(0, 2)), Integer.valueOf(obj.getOrderNum().substring(2, 4)), 0, 0);
                    ymd = dtf.format(replacedDate);

                } else {
                    if (orderNum.length() == 7) {
                        ymd = created.getYear() + "-" + obj.getOrderNum().substring(0, 2)+"-"+obj.getOrderNum().substring(2, 4);
                    } else if(orderNum.length() == 6){
                        ymd = created.getYear() + "-0" + obj.getOrderNum().substring(0, 1)+"-"+obj.getOrderNum().substring(1, 3);
                    }
                }
            }

            String oldVal = theDayOrderNumber.get(ymd);
            if(oldVal == null){
                oldVal = "0";
            }
            theDayOrderNumber.put(ymd,BigDecimalUtil.add(oldVal+"",obj.getOrderNumber()).toString());

        }

        for (Map.Entry<String,String> entry : theDayOrderNumber.entrySet()){
            ArrayList<Object> oneData = new ArrayList<>();
            oneData.add(entry.getKey());
            oneData.add(entry.getValue());
            returnLists.add(oneData);
        }

        return ResponseResult.succ(returnLists);
    }

    /**
     * 获取请求方法耗时统计
     */
    @GetMapping("/requestCast")
//    @PreAuthorize("hasAuthority('dataAnalysis:manage')")
    public ResponseResult onlineNum(Principal principal) {

        List<AnalysisRequest> list = analysisRequestService.list(new QueryWrapper<AnalysisRequest>().select("class_method", "avg(cast) as cast").groupBy("class_method").orderByDesc("cast"));

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        for (AnalysisRequest obj : list){
            legendData.add(obj.getClassMethod());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getClassMethod());
            nameValue.put("value",obj.getCast());
            seriesData.add(nameValue);
        }
        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }


    /**
     * 产品订单，款式数目合计列表
     */
    @GetMapping("/productOrder")
    public ResponseResult productOrder(String searchStartDate, String searchEndDate) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }

        List<AnalysisProductOrderVO> list = orderProductOrderService.listGroupByProductNum(searchStartDate,searchEndDate);

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int limitShowNum = 25;

        String otherLegendData = "其他";
        String otherSum = "0";

        for (int i = 0; i < list.size(); i++) {
            AnalysisProductOrderVO obj = list.get(i);
            if(i>=limitShowNum){
                otherSum = BigDecimalUtil.add(otherSum,obj.getSum()).toString();
                continue;
            }

            legendData.add(obj.getProductNum());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getProductNum());
            nameValue.put("value",obj.getSum());
            seriesData.add(nameValue);
        }
        if(!otherSum.equals("0")){
            legendData.add(otherLegendData);
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",otherLegendData);
            nameValue.put("value",otherSum);
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }


    /**
     * 产品订单，品牌数目占比
     */
    @GetMapping("/productOrderByProductBrandPercent")
    public ResponseResult productOrderByProductBrandPercent(String searchStartDate, String searchEndDate) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }

        List<AnalysisProductOrderVO> list = orderProductOrderService.listGroupByProductBrand(searchStartDate,searchEndDate);

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int limitShowNum = 25;

        String otherLegendData = "其他";
        String otherSum = "0";

        for (int i = 0; i < list.size(); i++) {
            AnalysisProductOrderVO obj = list.get(i);
            if(i>=limitShowNum){
                otherSum = BigDecimalUtil.add(otherSum,obj.getSum()).toString();
                continue;
            }

            legendData.add(obj.getProductBrand());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getProductBrand());
            nameValue.put("value",obj.getSum());
            seriesData.add(nameValue);
        }
        if(!otherSum.equals("0")){
            legendData.add(otherLegendData);
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",otherLegendData);
            nameValue.put("value",otherSum);
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }

    /**
     * 产品订单，款式被挑选最多的
     */
    @GetMapping("/productOrderByMostProductNumPercent")
    public ResponseResult productOrderByMostProductNumPercent(String searchStartDate, String searchEndDate) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }

        List<AnalysisProductOrderVO> list = orderProductOrderService.listGroupByMostProductNum(searchStartDate,searchEndDate);

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int limitShowNum = 25;


        for (int i = 0; i < list.size(); i++) {
            AnalysisProductOrderVO obj = list.get(i);
            if(i>=limitShowNum){
                continue;
            }

            legendData.add(obj.getProductNum());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getProductNum());
            nameValue.put("value",obj.getSum());
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }


    /**
     *  供应商金额排行榜
     */
    @GetMapping("/materialSupplierAmountPercent")
    public ResponseResult materialSupplierAmountPercent(String searchStartDate, String searchEndDate,String searchField) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }
        List<AnalysisMaterailVO> list = null;
        if(searchField.equals("all")){
            list = repositoryBuyinDocumentService.listSupplierAmountPercent(searchStartDate,searchEndDate);
        }else{
            list = repositoryBuyinDocumentService.listSupplierAmountPercentBySupType(searchStartDate,searchEndDate,searchField);
        }

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int limitShowNum = 25;


        for (int i = 0; i < list.size(); i++) {
            AnalysisMaterailVO obj = list.get(i);
            if(i>=limitShowNum){
                continue;
            }

            legendData.add(obj.getSupplierName());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getSupplierName());
            nameValue.put("value",new BigDecimal(obj.getSum()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }


    /**
     *  物料金额排行榜
     */
    @GetMapping("/materialAmountPercent")
    public ResponseResult materialAmountPercent(String searchStartDate, String searchEndDate,String searchField) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }
        List<AnalysisMaterailVO> list = null;
        if(searchField.equals("all")){
            list = repositoryBuyinDocumentService.listMaterialAmountPercent(searchStartDate,searchEndDate);
        }else{
            list = repositoryBuyinDocumentService.listMaterialAmountPercentByMaterialType(searchStartDate,searchEndDate,searchField);
        }

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int limitShowNum = 25;


        for (int i = 0; i < list.size(); i++) {
            AnalysisMaterailVO obj = list.get(i);
            if(i>=limitShowNum){
                continue;
            }

            legendData.add(obj.getMaterialName());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getMaterialName());
            nameValue.put("value",new BigDecimal(obj.getSum()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }

    /**
     *  物料金额排行榜
     */
    @GetMapping("/productNumBrandMaterialWinPercent")
    public ResponseResult productNumBrandMaterialWinPercent(String searchStartDate, String searchEndDate,String searchField) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }

        List<AnalysisMaterailVO> list = getProductNumBrandMaterialWinLoseLists(searchStartDate,searchEndDate);

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int limitShowNum = 25;


        for (int i = 0; i < list.size(); i++) {
            AnalysisMaterailVO obj = list.get(i);
            if(i>=limitShowNum){
                continue;
            }
            if(Double.valueOf(obj.getSquareFoot()) < 0){
                continue;
            }

            legendData.add(obj.getProductNumBrandMaterial());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getProductNumBrandMaterial());
            nameValue.put("value",new BigDecimal(obj.getSquareFoot()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }

    /**
     *  物料金额排行榜
     */
    @GetMapping("/productNumBrandMaterialLosePercent")
    public ResponseResult productNumBrandMaterialLosePercent(String searchStartDate, String searchEndDate,String searchField) {

        if(StringUtils.isBlank(searchStartDate) || searchStartDate.equals("null")||StringUtils.isBlank(searchEndDate) || searchEndDate.equals("null")){
            return ResponseResult.fail("查询开始和截至日期不能为空");
        }

        List<AnalysisMaterailVO> list = getProductNumBrandMaterialWinLoseLists(searchStartDate,searchEndDate);

        HashMap<String, Object> returnMap = new HashMap<>();
        List<String> legendData = new ArrayList<String>();
        List<HashMap<String, Object>> seriesData = new ArrayList<HashMap<String, Object>>();

        int count = 0;

        for (int i = list.size() -1; i > 0; i--) {
            AnalysisMaterailVO obj = list.get(i);
            count ++;
            if(count>=25){
                continue;
            }
            if(Double.valueOf(obj.getSquareFoot()) > 0){
                continue;
            }

            legendData.add(obj.getProductNumBrandMaterial());
            HashMap<String, Object> nameValue = new HashMap<>();
            nameValue.put("name",obj.getProductNumBrandMaterial());
            nameValue.put("value",Math.abs(new BigDecimal(obj.getSquareFoot()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()));
            seriesData.add(nameValue);
        }

        returnMap.put("legendData",legendData);
        returnMap.put("seriesData",seriesData);
        return ResponseResult.succ(returnMap);
    }

    private List<AnalysisMaterailVO> getProductNumBrandMaterialWinLoseLists(String searchStartDate, String searchEndDate) {

        List<RealDosageVO> lists = produceProductConstituentService.listRealDosageBetweenDate(searchStartDate,searchEndDate);
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


        HashMap<String, String> materialSum = new HashMap<>();
        HashMap<String, String> materialCount = new HashMap<>();


        // 根据物料进行分组，对实际用料进行平均求值,
        for(RealDosageVO vo : lists){
            String key = vo.getProductNum() + "_" + vo.getProductBrand() + "_" + vo.getMaterialId()+"("+vo.getMaterialName()+")";
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

        List<AnalysisMaterailVO> returnLists = new ArrayList<>();
        TreeMap<Double, String> resultMap = new TreeMap<>();

        // 求出均值
        for(RealDosageVO vo : lists) {
            String key = vo.getProductNum() + "_" + vo.getProductBrand() + "_" + vo.getMaterialId()+"("+vo.getMaterialName()+")";
            vo.setAvgDosage(BigDecimalUtil.div(materialSum.get(key),materialCount.get(key)).toString());

            // 计划用量-实际用量， > 0的就是盈利，<0的就是亏损
            if(resultMap.containsValue(key)){
                continue;
            }
            resultMap.put(BigDecimalUtil.sub(vo.getPlanDosage(),vo.getAvgDosage()).doubleValue(),key);
        }
        for (Map.Entry<Double ,String> entry:resultMap.descendingMap().entrySet()){
            Double winSquareFoot = entry.getKey();
            String productNumBrandMaterialid = entry.getValue();
            AnalysisMaterailVO vo = new AnalysisMaterailVO();
            vo.setProductNumBrandMaterial(productNumBrandMaterialid);
            vo.setSquareFoot(winSquareFoot+"");
            returnLists.add(vo);
        }
        return returnLists;

    }


}
