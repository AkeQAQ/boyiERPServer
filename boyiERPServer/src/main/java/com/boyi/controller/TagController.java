package com.boyi.controller;


import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.boyi.common.constant.DBConstant;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.SysUser;
import com.boyi.entity.Tag;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-11-06
 */
@RestController
@RequestMapping("/tag")
public class TagController extends BaseController {
    @GetMapping("/list")
    public ResponseResult list(Principal principal,Integer type) {
        List<Tag> list = tagService.list(new QueryWrapper<Tag>().eq(DBConstant.TABLE_TAG.TYPE_FIELDNAME, type)
                .eq(DBConstant.TABLE_TAG.CREATED_FIELDNAME, principal.getName())
                .orderByAsc(DBConstant.TABLE_TAG.CREATED_TIME_FIELDNAME));
        /*String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            names[i] = list.get(i).getTagName();
        }*/
        return ResponseResult.succ(list);
    }

    @PostMapping("/save")
    public ResponseResult save(Principal principal,String tagName,Integer type,String searchStr,
                               String searchField, String searchStartDate, String searchEndDate,
                               String searchStatus,@RequestBody List<Map<String,String>> manySearchArr) {
        if(StringUtils.isBlank(tagName) ){
            return ResponseResult.fail("标签名字不能为空");
        }
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setCreated(principal.getName());
        tag.setType(type);
        tag.setCreatedTime(LocalDateTime.now());
        if(StringUtils.isNotBlank(searchStr) && !searchStr.equals("null")){
            tag.setSearchStr(searchStr);
        }
        if(StringUtils.isNotBlank(searchField)  && !searchField.equals("null")){
            tag.setSearchField(searchField);
        }
        if(StringUtils.isNotBlank(searchStartDate) && !searchStartDate.equals("null")){
            LocalDate localDate = LocalDate.parse(searchStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            tag.setSearchStartDate(localDate);
        }if(StringUtils.isNotBlank(searchEndDate) && !searchEndDate.equals("null")){
            LocalDate localDate = LocalDate.parse(searchEndDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            tag.setSearchEndDate(localDate);
        }if(StringUtils.isNotBlank(searchStatus) && !searchStatus.equals("null")){
            tag.setSearchStatus(searchStatus);
        }
        if(manySearchArr!=null && manySearchArr.size() > 0){
            String jsonArray = JSON.toJSONString(manySearchArr);
            tag.setSearchOther(jsonArray);
        }
        tagService.save(tag);
        return ResponseResult.succ("保存成功");
    }
    @GetMapping("/del")
    public ResponseResult del(Principal principal,String tagName,Integer type) {
        String username = principal.getName();

        tagService.remove(new QueryWrapper<Tag>().eq(DBConstant.TABLE_TAG.TYPE_FIELDNAME,type)
                .eq(DBConstant.TABLE_TAG.TAG_NAME_FIELDNAME,tagName)
                .eq(DBConstant.TABLE_TAG.CREATED_FIELDNAME,principal.getName()));
        return ResponseResult.succ("删除成功");
    }
}
