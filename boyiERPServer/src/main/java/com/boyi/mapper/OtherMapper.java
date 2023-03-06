package com.boyi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boyi.entity.BaseDepartment;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherMapper {
    Long getAutoIncrement(String tableName);
    void alertBuyInAutoIncrement(Long increment);
    void alertBuyOutAutoIncrement(Long increment);
    void alertPickMaterialAutoIncrement(Long increment);
    void alertReturnMaterialAutoIncrement(Long increment);
    void alertBuyOrderAutoIncrement(Long increment);
    void alertEABuyInAutoIncrement(Long increment);
    void alertEAPickMaterialAutoIncrement(Long increment);
    void alertEASendOutGoodsAutoIncrement(Long increment);

}
