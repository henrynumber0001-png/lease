package com.nocompanyname.lease.web.app.service;

import com.nocompanyname.lease.model.entity.LeaseAgreement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementDetailVo;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementItemVo;
import com.nocompanyname.lease.web.app.vo.agreement.AgreementRenewVo;

import java.util.List;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {
    List<AgreementItemVo> listItem();

    AgreementDetailVo getDetailById(Long id);

    void updateStatusById(Long id, LeaseStatus leaseStatus);


    void renew(AgreementRenewVo renewVo);
}
