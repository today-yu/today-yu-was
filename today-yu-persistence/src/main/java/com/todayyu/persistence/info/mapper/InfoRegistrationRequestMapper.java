package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.info.entity.InfoRegistrationRequestJpaEntity;

@Mapper
public class InfoRegistrationRequestMapper {
    /**
     * {@link InfoRegistrationRequest}를 {@link InfoRegistrationRequestJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link InfoRegistrationRequest} 객체
     * @return {@link InfoRegistrationRequestJpaEntity} 객체
     */
    public InfoRegistrationRequestJpaEntity toJpaEntity(InfoRegistrationRequest domainEntity) {
        return InfoRegistrationRequestJpaEntity.builder()
                .requesterId(domainEntity.getRequesterId().getValue())
                .category(domainEntity.getCategory())
                .title(domainEntity.getTitle().getValue())
                .content(domainEntity.getContent().getValue())
                .status(domainEntity.getStatus())
                .infoId(domainEntity.getInfoId() == null ? null : domainEntity.getInfoId().getValue())
                .contactEmail(domainEntity.getContactEmail().getValue())
                .rejectionReason(domainEntity.getRejectionReason() == null ? null : domainEntity.getRejectionReason().getValue())
                .requestedAt(domainEntity.getRequestedAt())
                .build();
    }

    /**
     * {@link InfoRegistrationRequestJpaEntity}를 {@link InfoRegistrationRequest}로 변환하는 메서드
     *
     * @param jpaEntity {@link InfoRegistrationRequestJpaEntity} 객체
     * @return {@link InfoRegistrationRequest} 객체
     */
    public InfoRegistrationRequest toDomain(InfoRegistrationRequestJpaEntity jpaEntity) {
        return InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(jpaEntity.getId()),
                new MemberId(jpaEntity.getRequesterId()),
                jpaEntity.getCategory(),
                new InfoRegistrationRequestTitle(jpaEntity.getTitle()),
                new InfoRegistrationRequestContent(jpaEntity.getContent()),
                jpaEntity.getStatus(),
                jpaEntity.getInfoId() == null ? null : new InfoId(jpaEntity.getInfoId()),
                new Email(jpaEntity.getContactEmail()),
                jpaEntity.getRejectionReason() == null ? null : new RejectionReason(jpaEntity.getRejectionReason()),
                jpaEntity.getRequestedAt()
        );
    }
}
