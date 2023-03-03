package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelImportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-03-26
 */
@RestController
@RequestMapping("/produce/orderMaterialProgress")
@Slf4j
public class ProduceOrderMaterialProgressController extends BaseController {

    private ExecutorService fixThread = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public static void main(String[] args) {
        BigDecimal preparedNum = new BigDecimal("1").add(new BigDecimal("2"));
        System.out.println(preparedNum);

        TreeMap<String, String> map = new TreeMap<>();
        map.put("04.04.1","1");
        map.put("04.04.2","2");
        map.put("04.04.2","22");

        map.put("04.04.1","11");
        System.out.println(map.values());

    }

    @Value("${poi.orderProductOrderImportDemoPath}")
    private String poiImportDemoPath;
    public static final Map<Object,Object> replaceMap = new HashMap<Object,Object>();
    static {
        replaceMap.put("订单",0);
        replaceMap.put("回单",1);
    }


    /**
     * 分组显示备料信息
     */
    @PostMapping("/showMsgs")
    @Transactional
    public ResponseResult showMsgs(Principal principal,Long orderId,@Validated @RequestBody List<ProduceOrderMaterialProgress> materialProgresses) {
        try {

            HashMap<String, Collection<SupplierMaterialVO>> returnMap = new HashMap<>();
            TreeMap<String, Collection<SupplierMaterialVO>> zeroOrMoreLists = new TreeMap<>();
            TreeMap<String, List<SupplierMaterialVO>> oneLists = new TreeMap<>();


            Map<String, Map<String,SupplierMaterialVO>> materialSupplier_obj = new HashMap<>();

            // 没有供应商信息的物料
            List<SupplierMaterialVO> zeroSupplierIdMsgLists = new ArrayList<SupplierMaterialVO>();


            // 1. 筛选填了数量的物料
            // 2. 查询该物料的物料供应商表，查询该物料的价目信息表
            // 3. 遍历2表，存储对象，map : materialId:List<obj>，物料供应商表设置供应商的编码、供应商名称。价目表设置系统内部审核价格
            // 4.
                // 1. 当供应商数为0 ，则分类到0或多个供应商的 集合
                // 2. 当供应商数为1，则分类到1供应商集合
                // 3. 当供应商数>1，则分类到0或多个供应商的集合


            for (ProduceOrderMaterialProgress process : materialProgresses) {
                boolean noAddNum =(process.getAddNum() == null || process.getAddNum().isEmpty() || Double.valueOf(process.getAddNum()) <= 0);
                boolean noAddNums =( process.getAddNums() == null || process.getAddNums().isEmpty() || Double.valueOf(process.getAddNums()) <= 0);
                if(noAddNum
                        && noAddNums){
                    continue;
                }
                String addNum = "";
                if(!noAddNum){
                    addNum = process.getAddNum();
                }else{
                    addNum=process.getAddNums();
                }
                String innerMaterialId = process.getMaterialId();
                BaseMaterial bm = baseMaterialService.getById(innerMaterialId);

                List<Map<String, Object>> details = process.getDetails();
                HashSet<String> productNumBrandSets = new HashSet<>();
                if(details!=null && !details.isEmpty() &&orderId==null){

                    for(Map<String,Object> obj : details){
                        StringBuilder sb = new StringBuilder();
                        sb.append(obj.get("productNum").toString()).append("_").append(obj.get("productBrand"));
                        productNumBrandSets.add(sb.toString());
                    }

                }
                if(orderId!=null){
                    OrderProductOrder opo = orderProductOrderService.getById(orderId);
                    StringBuilder sb = new StringBuilder();
                    sb.append(opo.getProductNum()).append("_").append(opo.getProductBrand());
                    productNumBrandSets.add(sb.toString());
                }

                List<BuyMaterialSupplier> buyMaterialSuppliers = buyMaterialSupplierService.listByInnerMaterialId(innerMaterialId);
                for(BuyMaterialSupplier buyMaterialSupplier:buyMaterialSuppliers){
                    String supplierId = buyMaterialSupplier.getSupplierId();
                    Map<String, SupplierMaterialVO> supplierMaps = materialSupplier_obj.get(innerMaterialId);
                    if(supplierMaps==null){
                        supplierMaps = new HashMap<String,SupplierMaterialVO>();
                        materialSupplier_obj.put(innerMaterialId,supplierMaps);
                    }
                    SupplierMaterialVO supplierMaterialVO = supplierMaps.get(supplierId);
                    if(supplierMaterialVO==null){
                        supplierMaterialVO = new SupplierMaterialVO();
                        supplierMaps.put(supplierId,supplierMaterialVO);
                        supplierMaterialVO.setMaterialInnerName(bm.getName());
                        supplierMaterialVO.setUnit(bm.getUnit());
                        supplierMaterialVO.setNum(addNum);

                        if(productNumBrandSets.size()!=0){
                            StringBuilder sb = new StringBuilder("(备注:");
                            for(String str:productNumBrandSets){
                                sb.append(str).append(",");
                            }
                            sb.deleteCharAt(sb.length()-1);
                            sb.append(")");
                            supplierMaterialVO.setProductNumBrand(sb.toString());
                        }

                        supplierMaterialVO.setMaterialInnerId(innerMaterialId);
                        supplierMaterialVO.setSupplierId(supplierId);
                        BaseSupplier bs = baseSupplierService.getById(supplierId);
                        supplierMaterialVO.setSupplierName(bs.getName());

                        supplierMaterialVO.setMaterialOutId(buyMaterialSupplier.getSupplierMaterialId());
                        supplierMaterialVO.setMaterialOutName(buyMaterialSupplier.getSupplierMaterialName());
                    }
                }

                List<BaseSupplierMaterial> baseSupplierMaterials = baseSupplierMaterialService.listByMaterialIdWithSuccessDate(innerMaterialId,LocalDate.now());
                for(BaseSupplierMaterial baseSupplierMaterial : baseSupplierMaterials){
                    String supplierId = baseSupplierMaterial.getSupplierId();
                    Map<String, SupplierMaterialVO> supplierMaps = materialSupplier_obj.get(innerMaterialId);
                    if(supplierMaps==null){
                        supplierMaps = new HashMap<String,SupplierMaterialVO>();
                        materialSupplier_obj.put(innerMaterialId,supplierMaps);
                    }
                    SupplierMaterialVO supplierMaterialVO = supplierMaps.get(supplierId);
                    if(supplierMaterialVO==null){
                        supplierMaterialVO = new SupplierMaterialVO();
                        supplierMaps.put(supplierId,supplierMaterialVO);
                        supplierMaterialVO.setMaterialInnerName(bm.getName());
                        supplierMaterialVO.setUnit(bm.getUnit());
                        supplierMaterialVO.setNum(addNum);
                        if(productNumBrandSets.size()!=0){
                            StringBuilder sb = new StringBuilder("(备注:");
                            for(String str:productNumBrandSets){
                                sb.append(str).append(",");
                            }
                            sb.deleteCharAt(sb.length()-1);
                            sb.append(")");
                            supplierMaterialVO.setProductNumBrand(sb.toString());
                        }

                        supplierMaterialVO.setMaterialInnerId(innerMaterialId);
                        supplierMaterialVO.setSupplierId(supplierId);
                        BaseSupplier bs = baseSupplierService.getById(supplierId);
                        supplierMaterialVO.setSupplierName(bs.getName());

                        supplierMaterialVO.setPrice(baseSupplierMaterial.getPrice());
                    }else{
                        supplierMaterialVO.setPrice(baseSupplierMaterial.getPrice());
                    }

                }
                // 假如都是空，则显示物料信息
                if((buyMaterialSuppliers== null || buyMaterialSuppliers.size() == 0) &&
                        (baseSupplierMaterials==null || baseSupplierMaterials.size()==0)){
                    SupplierMaterialVO supplierMaterialVO = new SupplierMaterialVO();
                    supplierMaterialVO.setMaterialInnerId(innerMaterialId);
                    supplierMaterialVO.setMaterialInnerName(bm.getName());
                    supplierMaterialVO.setUnit(bm.getUnit());
                    supplierMaterialVO.setNum(addNum);
                    if(productNumBrandSets.size()!=0){
                        StringBuilder sb = new StringBuilder("(备注:");
                        for(String str:productNumBrandSets){
                            sb.append(str).append(",");
                        }
                        sb.deleteCharAt(sb.length()-1);
                        sb.append(")");
                        supplierMaterialVO.setProductNumBrand(sb.toString());
                    }

                    zeroSupplierIdMsgLists.add(supplierMaterialVO);
                }
            }

            // 遍历
            for(Map.Entry<String,Map<String,SupplierMaterialVO>> entry : materialSupplier_obj.entrySet()){
                String materialId = entry.getKey();
                Map<String, SupplierMaterialVO> suppliers = entry.getValue();
                if(suppliers.size()==1){
                    // 按供应商顺序分
                    for(Map.Entry<String,SupplierMaterialVO> supplier : suppliers.entrySet()){
                        List<SupplierMaterialVO> supplierMaps = oneLists.get(supplier.getKey());
                        if(supplierMaps == null){
                            supplierMaps = new ArrayList<SupplierMaterialVO>();
                            oneLists.put(supplier.getKey(),supplierMaps);
                        }
                        supplierMaps.add(supplier.getValue());
                    }
                }else{
                    // 多个物料的，按物料顺序
                    zeroOrMoreLists.put(materialId,suppliers.values());
                }

            }


            ArrayList<SupplierMaterialVO> zeroOrMoreAllLists = new ArrayList<>();
            ArrayList<SupplierMaterialVO> oneAllLists = new ArrayList<>();


            for(Map.Entry<String,Collection<SupplierMaterialVO>> entry : zeroOrMoreLists.entrySet()){
                zeroOrMoreAllLists.addAll(entry.getValue());
            }

            for(Map.Entry<String,List<SupplierMaterialVO>> entry : oneLists.entrySet()){
                oneAllLists.addAll(entry.getValue());
            }

            zeroOrMoreAllLists.addAll(zeroSupplierIdMsgLists);
            returnMap.put("zeroOrMoreLists",zeroOrMoreAllLists);
            returnMap.put("oneLists",oneAllLists);

            return ResponseResult.succ(returnMap);
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    /***
     * @param principal
     * @return
     * @throws Exception
     */

    @PostMapping("/groupMaterialView")
    public ResponseResult groupMaterialView(Principal principal,String startDate,String endDate)throws Exception {

        if(startDate==null || startDate.isEmpty() || startDate.equals("null")){
            startDate = "2021-01-01";
        }
        if(endDate ==null || endDate.isEmpty() || startDate.equals("null")){
            endDate = "2100-01-01";
        }


        List<Map<String,Object>> lists = orderProductOrderService.listByCalMaterial(startDate,endDate);

        if(lists.isEmpty()){
            return ResponseResult.fail("该时间段订单数据为空");
        }
        Map<String,ProduceOrderMaterialProgress> materialIdLists = new TreeMap<>();
        TreeMap<Double, ProduceOrderMaterialProgress> returnMap = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if(o1>o2){
                    return 1;
                }else {
                    return -1;
                }
            }
        });

