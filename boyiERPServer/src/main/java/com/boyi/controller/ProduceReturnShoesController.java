package com.boyi.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.controller.base.BaseController;
import com.boyi.controller.base.ResponseResult;
import com.boyi.entity.ProduceReturnShoes;
import com.boyi.entity.SysUser;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
@RestController
@RequestMapping("/produce/returnShoes")
public class ProduceReturnShoesController extends BaseController {


    @GetMapping("/list")
    @PreAuthorize("hasAuthority('produce:returnShoes:list')")
    public ResponseResult list(String searchUserName) {

        Page<ProduceReturnShoes> pageData = produceReturnShoesService.pageBySearch(getPage(),searchUserName);

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
