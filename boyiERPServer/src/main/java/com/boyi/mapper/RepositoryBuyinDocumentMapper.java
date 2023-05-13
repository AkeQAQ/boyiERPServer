package com.boyi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.AnalysisMaterailVO;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BaseSupplierMaterialCopy;
import com.boyi.entity.RepositoryBuyinDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单据表 Mapper 接口
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Repository

public interface RepositoryBuyinDocumentMapper extends BaseMapper<RepositoryBuyinDocument> {

    String querySql = "" +
            "select t.*,(sm.price * num) amount ,sm.price price from (" +
            "select  doc.id id, " +
            "        doc.buy_in_date , " +
            "        doc.supplier_document_num , " +
            "        doc.status, " +
            "        doc.source_type, " +
            "        doc.updated , " +
            "        doc.updated_user , " +
            "        sup.name supplier_name, " +
            "        sup.id supId, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.big_unit , " +
            "        docD.order_seq," +
            "        docD.num," +
            "        docD.order_id , " +
            "        docD.id detail_id , " +
            "        docD.price_date  " +
            " from " +
            "                        repository_buyin_document doc , " +
            "                        repository_buyin_document_detail docD, " +
            "                        base_supplier sup, " +
            "                        base_material m " +
            " " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id " +
            ") t " +
            "left join base_supplier_material sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date order by id desc,detail_id desc";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";

    // 特殊核算用户，特殊SQL
    String queryZZWSql = "" +
            "select t.*,(sm.price * num) amount ,sm.price price from (" +
            "select  doc.id id, " +
            "        doc.buy_in_date , " +
            "        doc.supplier_document_num , " +
            "        doc.status, " +
            "        doc.source_type, " +
            "        doc.updated , " +
            "        doc.updated_user , " +
            "        sup.name supplier_name, " +
            "        sup.id supId, " +
            "        m.id material_id, " +
            "        m.name material_name, " +
            "        m.unit , " +
            "        m.big_unit , " +
            "        docD.order_seq," +
            "        docD.num," +
            "        docD.order_id , " +
            "        docD.id detail_id , " +
            "        docD.price_date  " +
            " from " +
            "                        repository_buyin_document doc , " +
            "                        repository_buyin_document_detail docD, " +
            "                        base_supplier sup, " +
            "                        base_material m " +
            " " +
            "            where doc.supplier_id = sup.id and " +
            "                  doc.id = docD.document_id and " +
            "                  docD.material_id = m.id " +
            ") t " +
            "left join base_supplier_material_copy sm " +
            "on sm.status=0 and t.material_id = sm.material_id and supId = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date order by id desc,detail_id desc";
    String wrapperZZWSql = "SELECT * from ( " + queryZZWSql + " ) AS q ${ew.customSqlSegment}";

    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<RepositoryBuyinDocument> page(Page page, @Param("ew") Wrapper queryWrapper);

    // 特殊核算用户，特殊SQL
    /**
     * 分页查询ZZW
     */
    @Select(wrapperZZWSql)
    Page<RepositoryBuyinDocument> pageZZW(Page page, @Param("ew") Wrapper queryWrapper);


    /**
     * 普通查询
     */
    @Select(wrapperSql)
    List<RepositoryBuyinDocument> list(@Param("ew") Wrapper queryWrapper);

    // 特殊核算用户，特殊SQL
    /**
     * 普通查询
     */
    @Select(wrapperZZWSql)
    List<RepositoryBuyinDocument> listZZW(@Param("ew") Wrapper queryWrapper);

    /**
     * 单独查询
     */
    @Select(wrapperSql)
    RepositoryBuyinDocument one(@Param("ew") Wrapper queryWrapper);


    @Select("select count(1) from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where rbd.status = 0 and rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId} and rbdd.price_date between #{startDate} and #{endDate}")
    Integer getSupplierMaterialPassBetweenDate(BaseSupplierMaterial baseSupplierMaterial);

    // 特殊核算用户，特殊SQL

    @Select("select count(1) from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where rbd.status = 0 and rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId} and rbdd.price_date between #{startDate} and #{endDate}")
    Integer getSupplierMaterialCopyPassBetweenDate(BaseSupplierMaterialCopy baseSupplierMaterial);

