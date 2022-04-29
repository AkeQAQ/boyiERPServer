package com.boyi.controller;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


}
