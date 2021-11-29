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
            " select 1 as type_order,-1 as status,qichu.material_id as material_id,qichu.material_name as material_name,null as date,null as doc_name,null as doc_num," +
            " null as unit,qichu.qichunum as start_num ,null as add_num,null as sub_num" +
            " from " +

            /*
            " base_material bm " +
            " left join repository_stock_history rsh" +
            " on bm.id = rsh.material_id and rsh.date = DATE_SUB(#{startDate},INTERVAL 1 DAY) " +*/
            "("+
            "select material_id,material_name,(IFNULL(t.initnum,0)+  IFNULL(t.csum,0) + IFNULL(t.isum,0)+IFNULL(t.rsum,0)-IFNULL(t.osum,0)-IFNULL(t.psum,0)) as qichunum " +
            "from  " +
            " " +
            "( " +
            "select mat.id material_id,mat.material_name ,initS.num initnum, buyin.sum isum,buyOut.sum osum,pick.sum psum,ret.sum rsum,clos.sum csum from  " +
            " " +
            "( " +
            "select id,name material_name " +
            "  from  " +
            "  base_material " +
            "   " +
            ") mat " +
            "left join " +
            " " +
            "( " +
            "select sum(rbdd.radio_num) sum,rbdd.material_id " +
            "  from  " +
            "  repository_buyin_document rbd,repository_buyin_document_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.buy_in_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") buyin " +
            "on mat.id = buyin.material_id " +
            "left join " +
            " " +
            "( " +
            "select sum(rbdd.radio_num) sum,rbdd.material_id " +
            "  from  " +
            "  repository_buyout_document rbd,repository_buyout_document_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.buy_out_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") buyOut " +
            "on mat.id = buyOut.material_id " +
            " " +
            "left join " +
            "( " +
            "select sum(rbdd.num) sum,rbdd.material_id " +
            "  from  " +
            "  repository_pick_material rbd,repository_pick_material_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.pick_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") pick " +
            "on mat.id = pick.material_id " +
            " " +
            "left join " +
            "( " +
            "select sum(rbdd.num) sum,rbdd.material_id " +
            "  from  " +
            "  repository_return_material rbd,repository_return_material_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.return_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") ret " +
            "on mat.id = ret.material_id " +
            " " +
            "left join " +
            "( " +
            "select  num,material_id " +
            "  from  " +
            "  sys_init_stock " +
            " " +
            ") initS " +
            "on mat.id = initS.material_id " +
            " " +
            "left join " +
            "( " +
            "select sum(rbdd.change_num) sum,rbdd.material_id " +
            "  from  " +
            "  repository_check rbd,repository_check_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.check_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") clos " +
            "on mat.id = clos.material_id " +
            " " +
            ")t order by t.material_id"+
            " )qichu"+

            " UNION ALL" +
            "" +
            " (select 2 as type_order,rbd.status as status,bm.id as material_id,bm.name as material_name,rbd.buy_in_date as date,'采购入库' as doc_name,rbdd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,rbdd.radio_num as add_num,null as sub_num" +
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
            " bm.unit as unit,null as start_num ,null as add_num,rbdd2.radio_num as sub_num" +
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