    @Select("select sum(num) from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where  rbd.id = rbdd.document_id and rbd.supplier_id = #{supplierId}" +
            " and rbdd.material_id = #{materialId}")
    Double getSumNumBySupplierIdAndMaterialId(@Param("supplierId") String supplierId,@Param("materialId")String materialId);

    @Select("select rbd.id id, rbd.status status,rbdd.material_id material_id,rbdd.radio_num num from repository_buyin_document rbd," +
            "              repository_buyin_document_detail rbdd" +
            " where rbd.source_type = 1 and rbd.id = rbdd.document_id " +
            " and rbd.buy_in_date between #{startDate} and #{endDate}")
    List<RepositoryBuyinDocument> getListFromOrderBetweenDate(@Param("startDate")LocalDate startDate, @Param("endDate")LocalDate endDate);

    @Select("" +
            " select CONVERT(" +
            " (" +
            " select IFNULL(sum(num),0) from " +
            " " +
            " repository_buyin_document rbd," +
            " repository_buyin_document_detail rbdd" +
            " where rbd.id = rbdd.document_id" +
            " and rbd.buy_in_date >= #{startDate} and rbd.buy_in_date < #{endDate}"+
            " and rbdd.material_id=#{materialId}" +
            "" +
            " ) -" +
            " (" +
            " select IFNULL(sum(num),0)from " +
            "" +
            " repository_buyout_document rbd," +
            " repository_buyout_document_detail rbdd" +
            " where rbd.id = rbdd.document_id" +
            " and rbd.buy_out_date >= #{startDate} and rbd.buy_out_date < #{endDate}"+
            " and rbdd.material_id=#{materialId}" +
            "" +
            ")"+
            ",DECIMAL(9,2)) as num"

    )
    RepositoryBuyinDocument getNetInFromOrderBetweenDate(@Param("startDate")LocalDate startD,@Param("endDate") LocalDate endD,@Param("materialId")String materialId);


    @Select("select bs2.name supplier_name,t3.sum from ( " +
            " select supplier_id,sum(amount) sum from (" +
            "" +
            " select t.supplier_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " repository_buyin_document rbd ," +
            " repository_buyin_document_detail rbdd," +
            " base_supplier bs  ," +
            " base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +
            " ) t" +
            " left join base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by supplier_id "+
            " ) t3 ,base_supplier bs2 " +
            " where t3.supplier_id = bs2.id and sum is not null order by sum desc")
    List<AnalysisMaterailVO> listSupplierAmountPercent(@Param("searchStartDate") String searchStartDate,@Param("searchEndDate") String searchEndDate);

    @Select("select bs2.name supplier_name,t3.sum from ( " +
            " select supplier_id,sum(amount) sum from (" +
            "" +
            " select t.supplier_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " repository_buyin_document rbd ," +
            " repository_buyin_document_detail rbdd," +
            " base_supplier bs  ," +
            " base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and bs.group_code = #{searchField}"+
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +
            " ) t" +
            " left join base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by supplier_id "+
            " ) t3 ,base_supplier bs2 " +
            " where t3.supplier_id = bs2.id and sum is not null order by sum desc")
    List<AnalysisMaterailVO> listSupplierAmountPercentBySupType(@Param("searchStartDate") String searchStartDate,@Param("searchEndDate")  String searchEndDate,@Param("searchField") String searchField);

    @Select("" +
            " select bm2.name material_name,t3.sum from (" +
            " select material_id,sum(amount) sum from (" +
            " select t.material_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " repository_buyin_document rbd ," +
            " repository_buyin_document_detail rbdd," +
            " base_supplier bs  ," +
            " base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +

            " ) t" +
            " left join base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by material_id " +
            " ) t3 ,base_material bm2 " +
            " where t3.material_id = bm2.id and t3.sum is not  null  order by sum desc")
    List<AnalysisMaterailVO> listMaterialAmountPercent(@Param("searchStartDate")String searchStartDate, @Param("searchEndDate") String searchEndDate);

