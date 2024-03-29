package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.common.utils.BigDecimalUtil;
import com.boyi.common.utils.ExcelImportUtil;
import com.boyi.common.vo.OrderProductCalVO;
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
import java.sql.BatchUpdateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-03-25
 */
@RestController
@RequestMapping("/order/productOrder")
@Slf4j
public class OrderProductOrderController extends BaseController {

    @Value("${poi.orderProductOrderImportDemoPath}")
    private String poiImportDemoPath;
    public static final Map<Object,Object> replaceMap = new HashMap<Object,Object>();
    static {
        replaceMap.put("订单",0);
        replaceMap.put("回单",1);
    }


    @Transactional
    @GetMapping("updateTbom")
    @PreAuthorize("hasAuthority('produce:technologyBOM:valid')")
    public ResponseResult updateTbom(Principal principal,Long id,Long tBomId) throws Exception{
        try {
            OrderProductOrder old = orderProductOrderService.getById(id);

            if(tBomId==-1){
                old.setTechnologyBomId(null);

                orderProductOrderService.update(new UpdateWrapper<OrderProductOrder>()
                        .set(DBConstant.TABLE_ORDER_PRODUCT_ORDER.T_BOM_ID_FIELDNAME,null)
                        .eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ID_FIELDNAME,id));

            }else{

                // 假如物料BOM 的工厂型号和品牌不一致，则不能选择
                ProduceTechnologyBom ppc = produceTechnologyBomService.getById(tBomId);
                if(!old.getProductBrand().equals(ppc.getProductBrand()) ||
                        !old.getProductNum().equals(ppc.getProductNum())){
                    return ResponseResult.fail("请选择一致的工厂货号和品牌!");
                }

                old.setTechnologyBomId(tBomId);
                orderProductOrderService.updateById(old);

            }

            return ResponseResult.succ("选择工艺BOM成功!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }



    @Transactional
    @GetMapping("updateMbom")
    @PreAuthorize("hasAuthority('produce:productConstituent:valid')")
    public ResponseResult updateMbom(Principal principal,Long id,Long mBomId) throws Exception{
        try {
            OrderProductOrder old = orderProductOrderService.getById(id);
            // 假如订单备料有信息的话，则需要备料信息清空才可以
            List<ProduceOrderMaterialProgress> pomps = produceOrderMaterialProgressService.listByOrderId(id);
            if(pomps!=null && !pomps.isEmpty()){
                // 假如备料信息有存在且>0 则不能更换
                for(ProduceOrderMaterialProgress pomp : pomps){
                    if(pomp.getPreparedNum()!=null && Double.valueOf(pomp.getPreparedNum())>0){
                        return ResponseResult.fail("老BOM存在备料>0的内容，请清空!");
                    }
                }
            }

            if(mBomId==-1){
                old.setMaterialBomId(null);

                orderProductOrderService.update(new UpdateWrapper<OrderProductOrder>()
                        .set(DBConstant.TABLE_ORDER_PRODUCT_ORDER.MATERIAL_BOM_ID_FIELDNAME,null)
                        .eq(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ID_FIELDNAME,id));

            }else{

                // 假如物料BOM 的工厂型号和品牌不一致，则不能选择
                ProduceProductConstituent ppc = produceProductConstituentService.getById(mBomId);
                if(!old.getProductBrand().equals(ppc.getProductBrand()) ||
                        !old.getProductNum().equals(ppc.getProductNum())){
                    return ResponseResult.fail("请选择一致的工厂货号和品牌!");
                }

                old.setMaterialBomId(mBomId);
                orderProductOrderService.updateById(old);

            }

            // 并且要删除老的进度表
            produceOrderMaterialProgressService.removeByOrderId(id);

            return ResponseResult.succ("选择物料BOM成功!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }



    @Transactional
    @PostMapping("buyInMove")
    @PreAuthorize("hasAuthority('order:productOrder:del')")
    public ResponseResult buyInMove(Principal principal,Long id) throws Exception{
        try {
            // 1. 查询该订单的采购采购进度表，筛选入库记录不为0的数据

            List<ProduceOrderMaterialProgress> pomp = produceOrderMaterialProgressService.listByOrderId(id);
            if(pomp==null || pomp.isEmpty()){
                return ResponseResult.fail("该订单没有进度表信息");
            }
            for(ProduceOrderMaterialProgress one:pomp){
                if(one.getInNum()==null || one.getInNum().isEmpty() || one.getInNum().equals("0") || one.getInNum().equals("0.0")){
                    continue;
                }
                String materialId = one.getMaterialId();
                // 假如进度表的该物料，只有一条数据，不能进行迁移
                if(produceOrderMaterialProgressService.countHasPreparedByMaterialIdExcludeSelf(materialId,one.getId())==0){
                    return ResponseResult.fail("该订单的物料:"+materialId+",进度表仅有当前一条有备料数量>0的信息，无法迁移,请先删除入库记录!");
                }
                // 2. 对物料进行查询，进行消单处理。该订单的进度表设置为0

                ProduceOrderMaterialProgress theLatest = produceOrderMaterialProgressService.getByTheLatestByMaterialIdCreatedDescExcludeSelf(materialId,one.getId());

                produceOrderMaterialProgressService.updateInNum(theLatest.getId(),BigDecimalUtil.add(theLatest.getInNum(),one.getInNum()).toString());

                one.setInNum("0");
                one.setUpdated(LocalDateTime.now());
                one.setUpdatedUser(principal.getName());
                produceOrderMaterialProgressService.updateById(one);
            }

            return ResponseResult.succ("订单入库消单迁移成功!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }


    @PostMapping("/mergerOrders")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    @Transactional
    public ResponseResult mergerOrders(Principal principal, @Validated @RequestBody  OrderProductOrderVO vo)throws Exception {
        LocalDateTime now = LocalDateTime.now();

        try {
            if(vo==null){
                return ResponseResult.fail("对象为null!");
            }
            String fromOrders = vo.getOrders();
            String toMergeOrder = vo.getToMergeOrder();
            //0. 非空判断
            if( StringUtils.isBlank(fromOrders) || StringUtils.isBlank(toMergeOrder)){
                return ResponseResult.fail("来源订单和目标订单内容为空!");
            }
            // 1. 来源订单，分割分组
            String[] orders = fromOrders.split(",");
            HashSet<String> ordersSet = new HashSet<>();

            for(String orderNum : orders){
                ordersSet.add(orderNum);
            }
            List<OrderProductOrder> fromOrderProdocutOrders = orderProductOrderService.listByOrderNums(ordersSet);


            if(fromOrderProdocutOrders==null || fromOrderProdocutOrders.size()!= orders.length){
                return ResponseResult.fail("来源订单号存在无效或者重复订单号!");
            }

            OrderProductOrder toMergerOrderProductOrder = orderProductOrderService.getByOrderNum(toMergeOrder);
            if(toMergerOrderProductOrder==null){
                return ResponseResult.fail("目标订单号存在无效订单号!");
            }

            // 2. 把来源订单号的数量+到目标订单号的数量上，并且取消状态。
            Set<OrderProductOrder> removeOrders = new HashSet<>();
            String removeNum ="0";
            for(OrderProductOrder opo :fromOrderProdocutOrders){
                // 遍历再这判断，假如来源目标是取消的。不能进行
                if(opo.getOrderType().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2)){
                    return ResponseResult.fail("来源订单号:"+opo.getOrderNum()+"是取消状态的订单!无法合并");
                }
                // 假如来源目标和目标订单不是同货号、同品牌，不能合并
                if(!opo.getProductNum().equals(toMergerOrderProductOrder.getProductNum())
                        || !opo.getProductBrand().equals(toMergerOrderProductOrder.getProductBrand())){
                    return ResponseResult.fail("来源订单号:"+opo.getOrderNum()+"工厂货号:"+opo.getProductNum()
                            +",品牌:"+opo.getProductBrand()+",和目标工厂货号:"+toMergerOrderProductOrder.getProductNum()+
                            ",目标品牌:"+toMergerOrderProductOrder.getProductBrand()+",不一致，无法合并！");
                }
                //假如来源目标和目标订单是同订单号，不能合并
                if(opo.getOrderNum().equals(toMergerOrderProductOrder.getOrderNum())){
                    return ResponseResult.fail("来源订单号:"+opo.getOrderNum()+" 和目标订单号相同，不能合并");
                }
                removeNum = BigDecimalUtil.add(removeNum,opo.getOrderNumber()+"").toString();

                OrderProductOrder productOrder = new OrderProductOrder();
                productOrder.setId(opo.getId());
                productOrder.setOrderType(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2);
                productOrder.setUpdated(now);
                productOrder.setUpdatedUser(principal.getName());
                removeOrders.add(productOrder);
            }
            orderProductOrderService.updateBatchById(removeOrders);
            OrderProductOrder updateToMergerOrder = new OrderProductOrder();
            updateToMergerOrder.setId(toMergerOrderProductOrder.getId());
            updateToMergerOrder.setOrderNumber(Integer.valueOf(BigDecimalUtil.add(toMergerOrderProductOrder.getOrderNumber()+"",removeNum).toString()));
            updateToMergerOrder.setUpdated(now);
            updateToMergerOrder.setUpdatedUser(principal.getName());
            orderProductOrderService.updateById(updateToMergerOrder);

            // 3. 进度表关联的已备信息，+到目标订单号的进度表上，并且删除记录

            Map<String, String> mergeMaterialPreparedSum = new HashMap<>();
            Map<String, String> mergeMaterialInSum = new HashMap<>();
            Map<String, String> mergeMaterialCalSum = new HashMap<>();

            HashSet<Long> removeProgressIds = new HashSet<>();

            for(OrderProductOrder opo :fromOrderProdocutOrders) {

                List<ProduceOrderMaterialProgress> progresses = produceOrderMaterialProgressService.listByOrderId(opo.getId());
                for(ProduceOrderMaterialProgress progress:progresses){
                    String materialId = progress.getMaterialId();
                    String preparedNum = progress.getPreparedNum();
                    String inNum = progress.getInNum();
                    String calNum = progress.getCalNum();

                    String mapPreparedSum = mergeMaterialPreparedSum.get(materialId);
                    String mapInSum = mergeMaterialInSum.get(materialId);
                    String mapCalSum = mergeMaterialCalSum.get(materialId);

                    if(StringUtils.isBlank(mapPreparedSum)){
                        mapPreparedSum="0";
                    }
                    if(StringUtils.isBlank(mapInSum)){
                        mapInSum="0";
                    }
                    if(StringUtils.isBlank(mapCalSum)){
                        mapCalSum="0";
                    }
                    mergeMaterialPreparedSum.put(materialId,BigDecimalUtil.add(mapPreparedSum,preparedNum).toString());
                    mergeMaterialInSum.put(materialId,BigDecimalUtil.add(mapInSum,inNum).toString());
                    mergeMaterialCalSum.put(materialId,BigDecimalUtil.add(mapCalSum,calNum).toString());
                    removeProgressIds.add(progress.getId());
                }
            }

            List<ProduceOrderMaterialProgress> toMergerProgresses = produceOrderMaterialProgressService.listByOrderId(toMergerOrderProductOrder.getId());
            if(toMergerProgresses.isEmpty()){
                for (Map.Entry<String,String> entry : mergeMaterialPreparedSum.entrySet()){
                    String materialId = entry.getKey();

                    ProduceOrderMaterialProgress progress = new ProduceOrderMaterialProgress();
                    progress.setMaterialId(materialId);
                    String preparedNum = entry.getValue();
                    String inNum = mergeMaterialInSum.get(materialId);
                    String calNum = mergeMaterialCalSum.get(materialId);

                    progress.setPreparedNum(preparedNum);
                    progress.setInNum(inNum);
                    progress.setCalNum(calNum);
                    progress.setUpdated(now);
                    progress.setUpdatedUser(principal.getName());

                    progress.setOrderId(toMergerOrderProductOrder.getId());
                    progress.setMaterialId(materialId);
                    progress.setCreated(now);
                    progress.setCreatedUser(principal.getName());
                    double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();
                    progress.setProgressPercent((int)thePercent);
                    toMergerProgresses.add(progress);
                }
                produceOrderMaterialProgressService.saveBatch(toMergerProgresses);
            }else{
                for(ProduceOrderMaterialProgress progress : toMergerProgresses){
                    progress.setPreparedNum(BigDecimalUtil.add(progress.getPreparedNum(),mergeMaterialPreparedSum.get(progress.getMaterialId())).toString());
                    progress.setInNum(BigDecimalUtil.add(progress.getInNum(),mergeMaterialInSum.get(progress.getMaterialId())).toString());
                    progress.setCalNum(BigDecimalUtil.add(progress.getCalNum(),mergeMaterialCalSum.get(progress.getMaterialId())).toString());
                    progress.setUpdated(now);
                    progress.setUpdatedUser(principal.getName());

                    double thePercent = BigDecimalUtil.div(BigDecimalUtil.mul(Double.valueOf(progress.getPreparedNum()), 100).doubleValue(),Double.valueOf(progress.getCalNum())).doubleValue();
                    progress.setProgressPercent((int)thePercent);

                }
                produceOrderMaterialProgressService.updateBatchById(toMergerProgresses);
            }


            produceOrderMaterialProgressService.removeByIds(removeProgressIds);

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE);
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("订单号不能重复!");
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 修改订单号
     */
    @PostMapping("/supOrderNumber")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    public ResponseResult supOrderNumber(Principal principal, @Validated @RequestBody List<Map<String,String>> supMaps)
            throws Exception{

        if(supMaps.isEmpty()){
            return ResponseResult.fail("补充订单集合内容为空");
        }
        log.info("一键补订单的map:{}",supMaps);

        // 根据key：订单号，value： 数量，进行加减
        for(Map<String,String> entry : supMaps){
            Set<String> key = entry.keySet();
            for(String orderNum : key){
                String needAddNum = entry.get(orderNum);
                orderProductOrderService.addOrderNumberByOrderNum(orderNum,needAddNum);
            }
        }

        return ResponseResult.succ("补充订单集合成功!");

    }



    @Transactional
    @PostMapping("calNoProductOrders")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult calNoProductOrders() throws Exception{
        try {
            List<Object> returnLists = new ArrayList<>();
            List<OrderProductCalVO> lists = orderProductOrderService.calNoProductOrders();

            List<OrderProductCalVO> noInNums = produceOrderMaterialProgressService.listNoInNums();

            HashMap<String, String> materialAndNoInNum = new HashMap<>();
            for(OrderProductCalVO vo : noInNums){
                materialAndNoInNum.put(vo.getMaterialId(),vo.getNoInNum());
            }

            // 获取投产未领料出库的物料数目
            List<RepositoryStock> noPickMaterials = orderProductOrderService.listNoPickMaterials();
            HashMap<String, String> materialAndNoPickNum = new HashMap<>();
            for(RepositoryStock rs : noPickMaterials){
                materialAndNoPickNum.put(rs.getMaterialId(),rs.getNum()+"");
            }

            HashMap<String, OrderProductCalVO> groupMap = new HashMap<>();

            // 遍历，根据物料进行分组，求和数量
            for(OrderProductCalVO vo : lists){
                String materialId = vo.getMaterialId();
                OrderProductCalVO theOneMaterialIdOBJ = groupMap.get(materialId);

                if(theOneMaterialIdOBJ==null){
                    theOneMaterialIdOBJ  = new OrderProductCalVO();
                    theOneMaterialIdOBJ.setMaterialId(vo.getMaterialId());
                    theOneMaterialIdOBJ.setMaterialName(vo.getMaterialName());
                    theOneMaterialIdOBJ.setNeedNum(vo.getNeedNum());
                    theOneMaterialIdOBJ.setStockNum(vo.getStockNum());
                    theOneMaterialIdOBJ.setNoInNum(materialAndNoInNum.get(materialId)==null?"0":materialAndNoInNum.get(materialId));
                    theOneMaterialIdOBJ.setNoPickNum(materialAndNoPickNum.get(materialId)==null?"0":materialAndNoPickNum.get(materialId));
                    groupMap.put(materialId,theOneMaterialIdOBJ);
                }else{
                    theOneMaterialIdOBJ.setNeedNum(BigDecimalUtil.add( theOneMaterialIdOBJ.getNeedNum(),vo.getNeedNum()).toString() );
                    theOneMaterialIdOBJ.setNoPickNum(BigDecimalUtil.add( theOneMaterialIdOBJ.getNoPickNum(),vo.getNoPickNum()==null?"0":vo.getNoPickNum()).toString() );
                }

            }
            returnLists.add(lists);
            returnLists.add(groupMap.values());
            return ResponseResult.succ(returnLists);
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }



    @Transactional
    @PostMapping("noCancelOrder")
    @PreAuthorize("hasAuthority('order:productOrder:del')")
    public ResponseResult noCancelOrder(Principal principal,Long id) throws Exception{
        try {
            OrderProductOrder orderProductOrder = new OrderProductOrder();
            orderProductOrder.setUpdated(LocalDateTime.now());
            orderProductOrder.setUpdatedUser(principal.getName());
            orderProductOrder.setId(id);
            orderProductOrder.setOrderType(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_0);
            orderProductOrderService.updateById(orderProductOrder);
            return ResponseResult.succ("订单还原成功!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }



    @Transactional
    @PostMapping("cancelOrder")
    @PreAuthorize("hasAuthority('order:productOrder:del')")
    public ResponseResult cancelOrder(Principal principal,Long id) throws Exception{
        try {
            // 1. 查询进度表是否有入库记录，有入库记录提示

            List<ProduceOrderMaterialProgress> pompes = produceOrderMaterialProgressService.listByOrderId(id);
            for(ProduceOrderMaterialProgress POMP :pompes){
                String inNum = POMP.getInNum();
                if(inNum!=null && !inNum.equals("0") && !inNum.equals("0.0")){
                    return ResponseResult.fail("有入库消单，请先迁移!");
                }
                POMP.setPreparedNum("0");
                POMP.setProgressPercent(0);
            }
            OrderProductOrder order = orderProductOrderService.getById(id);

            List<ProduceBatch> pbs = produceBatchService.listByOrderNum(order.getOrderNum());
            if(pbs != null && pbs.size() > 0){
                return ResponseResult.fail("【生产序号模块】已引用该订单号:"+order.getOrderNum()+",无法删除");
            }
            // 2.

            OrderProductOrder orderProductOrder = new OrderProductOrder();
            orderProductOrder.setUpdated(LocalDateTime.now());
            orderProductOrder.setUpdatedUser(principal.getName());
            orderProductOrder.setId(id);
            orderProductOrder.setOrderType(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2);
            orderProductOrderService.updateById(orderProductOrder);

            // 2. 把进度表的备料信息清空
            produceOrderMaterialProgressService.updateBatchById(pompes);
            return ResponseResult.succ("订单取消成功!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }


    @PostMapping("/batchNotSurePrepare")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult batchNotSurePrepare(Principal principal,@RequestBody Long[] ids) {
        ArrayList<OrderProductOrder> lists = new ArrayList<>();

        for (Long id : ids){
            OrderProductOrder old = orderProductOrderService.getById(id);
            if(!old.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2)){
                return ResponseResult.fail("备料状态不对，已修改，请刷新!");
            }

            OrderProductOrder orderProductOrder = new OrderProductOrder();
            orderProductOrder.setUpdated(LocalDateTime.now());
            orderProductOrder.setUpdatedUser(principal.getName());
            orderProductOrder.setId(id);
            orderProductOrder.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
            lists.add(orderProductOrder);

        }
        orderProductOrderService.updateBatchById(lists);
        return ResponseResult.succ("批量取消确认");
    }

    @Transactional
    @PostMapping("prepareNotSure")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult prepareNotSure(Long id) throws Exception{
        try {
            OrderProductOrder old = orderProductOrderService.getById(id);
            if(!old.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2)){
                return ResponseResult.fail("备料状态不对，已修改，请刷新!");
            }
            orderProductOrderService.updatePrepared(id,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
            return ResponseResult.succ("备料取消确认成功!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("prepareSure")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult prepareSure(Long id) throws Exception{
        try {
            OrderProductOrder old = orderProductOrderService.getById(id);
            if(!old.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1)){
                return ResponseResult.fail("备料状态不对，已修改，请刷新!");
            }
            orderProductOrderService.updatePrepared(id,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2);
            return ResponseResult.succ("备料确认完成!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("preparedSuccess")
    @PreAuthorize("hasAuthority('order:productOrder:prepareDone')")
    public ResponseResult preparedSuccess(Long id) throws Exception{
        try {
            OrderProductOrder old = orderProductOrderService.getById(id);
            if(!old.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2)){
                return ResponseResult.fail("备料状态不对，已修改，请刷新!");
            }

            orderProductOrderService.updatePrepared(id,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_0);
            return ResponseResult.succ("备料完成!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }
    @Transactional
    @PostMapping("preparedNotSuccess")
    @PreAuthorize("hasAuthority('order:productOrder:prepareDone')")
    public ResponseResult preparedNotSuccess(Long id) throws Exception{
        try {
            OrderProductOrder old = orderProductOrderService.getById(id);
            if(!old.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_0)){
                return ResponseResult.fail("备料状态不对，已修改，请刷新!");
            }
            // 假如有入库的，就不能反
            List<ProduceOrderMaterialProgress> progresses = produceOrderMaterialProgressService.listByOrderId(id);
            if(progresses != null && progresses.size() > 0){
                for (ProduceOrderMaterialProgress progress : progresses){
                    if(Double.valueOf(progress.getInNum()) > 0.0){
                        return ResponseResult.fail("物料["+progress.getMaterialId()+"],已经存在入库的消单记录,不能解除!");
                    }
                }
            }

            orderProductOrderService.updatePrepared(id,DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2);
            return ResponseResult.succ("备料解除完成!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    /***
     * 根据订单，获取订单，产品组成，用料，进度表信息
     * @param principal
     * @param orderId
     * @return
     * @throws Exception
     */

    @GetMapping("/listOrderConstituentProgress")
    public ResponseResult listOrderConstituentProgress(Principal principal, Long orderId)throws Exception {
        OrderProductOrder order = orderProductOrderService.getById(orderId);
        if(order.getMaterialBomId()==null){
            return ResponseResult.fail("没有选择物料BOM，请确认!");
        }
        List<ProduceProductConstituentDetail> theConsitituentDetails = produceProductConstituentDetailService.listByForeignId(order.getMaterialBomId());

        List<ProduceOrderMaterialProgress> theProgress = produceOrderMaterialProgressService.listByOrderId(order.getId());
        HashMap<String, ProduceOrderMaterialProgress> theMaterialIdAndProgress = new HashMap<>();

        if(theProgress!=null && theProgress.size()>0){
            for (ProduceOrderMaterialProgress progress : theProgress){
                theMaterialIdAndProgress.put(progress.getMaterialId(),progress);
            }
        }

        // 获取大皮的物料
        Set<String> materialIds = new HashSet<>();

        Integer orderNumber = order.getOrderNumber();
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        // 计算数目 * 每个物料的用量
        for (ProduceProductConstituentDetail item : theConsitituentDetails){
            HashMap<String, Object> calTheMap = new HashMap<>();
            BaseMaterial material = baseMaterialService.getById(item.getMaterialId());
           /* // 查看该物料，最近的供应商价目，
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
            }*/
            calTheMap.put("orderNumber",orderNumber);
            calTheMap.put("dosage",item.getDosage());
            calTheMap.put("materialId",material.getId());
            calTheMap.put("materialName",material.getName());
            ProduceOrderMaterialProgress dbProgress = theMaterialIdAndProgress.get(material.getId());

            calTheMap.put("calNum",BigDecimalUtil.mul(item.getDosage(),orderNumber+"").doubleValue() );
            calTheMap.put("materialUnit",material.getUnit());
            calTheMap.put("preparedNum",dbProgress==null?0:dbProgress.getPreparedNum());
            calTheMap.put("comment",dbProgress==null?"":dbProgress.getComment());
            calTheMap.put("addNum",0);
            calTheMap.put("prepared",order.getPrepared());
            double thePercent = Double.valueOf(calTheMap.get("preparedNum").toString())*100 / Double.valueOf(calTheMap.get("calNum").toString());
            if(thePercent > 100){
                thePercent = 100;
            }
            calTheMap.put("progressPercent",(int)thePercent);

            calTheMap.put("needNum","0");
            calTheMap.put("noInNum","0");
            calTheMap.put("noPickNum","0");

            result.add(calTheMap);

            materialIds.add(material.getId());
        }
        List<String> materialIdsLists = new ArrayList<String>();
        materialIdsLists.addAll(materialIds);
        List<RepositoryStock> stocks = repositoryStockService.listByMaterialIds(materialIdsLists);
        HashMap<String, RepositoryStock> map_stock = new HashMap<>();

        for(RepositoryStock stock : stocks){
            map_stock.put(stock.getMaterialId(),stock);
        }


        // 获取未投产应需用量
        List<OrderProductCalVO> noProductionNums = orderProductOrderService.calNoProductOrdersWithMaterialIds(materialIds);
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
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(0D);
                map_stock.put(materialId,stock);
            }
            stock.setNeedNum(needNum);
            stock.setNoProductionNums(noProductionGroupDetails.get(materialId));
        }

        // 获取投产未领数量
        List<RepositoryStock> noPickMaterials = orderProductOrderService.listNoPickMaterialsWithMaterialIds(materialIds);
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
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(0D);
                map_stock.put(materialId,stock);
            }
            stock.setNoPickNum(needNum);
            stock.setNoPickNums(noPickDetails.get(materialId));
        }
        // 获取已报未入库数量
        List<OrderProductCalVO> noInNums = produceOrderMaterialProgressService.listNoInNumsWithMaterialIds(materialIds);
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
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(0D);
                map_stock.put(materialId,stock);
            }
            stock.setNoInNum(needNum);
            stock.setNoInNums(noInDetails.get(materialId));
        }

        for(Map<String,Object> row : result){
            String materialId = row.get("materialId").toString();
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                log.error("物料{}空，不能查询库存、已报未入库、未领、未投应需数量",stock);
                continue;
            }
            row.put("noInNums",stock.getNoInNums());
            row.put("noInNum",stock.getNoInNum());
            row.put("noPickNums",stock.getNoPickNums());
            row.put("noPickNum",stock.getNoPickNum());
            row.put("noProductionNums",stock.getNoProductionNums());
            row.put("needNum",stock.getNeedNum());
            row.put("stockNum",stock.getNum());
        }

        return ResponseResult.succ(result);
    }

    /***
     * 根据批量订单，获取订单，产品组成，用料，进度表信息
     * @param principal
     * @return
     * @throws Exception
     */

    @PostMapping("/listBatchOrderConstituentProgress")
    public ResponseResult listBatchOrderConstituentProgress(Principal principal, @RequestBody Long[] ids)throws Exception {

        boolean canBatchPrepareFlag = true;
        List<OrderProductOrder> ods = orderProductOrderService.listByIds(Arrays.asList(ids));
        for (OrderProductOrder obj : ods){
            if(!obj.getStatus().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("订单号["+obj.getOrderNum()+"]没有审核通过");
            }
            if(!obj.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1)){
                canBatchPrepareFlag=false;
            }
            if(obj.getMaterialBomId() == null){
                return ResponseResult.fail("订单号:["+obj.getOrderNum()+"]没有选择物料BOM");
            }
        }


        // 1. 根据批量订单ID，获取订单的组成信息，获取订单数量信息
        // 2. 获取进度表对应的信息
        List<OrderProductOrder> orders = orderProductOrderService.listBatchMaterialsByOrderIds(Arrays.asList(ids));

        // 3. 根据物料分组，列出订单号，公司货号，品牌，颜色，订单数目，用量，应备数目，已报备数目的明细列表，应备数目（合计），已报备（合计）
        Map<String,Map<String, Object>> result2 = new TreeMap<String,Map<String, Object>>();

        for (OrderProductOrder item : orders){
            Map<String, Object> theMaterialIdMaps = result2.get(item.getMaterialId());
            if(theMaterialIdMaps ==null){
                theMaterialIdMaps = new HashMap<String,Object>();
                result2.put(item.getMaterialId(),theMaterialIdMaps);
            }

            theMaterialIdMaps.put("addNums",0);

            Object materialName = theMaterialIdMaps.get("materialName");
            if(materialName == null ){
                theMaterialIdMaps.put("materialName",item.getMaterialName());
            }

            Object materialId = theMaterialIdMaps.get("materialId");
            if(materialId == null ){
                theMaterialIdMaps.put("materialId",item.getMaterialId());
            }

            Object materialUnit = theMaterialIdMaps.get("materialUnit");
            if(materialUnit == null){
                theMaterialIdMaps.put("materialUnit",item.getMaterialUnit());
            }

            Object calNums = theMaterialIdMaps.get("calNums");
            if(calNums == null ){
                theMaterialIdMaps.put("calNums",item.getCalNum());
            }else{
                theMaterialIdMaps.put("calNums",BigDecimalUtil.add(calNums+"",item.getCalNum()).doubleValue());
            }

            Object preparedNums = theMaterialIdMaps.get("preparedNums");
            String preparedNum = item.getPreparedNum() == null ? "0":item.getPreparedNum();
            if(preparedNums == null ){
                theMaterialIdMaps.put("preparedNums", preparedNum);
            }else{
                theMaterialIdMaps.put("preparedNums",BigDecimalUtil.add(preparedNums+"",preparedNum).doubleValue());
            }


            Object theMaterialIdLists = theMaterialIdMaps.get("details");

            if(theMaterialIdLists ==null){
                theMaterialIdLists = new LinkedList<>();
                theMaterialIdMaps.put("details",theMaterialIdLists);
            }

            HashMap<String, Object> oneRow = new HashMap<>();
            oneRow.put("orderId",item.getOrderId());
            oneRow.put("orderNum",item.getOrderNum());
            oneRow.put("productNum",item.getProductNum());
            oneRow.put("productBrand",item.getProductBrand());
            oneRow.put("productColor",item.getProductColor());
            oneRow.put("orderNumber",item.getOrderNumber());
            oneRow.put("dosage",item.getDosage());
            oneRow.put("calNum",item.getCalNum());
            oneRow.put("preparedNum", preparedNum);
            ((List)theMaterialIdLists).add(oneRow);

        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        Set<String> materialIds = new HashSet<>();

        for (Map.Entry<String,Map<String, Object>> entry : result2.entrySet()){
            materialIds.add(entry.getKey());
            result.add(entry.getValue());
        }
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("datas",result);
        returnMap.put("canBatchPrepareFlag",canBatchPrepareFlag);

        List<String> materialIdsLists = new ArrayList<String>();
        materialIdsLists.addAll(materialIds);
        List<RepositoryStock> stocks = repositoryStockService.listByMaterialIds(materialIdsLists);
        HashMap<String, RepositoryStock> map_stock = new HashMap<>();

        for(RepositoryStock stock : stocks){
            map_stock.put(stock.getMaterialId(),stock);
        }


        // 获取未投产应需用量
        List<OrderProductCalVO> noProductionNums = orderProductOrderService.calNoProductOrdersWithMaterialIds(materialIds);
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
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(0D);
                map_stock.put(materialId,stock);
            }
            stock.setNeedNum(needNum);
            stock.setNoProductionNums(noProductionGroupDetails.get(materialId));
        }

        // 获取投产未领数量
        List<RepositoryStock> noPickMaterials = orderProductOrderService.listNoPickMaterialsWithMaterialIds(materialIds);
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
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(0D);
                map_stock.put(materialId,stock);
            }
            stock.setNoPickNum(needNum);
            stock.setNoPickNums(noPickDetails.get(materialId));
        }
        // 获取已报未入库数量
        List<OrderProductCalVO> noInNums = produceOrderMaterialProgressService.listNoInNumsWithMaterialIds(materialIds);
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
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                stock = new RepositoryStock();
                stock.setMaterialId(materialId);
                stock.setNum(0D);
                map_stock.put(materialId,stock);
            }
            stock.setNoInNum(needNum);
            stock.setNoInNums(noInDetails.get(materialId));
        }

        for(Map<String,Object> row : result){
            String materialId = row.get("materialId").toString();
            RepositoryStock stock = map_stock.get(materialId);
            if(stock==null){
                log.error("物料{}空，不能查询库存、已报未入库、未领、未投应需数量",stock);
                continue;
            }
            row.put("noInNums",stock.getNoInNums());
            row.put("noInNum",stock.getNoInNum());
            row.put("noPickNums",stock.getNoPickNums());
            row.put("noPickNum",stock.getNoPickNum());
            row.put("noProductionNums",stock.getNoProductionNums());
            row.put("needNum",stock.getNeedNum());
            row.put("stockNum",stock.getNum());
        }

        return ResponseResult.succ(returnMap);
    }


    /**
     * 上传校验 订单是否一致
     */
    @PostMapping("/uploadValidOrderNum")
    @PreAuthorize("hasAuthority('order:productOrder:import')")
    public ResponseResult uploadValidOrderNum(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<OrderProductOrder> utils = new ExcelImportUtil<OrderProductOrder>(OrderProductOrder.class);
        List<OrderProductOrder> orderProductOrders = null;
        try (InputStream fis = file.getInputStream();){
            orderProductOrders = utils.readExcel(fis, 1, 0,18,replaceMap);
            log.info("解析的excel数据:{}",orderProductOrders);

            if(orderProductOrders == null || orderProductOrders.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();

            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("showContent",errorMsgs);

            Integer minOrderNum = Integer.MAX_VALUE;
            Integer maxOrderNum = Integer.MIN_VALUE;
            for (OrderProductOrder order: orderProductOrders){
                if(StringUtils.isBlank(order.getProductNum()) && StringUtils.isBlank(order.getProductBrand())){
                    continue;
                }
                Integer orderNum = Integer.valueOf(order.getOrderNum().split("-")[0]);
                if(minOrderNum > orderNum){
                    minOrderNum=orderNum;
                }
                if(maxOrderNum < orderNum){
                    maxOrderNum = orderNum;
                }
            }
            // 查询DB在该时间段全部的订单
            List<OrderProductOrder> dbOrders = orderProductOrderService.listByOrderNumWithStartAndEnd(minOrderNum,maxOrderNum);

            Set<String> dbOrderNums = new HashSet<>();
            for(OrderProductOrder opo : dbOrders){
                dbOrderNums.add(opo.getOrderNum());
            }

            Set<String> excelOrderNums = new HashSet<>();
            HashMap<Long, OrderProductOrder> updateOrders = new HashMap<>();


            for (OrderProductOrder order: orderProductOrders){
                if(StringUtils.isBlank(order.getProductNum()) && StringUtils.isBlank(order.getProductBrand())){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","系统订单号:"+order.getOrderNum()+"，品牌和货号都为空值!");
                    errorMsgs.add(errorMsg);
                    continue;
                }
                excelOrderNums.add(order.getOrderNum());

                Integer orderNum = Integer.valueOf(order.getOrderNum().split("-")[0]);
                if(minOrderNum > orderNum){
                    minOrderNum=orderNum;
                }
                if(maxOrderNum < orderNum){
                    maxOrderNum = orderNum;
                }

                OrderProductOrder sysOrder = orderProductOrderService.getByOrderNum(order.getOrderNum());
                if(sysOrder==null){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","系统不存在订单号:"+order.getOrderNum()+"");
                    errorMsgs.add(errorMsg);
                }else{
                    // 存在的情况下，看下内容
                    if(!order.getProductNum().equals(sysOrder.getProductNum())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",order.getOrderNum()+":"+"系统工厂货号:"+sysOrder.getProductNum()+"，EXCEL工厂货号:"+order.getProductNum());
                        errorMsgs.add(errorMsg);
                    }
                    if(!order.getProductBrand().equals(sysOrder.getProductBrand())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",order.getOrderNum()+":"+"系统品牌:"+sysOrder.getProductBrand()+"，EXCEL品牌:"+order.getProductBrand());
                        errorMsgs.add(errorMsg);
                    }
                    if(!order.getProductColor().equals(sysOrder.getProductColor())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",order.getOrderNum()+":"+"系统颜色:"+sysOrder.getProductColor()+"，EXCEL颜色:"+order.getProductColor());
                        errorMsgs.add(errorMsg);
                    }

                    if(!order.getOrderNumber().equals(sysOrder.getOrderNumber())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",order.getOrderNum()+":"+"系统订单数目:"+sysOrder.getOrderNumber()+"，EXCEL订单数目:"+order.getOrderNumber());
                        errorMsgs.add(errorMsg);
                    }
                    if(sysOrder.getOrderType()!=null && sysOrder.getOrderType().equals(2)){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",order.getOrderNum()+":"+"系统订单状态是取消!!");
                        errorMsgs.add(errorMsg);
                    }
                    // 把excel的货期，修改到db对象中
                    String excelEndDate = order.getEndDate();
                    String dbEndDate = sysOrder.getEndDate();

                    String excelShoeLast = order.getShoeLast();
                    String dbShoeLast = sysOrder.getShoeLast();

                    if(excelEndDate!=null && !excelEndDate.equals(dbEndDate)){
                        OrderProductOrder productOrder = updateOrders.get(sysOrder.getId());
                        if(productOrder==null){
                            productOrder = sysOrder;
                        }
                        productOrder.setEndDate(excelEndDate);

                        updateOrders.put(sysOrder.getId(),productOrder);

                    }
                    if(excelShoeLast!=null && !excelShoeLast.equals(dbShoeLast)){
                        OrderProductOrder productOrder = updateOrders.get(sysOrder.getId());
                        if(productOrder==null){
                            productOrder = sysOrder;
                        }
                        productOrder.setShoeLast(excelShoeLast);
                        updateOrders.put(sysOrder.getId(),productOrder);
                    }


                    /*if(!order.getEndDate().equals(sysOrder.getEndDate())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",order.getOrderNum()+":"+"系统货期:"+sysOrder.getEndDate()+"，EXCEL货期:"+order.getEndDate());
                        errorMsgs.add(errorMsg);
                    }*/
                }
            }
            if(dbOrderNums.size()!=excelOrderNums.size()){
                for(String orderNumStr : dbOrderNums){
                    if(!excelOrderNums.contains(orderNumStr)){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","最小订单号["+minOrderNum+"]，最大订单号["+maxOrderNum+"]区间内."+"EXCEL不存在订单号:"+orderNumStr+"");
                        errorMsgs.add(errorMsg);
                    }
                }
            }

            // 修改订单信息。（目前货期不一致，在这里修改）
            if(updateOrders.size() > 0){
                orderProductOrderService.updateBatchById(updateOrders.values());
            }

            if(errorMsgs.isEmpty()){
                HashMap<String, String> errorMsg = new HashMap<>();
                errorMsg.put("content","导入的EXCEL订单号都存在且一致！");
                errorMsgs.add(errorMsg);
            }
            return ResponseResult.succ(returnMap);

        }
        catch (Exception e) {
            if( e instanceof  DuplicateKeyException){
                return ResponseResult.fail("订单号重复！");
            }
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 上传校验未投订单数目是否精准
     */
    @PostMapping("/uploadValidNoProduct")
    @PreAuthorize("hasAuthority('order:productOrder:import')")
    public ResponseResult uploadValidNoProduct(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<OrderProductOrder> utils = new ExcelImportUtil<OrderProductOrder>(OrderProductOrder.class);
        List<OrderProductOrder> orderProductOrders = null;
        try (InputStream fis = file.getInputStream();){
            orderProductOrders = utils.readExcel(fis, 1, 0,18,replaceMap);
            log.info("解析的excel数据:{}",orderProductOrders);


            if(orderProductOrders == null || orderProductOrders.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();

            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("showContent",errorMsgs);

            List<Map<String, String>> needRepairs = new ArrayList<Map<String, String>>();
            returnMap.put("needRepairLists",needRepairs);

            HashMap<String, OrderProductOrder> validOrders = new HashMap<>();
            for (OrderProductOrder order: orderProductOrders){
                if(StringUtils.isBlank(order.getProductNum()) && StringUtils.isBlank(order.getProductBrand())){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content",order.getOrderNum()+":"+"EXCEL品牌、货号都为空");
                    errorMsgs.add(errorMsg);
                    continue;
                }
                String orderNum = order.getOrderNum();
                if(validOrders.containsKey(orderNum)){
                    return ResponseResult.fail("EXCEL存在重复订单号:",order.getOrderNum());
                }
                validOrders.put(orderNum,order);
                ids.add(order.getOrderNum());
            }

            // 查询系统的未投订单
            List<OrderProductOrder> sysNoProduct = orderProductOrderService.listNoProduct();
            HashMap<String, OrderProductOrder> sysOrders = new HashMap<>();

            for (OrderProductOrder order: sysNoProduct) {
                String orderNum = order.getOrderNum();
                if(sysOrders.containsKey(orderNum)){
                    return ResponseResult.fail("系统存在重复订单号:",order.getOrderNum());
                }
                sysOrders.put(orderNum,order);
            }

            HashMap<Long, OrderProductOrder> updateOrders = new HashMap<>();

            if(sysNoProduct != null && !sysNoProduct.isEmpty()){


                for (Map.Entry<String,OrderProductOrder> entry: validOrders.entrySet()){
                    String orderNum = entry.getKey();
                    OrderProductOrder excelOpo = entry.getValue();
                    OrderProductOrder sysOrder = sysOrders.get(orderNum);
                    // 对未投的全部进行系统数量校验，有数量出入的，提示差值。，假如系统没有的也要提示不存在。

                    if(sysOrder==null){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+orderNum+"系统不存在或系统取消");
                        errorMsgs.add(errorMsg);
                        continue;
                    }
                    Integer excelNumber = excelOpo.getOrderNumber();
                    Integer orderNumber = sysOrder.getOrderNumber();

                    if(!excelOpo.getProductNum().equals(sysOrder.getProductNum())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",excelOpo.getOrderNum()+":"+"系统工厂货号:"+sysOrder.getProductNum()+"，EXCEL工厂货号:"+excelOpo.getProductNum());
                        errorMsgs.add(errorMsg);
                    }
                    if(!excelOpo.getProductBrand().equals(sysOrder.getProductBrand())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",excelOpo.getOrderNum()+":"+"系统品牌:"+sysOrder.getProductBrand()+"，EXCEL品牌:"+excelOpo.getProductBrand());
                        errorMsgs.add(errorMsg);
                    }
                    if(!excelOpo.getProductColor().equals(sysOrder.getProductColor())){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content",excelOpo.getOrderNum()+":"+"系统颜色:"+sysOrder.getProductColor()+"，EXCEL颜色:"+excelOpo.getProductColor());
                        errorMsgs.add(errorMsg);
                    }

                    // 把excel的货期，修改到db对象中
                    String excelEndDate = excelOpo.getEndDate();
                    String dbEndDate = sysOrder.getEndDate();

                    String excelShoeLast = excelOpo.getShoeLast();
                    String dbShoeLast = sysOrder.getShoeLast();

                    if(excelEndDate!=null && !excelEndDate.equals(dbEndDate)){
                        OrderProductOrder productOrder = updateOrders.get(sysOrder.getId());
                        if(productOrder==null){
                            productOrder = sysOrder;
                        }
                        productOrder.setEndDate(excelEndDate);

                        updateOrders.put(sysOrder.getId(),productOrder);

                    }
                    if(excelShoeLast!=null && !excelShoeLast.equals(dbShoeLast)){
                        OrderProductOrder productOrder = updateOrders.get(sysOrder.getId());
                        if(productOrder==null){
                            productOrder = sysOrder;
                        }
                        productOrder.setShoeLast(excelShoeLast);
                        updateOrders.put(sysOrder.getId(),productOrder);
                    }

                    if(excelNumber == null || orderNumber ==null){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+orderNum+"EXCEL或者系统订单数目为空");
                        errorMsgs.add(errorMsg);
                        continue;
                    }
                    if(excelNumber.intValue()!=orderNumber.intValue()){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+orderNum+",EXCEL数量["+excelNumber+"],系统数量["+orderNumber+"]");

                        errorMsgs.add(errorMsg);

                        int chazhi = BigDecimalUtil.sub(excelNumber, orderNumber).intValue();
                        if(chazhi > 0){
                            HashMap<String, String> repair = new HashMap<>();
                            repair.put(orderNum,chazhi+"");
                            needRepairs.add(repair);
                        }
                        // 负数的目前先不考虑

                    }

                }


            }else{
                HashMap<String, String> errorMsg = new HashMap<>();
                errorMsg.put("content","系统的未投数量为0");
                errorMsgs.add(errorMsg);
            }

            // 系统里的订单，要是没在生管未投订单里，也显示错误
            for(Map.Entry<String,OrderProductOrder> sysOrder: sysOrders.entrySet()){
                if(!validOrders.containsKey(sysOrder.getKey())){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","系统未投订单号:"+sysOrder.getKey()+",在生管EXCEL中不存在");
                    errorMsgs.add(errorMsg);
                }
            }

            if(errorMsgs.isEmpty()){
                HashMap<String, String> errorMsg = new HashMap<>();
                errorMsg.put("content","校验一致！");
                errorMsgs.add(errorMsg);
            }

            // 修改订单信息。（目前货期不一致，在这里修改）
            if(updateOrders.size() > 0){
                orderProductOrderService.updateBatchById(updateOrders.values());
            }
            return ResponseResult.succ(returnMap);

        }
        catch (Exception e) {
            if( e instanceof  DuplicateKeyException){
                return ResponseResult.fail("订单号重复！");
            }
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('order:productOrder:import')")
    public ResponseResult upload(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<OrderProductOrder> utils = new ExcelImportUtil<OrderProductOrder>(OrderProductOrder.class);
        List<OrderProductOrder> orderProductOrders = null;
        try (InputStream fis = file.getInputStream();){
            orderProductOrders = utils.readExcel(fis, 1, 0,18,replaceMap);
            log.info("解析的excel数据:{}",orderProductOrders);


            if(orderProductOrders == null || orderProductOrders.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();

            List<OrderProductOrder> saveOrders = new ArrayList<OrderProductOrder>();
            for (OrderProductOrder order: orderProductOrders){
                if(StringUtils.isBlank(order.getProductNum()) && StringUtils.isBlank(order.getProductBrand())){
                    continue;
                }
                if(order.getOrderType()==null){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","订单号："+order.getOrderNum()+"的订单类型不能为空!");
                    errorMsgs.add(errorMsg);
                }
                if(order.getOrderNum().length() >=9){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","订单号："+order.getOrderNum()+"长度不允许超过8个");
                    errorMsgs.add(errorMsg);
                }
                LocalDateTime now = LocalDateTime.now();
                order.setCreated(now);
                order.setUpdated(now);
                order.setCreatedUser(principal.getName());
                order.setUpdatedUser(principal.getName());
                order.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0);
                order.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
                saveOrders.add(order);

                ProduceProductConstituent ppc = produceProductConstituentService.getValidLatestByNumAndBrand(order.getProductNum(), order.getProductBrand());
                if(ppc!=null){
                    order.setMaterialBomId(ppc.getId());
                }


                ids.add(order.getOrderNum());
            }
            List<OrderProductOrder> exist = orderProductOrderService.list(new QueryWrapper<OrderProductOrder>()
                    .in(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_NUM_FIELDNAME,ids));
            if(exist != null && !exist.isEmpty()){
                for (OrderProductOrder existOne:exist){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","订单号："+existOne.getOrderNum()+"已存在");
                    errorMsgs.add(errorMsg);
                }
            }
            if(errorMsgs.size() > 0){
                return ResponseResult.succ(errorMsgs);
            }
            orderProductOrderService.saveBatch(saveOrders);
        }
        catch (Exception e) {
            if( e instanceof  DuplicateKeyException){
                return ResponseResult.fail("订单号重复！");
            }
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }

        return ResponseResult.succ("上传成功");
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
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
    @PreAuthorize("hasAuthority('order:productOrder:del')")
    public ResponseResult delete(@RequestBody Long[] ids) throws Exception{
        try {

            for (Long id : ids){
                OrderProductOrder order = orderProductOrderService.getById(id);

                List<ProduceBatch> pb = produceBatchService.getByOrderNum(order.getOrderNum());
                if(pb != null && pb.size()>0 ){
                    return ResponseResult.fail("【生产序号模块】已引用该订单号:"+order.getOrderNum());
                }
            }

            // 查看是否有生产序号引用

            boolean flag = orderProductOrderService.removeByIds(Arrays.asList(ids));

            log.info("删除产品订单表信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("产品订单删除失败");
            }
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
    @PreAuthorize("hasAuthority('order:productOrder:list')")
    public ResponseResult queryById(Long id) {
        OrderProductOrder orderProductOrder = orderProductOrderService.getById(id);
        return ResponseResult.succ(orderProductOrder);
    }

    /**
     * 修改订单号
     */
    @PostMapping("/updateOrderNum")
    @PreAuthorize("hasAuthority('order:productOrder:update')")
    @Transactional
    public ResponseResult updateOrderNum(Principal principal, @Validated @RequestBody OrderProductOrder orderProductOrder)
            throws Exception{

        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        try {
            if(orderProductOrder.getOrderNum()!=null && orderProductOrder.getOrderNum().length() >=9){
                return ResponseResult.fail("订单号不能空并且超过8位!");
            }
//            OrderProductOrder old = orderProductOrderService.getById(orderProductOrder.getId());
            /*if(old.getOrderNumber().intValue() > orderProductOrder.getOrderNumber()){
                return ResponseResult.fail("新的订单数目["+orderProductOrder.getOrderNumber().intValue()+"] 不允许比老的订单数目["+old.getOrderNumber()+"] 少");
            }*/
            Long orderId = orderProductOrder.getId();
            OrderProductOrder oldOrder = orderProductOrderService.getById(orderId);
            // 1. 假如投产计划单有该订单号的批次号信息，同步修改
            List<ProduceBatch> orderNum_batches = produceBatchService.listByOrderNum(oldOrder.getOrderNum());
            orderProductOrderService.updateById(orderProductOrder);

            if(orderNum_batches!=null && orderNum_batches.size() > 0){
                Double totalNum = 0D;
                for(ProduceBatch pb :orderNum_batches){
                    pb.setOrderNum(orderProductOrder.getOrderNum());
                    BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                            .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                            .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                            .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                            .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
                    totalNum = BigDecimalUtil.add(totalNum,theTotalNum.doubleValue()).doubleValue();
                }
                if((totalNum.doubleValue() != oldOrder.getOrderNumber() ) ||
                        (totalNum.doubleValue() != orderProductOrder.getOrderNumber() )){
                    if((totalNum.doubleValue() != oldOrder.getOrderNumber() )){
                        return ResponseResult.fail("批次号数量["+totalNum+"]不等于新订单数量["+orderProductOrder.getOrderNumber()+"]!");
                    }else {
                        return ResponseResult.fail("批次号数量["+totalNum+"]不等于老订单数量["+oldOrder.getOrderNumber()+"]!");
                    }
                }
                produceBatchService.updateBatchById(orderNum_batches);

            }




            log.info("产品订单模块-更新订单号内容:{}",orderProductOrder);

            return ResponseResult.succ("编辑成功");
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("订单号不能重复!");
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 修改入库
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('order:productOrder:update')")
    @Transactional
    public ResponseResult update(Principal principal, @Validated @RequestBody OrderProductOrder orderProductOrder)
            throws Exception{

        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_2);
        try {
            if(orderProductOrder.getOrderNum()!=null && orderProductOrder.getOrderNum().length() >=9){
                return ResponseResult.fail("订单号不能超过8位!");
            }
            orderProductOrderService.updateById(orderProductOrder);

            log.info("产品订单模块-更新内容:{}",orderProductOrder);

            return ResponseResult.succ("编辑成功");
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("订单号不能重复!");
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    @Transactional
    public ResponseResult save(Principal principal, @Validated @RequestBody OrderProductOrder orderProductOrder)throws Exception {
        LocalDateTime now = LocalDateTime.now();
        orderProductOrder.setCreated(now);
        orderProductOrder.setUpdated(now);
        orderProductOrder.setCreatedUser(principal.getName());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_2);
        orderProductOrder.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1);
        try {

            if(orderProductOrder.getOrderNum()!=null && orderProductOrder.getOrderNum().length() >=9){
                return ResponseResult.fail("订单号不能超过8位!");
            }
            orderProductOrderService.save(orderProductOrder);

            return ResponseResult.succ(ResponseResult.SUCCESS_CODE,"新增成功",orderProductOrder.getId());
        }
        catch (DuplicateKeyException de){
            return ResponseResult.fail("订单号不能重复!");
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取产品订单 分页全部数据
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('order:productOrder:list')")
    public ResponseResult list( String searchField, String searchStatus, String searchStatus2,
                                String searchStatus3,
                                @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<OrderProductOrder> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (!searchField.equals("")) {
            if (searchField.equals("productNum")) {
                queryField = "product_num";
            }
            else if (searchField.equals("productBrand")) {
                queryField = "product_brand";

            }
            else if (searchField.equals("orderNum")) {
                queryField = "order_num";

            }else if (searchField.equals("customerNum")) {
                queryField = "customer_num";

            }else {
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
                    else if (oneField.equals("orderNum")) {
                        theQueryField = "order_num";

                    }else if (oneField.equals("customerNum")) {
                        theQueryField = "customer_num";

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

        List<Long> searchStatusList3 = new ArrayList<Long>();
        if(StringUtils.isNotBlank(searchStatus3)){
            String[] split = searchStatus3.split(",");
            for (String statusVal : split){
                searchStatusList3.add(Long.valueOf(statusVal));
            }
        }
        if(searchStatusList3.size() == 0){
            return ResponseResult.fail("订单状态不能为空");
        }

        pageData = orderProductOrderService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStatusList,searchStatusList2,searchStatusList3,queryMap);

        // 假如有组成结构的往前排
//        LinkedList<OrderProductOrder> newRecords = new LinkedList<>();

        // 标识是否有产品组成结构
        for(OrderProductOrder opo :pageData.getRecords()){
            Long materialBomId = opo.getMaterialBomId();
            opo.setHasProductConstituent(materialBomId !=null); // 标记是否有组成结构
            if(materialBomId==null){
                continue;
            }
            ProduceProductConstituent productConsi = produceProductConstituentService.getById(materialBomId);
            String showStr = productConsi.getProductNum() + productConsi.getProductBrand();

            opo.setMaterialBomName(showStr);

            Long tBomId = opo.getTechnologyBomId();
            if(tBomId!=null){
                ProduceTechnologyBom tBom = produceTechnologyBomService.getById(tBomId);
                String showStr2 = tBom.getProductNum() + tBom.getProductBrand();
                opo.setTechnologyBomName(showStr2);
            }



            int count = produceProductConstituentService.countProductNum(opo.getProductNum());

            opo.setHasProductNum(count > 0);

            // 标识是否已经投产
            List<ProduceBatch> pb = produceBatchService.getByOrderNum(opo.getOrderNum());
            opo.setHasProduction(pb!=null && pb.size() > 0);

        }

        return ResponseResult.succ(pageData);
    }

    /**
     * 提交
     */
    @GetMapping("/statusSubmit")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    public ResponseResult statusSubmit(Principal principal,Long id)throws Exception {

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_2);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("审核通过");
    }

    /**
     * 撤销
     */
    @GetMapping("/statusSubReturn")
    @PreAuthorize("hasAuthority('order:productOrder:save')")
    public ResponseResult statusSubReturn(Principal principal,Long id)throws Exception {

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_1);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("审核通过");
    }

    @PostMapping("/sureBatch")
    @PreAuthorize("hasAuthority('order:productOrder:prepare')")
    public ResponseResult sureBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<OrderProductOrder> lists = new ArrayList<>();

        for (Long id : ids){
            OrderProductOrder old = orderProductOrderService.getById(id);
            if(old.getOrderType()!=null && old.getOrderType().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.ORDER_TYPE_FIELDVALUE_2)){
                return ResponseResult.fail("备料:"+old.getOrderNum()+",订单类型错误，不能是订单取消的");
            }
            if(!old.getStatus().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("备料:"+old.getOrderNum()+",状态错误，要求是是审核通过的");

            }
            if(!old.getPrepared().equals(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_1)){
                return ResponseResult.fail("备料:"+old.getOrderNum()+",备料状态错误，要求是备料未确认的");

            }
            // 假如是不存在组成结构的，也不能确认
            if(old.getMaterialBomId() ==null){
                return ResponseResult.fail("备料:"+old.getOrderNum()+",要求是存在有组成结构的");
            }
            OrderProductOrder orderProductOrder = new OrderProductOrder();
            orderProductOrder.setId(id);
            orderProductOrder.setPrepared(DBConstant.TABLE_ORDER_PRODUCT_ORDER.PREPARED_FIELDVALUE_2);
            lists.add(orderProductOrder);


        }

        orderProductOrderService.updateBatchById(lists);
        return ResponseResult.succ("批量确认通过");
    }

    @PostMapping("/statusPassBatch")
    @PreAuthorize("hasAuthority('order:productOrder:valid')")
    @Transactional
    public ResponseResult statusPassBatch(Principal principal,@RequestBody Long[] ids) {
        ArrayList<OrderProductOrder> lists = new ArrayList<>();

        for (Long id : ids){
            OrderProductOrder orderProductOrder = new OrderProductOrder();
            orderProductOrder.setUpdated(LocalDateTime.now());
            orderProductOrder.setUpdatedUser(principal.getName());
            orderProductOrder.setId(id);
            orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0);


            OrderProductOrder old = orderProductOrderService.getById(id);
            ProduceProductConstituent ppc = produceProductConstituentService.getValidLatestByNumAndBrand(old.getProductNum(), old.getProductBrand());
            if(ppc!=null){
                orderProductOrder.setMaterialBomId(ppc.getId());
            }

            lists.add(orderProductOrder);

        }
        orderProductOrderService.updateBatchById(lists);
        return ResponseResult.succ("审核通过");
    }

    /**
     * 审核通过
     */
    @GetMapping("/statusPass")
    @PreAuthorize("hasAuthority('order:productOrder:valid')")
    public ResponseResult statusPass(Principal principal,Long id)throws Exception {

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_0);

        OrderProductOrder old = orderProductOrderService.getById(id);
        ProduceProductConstituent ppc = produceProductConstituentService.getValidLatestByNumAndBrand(old.getProductNum(), old.getProductBrand());
        if(ppc!=null){
            orderProductOrder.setMaterialBomId(ppc.getId());
        }

        orderProductOrderService.updateById(orderProductOrder);



        return ResponseResult.succ("审核通过");
    }

    /**
     * 反审核
     */
    @GetMapping("/statusReturn")
    @PreAuthorize("hasAuthority('order:productOrder:valid')")
    public ResponseResult statusReturn(Principal principal,Long id)throws Exception {
        // 假如有进度表关联了，不能反审核了。
        List<ProduceOrderMaterialProgress> progresses = produceOrderMaterialProgressService.listByOrderId(id);
        List<Long> needRemovePOMPIds = new ArrayList<>();
        if(progresses!=null && progresses.size() > 0){
            // 假如进度表备料信息都是0，可以反审核，并且删除备料信息
            Boolean isExistPreparedNumflag = false;
            for (ProduceOrderMaterialProgress pomp : progresses){
                String preparedNum = pomp.getPreparedNum();
                if(preparedNum!=null && Double.valueOf(preparedNum).doubleValue() > 0){
                    isExistPreparedNumflag=true;
                }
                needRemovePOMPIds.add(pomp.getId());
            }
            if(isExistPreparedNumflag){
                return ResponseResult.fail("已存在物料报备数量，无法反审核!");
            }
        }

        // 假如有生产序号引用，不能反审核
        OrderProductOrder order = orderProductOrderService.getById(id);

        List<ProduceBatch> pbs = produceBatchService.listByOrderNum(order.getOrderNum());
        if(pbs != null && pbs.size() > 0){
            return ResponseResult.fail("【生产序号模块】已引用该订单号:"+order.getOrderNum());
        }

        // 假如有BOM关联，不能反审核
        if(order.getMaterialBomId()!=null){
            return ResponseResult.fail("【物料BOM】已引用。订单号:"+order.getOrderNum());
        }

        // 删除对应的进度表信息
        if(progresses!=null && progresses.size() > 0 && needRemovePOMPIds.size() > 0) {
            produceOrderMaterialProgressService.removeByIds(needRemovePOMPIds);
        }

        OrderProductOrder orderProductOrder = new OrderProductOrder();
        orderProductOrder.setUpdated(LocalDateTime.now());
        orderProductOrder.setUpdatedUser(principal.getName());
        orderProductOrder.setId(id);
        orderProductOrder.setStatus(DBConstant.TABLE_ORDER_PRODUCT_ORDER.STATUS_FIELDVALUE_3);
        orderProductOrderService.updateById(orderProductOrder);

        return ResponseResult.succ("反审核成功");
    }

}
