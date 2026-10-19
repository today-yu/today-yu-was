package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoCursor;
import com.todayyu.application.info.port.dto.InfoSortType;
import com.todayyu.application.info.port.dto.InfoSummaryView;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.persistence.info.dto.InfoSummaryViewProjection;
import com.todayyu.persistence.info.entity.InfoJpaEntity;
import com.todayyu.persistence.info.mapper.InfoMapper;
import com.todayyu.persistence.info.repository.InfoJpaRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JpaInfoAdapterTest {
    private static final Long INFO_ID = 1L;
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Mock
    private InfoJpaRepository infoRepository;

    @Mock
    private InfoMapper infoMapper;

    @InjectMocks
    private JpaInfoAdapter infoAdapter;

    @Test
    @DisplayName("정보 ID에 해당하는 정보가 존재하면 도메인 엔티티로 변환해 반환한다")
    void readById() {
        // given
        InfoJpaEntity jpaEntity = infoJpaEntity();
        Info info = info();
        given(infoRepository.findById(INFO_ID)).willReturn(Optional.of(jpaEntity));
        given(infoMapper.toDomain(jpaEntity)).willReturn(info);

        // when
        Optional<Info> result = infoAdapter.readById(new InfoId(INFO_ID));

        // then
        assertEquals(Optional.of(info), result);
    }

    @Test
    @DisplayName("정보 ID에 해당하는 정보가 존재하지 않으면 빈 Optional을 반환하고 변환하지 않는다")
    void readByIdWithNotExistingInfo() {
        // given
        given(infoRepository.findById(INFO_ID)).willReturn(Optional.empty());

        // when
        Optional<Info> result = infoAdapter.readById(new InfoId(INFO_ID));

        // then
        assertTrue(result.isEmpty());
        then(infoMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("커서가 있으면 커서의 수정 일시와 정보 ID로 조회하고, 조회 결과를 정보 요약 목록으로 변환해 반환한다")
    void readByCursor() {
        // given
        LocalDateTime cursorUpdatedAt = LocalDateTime.of(2026, 10, 10, 9, 0);
        InfoCursor cursor = new InfoCursor(cursorUpdatedAt, new InfoId(10L));
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 9, 9, 0);
        given(infoRepository.findByCursor(InfoCategory.ACADEMIC, InfoSortType.LATEST, cursorUpdatedAt, 10L, 3))
                .willReturn(List.of(new InfoSummaryViewProjection(INFO_ID, InfoCategory.ACADEMIC, "수강신청 일정", updatedAt)));

        // when
        List<InfoSummaryView> result = infoAdapter.readByCursor(InfoCategory.ACADEMIC, InfoSortType.LATEST, cursor, 3);

        // then
        InfoSummaryView expected = InfoSummaryView.builder()
                .infoId(new InfoId(INFO_ID))
                .category(InfoCategory.ACADEMIC)
                .title(new InfoTitle("수강신청 일정"))
                .updatedAt(updatedAt)
                .build();
        assertEquals(List.of(expected), result);
    }

    @Test
    @DisplayName("커서가 없으면 커서 조건 없이 처음부터 조회한다")
    void readByCursorWithoutCursor() {
        // given
        given(infoRepository.findByCursor(null, InfoSortType.OLDEST, null, null, 3)).willReturn(List.of());

        // when
        List<InfoSummaryView> result = infoAdapter.readByCursor(null, InfoSortType.OLDEST, null, 3);

        // then
        assertTrue(result.isEmpty());
        then(infoRepository).should().findByCursor(null, InfoSortType.OLDEST, null, null, 3);
    }

    private InfoJpaEntity infoJpaEntity() {
        InfoJpaEntity jpaEntity = InfoJpaEntity.builder()
                .category(InfoCategory.ACADEMIC)
                .title("수강신청 일정")
                .content("수강신청은 10월 20일부터 진행됩니다.")
                .publishedAt(PUBLISHED_AT)
                .updatedAt(PUBLISHED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", INFO_ID);
        return jpaEntity;
    }

    private Info info() {
        return Info.withId(
                new InfoId(INFO_ID),
                InfoCategory.ACADEMIC,
                new InfoTitle("수강신청 일정"),
                new InfoContent("수강신청은 10월 20일부터 진행됩니다."),
                PUBLISHED_AT,
                PUBLISHED_AT
        );
    }
}
