package com.nocompanyname.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nocompanyname.lease.model.entity.FeeValue;
import com.nocompanyname.lease.web.admin.service.FeeValueService;
import com.nocompanyname.lease.web.admin.mapper.FeeValueMapper;
import org.springframework.stereotype.Service;


@Service
public class FeeValueServiceImpl extends ServiceImpl<FeeValueMapper, FeeValue>
    implements FeeValueService{

    @Override
    public void removeByFeeKeyId(Long feeKeyId) {
        LambdaQueryWrapper<FeeValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeValue::getFeeKeyId, feeKeyId);
        this.remove(queryWrapper);
        queryWrapper.clear();
        /*
        但在你这个方法里，这句没有必要。因为 queryWrapper 是方法内部的局部变量。
        方法执行完以后，这个 queryWrapper 就不会再被使用了，Java 会等待垃圾回收处理它。
        所以就不需要clear()了。
         */
    }
}




