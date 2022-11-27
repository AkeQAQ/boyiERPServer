package com.boyi.service;

import com.boyi.entity.OrderBuyorderDocumentDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryBuyinDocumentDetail;

import java.util.List;

/**
 * <p>
 * 订单模块-采购订单-详情内容 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
public interface OrderBuyorderDocumentDetailService extends IService<OrderBuyorderDocumentDetail> {
    boolean delByDocumentIds(Long[] ids);

    List<OrderBuyorderDocumentDetail> listByDocumentId(Long id);

    // 根据入库单ID 删除
    boolean removeByDocId(Long id);

    int countByMaterialId(String[] ids);
    int countBySupplierId(String ids[]);


    void statusSuccess(Long[] orderDetailIds);
    void statusNotSuccess(List<Long> orderDetailIds);

    List<OrderBuyorderDocumentDetail> getByMaterialIdAndOrderSeq(String materialId, String docNum);

    List<OrderBuyorderDocumentDetail> listNoPriceForeignMaterials();


}
