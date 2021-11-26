package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceReturnShoes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.SysUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2021-11-26
 */
public interface ProduceReturnShoesService extends IService<ProduceReturnShoes> {
    Page<ProduceReturnShoes> pageBySearch(Page page, String searchUserName);

}
