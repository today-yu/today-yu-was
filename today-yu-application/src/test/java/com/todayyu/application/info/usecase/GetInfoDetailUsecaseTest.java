package com.todayyu.application.info.usecase;

import com.todayyu.application.info.dto.ContributionResult;
import com.todayyu.application.info.dto.ContributionType;
import com.todayyu.application.info.dto.GetInfoDetailQuery;
import com.todayyu.application.info.dto.InfoDetailResult;
import com.todayyu.application.info.dto.InfoRegistrationRequestView;
import com.todayyu.application.info.dto.InfoUpdateRequestView;
import com.todayyu.application.info.error.BusinessInfoErrorType;
import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.application.info.port.read.ReadInfoViewCountPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.info.vo.ViewCount;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetInfoDetailUsecaseTest {
    private static final InfoId INFO_ID = new InfoId(1L);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final GetInfoDetailQuery QUERY = new GetInfoDetailQuery(INFO_ID, MEMBER_ID);
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 10, 5, 9, 0);
    private static final ViewCount VIEW_COUNT = new ViewCount(10L);

    private static final MemberId REGISTRANT_ID = new MemberId(2L);
    private static final Nickname REGISTRANT_NICKNAME = new Nickname("명랑한 영대인 2");
    private static final MemberId UPDATER_ID = new MemberId(3L);
    private static final Nickname UPDATER_NICKNAME = new Nickname("명랑한 영대인 3");

    @Mock
    private ReadInfoPort readInfoPort;

    @Mock
    private ReadInfoViewCountPort readInfoViewCountPort;

    @Mock
    private ReadHelpfulMarkPort readHelpfulMarkPort;

    @Mock
    private ReadInfoRegistrationRequestPort readInfoRegistrationRequestPort;

    @Mock
    private ReadInfoUpdateRequestPort readInfoUpdateRequestPort;

    @InjectMocks
    private GetInfoDetailUsecase getInfoDetailUsecase;

    @Test
    @DisplayName("정보가 존재하면 정보 내용, 조회수, '도움이 되는 정보에요' 수와 요청 회원의 클릭 여부를 반환한다")
    void getInfoDetail() {
        // given
        Info info = info();
        givenInfoDetail(info, 5L, true, List.of(), List.of());

        // when
        InfoDetailResult result = getInfoDetailUsecase.getInfoDetail(QUERY);

        // then
        assertEquals(INFO_ID, result.id());
        assertEquals(info.getCategory(), result.category());
        assertEquals(info.getTitle(), result.title());
        assertEquals(info.getContent(), result.content());
        assertEquals(PUBLISHED_AT, result.publishedAt());
        assertEquals(UPDATED_AT, result.updatedAt());
        assertEquals(VIEW_COUNT, result.viewCount());
        assertEquals(5L, result.helpfulMarkCount());
        assertTrue(result.helpfulMarked());
    }

    @Test
    @DisplayName("요청 회원이 '도움이 되는 정보에요'를 누르지 않았으면 클릭 여부를 false로 반환한다")
    void getInfoDetailWithoutHelpfulMark() {
        // given
        givenInfoDetail(info(), 0L, false, List.of(), List.of());

        // when
        InfoDetailResult result = getInfoDetailUsecase.getInfoDetail(QUERY);

        // then
        assertEquals(0L, result.helpfulMarkCount());
        assertFalse(result.helpfulMarked());
    }

    @Test
    @DisplayName("반영된 정보 등록·수정 요청을 기여 내역으로 합쳐 요청 일시 내림차순으로 반환한다")
    void getInfoDetailWithContributions() {
        // given
        LocalDateTime registeredAt = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime firstUpdatedAt = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime secondUpdatedAt = LocalDateTime.of(2026, 9, 20, 9, 0);

        List<InfoRegistrationRequestView> registrationRequestViews = List.of(
                registrationRequestView(10L, REGISTRANT_ID, REGISTRANT_NICKNAME, registeredAt)
        );
        List<InfoUpdateRequestView> updateRequestViews = List.of(
                updateRequestView(20L, UPDATER_ID, UPDATER_NICKNAME, firstUpdatedAt),
                updateRequestView(21L, REGISTRANT_ID, REGISTRANT_NICKNAME, secondUpdatedAt)
        );
        givenInfoDetail(info(), 0L, false, registrationRequestViews, updateRequestViews);

        // when
        InfoDetailResult result = getInfoDetailUsecase.getInfoDetail(QUERY);

        // then
        List<ContributionResult> expected = List.of(
                contribution(REGISTRANT_ID, REGISTRANT_NICKNAME, ContributionType.UPDATE, 21L, secondUpdatedAt),
                contribution(UPDATER_ID, UPDATER_NICKNAME, ContributionType.UPDATE, 20L, firstUpdatedAt),
                contribution(REGISTRANT_ID, REGISTRANT_NICKNAME, ContributionType.REGISTRATION, 10L, registeredAt)
        );
        assertEquals(expected, result.contributions());
    }

    @Test
    @DisplayName("반영된 요청이 없으면 기여 내역을 빈 목록으로 반환한다")
    void getInfoDetailWithoutContributions() {
        // given
        givenInfoDetail(info(), 0L, false, List.of(), List.of());

        // when
        InfoDetailResult result = getInfoDetailUsecase.getInfoDetail(QUERY);

        // then
        assertTrue(result.contributions().isEmpty());
    }

    @Test
    @DisplayName("기여 내역은 반영(ACCEPTED) 상태의 정보 등록·수정 요청만 조회한다")
    void getInfoDetailReadsOnlyAcceptedRequests() {
        // given
        givenInfoDetail(info(), 0L, false, List.of(), List.of());

        // when
        getInfoDetailUsecase.getInfoDetail(QUERY);

        // then
        then(readInfoRegistrationRequestPort).should().readByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED);
        then(readInfoUpdateRequestPort).should().readByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("정보가 존재하지 않으면 NOT_FOUND 예외가 발생하고 이후 조회를 하지 않는다")
    void getInfoDetailWithNotExistingInfo() {
        // given
        given(readInfoPort.readById(INFO_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getInfoDetailUsecase.getInfoDetail(QUERY));

        // then
        assertEquals(BusinessInfoErrorType.NOT_FOUND, exception.getErrorType());
        then(readInfoViewCountPort).shouldHaveNoInteractions();
        then(readHelpfulMarkPort).shouldHaveNoInteractions();
        then(readInfoRegistrationRequestPort).shouldHaveNoInteractions();
        then(readInfoUpdateRequestPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("정보의 조회수가 존재하지 않으면 IllegalStateException이 발생하고 이후 조회를 하지 않는다")
    void getInfoDetailWithoutViewCount() {
        // given
        given(readInfoPort.readById(INFO_ID)).willReturn(Optional.of(info()));
        given(readInfoViewCountPort.readByInfoId(INFO_ID)).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalStateException.class, () -> getInfoDetailUsecase.getInfoDetail(QUERY));
        then(readHelpfulMarkPort).shouldHaveNoInteractions();
        then(readInfoRegistrationRequestPort).shouldHaveNoInteractions();
        then(readInfoUpdateRequestPort).shouldHaveNoInteractions();
    }

    private void givenInfoDetail(
            Info info,
            long helpfulMarkCount,
            boolean helpfulMarked,
            List<InfoRegistrationRequestView> registrationRequestViews,
            List<InfoUpdateRequestView> updateRequestViews
    ) {
        given(readInfoPort.readById(INFO_ID)).willReturn(Optional.of(info));
        given(readInfoViewCountPort.readByInfoId(INFO_ID)).willReturn(Optional.of(InfoViewCount.of(INFO_ID, VIEW_COUNT)));
        given(readHelpfulMarkPort.countByInfoId(INFO_ID)).willReturn(helpfulMarkCount);
        given(readHelpfulMarkPort.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(helpfulMarked);
        given(readInfoRegistrationRequestPort.readByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED))
                .willReturn(registrationRequestViews);
        given(readInfoUpdateRequestPort.readByInfoIdAndStatus(INFO_ID, InfoRequestStatus.ACCEPTED))
                .willReturn(updateRequestViews);
    }

    private Info info() {
        return Info.withId(
                INFO_ID,
                InfoCategory.ACADEMIC,
                new InfoTitle("수강신청 일정"),
                new InfoContent("수강신청은 10월 20일부터 진행됩니다."),
                PUBLISHED_AT,
                UPDATED_AT
        );
    }

    private InfoRegistrationRequestView registrationRequestView(
            Long requestId,
            MemberId requesterId,
            Nickname nickname,
            LocalDateTime requestedAt
    ) {
        InfoRegistrationRequest request = InfoRegistrationRequest.withId(
                new InfoRegistrationRequestId(requestId),
                requesterId,
                InfoCategory.ACADEMIC,
                new InfoRegistrationRequestContent("수강신청 일정을 알려주세요."),
                InfoRequestStatus.ACCEPTED,
                INFO_ID,
                new Email("user@example.com"),
                null,
                requestedAt
        );
        return new InfoRegistrationRequestView(request, nickname);
    }

    private InfoUpdateRequestView updateRequestView(
            Long requestId,
            MemberId requesterId,
            Nickname nickname,
            LocalDateTime requestedAt
    ) {
        InfoUpdateRequest request = InfoUpdateRequest.withId(
                new InfoUpdateRequestId(requestId),
                requesterId,
                InfoRequestStatus.ACCEPTED,
                INFO_ID,
                new InfoUpdateRequestContent("수강신청 일정이 변경되었습니다."),
                new Email("user@example.com"),
                null,
                requestedAt,
                requestedAt
        );
        return new InfoUpdateRequestView(request, nickname);
    }

    private ContributionResult contribution(
            MemberId memberId,
            Nickname nickname,
            ContributionType type,
            Long requestId,
            LocalDateTime requestedAt
    ) {
        return new ContributionResult(memberId, nickname, type, requestId, requestedAt);
    }
}
