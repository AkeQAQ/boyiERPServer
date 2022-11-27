package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelExportUtil;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 订单模块-采购订单单据表 前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Slf4j
@RestController
@RequestMapping("/order/buyOrder")
public class OrderBuyorderDocumentController extends BaseController {

    @Value("${poi.orderBuyOrderDemoPath}")
    private String poiDemoPath;

    @Value("${poi.orderBuyOrderImportDemoPath}")
    private String poiImportDemoPath;

    public static final Map<Long,String> locks = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        DateTimeFormatter dfm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String test = "2022-08-07 12:00:00";
        LocalDate parse = LocalDate.parse(test, dfm);
        System.out.println(parse);

    }

    @GetMapping("/getNoPriceForeignMaterialLists")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult getNoPriceForeignMaterialLists(){
        Map<String, List<NoPricePrintVO>> returnMap = new HashMap<>();
        List<NoPricePrintVO> lists = new ArrayList<>();
        HashMap<String, List<OrderBuyorderDocumentDetail>> supplier_materials = new HashMap<>();

        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listNoPriceForeignMaterials();

        //进行分组

        for (OrderBuyorderDocumentDetail detail : details){
            String supplierName = detail.getSupplierName();
            List<OrderBuyorderDocumentDetail> baseMaterials = supplier_materials.get(supplierName);
            if(baseMaterials == null || baseMaterials.size() == 0){
                baseMaterials = new ArrayList<>();
                supplier_materials.put(supplierName,baseMaterials);
            }
            baseMaterials.add(detail);
        }
        for(Map.Entry<String,List<OrderBuyorderDocumentDetail>> entry : supplier_materials.entrySet()){
            String key = entry.getKey();
            List<OrderBuyorderDocumentDetail> value = entry.getValue();
            NoPricePrintVO vo = new NoPricePrintVO();
            vo.setSupplierName(key);
            vo.setRowList(value);
            lists.add(vo);
        }
        returnMap.put("content",lists);
        return ResponseResult.succ(returnMap);
    }


    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('order:buyOrder:import')")
    @Transactional
    public ResponseResult upload(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];
        String userName = principal.getName();

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<OrderBuyOrderImportVO> utils = new ExcelImportUtil<OrderBuyOrderImportVO>(OrderBuyOrderImportVO.class);
        List<OrderBuyOrderImportVO> orders = null;
        try (InputStream fis = file.getInputStream();){
            orders = utils.readExcel(fis, 1, 0,20,null);
            log.info("解析的excel数据:{}",orders);


            if(orders == null || orders.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            List<Map<String,String>> errorMsgs = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            // 一个供应商编号代表一个单据
            Map<String, OrderBuyorderDocument> supplierId_vo = new HashMap<>();
            // 一个供应商编号下面的所有记录列
            Map<String, List<OrderBuyorderDocumentDetail>> supplierId_details = new HashMap<>();

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dfm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Set<String> materialId_docNum = new HashSet<>();

            for (int i = 0; i < orders.size(); i++) {

                OrderBuyOrderImportVO row = orders.get(i);

                if((row.getMaterialId()==null || row.getMaterialId().isEmpty()) &&
                        (row.getDocNum()==null || row.getDocNum().isEmpty() )&&
                        (row.getSupplierId()==null || row.getSupplierId().isEmpty())&&
                        (row.getBuyNum()==null || row.getBuyNum().isEmpty())&&
                        (row.getBuyDate()==null || row.getBuyDate().isEmpty()) ){
                    continue;
                }

                // 上传文件一个物料不能多个单号
                if(materialId_docNum.contains(row.getMaterialId()+"_" +row.getDocNum())){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","第："+(i+1)+" 行 记录 物料:"+row.getMaterialId()+" , 单号:"+row.getDocNum()+" 重复！");
                    errorMsgs.add(errorMsg);
                    continue;
                }
                materialId_docNum.add(row.getMaterialId()+"_" +row.getDocNum());


                // 空值校验
                if(StringUtils.isBlank(row.getSupplierId()) || StringUtils.isBlank(row.getBuyDate())
                || StringUtils.isBlank(row.getDocNum()) || StringUtils.isBlank(row.getMaterialId())
                || StringUtils.isBlank(row.getBuyNum()) ){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","第："+(i+1)+" 行 记录有空值内容！");
                    errorMsgs.add(errorMsg);
                    continue;
                }

                String supplierId = row.getSupplierId();
                String materialId = row.getMaterialId();
                BaseSupplier isExistSupplierId = baseSupplierService.getById(supplierId);
                // 校验供应商编码
                if(isExistSupplierId == null){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content",""+supplierId+" ，供应商编码不存在！");
                    errorMsgs.add(errorMsg);
                    continue;
                }
                BaseMaterial isExistMaterialId = baseMaterialService.getById(materialId);
                // 校验物料编码
                if(isExistMaterialId == null){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content",""+materialId+" ，物料编码不存在！");
                    errorMsgs.add(errorMsg);
                    continue;
                }

                String docNum = row.getDocNum();
                // 校验一个物料下，300日内，是否已经存在该订单号,
                List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.getByMaterialIdAndOrderSeq(materialId,docNum);
                if(details != null && details.size() > 0){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","物料："+materialId+",单号:"+docNum+" 已经存在");
                    errorMsgs.add(errorMsg);
                    continue;
                }

                // 设置该供应商主单据对象
                OrderBuyorderDocument oneRowDBObj = supplierId_vo.get(supplierId);
                if(oneRowDBObj == null){
                    oneRowDBObj = new OrderBuyorderDocument();
                    oneRowDBObj.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1);
                    oneRowDBObj.setSupplierId(supplierId);
                    oneRowDBObj.setCreated(now);
                    oneRowDBObj.setUpdated(now);
                    oneRowDBObj.setCreatedUser(userName);
                    oneRowDBObj.setUpdatedUser(userName);

                    LocalDate parse = LocalDate.parse(row.getBuyDate(), dfm);
                    oneRowDBObj.setOrderDate(parse);
                    supplierId_vo.put(supplierId,oneRowDBObj);
                }

                // 设置该供应商下面的记录集合
                List<OrderBuyorderDocumentDetail> oneRowDetailObj = supplierId_details.get(supplierId);
                if(oneRowDetailObj == null){
                    oneRowDetailObj = new ArrayList<OrderBuyorderDocumentDetail>();
                    supplierId_details.put(supplierId,oneRowDetailObj);
                }
                OrderBuyorderDocumentDetail detail = new OrderBuyorderDocumentDetail();
                detail.setMaterialId(row.getMaterialId());
                detail.setNum(Double.valueOf(row.getBuyNum()));
                detail.setOrderSeq(row.getDocNum());
                detail.setOrderDate(oneRowDBObj.getOrderDate());
                detail.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1);
                detail.setRadioNum(BigDecimalUtil.mul(detail.getNum(),isExistMaterialId.getUnitRadio()).doubleValue());
                detail.setSupplierId(supplierId);

                oneRowDetailObj.add(detail);
            }
            if(errorMsgs.size() > 0){
                return ResponseResult.succ(errorMsgs);
            }

            // 先批次插入主单据对象，生成ID
            orderBuyorderDocumentService.saveBatch(supplierId_vo.values());

            // 赋值外键
            for (Map.Entry<String,List<OrderBuyorderDocumentDetail>> entry : supplierId_details.entrySet()){
                String supplierId = entry.getKey();
                List<OrderBuyorderDocumentDetail> details = entry.getValue();

                OrderBuyorderDocument dbRow = supplierId_vo.get(supplierId);
                for(OrderBuyorderDocumentDetail detail : details){
                    detail.setDocumentId(dbRow.getId());
                }
            }

            // 循环插入，一个供应商对应下面的记录
            for (Map.Entry<String,List<OrderBuyorderDocumentDetail>> entry : supplierId_details.entrySet()){
                orderBuyorderDocumentDetailService.saveBatch(entry.getValue());
            }
        }
        catch (Exception e) {
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }

        return ResponseResult.succ("上传成功");
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('order:buyOrder:export')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String("采购订单导入模板".getBytes("ISO8859-1")));
        response.setHeader("filename","采购订单导入模板" );

        FileInputStream fis = new FileInputStream(new File(poiImportDemoPath));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("下载成功");
    }

    /**
     * 锁单据
     */
    @GetMapping("/lockById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult lockById(Principal principal,Long id) {
        locks.put(id,principal.getName());
        log.info("锁单据:{}",id);
        return ResponseResult.succ("");
    }
    /**
     * 解锁单据
     */
    @GetMapping("/lockOpenById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult lockOpenById(Long id) {
        locks.remove(id);
        log.info("解锁单据:{}",id);
        return ResponseResult.succ("");
    }

    @Transactional
    @PostMapping("/push")
    @PreAuthorize("hasAuthority('order:buyOrder:push')")
    public ResponseResult push(Principal principal, @RequestBody RepositoryBuyinDocument repositoryBuyinDocument, Long[] orderDetailIds, Long id) {

        boolean validIsClose = validIsClose(repositoryBuyinDocument.getBuyInDate());
        if (!validIsClose) {
            return ResponseResult.fail("日期请设置在关账日之后.");
        }

        List<OrderBuyorderDocumentDetail> details = null;
        if (id != -1) {
            String user = locks.get(id);
            if(StringUtils.isNotBlank(user)){
                return ResponseResult.fail("单据被["+user+"]占用");
            }
            details = orderBuyorderDocumentDetailService.listByDocumentId(id);
        } else {
            details = orderBuyorderDocumentDetailService.listByIds(Arrays.asList(orderDetailIds));
            for (OrderBuyorderDocumentDetail detail : details){
                String user = locks.get(detail.getDocumentId());
                if(StringUtils.isNotBlank(user)){
                    return ResponseResult.fail("单据"+detail.getDocumentId()+"被["+user+"]占用");
                }
            }
        }

        List<Long> detailIds = new ArrayList<>();
        String supplierId = details.get(0).getSupplierId();
        // 已经是下推过的，则不能执行
        for (OrderBuyorderDocumentDetail detail : details) {
            if (detail.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0) {
                return ResponseResult.fail("请选择未下推的订单进行下推！！！");
            }
            if (!detail.getSupplierId().equals(supplierId)) {
                return ResponseResult.fail("请选择同供应商进行下推！！！");
            }
            detailIds.add(detail.getId());
        }
        if (id != null) {
            orderDetailIds = detailIds.toArray(new Long[detailIds.size()]);
        }

        repositoryBuyinDocument.setSupplierId(supplierId);
        // 1. 先查询该供应商，该单号是否已经有记录，有则不能插入
        int exitCount = repositoryBuyinDocumentService.countSupplierOneDocNum(
                repositoryBuyinDocument.getSupplierDocumentNum(),
                repositoryBuyinDocument.getSupplierId());
        if (exitCount > 0) {
            return ResponseResult.fail("该供应商的单据已经存在！请确认信息!");
        }

        // 假如没有价格也不能下推
        for (OrderBuyorderDocumentDetail item : details) {
            BaseSupplierMaterial successPrice = baseSupplierMaterialService.getSuccessPrice(item.getSupplierId(), item.getMaterialId(), item.getOrderDate());
            if(successPrice == null || successPrice.getPrice()==null || successPrice.getPrice()==0D){
                return ResponseResult.fail("供应商:"+item.getSupplierId()+",物料:"+item.getMaterialId()+",没有价格，不能下推");
            }
        }

        try {
            // 2.封装入库单据表的信息
            LocalDateTime now = LocalDateTime.now();
            repositoryBuyinDocument.setCreated(now);
            repositoryBuyinDocument.setUpdated(now);
            repositoryBuyinDocument.setCreatedUser(principal.getName());
            repositoryBuyinDocument.setUpdatedUser(principal.getName());
            repositoryBuyinDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_2);
            repositoryBuyinDocument.setSourceType(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SOURCE_TYPE_FIELDVALUE_1);

            repositoryBuyinDocumentService.save(repositoryBuyinDocument);

            ArrayList<RepositoryBuyinDocumentDetail> detailArrayList = new ArrayList<>();
            for (OrderBuyorderDocumentDetail item : details) {
                // 3. 存入库表
                RepositoryBuyinDocumentDetail detail = new RepositoryBuyinDocumentDetail();
                detail.setMaterialId(item.getMaterialId());
                detail.setDocumentId(repositoryBuyinDocument.getId());
                detail.setNum(item.getNum());
                detail.setRadioNum(item.getRadioNum());

                detail.setComment(item.getComment());
                detail.setSupplierId(item.getSupplierId());
                detail.setOrderSeq(item.getOrderSeq());
                detail.setOrderId(item.getDocumentId());
                detail.setPriceDate(item.getOrderDate());
                detail.setOrderDetailId(item.getId());

                detailArrayList.add(detail);

            }

            // 4. 存储入库详情
            repositoryBuyinDocumentDetailService.saveBatch(detailArrayList);

            // 5. 修改该订单详情 状态
            orderBuyorderDocumentDetailService.statusSuccess(orderDetailIds);

            // 6. 增加库存
            for (RepositoryBuyinDocumentDetail detail : detailArrayList) {
                repositoryStockService.addNumByMaterialId(detail.getMaterialId()
                        , detail.getRadioNum());
            }
            return ResponseResult.succ("下推入库成功");
        } catch (Exception e) {
            log.error("发生error:",e);
            throw new RuntimeException(e.getMessage()); // 事务默认需要RuntimeException，但是我们要求有Exception异常也要回滚
        }
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('order:buyOrder:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {
        String user = locks.get(ids[0]);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        // 先查询，假如有状态已下推的，不能删除
        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(ids[0]);
        List<Long> removeIds = new ArrayList<>();
        for (OrderBuyorderDocumentDetail detail : details) {
            if (detail.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0) {
                return ResponseResult.fail("该采购订单存在已下推的记录。删除失败!");
            }
            removeIds.add(detail.getId());
        }

        try {
            boolean flag = orderBuyorderDocumentService.removeByIds(Arrays.asList(ids));

            log.info("删除采购订单表信息,ids:{},是否成功：{}", ids, flag ? "成功" : "失败");
            if (!flag) {
                return ResponseResult.fail("采购订单删除失败");
            }

            boolean flagDetail = orderBuyorderDocumentDetailService.removeByIds(removeIds);
            log.info("删除采购订单表详情信息,document_id:{},是否成功：{}", ids, flagDetail ? "成功" : "失败");

            if (!flagDetail) {
                return ResponseResult.fail("采购订单详情表没有删除成功!");
            }
            return ResponseResult.succ("删除成功");
        } catch (Exception e) {
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /**
     * 查询入库
     */
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult queryById(Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        OrderBuyorderDocument orderBuyorderDocument = orderBuyorderDocumentService.getById(id);

        List<OrderBuyorderDocumentDetail> details = orderBuyorderDocumentDetailService.listByDocumentId(id);

        BaseSupplier supplier = baseSupplierService.getById(orderBuyorderDocument.getSupplierId());

        Double totalNum = 0D;
        Double totalAmount = 0D;

        for (OrderBuyorderDocumentDetail detail : details) {
            BaseMaterial material = baseMaterialService.getById(detail.getMaterialId());
            detail.setMaterialName(material.getName());
            detail.setUnit(material.getUnit());
            detail.setBigUnit(material.getBigUnit());
            detail.setUnitRadio(material.getUnitRadio());
            detail.setSpecs(material.getSpecs());

            // 查询对应的价目记录
            BaseSupplierMaterial one = baseSupplierMaterialService.getSuccessPrice(supplier.getId(), material.getId(), detail.getOrderDate());

            if (one != null) {
                detail.setPrice(one.getPrice());
                double amount = BigDecimalUtil.mul(detail.getPrice(),detail.getNum()).doubleValue();
                detail.setAmount(new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                totalAmount += amount;
            }

            totalNum += detail.getNum();
        }

        orderBuyorderDocument.setTotalNum(totalNum);
        orderBuyorderDocument.setTotalAmount(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

        orderBuyorderDocument.setSupplierName(supplier.getName());

        orderBuyorderDocument.setRowList(details);
        return ResponseResult.succ(orderBuyorderDocument);
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:buyOrder:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody OrderBuyorderDocument orderBuyorderDocument) {

        if (orderBuyorderDocument.getRowList() == null || orderBuyorderDocument.getRowList().size() == 0) {
            return ResponseResult.fail("物料信息不能为空");
        }

        orderBuyorderDocument.setUpdated(LocalDateTime.now());
        orderBuyorderDocument.setUpdatedUser(principal.getName());

        try {
            //2. 先删除老的，再插入新的
            ArrayList<Long> updateIds = new ArrayList<>();// 存放未下推的详情。
            ArrayList<OrderBuyorderDocumentDetail> updateDetails = new ArrayList<>();

            int pushCount = 0;
            ArrayList<Long> nowIds = new ArrayList<>();// 删除的IDS
            for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()) {
                nowIds.add(item.getId());
                // 只有状态是 1|| null（新增的），未下推的，才能编辑。
                if (item.getStatus() == DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1) {
                    updateIds.add(item.getId());
                    updateDetails.add(item);
                } else {
                    pushCount++;
                }
            }
            ArrayList<Long> removeIds = new ArrayList<>();// 删除的IDS
            List<OrderBuyorderDocumentDetail> oldDetails = orderBuyorderDocumentDetailService.listByDocumentId(orderBuyorderDocument.getId());
            for (OrderBuyorderDocumentDetail item : oldDetails) {
                if (!nowIds.contains(item.getId())) {
                    removeIds.add(item.getId());
                }
            }

            if (updateIds.size() == 0 && removeIds.size() == 0) {
                return ResponseResult.succ("公共部分更新成功，但详情无更新。");
            }
            // 没有下推过的，更新供应商，采购日期信息。
            if (pushCount == 0) {
                orderBuyorderDocumentService.updateById(orderBuyorderDocument);
            } else {
                // 有推送过的，以之前数据库的未标准。
                orderBuyorderDocument = orderBuyorderDocumentService.getById(orderBuyorderDocument.getId());
            }

            orderBuyorderDocumentDetailService.removeByIds(updateIds);
            orderBuyorderDocumentDetailService.removeByIds(removeIds);
            for (OrderBuyorderDocumentDetail item : updateDetails) {
                item.setId(null);
                item.setDocumentId(orderBuyorderDocument.getId());
                item.setSupplierId(orderBuyorderDocument.getSupplierId());
                item.setStatus(item.getStatus());
                item.setOrderDate(orderBuyorderDocument.getOrderDate());
                item.setRadioNum(BigDecimalUtil.mul(item.getNum(),item.getUnitRadio()).doubleValue() );
            }
            orderBuyorderDocumentDetailService.saveBatch(updateDetails);
            log.info("采购订单模块-更新内容:{}", orderBuyorderDocument);

            return ResponseResult.succ("编辑成功");
        } catch (Exception e) {
            log.error("供应商，更新异常", e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 新增入库
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:buyOrder:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody OrderBuyorderDocument orderBuyorderDocument) {
        LocalDateTime now = LocalDateTime.now();
        orderBuyorderDocument.setCreated(now);
        orderBuyorderDocument.setUpdated(now);
        orderBuyorderDocument.setCreatedUser(principal.getName());
        orderBuyorderDocument.setUpdatedUser(principal.getName());
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1);
        try {
            orderBuyorderDocumentService.save(orderBuyorderDocument);

            for (OrderBuyorderDocumentDetail item : orderBuyorderDocument.getRowList()) {
                item.setDocumentId(orderBuyorderDocument.getId());
                item.setSupplierId(orderBuyorderDocument.getSupplierId());
                item.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1);
                item.setOrderDate(orderBuyorderDocument.getOrderDate());
                item.setRadioNum(BigDecimalUtil.mul(item.getNum(),item.getUnitRadio()).doubleValue()  );
            }

            orderBuyorderDocumentDetailService.saveBatch(orderBuyorderDocument.getRowList());

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",orderBuyorderDocument.getId());
        } catch (Exception e) {
            log.error("采购订单单，插入异常", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取采购订单 分页导出
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('order:buyOrder:export')")
    public void export(HttpServletResponse response,String searchDocNum,  String searchField,String searchStatus, String searchStartDate, String searchEndDate,
                       @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<OrderBuyorderDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            } else if (searchField.equals("materialName")) {
                queryField = "material_name";

            } else if (searchField.equals("id")) {
                queryField = "id";

            } else {
            }
        } else {

        }

        Map<String, String> queryMap = new HashMap<>();
        if(manySearchArr!=null && manySearchArr.size() > 0){
            for (int i = 0; i < manySearchArr.size(); i++) {
                Map<String, String> theOneSearch = manySearchArr.get(i);
                String oneField = theOneSearch.get("selectField");
                String oneStr = theOneSearch.get("searchStr");
                String theQueryField = null;
                if (StringUtils.isNotBlank(oneField)) {
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
        List<Long> searchStatusList = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus)){
            String[] split = searchStatus.split(",");
            for (String statusVal : split){
                searchStatusList.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList.size() == 0){
            throw new RuntimeException("状态不能为空");
        }

        log.info("搜索字段:{},对应ID:{}", searchField, ids);
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        pageData = orderBuyorderDocumentService.innerQueryByManySearch(page, searchField, queryField, searchStr, searchStatusList, searchStartDate, searchEndDate,queryMap,StringUtils.isBlank(searchDocNum)?null:searchDocNum.split(","));

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);) {
            new ExcelExportUtil(OrderBuyorderDocument.class, 1, 0).export(null,null,response, fis, pageData.getRecords(), "报表.xlsx", DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.statusMap);
        } catch (Exception e) {
            log.error("导出模块报错.", e);
        }
    }

    /**
     * 获取采购订单 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('order:buyOrder:list')")
    public ResponseResult list(String searchDocNum, String searchField,String searchStatus, String searchStartDate, String searchEndDate
                               ,@RequestBody Map<String,Object> params) {

        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<OrderBuyorderDocument> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("supplierName")) {
                queryField = "supplier_name";
            } else if (searchField.equals("materialName")) {
                queryField = "material_name";

            } else if (searchField.equals("id")) {
                queryField = "id";

            }else if (searchField.equals("price")) {
                queryField = "price";

            } else {
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
                    if (oneField.equals("supplierName")) {
                        theQueryField = "supplier_name";
                    }
                    else if (oneField.equals("materialName")) {
                        theQueryField = "material_name";

                    }else if (oneField.equals("id")) {
                        theQueryField = "id";
                    }else if (oneField.equals("price")) {
                        theQueryField = "price";
                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
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

        log.info("搜索字段:{},对应ID:{}", searchField, ids);
        pageData = orderBuyorderDocumentService.innerQueryByManySearch(getPage(), searchField, queryField, searchStr,searchStatusList, searchStartDate, searchEndDate,queryMap,StringUtils.isBlank(searchDocNum)?null:searchDocNum.split(","));

        Map<String,Double> allPageTotalAmountAndNum = orderBuyorderDocumentService.getAllPageTotalAmount( searchField, queryField, searchStr,searchStatusList, searchStartDate, searchEndDate,queryMap,StringUtils.isBlank(searchDocNum)?null:searchDocNum.split(","));
        Double sumAmount = allPageTotalAmountAndNum.get("sumAmount");
        Double sumNum = allPageTotalAmountAndNum.get("sumNum");

        return ResponseResult.succ(ResponseResult.SUCCESS_CODE,sumAmount+"_"+sumNum,pageData);

    }


    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('order:buyOrder:valid')")
    public ResponseResult statusPass(Principal principal, Long id) {
        String user = locks.get(id);
        if(StringUtils.isNotBlank(user)){
            return ResponseResult.fail("单据被["+user+"]占用");
        }
        OrderBuyorderDocument orderBuyorderDocument = new OrderBuyorderDocument();
        orderBuyorderDocument.setUpdated(LocalDateTime.now());
        orderBuyorderDocument.setUpdatedUser(principal.getName());
        orderBuyorderDocument.setId(id);
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_0);
        orderBuyorderDocumentService.updateById(orderBuyorderDocument);
        log.info("仓库模块-审核通过内容:{}", orderBuyorderDocument);

        return ResponseResult.succ("审核通过");
    }


    /* *//**
     * 反审核
     *//*
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('order:buyOrder:valid')")
    public ResponseResult statusReturn(Principal principal,Long id) {


        OrderBuyorderDocument OrderBuyorderDocument = new OrderBuyorderDocument();
        OrderBuyorderDocument.setUpdated(LocalDateTime.now());
        OrderBuyorderDocument.setUpdatedUser(principal.getName());
        OrderBuyorderDocument.setId(id);
        OrderBuyorderDocument.setStatus(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.STATUS_FIELDVALUE_1);
        OrderBuyorderDocumentService.updateById(OrderBuyorderDocument);
        log.info("仓库模块-反审核通过内容:{}",OrderBuyorderDocument);

        // 采购订单反审核之后，要把数量更新

        // 1. 根据单据ID 获取该单据的全部详情信息，
        List<OrderBuyorderDocumentDetail> details = OrderBuyorderDocumentDetailService.listByDocumentId(id);
        // 2. 遍历更新 一个供应商，一个物料对应的库存数量
        for (OrderBuyorderDocumentDetail detail : details){
            try {
                repositoryStockService.subNumBySupplierIdAndMaterialId(detail.getSupplierId()
                        ,detail.getMaterialId()
                        ,detail.getNum());
            } catch (Exception e) {
                log.error("数据异常",e);
            }
        }

        return ResponseResult.succ("反审核成功");
    }*/
}
