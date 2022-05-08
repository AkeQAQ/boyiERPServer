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
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2022-04-29
 */
@RestController
@RequestMapping("/produce/batch")
@Slf4j
public class ProduceBatchController extends BaseController {
    @Value("${poi.produceBatchImportDemoPath}")
    private String poiImportDemoPath;

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:batch:del')")
    public ResponseResult del(@RequestBody Long[] ids) throws Exception{
        try {
            List<ProduceBatch> batches = produceBatchService.listByIds(Arrays.asList(ids));
            ArrayList<Integer> batchIds = new ArrayList<>();
            for (ProduceBatch batch : batches){
                batchIds.add(batch.getBatchId());
            }

            List<RepositoryPickMaterial> picks = repositoryPickMaterialService.listByBatchIds(batchIds);

            if(picks.size() > 0){
                StringBuilder sb = new StringBuilder();
                for (RepositoryPickMaterial pick : picks){
                    sb.append(pick.getBatchId()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                return ResponseResult.fail("生产领料已关联生产序号["+sb.toString()+"]");
            }

            boolean flag = produceBatchService.removeByIds(Arrays.asList(ids));

            log.info("删除【生产序号】信息,ids:{},是否成功：{}",ids,flag?"成功":"失败");
            if(!flag){
                return ResponseResult.fail("删除失败");
            }
            return ResponseResult.succ("删除成功");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("valid")
    @PreAuthorize("hasAuthority('produce:batch:valid')")
    public ResponseResult complementReValid(Long id) throws Exception{
        try {
            ProduceBatch old = produceBatchService.getById(id);
            if(!old.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1)){
                return ResponseResult.fail("补数备料状态不对，已修改，请刷新!");
            }


            produceBatchService.updateStatus(id,DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0);
            return ResponseResult.succ("审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @Transactional
    @PostMapping("reValid")
    @PreAuthorize("hasAuthority('produce:batch:valid')")
    public ResponseResult complementValid(Long id) throws Exception{
        try {
            ProduceBatch old = produceBatchService.getById(id);
            if(!old.getStatus().equals(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0)){
                return ResponseResult.fail("补数备料状态不对，已修改，请刷新!");
            }

            // 假如存在生产领料，不能反审核，不能删除
            ProduceBatch batch = produceBatchService.getById(id);

            List<RepositoryPickMaterial> picks = repositoryPickMaterialService.getSameBatch(null, batch.getBatchId());

            if(picks.size() > 0){
                StringBuilder sb = new StringBuilder();
                for (RepositoryPickMaterial pick : picks){
                    sb.append(pick.getBatchId()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                return ResponseResult.fail("生产领料已关联生产序号["+sb.toString()+"]");
            }

            produceBatchService.updateStatus(id,DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1);
            return ResponseResult.succ("反审核通过!");
        }catch (Exception e){
            log.error("报错.",e);
            throw new RuntimeException("服务器报错");
        }
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult list( String searchField, @RequestBody Map<String,Object> params) {
        Object obj = params.get("manySearchArr");
        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        Page<ProduceBatch> pageData = null;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("batchId")) {
                queryField = "batch_id";
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
                    if (oneField.equals("batchId")) {
                        theQueryField = "batch_id";
                    }else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);


        pageData = produceBatchService.complementInnerQueryByManySearch(getPage(),searchField,queryField,searchStr,queryMap);

        return ResponseResult.succ(pageData);
    }

    /***
     * @param principal
     * @param produceBatch
     * @return
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:batch:save')")
    public ResponseResult save(Principal principal, @Validated @RequestBody ProduceBatch produceBatch) {
        LocalDateTime now = LocalDateTime.now();
        try {
                produceBatch.setCreated(now);
                produceBatch.setUpdated(now);
                produceBatch.setCreatedUser(principal.getName());
                produceBatch.setUpdatedUser(principal.getName());
                produceBatch.setStatus(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_1);
                //判断订单号是否存在
                OrderProductOrder order = orderProductOrderService.getByOrderNum(produceBatch.getOrderNum());
                if(order == null){
                    return ResponseResult.fail("【产品订单】不存在该订单号:"+produceBatch.getOrderNum());
                }
            produceBatchService.save(produceBatch);
            return ResponseResult.succ("新增成功");
        }
        catch (DuplicateKeyException e2){
            log.error("报错",e2);
            throw new RuntimeException("生产序号不能重复!");
        }
        catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }/***
     * @param principal
     * @param produceBatch
     * @return
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:batch:update')")
    public ResponseResult update(Principal principal, @Validated @RequestBody ProduceBatch produceBatch) {
        LocalDateTime now = LocalDateTime.now();
        try {

            produceBatch.setUpdated(now);
            produceBatch.setUpdatedUser(principal.getName());
            //判断订单号是否存在
            OrderProductOrder order = orderProductOrderService.getByOrderNum(produceBatch.getOrderNum());
            if(order == null){
                return ResponseResult.fail("【产品订单】不存在该订单号:"+produceBatch.getOrderNum());
            }
            produceBatchService.updateById(produceBatch);
            return ResponseResult.succ("修改成功");
        }
        catch (DuplicateKeyException e2){
            log.error("报错",e2);
            throw new RuntimeException("生产序号不能重复!");
        }
        catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }


    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryById(Long id ) {
        ProduceBatch pb = produceBatchService.getById(id);
        return ResponseResult.succ(pb);
    }

    @GetMapping("/queryCaiDuanPrintById")
    @PreAuthorize("hasAuthority('produce:batch:list')")
    public ResponseResult queryCaiDuanPrintById(Long id ) {

        try{
            HashMap<String, Object> theMap = new HashMap<>();

            List<HashMap<String, Object>> onePageLists = new ArrayList<>();

            HashMap<String, Object> onePage = new HashMap<>();

            ArrayList<HashMap<String, String>> subLists = new ArrayList<>();

            onePageLists.add(onePage);
            onePage.put("subList",subLists);



            ProduceBatch pb = produceBatchService.getById(id);
            // 1. 查询订单信息
            OrderProductOrder order = orderProductOrderService.getByOrderNum(pb.getOrderNum());
            if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                pb.setSize40("0");
            }
            if(pb.getSize41()==null||pb.getSize41().isEmpty()){
                pb.setSize41("0");
            }
            if(pb.getSize42()==null||pb.getSize42().isEmpty()){
                pb.setSize42("0");
            }if(pb.getSize43()==null||pb.getSize43().isEmpty()){
                pb.setSize43("0");
            }if(pb.getSize44()==null||pb.getSize44().isEmpty()){
                pb.setSize44("0");
            }if(pb.getSize45()==null||pb.getSize45().isEmpty()){
                pb.setSize45("0");
            }if(pb.getSize46()==null||pb.getSize46().isEmpty()){
                pb.setSize46("0");
            }if(pb.getSize47()==null||pb.getSize47().isEmpty()){
                pb.setSize47("0");
            }if(pb.getSize40()==null||pb.getSize40().isEmpty()){
                pb.setSize40("0");
            }if(pb.getSize39()==null||pb.getSize39().isEmpty()){
                pb.setSize39("0");
            }if(pb.getSize38()==null||pb.getSize38().isEmpty()){
                pb.setSize38("0");
            }if(pb.getSize37()==null||pb.getSize37().isEmpty()){
                pb.setSize37("0");
            }if(pb.getSize36()==null||pb.getSize36().isEmpty()){
                pb.setSize36("0");
            }if(pb.getSize35()==null||pb.getSize35().isEmpty()){
                pb.setSize35("0");
            }if(pb.getSize34()==null||pb.getSize34().isEmpty()){
                pb.setSize34("0");
            }

            onePage.put("productNum",order.getProductNum());
            onePage.put("productBrand",order.getProductBrand());
            onePage.put("productColor",order.getProductColor());

            onePage.put("batchId",pb.getBatchId());
            onePage.put("customerNum",order.getCustomerNum());
            onePage.put("size34",pb.getSize34());
            onePage.put("size35",pb.getSize35());
            onePage.put("size36",pb.getSize36());
            onePage.put("size37",pb.getSize37());
            onePage.put("size38",pb.getSize38());
            onePage.put("size39",pb.getSize39());
            onePage.put("size40",pb.getSize40());
            onePage.put("size41",pb.getSize41());
            onePage.put("size42",pb.getSize42());
            onePage.put("size43",pb.getSize43());
            onePage.put("size44",pb.getSize44());
            onePage.put("size45",pb.getSize45());
            onePage.put("size46",pb.getSize46());
            onePage.put("size47",pb.getSize47());
            BigDecimal theTotalNum = new BigDecimal(pb.getSize34()).add(new BigDecimal(pb.getSize35())).add(new BigDecimal(pb.getSize36()))
                    .add(new BigDecimal(pb.getSize37())).add(new BigDecimal(pb.getSize38())).add(new BigDecimal(pb.getSize39()))
                    .add(new BigDecimal(pb.getSize40())).add(new BigDecimal(pb.getSize41())).add(new BigDecimal(pb.getSize42()))
                    .add(new BigDecimal(pb.getSize43())).add(new BigDecimal(pb.getSize44())).add(new BigDecimal(pb.getSize45()))
                    .add(new BigDecimal(pb.getSize46())).add(new BigDecimal(pb.getSize47()));
            onePage.put("totalNum",theTotalNum.toString());


            // 2. 查询组成结构
            List<OrderProductOrder> details = produceProductConstituentDetailService.listByNumBrand(order.getProductNum(), order.getProductBrand());
            for (OrderProductOrder detail : details){
                String materialId = detail.getMaterialId();
                // 筛选皮料，
                if(materialId.startsWith("01.")){
                    HashMap<String, String> theSub = new HashMap<>();
                    theSub.put("materialId",materialId);
                    theSub.put("materialName",detail.getMaterialName());
                    theSub.put("dosage",detail.getDosage());

                    // 计算用量，
                    theSub.put("needNum",calOneOrderNeedNum(pb,detail.getDosage()));
                    subLists.add(theSub);
                }
            }

            theMap.put("rowList",onePageLists);
            return ResponseResult.succ(theMap);
        }catch (Exception e){
            log.error("报错",e);
            throw new RuntimeException(e.getMessage());
        }

    }

    private String calOneOrderNeedNum(ProduceBatch pb, String dosage) {
        String theRadio = "0.04";
        BigDecimal bd41 = BigDecimalUtil.mul(
                        BigDecimalUtil.add(dosage,
                                BigDecimalUtil.mul(theRadio,"1").toString()
                        ).toString()
                ,pb.getSize41());
        BigDecimal bd42 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"2").toString()
                ).toString()
                ,pb.getSize42());

        BigDecimal bd43 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"3").toString()
                ).toString()
                ,pb.getSize43() );

        BigDecimal bd44 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"4").toString()
                ).toString()
                ,pb.getSize44() );

        BigDecimal bd45 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"5").toString()
                ).toString()
                ,pb.getSize45() );

        BigDecimal bd46 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"6").toString()
                ).toString()
                ,pb.getSize46() );

        BigDecimal bd47 = BigDecimalUtil.mul(
                BigDecimalUtil.add(dosage,
                        BigDecimalUtil.mul(theRadio,"7").toString()
                ).toString()
                ,pb.getSize47() );


        BigDecimal bd39 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"1").toString()
                ).toString()
                ,pb.getSize39() );

        BigDecimal bd38 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"2").toString()
                ).toString()
                ,pb.getSize38() );
        BigDecimal bd37 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"3").toString()
                ).toString()
                ,pb.getSize37() );
        BigDecimal bd36 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"4").toString()
                ).toString()
                ,pb.getSize36() );
        BigDecimal bd35 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"5").toString()
                ).toString()
                ,pb.getSize35() );
        BigDecimal bd34 = BigDecimalUtil.mul(
                BigDecimalUtil.sub(dosage,
                        BigDecimalUtil.mul(theRadio,"6").toString()
                ).toString()
                ,pb.getSize34());

        BigDecimal bd40 = BigDecimalUtil.mul(dosage,pb.getSize40());

        return bd34.add(bd35).add(bd36).add(bd37).add(bd38).add(bd39).add(bd40).add(bd41)
                .add(bd42).add(bd43).add(bd44).add(bd45).add(bd46).add(bd47).toString();
    }

    /**
     * 上传
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('produce:batch:import')")
    public ResponseResult upload(Principal principal, MultipartFile[] files) {
        MultipartFile file = files[0];

        log.info("上传内容: files:{}",file);
        ExcelImportUtil<ProduceBatch> utils = new ExcelImportUtil<ProduceBatch>(ProduceBatch.class);
        List<ProduceBatch> batches = null;
        try (InputStream fis = file.getInputStream();){
            batches = utils.readExcel(fis, 1, 0,-1,null);
            log.info("解析的excel数据:{}",batches);


            if(batches == null || batches.size() == 0){
                return ResponseResult.fail("解析内容未空");
            }
            ArrayList<Map<String,String>> errorMsgs = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();
            HashSet<String> orderNums = new HashSet<>();


            for (ProduceBatch batch: batches){
                LocalDateTime now = LocalDateTime.now();
                batch.setCreated(now);
                batch.setUpdated(now);
                batch.setCreatedUser(principal.getName());
                batch.setUpdatedUser(principal.getName());
                batch.setStatus(DBConstant.TABLE_PRODUCE_BATCH.BATCH_STATUS_FIELDVALUE_0);

                ids.add(batch.getBatchId());
                orderNums.add(batch.getOrderNum());
            }

            List<ProduceBatch> exist = produceBatchService.list(new QueryWrapper<ProduceBatch>().in(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME, ids));
            if(exist != null && !exist.isEmpty()){
                for (ProduceBatch existOne:exist){
                    HashMap<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("content","生产序号："+existOne.getBatchId()+"已存在");
                    errorMsgs.add(errorMsg);
                }
                return ResponseResult.succ(errorMsgs);
            }

            List<OrderProductOrder> orders = orderProductOrderService.listByOrderNums(orderNums);
            if(orders==null || orders.size()==0){
                return ResponseResult.fail("没查到任何订单号");
            }else{
                Set<String> dbOrderNums = new HashSet<>();

                for (OrderProductOrder order : orders){
                    dbOrderNums.add(order.getOrderNum());
                }
                for (String uploadOrderNum : orderNums){
                    if(!dbOrderNums.contains(uploadOrderNum)){
                        HashMap<String, String> errorMsg = new HashMap<>();
                        errorMsg.put("content","订单号："+uploadOrderNum+"不存在");
                        errorMsgs.add(errorMsg);
                    }
                }

                if(errorMsgs.size() > 0){
                    return ResponseResult.succ(errorMsgs);
                }
            }
            produceBatchService.saveBatch(batches);
        }
        catch (Exception e) {
            if( e instanceof  DuplicateKeyException){
                return ResponseResult.fail("生产序号重复！");
            }
            log.error("发生错误:",e);
            throw new RuntimeException(e.getMessage());
        }

        return ResponseResult.succ("上传成功");
    }

    @PostMapping("/down")
    @PreAuthorize("hasAuthority('produce:batch:save')")
    public ResponseResult down(HttpServletResponse response, Long id)throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + new String("生产序号导入模板".getBytes("ISO8859-1")));
        response.setHeader("filename","生产序号导入模板" );

        FileInputStream fis = new FileInputStream(new File(poiImportDemoPath));
        FileCopyUtils.copy(fis,response.getOutputStream());
        return ResponseResult.succ("下载成功");
    }

}
