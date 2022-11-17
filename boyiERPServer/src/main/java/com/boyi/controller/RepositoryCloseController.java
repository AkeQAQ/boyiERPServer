package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.*;
import com.boyi.mapper.RepositoryInOutDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/repository/close")
public class RepositoryCloseController extends BaseController {

    @Autowired
    private RepositoryInOutDetailMapper repositoryInOutDetailMapper;

    /**
     * 获取部门 分页全部数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('repository:close:list')")
    public ResponseResult list() {
        List<RepositoryClose> list = repositoryCloseService.list(new QueryWrapper<RepositoryClose>()
                .orderByDesc(DBConstant.TABLE_REPOSITORY_CLOSE.CLOSE_DATE_FIELDNAME));
        return ResponseResult.succ(list);
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('repository:close:save')")
    public ResponseResult save(Principal principal,@Validated @RequestBody RepositoryClose repositoryClose) {
        LocalDateTime now = LocalDateTime.now();
        String name = principal.getName();
        repositoryClose.setUpdateUser(name);
        repositoryClose.setCreatedUser(name);
        repositoryClose.setUpdated(now);
        repositoryClose.setCreated(now);
        // 判断在该日期之前（包含该日期），存在未审核的单据
        List<RepositoryBuyinDocument> buyIns = repositoryBuyinDocumentService.countLTByCloseDate(repositoryClose.getCloseDate());
        List<RepositoryBuyoutDocument> outs = repositoryBuyoutDocumentService.countLTByCloseDate(repositoryClose.getCloseDate());

        List<RepositoryPickMaterial> picks = repositoryPickMaterialService.countLTByCloseDate(repositoryClose.getCloseDate());
        List<RepositoryReturnMaterial> returns = repositoryReturnMaterialService.countLTByCloseDate(repositoryClose.getCloseDate());

        List<RepositoryStock> stocks = repositoryStockService.listStockNumLTZero();
        List<Map<String,String>> strList = new ArrayList<Map<String,String>>();

        if( buyIns.size() > 0  ||  outs.size() > 0 ||  picks.size() > 0 ||  returns.size() > 0 || stocks.size() > 0){

            for (RepositoryBuyinDocument obj:buyIns){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[采购入库单]["+obj.getId()+"]");
                strList.add(map);
            }
            for (RepositoryBuyoutDocument obj:outs){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[采购退料单]["+obj.getId()+"]");
                strList.add(map);
            }
            for (RepositoryPickMaterial obj:picks){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[生产领料单]["+obj.getId()+"]");
                strList.add(map);
            }
            for (RepositoryReturnMaterial obj:returns){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在未审核通过的"+"[生产退料单]["+obj.getId()+"]");
                strList.add(map);
            }
            for (RepositoryStock obj:stocks){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在库存数目是负数的"+"[库存]["+obj.getMaterialId()+"]");
                strList.add(map);
            }


            return ResponseResult.succ(200,"存在未审核通过的单据",strList);
        }


        // 不能出现根据时间顺序排序，有日期当天有负库存的现象
        /*LocalDate closeDate = repositoryClose.getCloseDate();
        LocalDate startDate = closeDate.plusDays(1L);
        LocalDate endDate = LocalDate.of(2100, 01, 01);
        Page page1 = getPage();
        page1.setSize(10000000L);
        Page<RepositoryInOutDetail> page = repositoryInOutDetailMapper.page(page1, new QueryWrapper<RepositoryBuyinDocument>()
                ,startDate, endDate);
        List<RepositoryInOutDetail> records = page.getRecords();
        HashSet<String> errorMaterialIds = new HashSet<>();
        for (int i = 0; i < records.size(); i++) {
            RepositoryInOutDetail current = records.get(i);
            Integer typeOrder = current.getTypeOrder();
            if(typeOrder == 1){// 1:期初，2：采购入库，3:生产领料，4：采购退料，5：生产退料，6：盘点
                current.setAfterNum(current.getStartNum());
                // 结存出现负数，则错误返回提示
                if(current.getAfterNum() <0){
                    errorMaterialIds.add(current.getMaterialId());
                }

                continue;
            }
            RepositoryInOutDetail lastOne = records.get(i - 1);
            if(typeOrder == 2 || typeOrder == 5 || typeOrder == 6){
                Double afterNum = lastOne.getAfterNum();
                if(afterNum == null){
                    afterNum = 0D;
                }
                current.setAfterNum(afterNum+current.getAddNum());
            }else if(typeOrder == 3 || typeOrder == 4){
                Double afterNum = lastOne.getAfterNum();
                if(afterNum == null){
                    afterNum = 0D;
                }
                current.setAfterNum(afterNum - current.getSubNum());
            }
            // 结存出现负数，则错误返回提示
            if(current.getAfterNum() <0){
                errorMaterialIds.add(current.getMaterialId());
            }
        }

        if(errorMaterialIds.size() > 0){
            for (String materialId : errorMaterialIds){
                HashMap<String, String> map = new HashMap<>();
                map.put("content","存在历史库存数目是负数的"+"[历史库存]["+materialId+"]");
                strList.add(map);
            }

            return ResponseResult.succ(200,"历史库存负数的物料",strList);
        }*/


        repositoryCloseService.save(repositoryClose);
        return ResponseResult.succ("新增成功");
    }

    @GetMapping("/return")
    @PreAuthorize("hasAuthority('repository:close:return')")
    public ResponseResult returnLast(Principal principal) {
        LocalDateTime now = LocalDateTime.now();
        List<RepositoryClose> lists = repositoryCloseService.list(new QueryWrapper<RepositoryClose>().orderByDesc(DBConstant.TABLE_REPOSITORY_CLOSE.CLOSE_DATE_FIELDNAME));
        if(lists.size() <= 1){
            return ResponseResult.fail("历史<=1条记录，无法回退");
        }
        repositoryCloseService.removeById(lists.get(0).getId());// 删除最近的一条

        return ResponseResult.succ("回退成功");
    }
}
