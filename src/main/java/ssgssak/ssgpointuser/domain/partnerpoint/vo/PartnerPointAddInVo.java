package ssgssak.ssgpointuser.domain.partnerpoint.vo;

import lombok.Getter;
import ssgssak.ssgpointuser.domain.partnerpoint.entity.PartnerType;

@Getter
public class PartnerPointAddInVo {
    private Long pointId;
    private PartnerType type;
}