        for(Map<String,Object> map : lists){
            Object order_number = map.get("order_number");
            String materialId = map.get("material_id").toString();
            if(materialId==null){
                throw new RuntimeException("物料有空，异常..");
            }
            ProduceOrderMaterialProgress pomp = new ProduceOrderMaterialProgress();
            pomp.setMaterialId(materialId);
            BaseMaterial bm = baseMaterialService.getById(materialId);
            if(bm==null){
                throw new RuntimeException("物料"+materialId+"，没有物料信息。异常..");
            }
            pomp.setMaterialName(bm.getName());
            materialIdLists.put(materialId,pomp);
            if(order_number == null ){
                pomp.setCalNum("0");
            }else{
                pomp.setCalNum(new BigDecimal(order_number.toString()).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue()+"");
            }
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startD=null;
        LocalDate endD=null;
        LocalDate beforeD = LocalDate.parse("2020-01-01", timeFormatter);

        startD = LocalDate.parse(startDate, timeFormatter);
        endD = LocalDate.parse(endDate, timeFormatter);

        // 2. 再对应时间段内的每个物料，净入库数量
        for(Map.Entry<String,ProduceOrderMaterialProgress> entry : materialIdLists.entrySet()){

            String materialId = entry.getKey();
            ProduceOrderMaterialProgress pomp = entry.getValue();

            RepositoryBuyinDocument repositoryBuyinDocument = repositoryBuyinDocumentService.getNetInFromOrderBetweenDate(startD, endD,materialId);
            if(repositoryBuyinDocument==null||repositoryBuyinDocument.getNum()==null){
                pomp.setInNum("0");
            }else{
                pomp.setInNum(new BigDecimal(repositoryBuyinDocument.getNum()).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue()+"");
            }
            if(startDate !=null && !startDate.isEmpty()){
                RepositoryBuyinDocument beforeRbi = repositoryBuyinDocumentService.getNetInFromOrderBetweenDate(beforeD, startD,materialId);
                if(beforeRbi==null||beforeRbi.getNum()==null){
                    pomp.setPreparedNum("0");
                }else{
                    pomp.setPreparedNum(new BigDecimal(beforeRbi.getNum()).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue()+"");
                }
            }else{
                pomp.setPreparedNum("0");
            }
            returnMap.put(Double.valueOf(pomp.getPreparedNum()),pomp);

        }


        return ResponseResult.succ(returnMap.values());
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('order:productOrder:import')")
    public ResponseResult upload(Principal principal, MultipartFile[] files,String startDate,String endDate) {


        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<OrderProductOrder> utils = new ExcelImportUtil<OrderProductOrder>(OrderProductOrder.class);
        List<OrderProductOrder> orderProductOrders = null;
        try (InputStream fis = file.getInputStream();){
            orderProductOrders = utils.readExcel(fis, 1, 0,18,replaceMap);

            if(orderProductOrders == null || orderProductOrders.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            HashSet<String> queriedSet = new HashSet<>();

            // 查询是否有缺产品组成的，有则返回提示
            for (OrderProductOrder obj: orderProductOrders){
                String theKey = obj.getProductNum() + "_" + obj.getProductBrand() ;

                // 假如已经查过的，不需要再查了
                if(queriedSet.contains(theKey)){
                    continue;
                }
                ProduceProductConstituent theConstituent = produceProductConstituentService.getValidByNumBrand(obj.getProductNum(), obj.getProductBrand());
                queriedSet.add(theKey);
                if(theConstituent == null){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","公司货号["+obj.getProductNum()+"],品牌["+obj.getProductBrand()+"]没有审核通过的产品组成结构");
                    errorMsgs.add(errorMsg);
                }
            }
            if(errorMsgs.size() > 0){
                return ResponseResult.succ(errorMsgs);
            }


            // 查询该组成的物料，和订单进行计算
            ArrayList<Future<Map<String,Map<String, Object>>>> futures = new ArrayList<>();

            for (OrderProductOrder order : orderProductOrders) {
                // 利用多线程，进行分配
                Future<Map<String,Map<String, Object>>> future = fixThread.submit(new Callable<Map<String,Map<String, Object>>>() {
                    @Override
                    public Map<String,Map<String, Object>> call() throws Exception {
                        long start = System.currentTimeMillis();
                        Map<String,Map<String, Object>> result2 = new HashMap<String,Map<String, Object>>();

                        List<OrderProductOrder> details = produceProductConstituentDetailService.listByNumBrand(order.getProductNum(),order.getProductBrand());
                        for (OrderProductOrder orderOneMaterial : details){

                            Map<String, Object> theMaterialIdMaps = result2.get(orderOneMaterial.getMaterialId());
                            if(theMaterialIdMaps ==null){
                                theMaterialIdMaps = new HashMap<String,Object>();
                                result2.put(orderOneMaterial.getMaterialId(),theMaterialIdMaps);
                            }

                            Object materialName = theMaterialIdMaps.get("materialName");
                            if(materialName == null ){
                                theMaterialIdMaps.put("materialName",orderOneMaterial.getMaterialName());
                            }

                            Object materialId = theMaterialIdMaps.get("materialId");
                            if(materialId == null ){
                                theMaterialIdMaps.put("materialId",orderOneMaterial.getMaterialId());
                            }

                            Object materialUnit = theMaterialIdMaps.get("materialUnit");
                            if(materialUnit == null){
                                theMaterialIdMaps.put("materialUnit",orderOneMaterial.getMaterialUnit());
                            }

                            Object calNums = theMaterialIdMaps.get("calNums");
                            BigDecimal oneOrderOneMaterialNeedNum = BigDecimalUtil.mul(orderOneMaterial.getDosage(), order.getOrderNumber()+"");
                            if(calNums == null ){
                                theMaterialIdMaps.put("calNums",oneOrderOneMaterialNeedNum.doubleValue());
                            }else{
                                theMaterialIdMaps.put("calNums",BigDecimalUtil.add(calNums+"", oneOrderOneMaterialNeedNum.toString()).doubleValue());
                            }

                            Object theMaterialIdLists = theMaterialIdMaps.get("details");

                            if(theMaterialIdLists ==null){
                                theMaterialIdLists = new LinkedList<>();
                                theMaterialIdMaps.put("details",theMaterialIdLists);
                            }

                            HashMap<String, Object> oneRow = new HashMap<>();
                            oneRow.put("orderNum",order.getOrderNum());
                            oneRow.put("productNum",orderOneMaterial.getProductNum());
                            oneRow.put("productBrand",orderOneMaterial.getProductBrand());
                            oneRow.put("productColor",orderOneMaterial.getProductColor());
                            oneRow.put("orderNumber",order.getOrderNumber());
                            oneRow.put("dosage",orderOneMaterial.getDosage());
                            oneRow.put("calNum",oneOrderOneMaterialNeedNum.doubleValue());
                            ((List)theMaterialIdLists).add(oneRow);
                        }
                        long end = System.currentTimeMillis();
                        log.info("【订单批量计算】,线程:{},耗时:{}",Thread.currentThread().getName(),(end-start)+"ms");
                        return result2;
                    }
                });
                futures.add(future);

            }
            long start = System.currentTimeMillis();
            Map<String,Map<String, Object>> result = new HashMap<String,Map<String, Object>>();

            for (Future<Map<String,Map<String, Object>>> future : futures){
                Map<String, Map<String, Object>> theMap = future.get();
                for (Map.Entry<String,Map<String,Object>> entry : theMap.entrySet()){
                    String materialId = entry.getKey();
                    Map<String, Object> oneMaterialMsg = entry.getValue();

                    Map<String, Object> oldMap = result.get(materialId);
                    if(oldMap ==null){
                        result.put(materialId,oneMaterialMsg);
                    }else{
                        Object currentNum = oneMaterialMsg.get("calNums");
                        Object oldTotal = oldMap.get("calNums");
                        oldMap.put("calNums",BigDecimalUtil.add(oldTotal.toString(), currentNum.toString()).doubleValue());

                        LinkedList<HashMap<String, Object>> currentDetails = (LinkedList<HashMap<String, Object>>)oneMaterialMsg.get("details");
                        LinkedList<HashMap<String, Object>> oldDetails = (LinkedList<HashMap<String, Object>>)oldMap.get("details");
                        oldDetails.addAll(currentDetails);
                    }
                }
            }
            long end = System.currentTimeMillis();
            log.info("【等待线程get，for循环耗时】,耗时:{}",(end-start)+"ms");

            ArrayList<Map<String, Object>> result3 = new ArrayList<>();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startD=null;
            LocalDate endD=null;
            if(startDate!=null && endDate!=null && !startDate.equals("null") && !endDate.equals("null") && !startDate.isEmpty() && !endDate.isEmpty()){
                startD = LocalDate.parse(startDate, timeFormatter);
                endD = LocalDate.parse(endDate, timeFormatter);
            }


            for (Map.Entry<String,Map<String, Object>> entry : result.entrySet()){
                Map<String, Object> oneVal = entry.getValue();
                // 根据物料ID，时间段，获取净入库数量
                if(startD!=null && endD!=null){
                    RepositoryBuyinDocument in= repositoryBuyinDocumentService.getNetInFromOrderBetweenDate(startD,endD,entry.getKey());
                    oneVal.put("netInNums",in.getNum());
                }

                result3.add(oneVal);
            }
            HashMap<String, Object> returnMap = new HashMap<>();
            returnMap.put("datas",result3);

            long end2 = System.currentTimeMillis();
            log.info("【循环结束，put返回】,耗时:{}",(end2-end)+"ms");
            return ResponseResult.succ(returnMap);

        }
        catch (Exception e) {
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('dataAnalysis:orderProgress:list')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String("产品订单导入模板".getBytes("ISO8859-1")));
        response.setHeader("filename","产品订单导入模板" );

        FileInputStream fis = new FileInputStream(new File(poiImportDemoPath));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("下载成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:complementPrepare:del')")
    public ResponseResult del(@RequestBody Long[] ids) throws Exception{
        try {
            List<ProduceOrderMaterialProgress> dels = produceOrderMaterialProgressService.listByIds(Arrays.asList(ids));
            for (ProduceOrderMaterialProgress progress : dels){
                if(Double.valueOf(progress.getInNum()) > 0.0D){
                    return ResponseResult.fail("已存在入库，不能删除");
                }
            }

            boolean flag = produceOrderMaterialProgressService.removeByIds(Arrays.asList(ids));

            log.info("删除补数备料进度表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品订单删除失败");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("complementValid")
    @PreAuthorize("hasAuthority('order:complementPrepare:valid')")
    public ResponseResult complementReValid(Long id) throws Exception{
        try {
            ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getById(id);
            if(!old.getComplementStatus().equals(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_1)){
                return ResponseResult.fail("补数备料状态不对，已修改，请刷新!");
            }

            produceOrderMaterialProgressService.updateStatus(id,DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_0);
            return ResponseResult.succ("补数备料审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("complementReValid")
    @PreAuthorize("hasAuthority('order:complementPrepare:valid')")
    public ResponseResult complementValid(Long id) throws Exception{
        try {
            ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getById(id);
            if(!old.getComplementStatus().equals(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("补数备料状态不对，已修改，请刷新!");
            }

            produceOrderMaterialProgressService.updateStatus(id,DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_1);
            return ResponseResult.succ("补数备料反审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @GetMapping("/queryByComplementId")
    @PreAuthorize("hasAuthority('order:complementPrepare:list')")
    public ResponseResult queryByComplementId(Long id ) {
        ProduceOrderMaterialProgress progress = produceOrderMaterialProgressService.getById(id);
        BaseMaterial bm = baseMaterialService.getById(progress.getMaterialId());
        progress.setMaterialName(bm.getName());
        return ResponseResult.succ(progress);
    }

    /**
     * 查看进度表信息
     */
    @PostMapping("/complementList")
    @PreAuthorize("hasAuthority('order:complementPrepare:list')")
    public ResponseResult list( String searchField, @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceOrderMaterialProgress> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("materialName")) {
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
                    if (oneField.equals("materialName")) {
                        theQueryField = "material_name";
                    }else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);


        pageData = produceOrderMaterialProgressService.complementInnerQueryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);

        return ResponseResult.succ(pageData);
    }

    /***
     *  补数新增，修改
     * @param principal
     * @param materialProgresses
     * @return
     */
    @PostMapping("/complementSave")
    @PreAuthorize("hasAuthority('order:complementPrepare:save')")
    public ResponseResult complementSave(Principal principal, @Validated @RequestBody ProduceOrderMaterialProgress materialProgresses) {
        LocalDateTime now = LocalDateTime.now();
        try {

            if (materialProgresses.getId() == null) {
                materialProgresses.setCreated(now);
                materialProgresses.setUpdated(now);
                materialProgresses.setCreatedUser(principal.getName());
                materialProgresses.setUpdatedUser(principal.getName());
                materialProgresses.setComplementStatus(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMPLEMENT_STATUS_FIELDVALUE_1);
                materialProgresses.setPreparedNum(materialProgresses.getAddNum());
                if(Double.valueOf(Double.valueOf(materialProgresses.getAddNum())) < 0.0D){
                    throw new RuntimeException("备料数目不能为负数");
                }
                produceOrderMaterialProgressService.save(materialProgresses);
            } else {
                ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getById(materialProgresses.getId());
                BigDecimal preparedNum = new BigDecimal(old.getPreparedNum()==null?"0":old.getPreparedNum()).add(new BigDecimal(materialProgresses.getAddNum()));

                materialProgresses.setPreparedNum(preparedNum.toString());
                if(Double.valueOf(preparedNum.doubleValue()) < 0.0D){
                    throw new RuntimeException("备料数目不能为负数");
                }
                // 假如比已入库数量少，也不行
                if(Double.valueOf(preparedNum.doubleValue()) < Double.valueOf(old.getInNum())){
                    throw new RuntimeException("备料数目不能小于已入库数目");
                }
                UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, materialProgresses.getPreparedNum())
                        .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                        .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                        .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMMENT_FIELDNAME,materialProgresses.getComment())
                        .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ID_FIELDNAME, old.getId());
                produceOrderMaterialProgressService.update(updateW);
            }
            return ResponseResult.succ("备料成功");
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


    /**
     * 查看进度表信息
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('dataAnalysis:orderProgress:list')")
    public ResponseResult list( String searchField,String searchStartDate, String searchEndDate, String searchStatus,
                                String searchStatus2,String searchNoPropread,String searchNoAllIn,
                                @RequestBody Map<String,Object> params) {
        HashMap<String, Object> returnMap = new HashMap<>();
        // 是否需要查询补数进度表数据
        String  complementMaterialName = "";
        String  complementMaterialId = "";

        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceOrderMaterialProgress> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("productNum")) {
                queryField = "product_num";
            }
            else if (searchField.equals("productBrand")) {
                queryField = "product_brand";

            }
            else if (searchField.equals("materialId")) {
                queryField = "material_id";
                if(!searchStr.equals("")){
                    complementMaterialId = searchStr;
                }
            }
            else if (searchField.equals("materialName")) {
                queryField = "material_name";
                if(!searchStr.equals("")){
                    complementMaterialName = searchStr;
                }

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
                    else if (oneField.equals("materialId")) {
                        theQueryField = "material_id";
                        if(!oneStr.equals("")){
                            complementMaterialId = oneStr;
                        }
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";
                        if(!oneStr.equals("")){
                            complementMaterialName = oneStr;
                        }

                    }else {
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
        List<Long> searchStatusList2 = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus2)){
            String[] split = searchStatus2.split(",");
            for (String statusVal : split){
                searchStatusList2.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList2.size() == 0){
            return ResponseResult.fail("备料状态不能为空");
        }

        pageData = produceOrderMaterialProgressService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,searchStatusList2,queryMap,searchNoPropread,searchStartDate,searchEndDate,searchNoAllIn);

        // 遍历查询是否已有投产
        List<ProduceOrderMaterialProgress> records = pageData.getRecords();
        if(records==null || records.size() == 0){
            records = new ArrayList<ProduceOrderMaterialProgress>();
            pageData.setRecords(records) ;
        }

        HashMap<String,Boolean> queryOrderNum = new HashMap<String,Boolean>();

        for(ProduceOrderMaterialProgress progress : records){
            String orderNum = progress.getOrderNum();
            // 去重查询
            if(queryOrderNum.containsKey(orderNum)){
                progress.setIsHasProduceBatch(queryOrderNum.get(orderNum));
                continue;
            }

            List<ProduceBatch> batches = produceBatchService.listByOrderNum(orderNum);
            queryOrderNum.put(orderNum,(batches!=null && batches.size() != 0)  );
            progress.setIsHasProduceBatch((batches!=null && batches.size() != 0));
        }

        // 搜索字段是物料名称的，查询补数数量是该物料的查询出来，显示物料ID，物料名称，已报、已入库信息
        if (!complementMaterialName.equals("") || !complementMaterialId.equals("")) {
            String startDate = "1900-01-01";
            String endDate = "2100-01-01";
            if(searchStartDate!=null && !searchStartDate.isEmpty()){
                startDate = searchStartDate;
            }
            if(searchEndDate!=null && !searchEndDate.isEmpty()){
                endDate = searchStartDate;
            }
            List<BaseMaterial> bms = new ArrayList<>();
            if(!complementMaterialName.equals("")){
                 bms = baseMaterialService.list(new QueryWrapper<BaseMaterial>()
                        .like(DBConstant.TABLE_BASE_MATERIAL.NAME_FIELDNAME, complementMaterialName));
            }
            else{
                bms = baseMaterialService.list(new QueryWrapper<BaseMaterial>()
                        .like(DBConstant.TABLE_BASE_MATERIAL.ID, complementMaterialId));
            }

            if(bms.size() > 0){
                for(BaseMaterial bm : bms){
                    ProduceOrderMaterialProgress progress = produceOrderMaterialProgressService.groupByMaterialIdAndBetweenDateAndOrderIdIsNull(bm.getId(),startDate,endDate);
                    if(progress!=null){
                        progress.setMaterialName(bm.getName());
                        records.add(progress);
                    }

                }

            }

        }

        // 1. 根据返回结果，根据物料编码，分组求和信息（应报数量、已报备数量、入库数量）
        Map<String, Map<String, String>> groupMap = new TreeMap<>();
        String needTotalNumKey = "needTotalNum";
        String preparedTotalNumKey = "preparedTotalNum";
        String inTotalNumKey = "inTotalNum";
        String materialIdKey = "materialId";
        String materialNameKey = "materialName";

        for(ProduceOrderMaterialProgress pomp : records){
            String materialId = pomp.getMaterialId();
            Map<String, String> totalMap = groupMap.get(materialId);
            if(totalMap==null){
                totalMap = new HashMap<String,String>();
                totalMap.put(needTotalNumKey,"0");
                totalMap.put(preparedTotalNumKey,"0");
                totalMap.put(inTotalNumKey,"0");

                groupMap.put(materialId,totalMap);
            }
            if(pomp.getCalNum()!=null){
                totalMap.put(needTotalNumKey,BigDecimalUtil.add(totalMap.get(needTotalNumKey),pomp.getCalNum()).toString());
            }
            if(pomp.getPreparedNum()!=null){
                totalMap.put(preparedTotalNumKey,BigDecimalUtil.add(totalMap.get(preparedTotalNumKey),pomp.getPreparedNum()).toString());
            }if(pomp.getInNum()!=null){
                totalMap.put(inTotalNumKey,BigDecimalUtil.add(totalMap.get(inTotalNumKey),pomp.getInNum()).toString());
            }
            totalMap.put(materialIdKey,pomp.getMaterialId());
            totalMap.put(materialNameKey,pomp.getMaterialName());
        }
        returnMap.put("materialGroupMap",groupMap.values());

        returnMap.put("pageData",pageData);

        return ResponseResult.succ(returnMap);
    }

    /**
     * 新增,修改备料,并且确认
     */
    @PostMapping("/saveBatchAndSure")
    @Transactional
    public ResponseResult saveBatchAndSure(Principal principal,Long[] orderIds, @Validated @RequestBody List<ProduceOrderMaterialProgress> materialProgresses) {
        LocalDateTime now = LocalDateTime.now();
        try {
            // 订单排序
            for (ProduceOrderMaterialProgress materialAndAddNumMsg : materialProgresses) {
                String currentMaterialId = materialAndAddNumMsg.getMaterialId();

                List<Map<String, Object>> details = materialAndAddNumMsg.getDetails();
                String addNum = materialAndAddNumMsg.getAddNums();
                if(addNum == null || StringUtils.isBlank(addNum)){
                    continue;
                }
                Double addNumDouble = Double.valueOf(addNum);
                if(addNumDouble < 0.0D){
                    throw new RuntimeException("备料数目不能为负数");
                }

                // 该物料，有这几个订单关联
                for (int i = 0; i < details.size(); i++) {
                    Map<String, Object> theOrder = details.get(i);
                    Long orderId = ((Number) theOrder.get("orderId")).longValue();
                    Double calNum = Double.valueOf( theOrder.get("calNum").toString());// 应备数量
                    Double preparedNum = Double.valueOf(theOrder.get("preparedNum").toString()) ; // 已备数量

                    ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getByOrderIdAndMaterialId(orderId, currentMaterialId);
                    if (old == null) {

                        ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                        progress.setOrderId(orderId);
                        progress.setMaterialId(currentMaterialId);
                        progress.setCreated(now);
                        progress.setUpdated(now);
                        progress.setCreatedUser(principal.getName());
                        progress.setUpdatedUser(principal.getName());
                        progress.setCalNum(calNum+""); // 设置传进来的计算数量

                        // 假如备料比一个物料计算的多，则补到该物料所需，剩下的循环继续加
                        if(addNumDouble > calNum){
                            // 假如是最后一个的话，全部数量加到该订单物料上
                            if(i == details.size() -1){
                                progress.setPreparedNum(addNumDouble+"");
                                addNumDouble = 0.0d;
                            }else{
                                progress.setPreparedNum(calNum+"");
                                addNumDouble = BigDecimalUtil.sub(addNumDouble+"",calNum+"").doubleValue();
                            }

                        }else{
                            progress.setPreparedNum(addNumDouble+"");
                            addNumDouble=0.0d;
                        }

                        double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();
                        progress.setProgressPercent((int)thePercent);
                        produceOrderMaterialProgressService.save(progress);
                    } else {
                        // 假如老的存在，则查询需要补多少，补了之后循环后续

                        ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                        progress.setUpdated(now);
                        progress.setUpdatedUser(principal.getName());
                        progress.setOrderId(orderId);
                        progress.setMaterialId(currentMaterialId);
                        progress.setCalNum(calNum+""); // 设置传进来的计算数量

                        double needNum = BigDecimalUtil.sub(calNum, preparedNum).doubleValue();// 仍需 补的数量

                        if(addNumDouble > needNum){
                            // 假如是最后一个的话，全部数量加到该订单物料上
                            if(i == details.size() -1){
                                progress.setPreparedNum(BigDecimalUtil.add(old.getPreparedNum(),addNumDouble+"").toString()); // 补满所需数量
                                addNumDouble = 0.0d;
                            }else{
                                progress.setPreparedNum(calNum+""); // 补满所需数量
                                addNumDouble = BigDecimalUtil.sub(addNumDouble,needNum).doubleValue();
                            }

                        }else{
                            progress.setPreparedNum(BigDecimalUtil.add(preparedNum+"",addNumDouble+"").toString());
                            addNumDouble=0.0d;
                        }

                        double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();

                        progress.setProgressPercent((int)thePercent);

                        UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                        updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, progress.getPreparedNum())
                                .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PROGRESS_PERCENT_NUM_FIELDNAME,progress.getProgressPercent())
                                .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                                .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                                .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.CAL_NUM_FIELDNAME, calNum)
                                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME, orderId)
                                .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME, old.getMaterialId());

                        produceOrderMaterialProgressService.update(updateW);
                    }

                }

            }

            // 备料确认

            ArrayList<OrderProductOrder> lists = new ArrayList<>();

            for (Long id : orderIds){
                OrderProductOrder orderProductOrder = new OrderProductOrder();
                orderProductOrder.setId(id);
                orderProductOrder.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2);
                lists.add(orderProductOrder);
            }

            orderProductOrderService.updateBatchById(lists);
            return ResponseResult.succ("备料成功");
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * 新增,修改备料
     */
    @PostMapping("/saveBatch")
    @Transactional
    public ResponseResult saveBatch(Principal principal,Long[] orderIds, @Validated @RequestBody List<ProduceOrderMaterialProgress> materialProgresses) {
        LocalDateTime now = LocalDateTime.now();
        try {
            // 订单排序
                for (ProduceOrderMaterialProgress materialAndAddNumMsg : materialProgresses) {
                    String currentMaterialId = materialAndAddNumMsg.getMaterialId();

                    List<Map<String, Object>> details = materialAndAddNumMsg.getDetails();
                    String addNum = materialAndAddNumMsg.getAddNums();
                    if(addNum == null || StringUtils.isBlank(addNum)){
                        continue;
                    }
                    Double addNumDouble = Double.valueOf(addNum);
                    if(addNumDouble < 0.0D){
                        throw new RuntimeException("备料数目不能为负数");
                    }

                    // 该物料，有这几个订单关联
                    for (int i = 0; i < details.size(); i++) {
                        Map<String, Object> theOrder = details.get(i);
                        Long orderId = ((Number) theOrder.get("orderId")).longValue();
                        Double calNum = Double.valueOf( theOrder.get("calNum").toString());// 应备数量
                        Double preparedNum = Double.valueOf(theOrder.get("preparedNum").toString()) ; // 已备数量

                        ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getByOrderIdAndMaterialId(orderId, currentMaterialId);
                        if (old == null) {

                            ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                            progress.setOrderId(orderId);
                            progress.setMaterialId(currentMaterialId);
                            progress.setCreated(now);
                            progress.setUpdated(now);
                            progress.setCreatedUser(principal.getName());
                            progress.setUpdatedUser(principal.getName());
                            progress.setCalNum(calNum+""); // 设置传进来的计算数量

                            // 假如备料比一个物料计算的多，则补到该物料所需，剩下的循环继续加
                            if(addNumDouble > calNum){
                                // 假如是最后一个的话，全部数量加到该订单物料上
                                if(i == details.size() -1){
                                    progress.setPreparedNum(addNumDouble+"");
                                    addNumDouble = 0.0d;
                                }else{
                                    progress.setPreparedNum(calNum+"");
                                    addNumDouble = BigDecimalUtil.sub(addNumDouble+"",calNum+"").doubleValue();
                                }

                            }else{
                                progress.setPreparedNum(addNumDouble+"");
                                addNumDouble=0.0d;
                            }

                            double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();
                            progress.setProgressPercent((int)thePercent);
                            produceOrderMaterialProgressService.save(progress);
                        } else {
                            // 假如老的存在，则查询需要补多少，补了之后循环后续

                            ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                            progress.setUpdated(now);
                            progress.setUpdatedUser(principal.getName());
                            progress.setOrderId(orderId);
                            progress.setMaterialId(currentMaterialId);
                            progress.setCalNum(calNum+""); // 设置传进来的计算数量

                            double needNum = BigDecimalUtil.sub(calNum, preparedNum).doubleValue();// 仍需 补的数量

                            if(addNumDouble > needNum){
                                // 假如是最后一个的话，全部数量加到该订单物料上
                                if(i == details.size() -1){
                                    progress.setPreparedNum(BigDecimalUtil.add(old.getPreparedNum(),addNumDouble+"").toString()); // 补满所需数量
                                    addNumDouble = 0.0d;
                                }else{
                                    progress.setPreparedNum(calNum+""); // 补满所需数量
                                    addNumDouble = BigDecimalUtil.sub(addNumDouble,needNum).doubleValue();
                                }

                            }else{
                                progress.setPreparedNum(BigDecimalUtil.add(preparedNum+"",addNumDouble+"").toString());
                                addNumDouble=0.0d;
                            }

                            double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();

                            progress.setProgressPercent((int)thePercent);

                            UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                            updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, progress.getPreparedNum())
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PROGRESS_PERCENT_NUM_FIELDNAME,progress.getProgressPercent())
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                                    .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.CAL_NUM_FIELDNAME, calNum)
                                    .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME, orderId)
                                    .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME, old.getMaterialId());

                            produceOrderMaterialProgressService.update(updateW);
                        }

                    }

            }

            return ResponseResult.succ("备料成功");
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * 新增入库
     */
    @PostMapping("/save")
    @Transactional
    public ResponseResult save(Principal principal,Long orderId, @Validated @RequestBody List<ProduceOrderMaterialProgress> materialProgresses) {
        LocalDateTime now = LocalDateTime.now();
        try {

            StringBuilder sb = new StringBuilder();


            for (ProduceOrderMaterialProgress process : materialProgresses) {
//                if((process.getAddNum() == null || Double.valueOf(process.getAddNum()) == 0 ) &&( process.getComment()==null || process.getComment().isEmpty())){
//                    continue;
//                }
                process.setOrderId(orderId);
                ProduceOrderMaterialProgress old = produceOrderMaterialProgressService.getByOrderIdAndMaterialId(orderId, process.getMaterialId());

                if (old == null) {
                    if(Double.valueOf(process.getAddNum()) < 0.0D){
                        throw new RuntimeException("备料数目不能为负数");
                    }
                    process.setCreated(now);
                    process.setUpdated(now);
                    process.setCreatedUser(principal.getName());
                    process.setUpdatedUser(principal.getName());
                    process.setPreparedNum(process.getAddNum());

                    double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(process.getPreparedNum()), 100).doubleValue(),Double.valueOf(process.getCalNum())).doubleValue();
                    process.setProgressPercent((int)thePercent);
                    produceOrderMaterialProgressService.save(process);
                } else {
                    // 假如已经进度表已经有入库数量，不能修改
                    if(old.getInNum()!=null && !old.getInNum().isEmpty()&&Double.valueOf(old.getInNum())>0.0D&&!process.getAddNum().isEmpty()&&Double.valueOf(process.getAddNum()).doubleValue() < 0D){
                        sb.append(old.getMaterialId()+"已存在入库消单，不能减少备料数量,");
                        continue;
                    }

                    process.setUpdated(now);
                    process.setUpdatedUser(principal.getName());



                    BigDecimal preparedNum = new BigDecimal(old.getPreparedNum()).add(new BigDecimal(process.getAddNum()));
                    if(Double.valueOf(preparedNum.doubleValue()) < 0.0D){
                        throw new RuntimeException("备料数目不能为负数");
                    }

                    process.setPreparedNum(preparedNum.toString());
                    double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(process.getPreparedNum()), 100).doubleValue(),Double.valueOf(process.getCalNum())).doubleValue();

                    process.setProgressPercent((int)thePercent);

                    UpdateWrapper<ProduceOrderMaterialProgress> updateW = new UpdateWrapper<>();
                    updateW.set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PREPARED_NUM_FIELDNAME, process.getPreparedNum())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.PROGRESS_PERCENT_NUM_FIELDNAME,process.getProgressPercent())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_USER_FIELDNAME, principal.getName())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.UPDATED_FIELDNAME, now)
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.COMMENT_FIELDNAME,process.getComment())
                            .set(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.CAL_NUM_FIELDNAME,process.getCalNum())
                            .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.ORDER_ID_FIELDNAME, orderId)
                            .eq(DBConstant.TABLE_PRODUCE_ORDER_MATERIAL_PROGRESS.MATERIAL_ID_FIELDNAME, old.getMaterialId());

                    produceOrderMaterialProgressService.update(updateW);
                }
            }
            return ResponseResult.succ("备料成功"+sb.toString());
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    @GetMapping("/getByOrderId")
    public ResponseResult getByOrderId(Principal principal, Long orderId)throws Exception {

        List<ProduceOrderMaterialProgress> lists = produceOrderMaterialProgressService.listByOrderId(orderId);

        // 根据物料编码返回
        Map<String, String> theMaterialIdAndPreparedNum = new HashMap<>();
        for (ProduceOrderMaterialProgress obj : lists){
            theMaterialIdAndPreparedNum.put(obj.getMaterialId(),obj.getPreparedNum());
        }
        return ResponseResult.succ(theMaterialIdAndPreparedNum);
    }


}
