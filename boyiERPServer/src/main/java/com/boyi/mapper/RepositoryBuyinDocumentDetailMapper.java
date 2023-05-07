package com.boyi.mapper;

import com.boyi.entity.RepositoryBuyinDocumentDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Repository
public interface RepositoryBuyinDocumentDetailMapper extends BaseMapper<RepositoryBuyinDocumentDetail> {

    @Select("" +
            " select bm.id material_id,bm.name material_name,bm.specs,bm.unit,bs.name supplier_name from " +
            "             (" +
            "             select t.material_id,t.supplier_id from " +
            "             (select rbd.supplier_id,rbdd.material_id,rbdd.price_date from repository_buyin_document rbd ,repository_buyin_document_detail rbdd  where rbd.id = rbdd.document_id   ) t" +
            "             left join " +
            "             base_supplier_material bsm on" +
            "             t.material_id = bsm.material_id and" +
            "             t.supplier_id = bsm.supplier_id and " +
            "             t.price_date >= bsm.start_date and " +
            "             t.price_date <= bsm.end_date " +
            "             where bsm.price is null" +
            "             group by t.material_id,t.supplier_id" +
            "             ) t1,base_material bm ,base_supplier bs" +
            "             where t1.material_id = bm.id " +
            "              and t1.supplier_id = bs.id " +
            "            " +
            "             order by bs.name ")
    List<RepositoryBuyinDocumentDetail> listNoPriceForeignMaterials();

}
