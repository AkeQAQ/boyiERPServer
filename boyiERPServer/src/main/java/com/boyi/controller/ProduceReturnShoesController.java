package com.boyi.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.utils.ExcelExportUtil;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.BaseDepartment;
import com.boyi.entity.ProduceReturnShoes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
@Slf4j
@RestController
@RequestMapping("/produce/returnShoes")
public class ProduceReturnShoesController extends BaseController {
    @Value("${poi.produceReturnShoesDemoPath}")
    private String poiDemoPath;

    @PostMapping("/export")
    public void export(HttpServletResponse response, String searchField, String searchStartDate,String searchType, String searchEndDate,
                       @RequestBody Map<String,Object> params) {

        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("userName")) {
                queryField = "user_name";
            }
            else if (searchField.equals("packageNo")) {
                queryField = "package_no";

            }else if (searchField.equals("userArtNo")) {
                queryField = "user_art_no";

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
                    if (oneField.equals("userName")) {
                        theQueryField = "user_name";
                    }
                    else if (oneField.equals("packageNo")) {
                        theQueryField = "package_no";

                    }else if (oneField.equals("userArtNo")) {
                        theQueryField = "user_art_no";

                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
        List<String> searchStatusList = new ArrayList<String>();
        if(StringUtils.isNotBlank(searchType)){
            String[] split = searchType.split(",");
            for (String statusVal : split){
                searchStatusList.add(statusVal);
            }
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);
        Page page = getPage();
        if(page.getSize()==10 && page.getCurrent() == 1){
            page.setSize(1000000L); // 导出全部的话，简单改就一页很大一个条数
        }
        Page<ProduceReturnShoes> pageData = produceReturnShoesService.innerQueryByManySearch(page,searchField,queryField,searchStr,searchStartDate,searchEndDate, searchStatusList, queryMap);

        //加载模板流数据
        try (FileInputStream fis = new FileInputStream(poiDemoPath);){
            new ExcelExportUtil(ProduceReturnShoes.class,1,0).export("","TCX",response,fis,pageData.getRecords(),"报表.xlsx",new HashMap<Integer,String>());
        } catch (Exception e) {
            log.error("导出模块报错.",e);
        }
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('produce:returnShoes:list')")
    public ResponseResult list(String searchField, String searchStartDate, String searchEndDate,String searchType,@RequestBody Map<String,Object> params) {

        Object obj = params.get("manySearchArr");
        String searchStr = params.get("searchStr")==null?"":params.get("searchStr").toString();

        List<Map<String,String>> manySearchArr = (List<Map<String, String>>) obj;
        List<String> ids = new ArrayList<>();
        String queryField = "";
        if (searchField != "") {
            if (searchField.equals("userName")) {
                queryField = "user_name";
            }
            else if (searchField.equals("packageNo")) {
                queryField = "package_no";

            }else if (searchField.equals("userArtNo")) {
                queryField = "user_art_no";

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
                    if (oneField.equals("userName")) {
                        theQueryField = "user_name";
                    }
                    else if (oneField.equals("packageNo")) {
                        theQueryField = "package_no";

                    }else if (oneField.equals("userArtNo")) {
                        theQueryField = "user_art_no";

                    } else {
                        continue;
                    }
                    queryMap.put(theQueryField,oneStr);
                }
            }
        }
        List<String> searchStatusList = new ArrayList<String>();
        if(StringUtils.isNotBlank(searchType)){
            String[] split = searchType.split(",");
            for (String statusVal : split){
                searchStatusList.add(statusVal);
            }
        }

        if(searchStatusList.size() == 0){
            return ResponseResult.fail("类型不能为空");
        }

        log.info("搜索字段:{},对应ID:{}", searchField,ids);

        Page<ProduceReturnShoes> pageData = produceReturnShoesService.innerQueryByManySearch(getPage(),searchField,queryField,searchStr,searchStartDate,searchEndDate,searchStatusList,queryMap);

        return ResponseResult.succ(pageData);
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('produce:returnShoes:save')")
    public ResponseResult save(@Validated @RequestBody ProduceReturnShoes produceReturnShoes) {
        LocalDateTime now = LocalDateTime.now();
        produceReturnShoes.setCreated(now);
        produceReturnShoes.setUpdated(now);
        produceReturnShoesService.save(produceReturnShoes);
        return ResponseResult.succ("新增成功");
    }
    @GetMapping("/queryById")
    @PreAuthorize("hasAuthority('produce:returnShoes:list')")
    public ResponseResult queryById(Long id) {
        ProduceReturnShoes produceReturnShoes = produceReturnShoesService.getById(id);
        if(produceReturnShoes.getDepartmentId()!=null){
            BaseDepartment bd = baseDepartmentService.getById(produceReturnShoes.getDepartmentId());
            produceReturnShoes.setDepartmentName(bd.getName());
        }
        return ResponseResult.succ(produceReturnShoes);
    }


    @PostMapping("/update")
    @PreAuthorize("hasAuthority('produce:returnShoes:update')")
    public ResponseResult update(@Validated @RequestBody ProduceReturnShoes produceReturnShoes) {
        produceReturnShoes.setUpdated(LocalDateTime.now());
        produceReturnShoesService.updateById(produceReturnShoes);
        return ResponseResult.succ("编辑成功");
    }

    @Transactional
    @PostMapping("/del")
    @PreAuthorize("hasAuthority('produce:returnShoes:del')")
    public ResponseResult delete(@RequestBody Long[] ids) {

        produceReturnShoesService.removeByIds(Arrays.asList(ids));

        return ResponseResult.succ("删除成功");
    }
}