    @Select("" +
            " select bm2.name material_name,t3.sum from (" +
            " select material_id,sum(amount) sum from (" +
            " select t.material_id,(sm.price * num) amount" +
            " from (" +
            " select rbd.supplier_id  supplier_id,rbdd.material_id,rbdd.num,rbdd.price_date from " +
            " repository_buyin_document rbd ," +
            " repository_buyin_document_detail rbdd," +
            " base_supplier bs  ," +
            " base_material bm" +
            " where rbd.id = rbdd.document_id " +
            " and rbd.supplier_id = bs.id" +
            " and rbdd.material_id = bm.id " +
            " and bm.group_code = #{searchField}"+
            " and rbd.buy_in_date >= #{searchStartDate}  and rbd.buy_in_date <= #{searchEndDate} " +

            " ) t" +
            " left join base_supplier_material sm" +
            " on sm.status=0 and t.material_id = sm.material_id and t.supplier_id = sm.supplier_id" +
            " and t.price_date between  sm.start_date and sm.end_date " +
            " ) t2 group by material_id " +
            " ) t3 ,base_material bm2 " +
            " where t3.material_id = bm2.id and t3.sum is not  null  order by sum desc")
    List<AnalysisMaterailVO> listMaterialAmountPercentByMaterialType(@Param("searchStartDate")String searchStartDate, @Param("searchEndDate")String searchEndDate,@Param("searchField") String searchField);

    @Select("" +
            " select rbdd.material_id, cast( sum( rbdd.num )  as decimal(14,5)) totalNum  from " +
            " repository_buyin_document rbd," +
            " repository_buyin_document_detail rbdd" +
            " where " +
            " rbd.id = rbdd.document_id " +
            " and rbd.buy_in_date > #{endDate}" +
            " group by rbdd.material_id")
    List<RepositoryBuyinDocument> listGTEndDate(@Param("endDate") String endDate);

    @Select(
            " select t2.supplier_id,sum(material_amount) total_amount from " +
                    "             (" +
                    "             select t1.supplier_id,t1.material_id,t1.price_date,t1.total_num,bsm.price," +
                    "              (cast(t1.total_num as decimal(11,4))*(cast(bsm.price as decimal(11,4)) ) )   material_amount from" +
                    "             (" +
                    "             select rbd.supplier_id,rbdd.material_id,rbdd.price_date,sum(rbdd.num) total_num from repository_buyin_document rbd," +
                    "             repository_buyin_document_detail rbdd " +
                    "             where rbd.id = rbdd.document_id" +
                    "             and rbd.buy_in_date >=#{startDate} and rbd.buy_in_date <= #{endDate}" +
                    "             group by rbd.supplier_id,rbdd.material_id,rbdd.price_date" +
                    "             ) t1,base_supplier_material bsm " +
                    "             where t1.supplier_id = bsm.supplier_id" +
                    "             and t1.material_id = bsm.material_id" +
                    "             and t1.price_date >= bsm.start_date " +
                    "             and t1.price_date <= bsm.end_date   " +
                    "             ) t2 group by t2.supplier_id ")
    List<RepositoryBuyinDocument> getSupplierTotalAmountBetweenDate(@Param("startDate") LocalDate startDateTime,@Param("endDate") LocalDate endDateTime);

    @Select(" select t2.material_id,sum(material_amount) total_amount,sum(total_num) total_num from " +
            "             (" +
            "             select t1.supplier_id,t1.material_id,t1.buy_in_date,t1.total_num,bsm.price," +
            "              (cast(t1.total_num as decimal(11,4))*(cast(bsm.price as decimal(11,4)) ) )   material_amount from" +
            "             (" +
            "             select rbd.supplier_id,rbdd.material_id,rbd.buy_in_date,sum(rbdd.num) total_num from repository_buyin_document rbd," +
            "             repository_buyin_document_detail rbdd " +
            "             where rbd.id = rbdd.document_id and rbdd.material_id like '10.01.%'" +
            "             " +
            "             group by rbd.supplier_id,rbdd.material_id,rbd.buy_in_date" +
            "             ) t1,base_supplier_material bsm " +
            "             where t1.supplier_id = bsm.supplier_id" +
            "             and t1.material_id = bsm.material_id" +
            "             and t1.buy_in_date >= bsm.start_date " +
            "             and t1.buy_in_date <= bsm.end_date" +
            "             ) t2 group by t2.material_id ")
    List<RepositoryBuyinDocument> getMaterialTotalAmountByShoeLast();
}
