package com.todayyu.application.inquiry.usecase;

import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.dto.query.GetInquiriesQuery;
import com.todayyu.application.inquiry.dto.result.InquirySummaryResult;
import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryStatus;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetInquiriesUsecaseTest {
    private static final int SIZE = 2;
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final InquiryCursor CURSOR = new InquiryCursor(new InquiryId(10L));
    private static final GetInquiriesQuery QUERY = new GetInquiriesQuery(MEMBER_ID, CURSOR, SIZE);

    @Mock
    private ReadInquiryPort readInquiryPort;

    @InjectMocks
    private GetInquiriesUsecase getInquiriesUsecase;

    @Test
    @DisplayName("요청 크기보다 1개 더 조회되면 요청 크기만큼 반환하고, 다음 데이터가 존재하며 마지막 문의로 다음 커서를 생성한다")
    void getInquiriesWithNext() {
        // given
        InquirySummaryView first = view(9L, InquiryStatus.ANSWERED, LocalDateTime.of(2026, 10, 9, 9, 0));
        InquirySummaryView second = view(8L, InquiryStatus.WAITING, LocalDateTime.of(2026, 10, 8, 9, 0));
        InquirySummaryView extra = view(7L, InquiryStatus.WAITING, LocalDateTime.of(2026, 10, 7, 9, 0));
        given(readInquiryPort.readByCursor(MEMBER_ID, CURSOR, SIZE + 1)).willReturn(List.of(first, second, extra));

        // when
        CursorResult<InquirySummaryResult, InquiryCursor> result = getInquiriesUsecase.getInquiries(QUERY);

        // then
        List<InquirySummaryResult> expected = List.of(
                InquirySummaryResult.from(first),
                InquirySummaryResult.from(second)
        );
        assertEquals(expected, result.contents());
        assertTrue(result.hasNext());
        assertEquals(new InquiryCursor(second.id()), result.nextCursor());
    }

    @Test
    @DisplayName("요청 크기만큼 조회되면 모두 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInquiriesWithoutNext() {
        // given
        InquirySummaryView first = view(9L, InquiryStatus.ANSWERED, LocalDateTime.of(2026, 10, 9, 9, 0));
        InquirySummaryView second = view(8L, InquiryStatus.WAITING, LocalDateTime.of(2026, 10, 8, 9, 0));
        given(readInquiryPort.readByCursor(MEMBER_ID, CURSOR, SIZE + 1)).willReturn(List.of(first, second));

        // when
        CursorResult<InquirySummaryResult, InquiryCursor> result = getInquiriesUsecase.getInquiries(QUERY);

        // then
        assertEquals(2, result.contents().size());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("조회된 문의가 없으면 빈 목록을 반환하고, 다음 데이터가 존재하지 않으며 다음 커서는 null이다")
    void getInquiriesWithEmptyResult() {
        // given
        given(readInquiryPort.readByCursor(MEMBER_ID, CURSOR, SIZE + 1)).willReturn(List.of());

        // when
        CursorResult<InquirySummaryResult, InquiryCursor> result = getInquiriesUsecase.getInquiries(QUERY);

        // then
        assertTrue(result.contents().isEmpty());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("첫 요청(커서 없음)이면 회원 ID로만 커서 없이 문의를 조회한다")
    void getInquiriesWithFirstRequest() {
        // given
        GetInquiriesQuery query = new GetInquiriesQuery(MEMBER_ID, null, SIZE);
        given(readInquiryPort.readByCursor(MEMBER_ID, null, SIZE + 1)).willReturn(List.of());

        // when
        getInquiriesUsecase.getInquiries(query);

        // then
        then(readInquiryPort).should().readByCursor(MEMBER_ID, null, SIZE + 1);
    }

    private InquirySummaryView view(Long id, InquiryStatus status, LocalDateTime createdAt) {
        return new InquirySummaryView(
                new InquiryId(id),
                status,
                new InquiryTitle("문의 " + id),
                createdAt,
                createdAt
        );
    }
}
