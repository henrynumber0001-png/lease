package com.nocompanyname.lease.web.admin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nocompanyname.lease.model.entity.LeaseAgreement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.nocompanyname.lease.web.admin.vo.agreement.AgreementVo;
import org.apache.ibatis.annotations.Param;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.LeaseAgreement
*/
public interface LeaseAgreementMapper extends BaseMapper<LeaseAgreement> {

    IPage<AgreementVo> getPage(Page<AgreementVo> page, @Param("queryVo") AgreementQueryVo queryVo);
}




