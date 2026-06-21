package com.nocompanyname.lease.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nocompanyname.lease.common.exception.LeaseException;
import com.nocompanyname.lease.common.result.ResultCodeEnum;
import com.nocompanyname.lease.model.entity.LeaseAgreement;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.admin.service.LeaseAgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.rmi.dgc.Lease;
import java.util.Date;

@Component
public class ScheduleTasks {

    @Autowired
    LeaseAgreementService leaseAgreementService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkLeaseStatus() {
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING)
                .le(LeaseAgreement::getLeaseEndDate, new Date())
                .set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        boolean update = leaseAgreementService.update(updateWrapper);

        if(!update) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }
}

/*
Cron 的 6 个字段: 秒 分 时 日 月 星期

直接看两个例子：
Case1:
0 0 0 ? * MON 表示 每个月的每个星期一 的零点执行（忽略day这个条件，也就是无论周一是几号）
Case2:
0 0 0 1 * ? 表示 每个月的1号这一天 的零点执行（忽略星期这个条件，也就是不论星期几）

? = 不指定（忽略）这个字段的条件
 */
