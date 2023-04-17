package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryInOutDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository

public interface ExternalAccountRepositoryInOutDetailMapper extends BaseMapper<RepositoryInOutDetail> {

    String querySql = "" +
            "select * from (" +
            "" +
            " select 1 as type_order,-1 as status,qichu.material_id as material_id,qichu.material_name as material_name,null as date,null as doc_name,null as doc_num," +
            " null as unit,qichu.qichunum as start_num ,null as add_num,null as sub_num" +
            " from " +

            "("+
            "select material_id,material_name,(IFNULL(t.initnum,0)+ IFNULL(t.isum,0)-IFNULL(t.psum,0)) as qichunum " +
            "from  " +
            " " +
            "( " +
            "select mat.id material_id,mat.material_name ,initS.num initnum, buyin.sum isum,pick.sum psum from  " +
            " " +
            "( " +
            "select id,name material_name " +
            "  from  " +
            "  external_account_base_material " +
            "   " +
            ") mat " +
            "left join " +
            " " +
            "( " +
            "select sum(rbdd.radio_num) sum,rbdd.material_id " +
            "  from  " +
            "  external_account_repository_buyin_document rbd,external_account_repository_buyin_document_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.buy_in_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") buyin " +
            "on mat.id = buyin.material_id " +

            " " +
            "left join " +
            "( " +
            "select sum(rbdd.num) sum,rbdd.material_id " +
            "  from  " +
            "  external_account_repository_pick_material rbd,external_account_repository_pick_material_detail rbdd  " +
            "  where rbd.id = rbdd.document_id " +
            "  and rbd.pick_date <   #{startDate} " +
            "group by rbdd.material_id " +
            ") pick " +
            "on mat.id = pick.material_id " +

            " left join " +
            "( " +
            "select  num,material_id " +
            "  from  " +
            "  external_account_sys_init_stock " +
            " " +
            ") initS " +
            "on mat.id = initS.material_id " +
            " " +
            " " +
            ")t order by t.material_id"+
            " )qichu"+

            " UNION ALL" +
            "" +
            " (select 2 as type_order,rbd.status as status,bm.id as material_id,bm.name as material_name,rbd.buy_in_date as date,'采购入库' as doc_name,rbdd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,rbdd.radio_num as add_num,null as sub_num" +
            " from " +
            " external_account_base_material bm, external_account_repository_buyin_document_detail rbdd,external_account_repository_buyin_document rbd" +
            " where rbd.id = rbdd.document_id and bm.id = rbdd.material_id and rbd.buy_in_date >= #{startDate} and rbd.buy_in_date <= #{endDate} " +
            " order by rbdd.id asc )" +
            "" +
            " UNION ALL" +
            "" +
            " (select 3 as type_order,rpm.status as status,bm.id as material_id,bm.name as material_name,rpm.pick_date as date,'生产领料' as doc_name,rpmd.document_id as doc_num," +
            " bm.unit as unit,null as start_num ,null as add_num,rpmd.num as sub_num" +
            " from " +
            " external_account_base_material bm, external_account_repository_pick_material_detail rpmd,external_account_repository_pick_material rpm" +
            " where rpm.id=rpmd.document_id and bm.id = rpmd.material_id and rpm.pick_date >= #{startDate} and rpm.pick_date <= #{endDate}" +
            ")" +
            "" +
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
