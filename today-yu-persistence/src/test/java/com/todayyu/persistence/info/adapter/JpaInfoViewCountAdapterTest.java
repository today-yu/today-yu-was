package com.todayyu.persistence.info.adapter;

import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.ViewCount;
import com.todayyu.persistence.info.entity.InfoViewCountJpaEntity;
import com.todayyu.persistence.info.mapper.InfoViewCountMapper;
import com.todayyu.persistence.info.repository.InfoViewCountJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JpaInfoViewCountAdapterTest {
    private static final Long INFO_ID = 1L;
    private static final Long VIEW_COUNT = 10L;

    @Mock
    private InfoViewCountJpaRepository infoViewCountRepository;

    @Mock
    private InfoViewCountMapper infoViewCountMapper;

    @InjectMocks
    private JpaInfoViewCountAdapter infoViewCountAdapter;

    @Test
    @DisplayName("정보 ID에 해당하는 조회수가 존재하면 도메인 엔티티로 변환해 반환한다")
    void readByInfoId() {
        // given
        InfoViewCountJpaEntity jpaEntity = InfoViewCountJpaEntity.builder()
                .infoId(INFO_ID)
                .viewCount(VIEW_COUNT)
                .build();
        InfoViewCount infoViewCount = InfoViewCount.of(new InfoId(INFO_ID), new ViewCount(VIEW_COUNT));
        given(infoViewCountRepository.findByInfoId(INFO_ID)).willReturn(Optional.of(jpaEntity));
        given(infoViewCountMapper.toDomain(jpaEntity)).willReturn(infoViewCount);

        // when
        Optional<InfoViewCount> result = infoViewCountAdapter.readByInfoId(new InfoId(INFO_ID));

        // then
        assertEquals(Optional.of(infoViewCount), result);
    }

    @Test
    @DisplayName("정보 ID에 해당하는 조회수가 존재하지 않으면 빈 Optional을 반환하고 변환하지 않는다")
    void readByInfoIdWithNotExistingViewCount() {
        // given
        given(infoViewCountRepository.findByInfoId(INFO_ID)).willReturn(Optional.empty());

        // when
        Optional<InfoViewCount> result = infoViewCountAdapter.readByInfoId(new InfoId(INFO_ID));

        // then
        assertTrue(result.isEmpty());
        then(infoViewCountMapper).shouldHaveNoInteractions();
    }
}
