package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountBaseMaterial;
import com.boyi.entity.ExternalAccountBaseMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountBaseMaterialService extends IService<ExternalAccountBaseMaterial> {

    Integer countByGroupCode(String groupCode);

    Page<ExternalAccountBaseMaterial> pageByGroupCode(Page page, String searchStr);

    Page<ExternalAccountBaseMaterial> pageBySearch(Page page,String queryField, String searchStr);

    List<ExternalAccountBaseMaterial> listSame(String name, String unit, String specs, String groupCode);

    List<ExternalAccountBaseMaterial> listSameExcludSelf(String name, String unit, String specs, String groupCode, String id);

    List<ExternalAccountBaseMaterial> getLowWarningLines();

    void updateNull(ExternalAccountBaseMaterial baseMaterial);

    List<ExternalAccountBaseMaterial> listSame(String name, String unit, String groupCode);

    List<ExternalAccountBaseMaterial> listSameExcludSelf(String name, String unit, String groupCode, String id);

    void updateNullWithField(ExternalAccountBaseMaterial bm,String videoUrlFieldname);
}
