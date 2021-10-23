package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryInOutDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface RepositoryInOutDetailMapper extends BaseMapper<RepositoryInOutDetail> {

    String querySql = "" +
            "select * from (" +
            "" +
            " select 1 as type_order,-1 as status,bm.id as material_id,bm.name as material_name,null as date,null as doc_name,null as doc_num," +
            " null as unit,rsh.num as start_num ,null as add_num,null as sub_num" +
            " from " +
            " base_material bm " +
            " left join repository_stock_history rsh" +
            " on bm.id = rsh.material_id and rsh.date = DATE_SUB(#{startDate},INTERVAL 1 DAY) " +
            "" +
            " UNION ALL" +
            "" +
            " (select 2 as type_order,rbd.status as status,bm.id as material_id,bm.name as material_name,rbd.buy_in_date as date,'采购入库' as doc_name,rbdd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,rbdd.num as add_num,null as sub_num" +
            " from " +
            " base_material bm, repository_buyin_document_detail rbdd,repository_buyin_document rbd" +
            " where rbd.id = rbdd.document_id and bm.id = rbdd.material_id and rbd.buy_in_date >= #{startDate} and rbd.buy_in_date <= #{endDate} " +
//            " and rbd.status in" +
//            " <script> <foreach collection='#{statusList}' item='item' open='(' separator=',' close=')' >#{item} </foreach> </script>" +
            " order by rbdd.id asc )" +
            "" +
            " UNION ALL" +
            "" +
            " (select 3 as type_order,rpm.status as status,bm.id as material_id,bm.name as material_name,rpm.pick_date as date,'生产领料' as doc_name,rpmd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,null as add_num,rpmd.num as sub_num" +
            " from " +
            " base_material bm, repository_pick_material_detail rpmd,repository_pick_material rpm" +
            " where rpm.id=rpmd.document_id and bm.id = rpmd.material_id and rpm.pick_date >= #{startDate} and rpm.pick_date <= #{endDate}" +
//            " and rpm.status in" +
//            " <script> <foreach collection='#{statusList}' item='item' open='(' separator=',' close=')' >#{item} </foreach> </script>" +
            ")" +
            "" +
            "" +
            " UNION ALL" +
            "" +
            " (select 4 as type_order,rbd.status as status,bm.id as material_id,bm.name as material_name,rbd.buy_out_date as date,'采购退料' as doc_name,rbdd2.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,null as add_num,rbdd2.num as sub_num" +
            " from " +
            " base_material bm, repository_buyout_document_detail rbdd2,repository_buyout_document rbd" +
            " where rbd.id=rbdd2.document_id and bm.id = rbdd2.material_id and rbd.buy_out_date >= #{startDate} and rbd.buy_out_date <= #{endDate} " +
//            " and rbd.status in" +
//            " <script> <foreach collection='#{statusList}' item='item' open='(' separator=',' close=')' >#{item} </foreach> </script>" +
            " order by rbdd2.id asc )" +
            "" +
            "" +
            "" +
            "UNION ALL" +
            "" +
            " (select 5 as type_order,rrm.status as status,bm.id as material_id,bm.name as material_name,rrm.return_date as date,'生产退料' as doc_name,rrmd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,rrmd.num as add_num,null as sub_num" +
            " from " +
            " base_material bm, repository_return_material_detail rrmd,repository_return_material rrm" +
            " where rrm.id=rrmd.document_id and bm.id = rrmd.material_id and rrm.return_date >= #{startDate} and rrm.return_date <= #{endDate} " +
//            " and rrm.status in " +
//            " <script> <foreach collection='#{statusList}' item='item' open='(' separator=',' close=')' >#{item} </foreach> </script>" +
            ")" +
            "" +
            "" +
            " UNION ALL" +
            "" +
            " (select 6 as type_order,rc.status as status,bm.id as material_id,bm.name as material_name,rc.check_date as date,'盘点' as doc_name,rcd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,rcd.change_num as add_num,null as sub_num" +
            " from " +
            " base_material bm, repository_check_detail rcd,repository_check rc" +
            " where rc.id=rcd.document_id and bm.id = rcd.material_id and rc.check_date >= #{startDate} and rc.check_date <= #{endDate} " +
//            " and rc.status in" +
//            " <script> <foreach collection='#{statusList}' item='item' open='(' separator=',' close=')' >#{item} </foreach> </script>" +
            ")" +
            "" +
            "" +
            "" +
            ")t order by t.material_id asc,date asc,type_order asc"+
            "";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryInOutDetail> page(Page page, @Param("ew") Wrapper queryWrapper, @Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

}
