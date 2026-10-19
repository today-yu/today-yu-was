package com.todayyu.persistence.inquiry.mapper;

import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InquiryMapperTest {
    private static final Long INQUIRY_ID = 1L;
    private static final Long MEMBER_ID = 10L;
    private static final String CONTACT_EMAIL = "member@yu.ac.kr";
    private static final String TITLE = "정보 등록 요청 관련 문의";
    private static final String CONTENT = "정보 등록 요청 후 결과는 언제 확인할 수 있나요?";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 20, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 20, 18, 30);
    private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2026, 10, 20, 18, 30);

    private final InquiryMapper inquiryMapper = new InquiryMapper();

    @Test
    @DisplayName("신규 문의를 아이디가 없는 문의 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        Inquiry inquiry = Inquiry.withoutId(
                new MemberId(MEMBER_ID),
                new Email(CONTACT_EMAIL),
                new InquiryTitle(TITLE),
                new InquiryContent(CONTENT),
                CREATED_AT
        );

        // when
        InquiryJpaEntity jpaEntity = inquiryMapper.toJpaEntity(inquiry);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(MEMBER_ID, jpaEntity.getMemberId());
        assertEquals(CONTACT_EMAIL, jpaEntity.getContactEmail());
        assertEquals(InquiryStatus.WAITING, jpaEntity.getStatus());
        assertEquals(TITLE, jpaEntity.getTitle());
        assertEquals(CONTENT, jpaEntity.getContent());
        assertEquals(CREATED_AT, jpaEntity.getCreatedAt());
        assertEquals(CREATED_AT, jpaEntity.getUpdatedAt());
        assertNull(jpaEntity.getAnsweredAt());
    }

    @Test
    @DisplayName("문의 JPA 엔티티를 아이디를 가진 문의로 변환한다")
    void toDomain() {
        // given
        InquiryJpaEntity jpaEntity = InquiryJpaEntity.builder()
                .memberId(MEMBER_ID)
                .contactEmail(CONTACT_EMAIL)
                .status(InquiryStatus.ANSWERED)
                .title(TITLE)
                .content(CONTENT)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .answeredAt(ANSWERED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", INQUIRY_ID);

        // when
        Inquiry inquiry = inquiryMapper.toDomain(jpaEntity);

        // then
        assertEquals(new InquiryId(INQUIRY_ID), inquiry.getId());
        assertEquals(new MemberId(MEMBER_ID), inquiry.getMemberId());
        assertEquals(new Email(CONTACT_EMAIL), inquiry.getContactEmail());
        assertEquals(InquiryStatus.ANSWERED, inquiry.getStatus());
        assertEquals(new InquiryTitle(TITLE), inquiry.getTitle());
        assertEquals(new InquiryContent(CONTENT), inquiry.getContent());
        assertEquals(CREATED_AT, inquiry.getCreatedAt());
        assertEquals(UPDATED_AT, inquiry.getUpdatedAt());
        assertEquals(ANSWERED_AT, inquiry.getAnsweredAt());
    }
}
