package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseMaterialSameGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-11-14
 */
@Repository
public interface BaseMaterialSameGroupMapper extends BaseMapper<BaseMaterialSameGroup> {

    String querySql = "select bmsg.*,bmsgd.material_id,bm.name material_name from base_material_same_group bmsg ," +
            " base_material_same_group_detail bmsgd," +
            " base_material bm  " +
            " where bmsg.id = bmsgd.group_id" +
            " and bmsgd.material_id = bm.id";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";

    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<BaseMaterialSameGroup> page(Page page,@Param("ew") QueryWrapper<BaseMaterialSameGroup> queryWrapper);

}
