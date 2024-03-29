package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ProduceBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.RepositoryBuyinDocument;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2022-04-29
 */
@Repository
public interface ProduceBatchMapper extends BaseMapper<ProduceBatch> {

    String querySql =
            " select pb.*,opo.product_num,opo.product_brand,opo.order_type,opo.end_date,(select rpm.id from repository_pick_material rpm where rpm.batch_id = pb.batch_id and rpm.department_id=4) zc_pick_id " +
                    "  from " +
                    "             produce_batch pb inner join order_product_order opo on pb.order_num = opo.order_num" +
                    "            order by created desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";

    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<ProduceBatch> page(Page page, @Param("ew") Wrapper queryWrapper);

    @Select("select sum(IFNULL(size34,0)+IFNULL(size35,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)+IFNULL(size41,0)+IFNULL(size42,0)+IFNULL(size43,0)+IFNULL(size44,0)+IFNULL(size45,0)+IFNULL(size46,0)+IFNULL(size47,0)) from produce_batch pb " +
            "where pb.batch_id like CONCAT(#{preBatchId},'%') ")
    Long sumByBatchIdPre(@Param("preBatchId") String preBatchId);

    @Select("select opo.end_date,opo.order_type, pbp.cost_of_labour_type_id,pb.id ,pbp.id produce_batch_progress_id,pbp.is_accept,pbp.cost_of_labour_type_name,colt.seq, opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbp.out_date,pbp.send_foreign_product_date,pbp.back_foreign_product_date,pbp.supplier_name,pbp.material_name " +
            " from produce_batch pb inner join" +
            " order_product_order opo on pb.order_num = opo.order_num " +
            " inner join produce_batch_progress pbp " +
            " on pb.id = pbp.produce_batch_id inner join  cost_of_labour_type colt on pbp.cost_of_labour_type_id = colt.id" +
            " where date_format(pbp.out_date,'%Y-%m-%d') = #{searchQueryOutDateStr}")
    List<ProduceBatch> listByOutDate(@Param("searchQueryOutDateStr") String searchQueryOutDateStr);

    @Select("select opo.end_date,opo.order_type,pbp.cost_of_labour_type_id,pb.id ,pbp.id produce_batch_progress_id,pbp.is_accept,pbp.cost_of_labour_type_name,colt.seq,opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbp.out_date,pbp.send_foreign_product_date,pbp.back_foreign_product_date,pbp.supplier_name,pbp.material_name " +
            " from produce_batch pb inner join" +
            " order_product_order opo on pb.order_num = opo.order_num " +
            " inner join produce_batch_progress pbp " +
            " on pb.id = pbp.produce_batch_id inner join  cost_of_labour_type colt on pbp.cost_of_labour_type_id = colt.id" +
            " where pbp.out_date is null")
    List<ProduceBatch> listByOutDateIsNull();

    @Select(
            "select opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbd.material_name from produce_batch pb inner join " +
            " order_product_order opo on pb.order_num = opo.order_num" +
            " left join produce_batch_delay pbd on pb.id = pbd.produce_batch_id where pbd.material_name = #{name}")
    List<ProduceBatch> listByMaterialName(@Param("name") String name);

    @Select("" +
            "select opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbd.material_name from produce_batch pb inner join " +
            " order_product_order opo on pb.order_num = opo.order_num" +
            " left join produce_batch_delay pbd on pb.id = pbd.produce_batch_id" +
            " where pbd.material_name is null ")
    List<ProduceBatch> listByMaterialNameIsNull();


    @Select("" +
            " select opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbd.material_name from produce_batch pb inner join " +
            " order_product_order opo on pb.order_num = opo.order_num" +
            " left join produce_batch_delay pbd on pb.id = pbd.produce_batch_id" +
            " where pbd.material_name is not null and pbd.date is  null ")
    List<ProduceBatch> listDelay();

    @Select("select opo.end_date,opo.order_type,pbp.cost_of_labour_type_id,pb.id ,pbp.id produce_batch_progress_id,pbp.is_accept,pbp.cost_of_labour_type_name,colt.seq, opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbp.out_date,pbp.send_foreign_product_date,pbp.back_foreign_product_date,pbp.supplier_name,pbp.material_name " +
            " from produce_batch pb inner join" +
            " order_product_order opo on pb.order_num = opo.order_num " +
            " inner join produce_batch_progress pbp " +
            " on pb.id = pbp.produce_batch_id inner join  cost_of_labour_type colt on pbp.cost_of_labour_type_id = colt.id" +
            " where date_format(pbp.out_date,'%Y-%m-%d') = #{searchQueryOutDateStr} and date_format(pb.created,'%Y-%m-%d') >= #{dataDate}")
    List<ProduceBatch> listByOutDateDataDate(@Param("searchQueryOutDateStr") String searchQueryOutDateStr,@Param("dataDate")  String dataDate);

    @Select(" select opo.end_date,opo.order_type,pbp.cost_of_labour_type_id, pb.id ,pbp.id produce_batch_progress_id,pbp.is_accept,pbp.cost_of_labour_type_name,colt.seq,opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbp.out_date,pbp.send_foreign_product_date,pbp.back_foreign_product_date,pbp.supplier_name,pbp.material_name " +
            " from produce_batch pb inner join" +
            " order_product_order opo on pb.order_num = opo.order_num " +
            " inner join produce_batch_progress pbp " +
            " on pb.id = pbp.produce_batch_id inner join  cost_of_labour_type colt on pbp.cost_of_labour_type_id = colt.id" +
            " where pbp.out_date is null and date_format(pb.created,'%Y-%m-%d') >= #{dataDate}")
    List<ProduceBatch> listByOutDateIsNullWithDataDate(@Param("dataDate") String dataDate);

    @Select("<script> select sum(IFNULL(size34,0)+IFNULL(size35,0)+IFNULL(size36,0)+IFNULL(size37,0)+IFNULL(size38,0)+IFNULL(size39,0)+IFNULL(size40,0)+IFNULL(size41,0)+IFNULL(size42,0)+IFNULL(size43,0)+IFNULL(size44,0)+IFNULL(size45,0)+IFNULL(size46,0)+IFNULL(size47,0)) from produce_batch pb " +
            "where pb.batch_id in <foreach collection='batchIdPres' index='index' item='item' open='(' separator=',' close=')'>#{item}</foreach>  </script> ")
    Double sumByBatchIdPres(@Param("batchIdPres") Set<String> batchIdPres);


    @Select(
                    "select pzg.group_name, t1.* from " +
                    "(" +
                    "select pbp.zc_group_id," +
                    " opo.end_date,opo.order_type," +
                    " pb.id ,pbp.id produce_batch_progress_id,pbp.is_accept," +
                    " opo.order_num,pb.batch_id,opo.product_num,opo.product_brand,pbp.out_date,pbp.send_date send_foreign_product_ate" +
                    "             from produce_batch pb inner join" +
                    "             order_product_order opo on pb.order_num = opo.order_num " +
                    "             left join produce_batch_zc_progress pbp " +
                    "             on pb.id = pbp.produce_batch_id " +
                    " " +
                    "             where  date_format(pb.created,'%Y-%m-%d') >= #{dataDate}" +
                    " ) t1" +
                    " left join produce_zc_group pzg " +
                    " on t1.zc_group_id = pzg.id ")
    List<ProduceBatch> listByWithZCDataDate(@Param("dataDate")String date);
}
