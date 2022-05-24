package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.vo.RealDosageVO;
import com.boyi.entity.ProduceProductConstituent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.RepositoryReturnMaterial;
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
public interface ProduceProductConstituentMapper extends BaseMapper<ProduceProductConstituent> {


    String querySql = "" +
            "select  * from produce_product_constituent";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceProductConstituent> page(Page page, @Param("ew") Wrapper queryWrapper);

    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<ProduceProductConstituent> list(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    ProduceProductConstituent one(@Param("ew") Wrapper queryWrapper);

    String query2Sql = "" +
            " select  ppc.*,bm.name material_name from produce_product_constituent ppc,produce_product_constituent_detail ppcd,base_material bm" +
            " where ppc.id = ppcd.constituent_id and ppcd.material_id = bm.id";
    String wrapper2Sql = "SELECT * from ( " + query2Sql + " ) AS q ${ew.customSqlSegment}";

    @Select(wrapper2Sql)
    Page<ProduceProductConstituent> page2(Page page, @Param("ew") Wrapper queryWrapper);

    @Select("" +
            "select t1.*,t2.return_num," +
            "             (cast(t1.num as decimal(6,2)) - IFNULL(0,cast(t2.return_num as decimal(6,2))))  / cast(batchNum as decimal(6,2))    real_dosage " +
            "  from " +
            "             (" +
            "            select opo.order_num,opo.product_num ,opo.product_brand,opo.order_number,pb.batch_id,ppcd.material_id,bm.name material_name,rpmd.num ,(select IFNULL(size34,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)" +
            " +IFNULL(size41,0)+IFNULL(size42,0)+IFNULL(size43,0)+IFNULL(size44,0)+IFNULL(size45,0)+IFNULL(size46,0)" +
            "+IFNULL(size47,0) from produce_batch where batch_id = pb.batch_id) batchNum" +
            "             from " +
            "            order_product_order opo ," +
            "            produce_product_constituent ppc," +
            "            produce_product_constituent_detail ppcd," +
            "            produce_batch pb," +
            "            repository_pick_material rpm," +
            "            repository_pick_material_detail rpmd ," +
            "            base_material bm " +
            "            where opo.product_num = ppc.product_num" +
            "            and opo.product_brand = ppc.product_brand" +
            "            and ppc.id = #{id}" +
            "            and ppc.id = ppcd.constituent_id" +
            "            and ppcd.material_id = rpmd.material_id" +
            "            and ppcd.material_id like '01.%'" +
            "            and opo.order_num = pb.order_num" +
            "            and pb.batch_id = rpm.batch_id" +
            "            and rpm.id = rpmd.document_id" +
            "            and rpmd.material_id = bm.id" +
            "            )t1 left join" +
            "            (" +
            "            select rrmd.material_id,rrm.batch_id ,IFNULL(rrmd.num,0) return_num from" +
            "            repository_return_material rrm," +
            "            repository_return_material_detail rrmd " +
            "            where rrm.id = rrmd.document_id" +
            "            ) t2" +
            "            on t1.material_id = t2.material_id " +
            "            and t1.batch_id = t2.batch_id" +
            "            order by order_num desc")
    List<RealDosageVO> listRealDosageById(@Param("id") Long id);

    @Select("" +
            "select t1.*,t2.return_num," +
            "             (cast(t1.num as decimal(6,2)) - IFNULL(0,cast(t2.return_num as decimal(6,2))))  / cast(batchNum as decimal(6,2))    real_dosage " +
            "  from " +
            "             (" +
            "            select opo.order_num,opo.product_num ,opo.product_brand,opo.order_number,pb.batch_id,ppcd.material_id,bm.name material_name ,ppcd.dosage plan_dosage,rpmd.num ,(select IFNULL(size34,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)" +
            " +IFNULL(size41,0)+IFNULL(size42,0)+IFNULL(size43,0)+IFNULL(size44,0)+IFNULL(size45,0)+IFNULL(size46,0)" +
            "+IFNULL(size47,0) from produce_batch where batch_id = pb.batch_id) batchNum" +
            "             from " +
            "            order_product_order opo ," +
            "            produce_product_constituent ppc," +
            "            produce_product_constituent_detail ppcd," +
            "            produce_batch pb," +
            "            repository_pick_material rpm," +
            "            repository_pick_material_detail rpmd ," +
            "            base_material bm " +
            "            where opo.product_num = ppc.product_num" +
            "            and opo.product_brand = ppc.product_brand" +
            "            and ppc.id = ppcd.constituent_id" +
            "            and ppcd.material_id = rpmd.material_id" +
            "            and ppcd.material_id like '01.%'" +
            "            and opo.order_num = pb.order_num" +
            "            and pb.batch_id = rpm.batch_id" +
            "            and rpm.id = rpmd.document_id" +
            "            and rpmd.material_id = bm.id" +
            "            )t1 left join" +
            "            (" +
            "            select rrmd.material_id,rrm.batch_id ,IFNULL(rrmd.num,0) return_num from" +
            "            repository_return_material rrm," +
            "            repository_return_material_detail rrmd " +
            "            where rrm.id = rrmd.document_id" +
            "            ) t2" +
            "            on t1.material_id = t2.material_id " +
            "            and t1.batch_id = t2.batch_id" +
            "            order by order_num desc")
    List<RealDosageVO> listRealDosage();

}
