package com.todayyu.persistence.inquiry.adapter;

import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.inquiry.dto.InquirySummaryViewProjection;
import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;
import com.todayyu.persistence.inquiry.mapper.InquiryMapper;
import com.todayyu.persistence.inquiry.repository.InquiryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JpaInquiryAdapterTest {
    private static final Long INQUIRY_ID = 10L;
    private static final Long MEMBER_ID = 100L;
    private static final String CONTACT_EMAIL = "member@yu.ac.kr";
    private static final String TITLE = "문의 제목";
    private static final String CONTENT = "문의 내용";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Mock
    private InquiryJpaRepository inquiryRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private JpaInquiryAdapter inquiryAdapter;

    @Test
    @DisplayName("문의 ID에 해당하는 문의가 존재하면 도메인 엔티티로 변환해 반환한다")
    void readById() {
        // given
        InquiryJpaEntity jpaEntity = waitingJpaEntity(INQUIRY_ID);
        Inquiry inquiry = waitingInquiry(INQUIRY_ID);
        given(inquiryRepository.findById(INQUIRY_ID)).willReturn(Optional.of(jpaEntity));
        given(inquiryMapper.toDomain(jpaEntity)).willReturn(inquiry);

        // when
        Optional<Inquiry> result = inquiryAdapter.readById(new InquiryId(INQUIRY_ID));

        // then
        assertEquals(Optional.of(inquiry), result);
    }

    @Test
    @DisplayName("문의 ID에 해당하는 문의가 존재하지 않으면 빈 Optional을 반환하고 변환하지 않는다")
    void readByIdWithNotExistingInquiry() {
        // given
        given(inquiryRepository.findById(INQUIRY_ID)).willReturn(Optional.empty());

        // when
        Optional<Inquiry> result = inquiryAdapter.readById(new InquiryId(INQUIRY_ID));

        // then
        assertTrue(result.isEmpty());
        then(inquiryMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("문의를 JPA 엔티티로 변환해 저장하고, 저장된 JPA 엔티티를 도메인 엔티티로 변환해 반환한다")
    void save() {
        // given
        Inquiry newInquiry = Inquiry.withoutId(
                new MemberId(MEMBER_ID),
                new Email(CONTACT_EMAIL),
                new InquiryTitle(TITLE),
                new InquiryContent(CONTENT),
                CREATED_AT
        );
        InquiryJpaEntity newJpaEntity = waitingJpaEntity(null);
        InquiryJpaEntity savedJpaEntity = waitingJpaEntity(INQUIRY_ID);
        Inquiry savedInquiry = waitingInquiry(INQUIRY_ID);

        given(inquiryMapper.toJpaEntity(newInquiry)).willReturn(newJpaEntity);
        given(inquiryRepository.save(newJpaEntity)).willReturn(savedJpaEntity);
        given(inquiryMapper.toDomain(savedJpaEntity)).willReturn(savedInquiry);

        // when
        Inquiry result = inquiryAdapter.save(newInquiry);

        // then
        assertSame(savedInquiry, result);
        assertEquals(new InquiryId(INQUIRY_ID), result.getId());
        then(inquiryRepository).should().save(newJpaEntity);
    }

    @Test
    @DisplayName("영속 상태의 JPA 엔티티를 조회해 수정 가능한 필드를 변경하고, 도메인 엔티티로 변환해 반환한다")
    void update() {
        // given
        String newContactEmail = "new@yu.ac.kr";
        String newTitle = "수정된 문의 제목";
        String newContent = "수정된 문의 내용";
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 2, 9, 0);
        Inquiry updatedInquiry = Inquiry.withId(
                new InquiryId(INQUIRY_ID),
                new MemberId(MEMBER_ID),
                new Email(newContactEmail),
                InquiryStatus.WAITING,
                new InquiryTitle(newTitle),
                new InquiryContent(newContent),
                CREATED_AT,
                updatedAt,
                null
        );
        InquiryJpaEntity jpaEntity = waitingJpaEntity(INQUIRY_ID);

        given(inquiryRepository.findById(INQUIRY_ID)).willReturn(Optional.of(jpaEntity));
        given(inquiryMapper.toDomain(jpaEntity)).willReturn(updatedInquiry);

        // when
        Inquiry result = inquiryAdapter.update(updatedInquiry);

        // then
        assertSame(updatedInquiry, result);
        assertEquals(newContactEmail, jpaEntity.getContactEmail());
        assertEquals(newTitle, jpaEntity.getTitle());
        assertEquals(newContent, jpaEntity.getContent());
        assertEquals(updatedAt, jpaEntity.getUpdatedAt());
        assertEquals(MEMBER_ID, jpaEntity.getMemberId());
        assertEquals(InquiryStatus.WAITING, jpaEntity.getStatus());
        assertEquals(CREATED_AT, jpaEntity.getCreatedAt());
        then(inquiryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("수정할 문의가 존재하지 않으면 IllegalStateException이 발생하고 변환하지 않는다")
    void updateWithNotExistingInquiry() {
        // given
        given(inquiryRepository.findById(INQUIRY_ID)).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalStateException.class, () -> inquiryAdapter.update(waitingInquiry(INQUIRY_ID)));
        then(inquiryMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원 ID와 커서로 문의 목록을 조회해 문의 요약 목록으로 변환해 반환한다")
    void readByCursor() {
        // given
        Long cursorId = 20L;
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 2, 9, 0);
        given(inquiryRepository.findByCursor(MEMBER_ID, cursorId, 3)).willReturn(List.of(
                new InquirySummaryViewProjection(INQUIRY_ID, InquiryStatus.WAITING, TITLE, CREATED_AT, updatedAt)
        ));

        // when
        List<InquirySummaryView> result = inquiryAdapter.readByCursor(
                new MemberId(MEMBER_ID),
                new InquiryCursor(new InquiryId(cursorId)),
                3
        );

        // then
        InquirySummaryView expected = InquirySummaryView.builder()
                .id(new InquiryId(INQUIRY_ID))
                .status(InquiryStatus.WAITING)
                .title(new InquiryTitle(TITLE))
                .createdAt(CREATED_AT)
                .updatedAt(updatedAt)
                .build();
        assertEquals(List.of(expected), result);
    }

    @Test
    @DisplayName("커서가 없으면 커서 조건 없이 처음부터 조회한다")
    void readByCursorWithoutCursor() {
        // given
        given(inquiryRepository.findByCursor(MEMBER_ID, null, 3)).willReturn(List.of());

        // when
        List<InquirySummaryView> result = inquiryAdapter.readByCursor(new MemberId(MEMBER_ID), null, 3);

        // then
        assertTrue(result.isEmpty());
        then(inquiryRepository).should().findByCursor(MEMBER_ID, null, 3);
    }

    @Test
    @DisplayName("문의 ID 값으로 문의를 삭제한다")
    void deleteById() {
        // when
        inquiryAdapter.deleteById(new InquiryId(INQUIRY_ID));

        // then
        then(inquiryRepository).should().deleteById(INQUIRY_ID);
    }

    private InquiryJpaEntity waitingJpaEntity(Long inquiryId) {
        InquiryJpaEntity jpaEntity = InquiryJpaEntity.builder()
                .memberId(MEMBER_ID)
                .contactEmail(CONTACT_EMAIL)
                .status(InquiryStatus.WAITING)
                .title(TITLE)
                .content(CONTENT)
                .createdAt(CREATED_AT)
                .updatedAt(CREATED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", inquiryId);
        return jpaEntity;
    }

    private Inquiry waitingInquiry(Long inquiryId) {
        return Inquiry.withId(
                new InquiryId(inquiryId),
                new MemberId(MEMBER_ID),
                new Email(CONTACT_EMAIL),
                InquiryStatus.WAITING,
                new InquiryTitle(TITLE),
                new InquiryContent(CONTENT),
                CREATED_AT,
                CREATED_AT,
                null
        );
    }
}
