package com.boyi.service;

import com.boyi.entity.ProduceTechnologyBomDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-04-27
 */
public interface ProduceTechnologyBomDetailService extends IService<ProduceTechnologyBomDetail> {

    void delByDocumentIds(Long[] ids);

    List<ProduceTechnologyBomDetail> listByForeignId(Long id);

    boolean removeByDocId(Long id);
}
