package com.boyi.mapper;

import com.boyi.entity.OrderProductOrder;
import com.boyi.entity.ProduceProductConstituentDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-03-19
 */
@Repository
public interface ProduceProductConstituentDetailMapper extends BaseMapper<ProduceProductConstituentDetail> {

    @Select("select ppc.product_num,ppc.product_brand,ppc.product_color,ppcd.dosage ,bm.id material_id,bm.unit material_unit,bm.name material_name  from" +
            "" +
            " produce_product_constituent ppc , " +
            " produce_product_constituent_detail ppcd  ," +
            "base_material bm" +
            " where ppc.id = ppcd.constituent_id   and ppcd.material_id = bm.id " +
            " and product_num =#{productNum} and product_brand=#{productBrand} and product_color=#{productColor}")
    List<OrderProductOrder> listByNumBrandColor(@Param("productNum") String productNum,@Param("productBrand") String productBrand,@Param("productColor") String productColor);
}
