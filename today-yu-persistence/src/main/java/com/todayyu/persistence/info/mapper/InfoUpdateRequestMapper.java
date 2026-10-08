package com.todayyu.persistence.info.mapper;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.RejectionReason;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;

@Mapper
public class InfoUpdateRequestMapper {
    /**
     * {@link InfoUpdateRequest}를 {@link InfoUpdateRequestJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link InfoUpdateRequest} 객체
     * @return {@link InfoUpdateRequestJpaEntity} 객체
     */
    public InfoUpdateRequestJpaEntity toJpaEntity(InfoUpdateRequest domainEntity) {
        return InfoUpdateRequestJpaEntity.builder()
                .requesterId(domainEntity.getRequesterId().getValue())
                .status(domainEntity.getStatus())
                .infoId(domainEntity.getInfoId().getValue())
                .content(domainEntity.getContent().getValue())
                .contactEmail(domainEntity.getContactEmail().getValue())
                .rejectionReason(domainEntity.getRejectionReason() == null ? null : domainEntity.getRejectionReason().getValue())
                .observedAt(domainEntity.getObservedAt())
                .requestedAt(domainEntity.getRequestedAt())
                .build();
    }

    /**
     * {@link InfoUpdateRequestJpaEntity}를 {@link InfoUpdateRequest}로 변환하는 메서드
     *
     * @param jpaEntity {@link InfoUpdateRequestJpaEntity} 객체
     * @return {@link InfoUpdateRequest} 객체
     */
    public InfoUpdateRequest toDomain(InfoUpdateRequestJpaEntity jpaEntity) {
        return InfoUpdateRequest.withId(
                new InfoUpdateRequestId(jpaEntity.getId()),
                new MemberId(jpaEntity.getRequesterId()),
                jpaEntity.getStatus(),
                new InfoId(jpaEntity.getInfoId()),
                new InfoUpdateRequestContent(jpaEntity.getContent()),
                new Email(jpaEntity.getContactEmail()),
                jpaEntity.getRejectionReason() == null ? null : new RejectionReason(jpaEntity.getRejectionReason()),
                jpaEntity.getObservedAt(),
                jpaEntity.getRequestedAt()
        );
    }
}
