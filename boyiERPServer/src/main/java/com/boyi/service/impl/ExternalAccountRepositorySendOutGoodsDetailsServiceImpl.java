package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountRepositoryBuyinDocumentDetail;
import com.boyi.entity.ExternalAccountRepositorySendOutGoodsDetails;
import com.boyi.entity.ExternalAccountRepositorySendOutGoodsDetails;
import com.boyi.mapper.ExternalAccountRepositorySendOutGoodsDetailsMapper;
import com.boyi.service.ExternalAccountRepositorySendOutGoodsDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-05
 */
@Service
public class ExternalAccountRepositorySendOutGoodsDetailsServiceImpl extends ServiceImpl<ExternalAccountRepositorySendOutGoodsDetailsMapper, ExternalAccountRepositorySendOutGoodsDetails> implements ExternalAccountRepositorySendOutGoodsDetailsService {

    @Override
    public List<ExternalAccountRepositorySendOutGoodsDetails> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<ExternalAccountRepositorySendOutGoodsDetails>().eq(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS_DETAILS.SEND_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS_DETAILS.ID_FIELDNAME));
    }

    @Override
    public List<ExternalAccountRepositorySendOutGoodsDetails> listByForeignId(Long id) {
        return this.list(new QueryWrapper<ExternalAccountRepositorySendOutGoodsDetails>()
                .eq(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS_DETAILS.SEND_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS_DETAILS.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long id) {
        return this.remove(
                new QueryWrapper<ExternalAccountRepositorySendOutGoodsDetails>()
                        .eq(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS_DETAILS.SEND_ID_FIELDNAME, id));
    }

    @Override
    public void delByDocumentIds(Long[] ids) {
        this.remove(new QueryWrapper<ExternalAccountRepositorySendOutGoodsDetails>()
                .in(DBConstant.TABLE_EXTERNAL_ACCOUNT_REPOSITORY_SEND_OUT_GOODS_DETAILS.SEND_ID_FIELDNAME, ids));
    }
}
