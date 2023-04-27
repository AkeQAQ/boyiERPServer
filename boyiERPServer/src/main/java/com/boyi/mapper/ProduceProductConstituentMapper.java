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

import java.time.LocalDate;
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
            "select ppc.* " +
            " from produce_product_constituent ppc ";
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
            " select  ppc.*,bm.name material_name " +
            " from produce_product_constituent ppc,produce_product_constituent_detail ppcd,base_material bm" +
            " where ppc.id = ppcd.constituent_id and ppcd.material_id = bm.id";
    String wrapper2Sql = "SELECT * from ( " + query2Sql + " ) AS q ${ew.customSqlSegment}";

    @Select(wrapper2Sql)
    Page<ProduceProductConstituent> page2(Page page, @Param("ew") Wrapper queryWrapper);

    @Select("" +
            "select t1.*,t2.return_num," +
            "             (cast(t1.num as decimal(6,2)) - IFNULL(0,cast(t2.return_num as decimal(6,2))))  / cast(batchNum as decimal(6,2))    real_dosage " +
            "  from " +
            "             (" +
            "            select opo.order_num,opo.product_num ,opo.product_brand,opo.order_number,pb.batch_id,ppcd.material_id,ppcd.dosage plan_dosage,bm.name material_name,rpmd.num ,(select IFNULL(size34,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)" +
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
            "            where opo.material_bom_id = ppc.id" +
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
            " SELECT" +
            " t1.*," +
            " t2.return_num," +
            " (" +
            " cast(" +
            " t1.num AS DECIMAL ( 6, 2 )) - IFNULL(" +
            " 0," +
            " cast(" +
            " t2.return_num AS DECIMAL ( 6, 2 )))) / cast(" +
            " batchNum AS DECIMAL ( 6, 2 )) real_dosage " +
            " FROM" +
            " (" +
            " SELECT" +
            " opo.order_num," +
            " opo.product_num," +
            " opo.product_brand," +
            " opo.order_number," +
            " pb.batch_id," +
            " ppcd.material_id," +
            " bm.NAME material_name," +
            " (" +
            " SELECT" +
            " IFNULL( size34, 0 )+ IFNULL( size36, 0 )+ IFNULL( size37, 0 )+ IFNULL( size38, 0 )+ IFNULL( size39, 0 )+ IFNULL( size40, 0 ) + IFNULL( size41, 0 )+ IFNULL( size42, 0 )+ IFNULL( size43, 0 )+ IFNULL( size44, 0 )+ IFNULL( size45, 0 )+ IFNULL( size46, 0 ) + IFNULL( size47, 0 ) " +
            " FROM" +
            " produce_batch " +
            " WHERE" +
            " batch_id = pb.batch_id " +
            " ) batchNum," +
            " rpmd.num," +
            " ( SELECT " +
            " cast(IFNULL( size34, 0 ) * (ppcd.dosage - 0.24) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size35, 0 ) * (ppcd.dosage - 0.20) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size36, 0 ) * (ppcd.dosage - 0.16) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size37, 0 ) * (ppcd.dosage - 0.12) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size38, 0 ) * (ppcd.dosage - 0.08) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size39, 0 ) * (ppcd.dosage - 0.04) AS DECIMAL ( 6, 2 )) +" +
            " cast(IFNULL( size40, 0 ) * ppcd.dosage  AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size41, 0 ) * (ppcd.dosage + 0.04) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size42, 0 ) * (ppcd.dosage + 0.08) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size43, 0 ) * (ppcd.dosage + 0.12) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size44, 0 ) * (ppcd.dosage + 0.16) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size45, 0 ) * (ppcd.dosage + 0.2) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size46, 0 ) * (ppcd.dosage + 0.24) AS DECIMAL ( 6, 2 ))+" +
            " cast(IFNULL( size47, 0 ) * (ppcd.dosage + 0.28) AS DECIMAL ( 6, 2 )) FROM produce_batch WHERE batch_id = pb.batch_id ) caiduan_plan_pick_num," +
            " ppcd.dosage plan_dosage" +
            " FROM" +
            " order_product_order opo," +
            " produce_product_constituent ppc," +
            " produce_product_constituent_detail ppcd," +
            " produce_batch pb," +
            " repository_pick_material rpm," +
            " repository_pick_material_detail rpmd," +
            " base_material bm " +
            " WHERE" +
            " opo.material_bom_id = ppc.id " +
            " AND ppc.id = ppcd.constituent_id " +
            " AND ppcd.material_id = rpmd.material_id " +
            " AND ppcd.material_id LIKE '01.%' " +
            "  AND opo.order_num = pb.order_num " +
            "  AND pb.batch_id = rpm.batch_id " +
            "  AND rpm.id = rpmd.document_id " +
            "  AND rpmd.material_id = bm.id " +
            " ) t1" +
            " LEFT JOIN (" +
            " SELECT" +
            "  rrmd.material_id," +
            "  rrm.batch_id," +
            "  IFNULL( rrmd.num, 0 ) return_num " +
            " FROM" +
            "  repository_return_material rrm," +
            "  repository_return_material_detail rrmd " +
            " WHERE" +
            "  rrm.id = rrmd.document_id " +
            " ) t2 ON t1.material_id = t2.material_id " +
            " AND t1.batch_id = t2.batch_id " +
            " ORDER BY" +
            " order_num DESC")
    List<RealDosageVO> listRealDosage();


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
            "            where opo.material_bom_id = ppc.id" +
            "            and ppc.id = ppcd.constituent_id" +
            "            and ppcd.material_id = rpmd.material_id" +
            "            and ppcd.material_id like '01.%'" +
            "            and opo.order_num = pb.order_num" +
            "            and pb.batch_id = rpm.batch_id" +
            "            and rpm.id = rpmd.document_id" +
            "            and rpm.pick_date >=#{searchStartDate} and rpm.pick_date <= #{searchEndDate}" +
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
    List<RealDosageVO> listRealDosageBetweenDate(@Param("searchStartDate") String searchStartDate, @Param("searchEndDate") String searchEndDate);

    @Select("select count(1) from order_product_order opo ," +
            " produce_batch pb," +
            " repository_pick_material rpm ," +
            " repository_pick_material_detail rpmd " +
            " where opo.order_num = pb.order_num" +
            " and rpm.batch_id = pb.batch_id" +
            " and rpm.id=rpmd.document_id" +
            " and opo.product_num = #{productNum} and opo.product_brand=#{productBrand}" +
            " and rpmd.material_id = #{materialId}" +
            " and rpm.pick_date > #{localDate}")
    Long countPickMaterialRows(@Param("productNum")String productNum,@Param("productBrand") String productBrand,@Param("materialId") String materialId,@Param("localDate") LocalDate localDate);

    @Select("select distinct(product_num) product_num from produce_product_constituent")
    List<ProduceProductConstituent> listDistinctProductNum();

}
