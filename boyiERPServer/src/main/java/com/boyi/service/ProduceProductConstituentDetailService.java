package com.boyi.service;

import com.boyi.entity.ProduceProductConstituentDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryReturnMaterialDetail;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
public interface ProduceProductConstituentDetailService extends IService<ProduceProductConstituentDetail> {
    boolean delByDocumentIds(Long[] ids);

    List<ProduceProductConstituentDetail> listByForeignId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);

}
