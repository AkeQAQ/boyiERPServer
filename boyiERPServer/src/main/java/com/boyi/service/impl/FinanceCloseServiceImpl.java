package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceClose;
import com.boyi.entity.RepositoryClose;
import com.boyi.mapper.FinanceCloseMapper;
import com.boyi.service.FinanceCloseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 仓库关账模块 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-02
 */
@Service
public class FinanceCloseServiceImpl extends ServiceImpl<FinanceCloseMapper, FinanceClose> implements FinanceCloseService {

    @Override
    public FinanceClose listLatestOne() {
        List<FinanceClose> lists = this.list(new QueryWrapper<FinanceClose>().orderByDesc(DBConstant.TABLE_FINANCE_CLOSE.CLOSE_DATE_FIELDNAME)) ;
        if(lists != null  && lists.size() != 0){
            return lists.get(0);
        }
        return null;
    }
}
