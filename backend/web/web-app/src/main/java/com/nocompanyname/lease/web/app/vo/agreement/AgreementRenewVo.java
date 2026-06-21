package com.nocompanyname.lease.web.app.vo.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "续约租约")
public class AgreementRenewVo {
    //前端传原租约id，方便后端查找原租约
    private Long oldAgreementId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseEndDate;

    private Long leaseTermId;

    private Long paymentTypeId;

    private String additionalInfo;
}
