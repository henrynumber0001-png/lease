package com.nocompanyname.lease.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.model.entity.LeaseAgreement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nocompanyname.lease.model.enums.LeaseStatus;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementVo;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {

    IPage<AgreementVo> getPage(long current, long size, AgreementQueryVo queryVo);

    AgreementVo getByLeaseAgreementId(Long id);

    void updateStatusById(Long id, LeaseStatus status);
}
