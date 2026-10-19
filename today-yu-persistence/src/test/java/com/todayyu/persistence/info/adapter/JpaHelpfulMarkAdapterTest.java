package com.todayyu.persistence.info.adapter;

import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.HelpfulMarkId;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.info.dto.HelpfulMarkCountProjection;
import com.todayyu.persistence.info.entity.HelpfulMarkJpaEntity;
import com.todayyu.persistence.info.mapper.HelpfulMarkMapper;
import com.todayyu.persistence.info.repository.HelpfulMarkJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JpaHelpfulMarkAdapterTest {
    private static final Long HELPFUL_MARK_ID = 100L;
    private static final Long INFO_ID = 1L;
    private static final Long MEMBER_ID = 2L;

    @Mock
    private HelpfulMarkJpaRepository helpfulMarkRepository;

    @Mock
    private HelpfulMarkMapper helpfulMarkMapper;

    @InjectMocks
    private JpaHelpfulMarkAdapter helpfulMarkAdapter;

    @Test
    @DisplayName("정보 ID에 해당하는 '도움이 되는 정보에요' 수를 반환한다")
    void countByInfoId() {
        // given
        given(helpfulMarkRepository.countByInfoId(INFO_ID)).willReturn(5L);

        // when
        long count = helpfulMarkAdapter.countByInfoId(new InfoId(INFO_ID));

        // then
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("회원이 정보에 '도움이 되는 정보에요'를 눌렀으면 true를 반환한다")
    void existsByInfoIdAndMemberId() {
        // given
        given(helpfulMarkRepository.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(true);

        // when
        boolean exists = helpfulMarkAdapter.existsByInfoIdAndMemberId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("회원이 정보에 '도움이 되는 정보에요'를 누르지 않았으면 false를 반환한다")
    void existsByInfoIdAndMemberIdWithoutHelpfulMark() {
        // given
        given(helpfulMarkRepository.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(false);

        // when
        boolean exists = helpfulMarkAdapter.existsByInfoIdAndMemberId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // then
        assertFalse(exists);
    }

    @Test
    @DisplayName("정보 ID 목록에 해당하는 '도움이 되는 정보에요' 수를 정보 ID별로 반환한다")
    void countByInfoIds() {
        // given
        given(helpfulMarkRepository.countByInfoIds(List.of(1L, 2L))).willReturn(List.of(
                new HelpfulMarkCountProjection(2L, 3L),
                new HelpfulMarkCountProjection(1L, 5L)
        ));

        // when
        Map<InfoId, Long> counts = helpfulMarkAdapter.countByInfoIds(List.of(new InfoId(1L), new InfoId(2L)));

        // then
        assertEquals(Map.of(new InfoId(1L), 5L, new InfoId(2L), 3L), counts);
    }

    @Test
    @DisplayName("'도움이 되는 정보에요'가 없는 정보는 결과에 포함하지 않는다")
    void countByInfoIdsWithoutHelpfulMark() {
        // given
        given(helpfulMarkRepository.countByInfoIds(List.of(1L, 2L))).willReturn(List.of(
                new HelpfulMarkCountProjection(1L, 5L)
        ));

        // when
        Map<InfoId, Long> counts = helpfulMarkAdapter.countByInfoIds(List.of(new InfoId(1L), new InfoId(2L)));

        // then
        assertEquals(Map.of(new InfoId(1L), 5L), counts);
        assertFalse(counts.containsKey(new InfoId(2L)));
    }

    @Test
    @DisplayName("집계 결과가 없으면 빈 Map을 반환한다")
    void countByInfoIdsWithEmptyResult() {
        // given
        given(helpfulMarkRepository.countByInfoIds(anyList())).willReturn(List.of());

        // when
        Map<InfoId, Long> counts = helpfulMarkAdapter.countByInfoIds(List.of(new InfoId(1L)));

        // then
        assertTrue(counts.isEmpty());
    }

    @Test
    @DisplayName("정보 ID 목록이 비어있으면 조회하지 않고 빈 Map을 반환한다")
    void countByInfoIdsWithEmptyInfoIds() {
        // when
        Map<InfoId, Long> counts = helpfulMarkAdapter.countByInfoIds(List.of());

        // then
        assertTrue(counts.isEmpty());
        then(helpfulMarkRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("'도움이 되는 정보에요'를 JPA 엔티티로 변환해 저장하고, ID가 부여된 '도움이 되는 정보에요'를 반환한다")
    void save() {
        // given
        HelpfulMark newHelpfulMark = HelpfulMark.withoutId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));
        HelpfulMarkJpaEntity newJpaEntity = jpaEntity(null);
        HelpfulMarkJpaEntity savedJpaEntity = jpaEntity(HELPFUL_MARK_ID);
        HelpfulMark savedHelpfulMark = HelpfulMark.withId(
                new HelpfulMarkId(HELPFUL_MARK_ID), new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        given(helpfulMarkMapper.toJpaEntity(newHelpfulMark)).willReturn(newJpaEntity);
        given(helpfulMarkRepository.save(newJpaEntity)).willReturn(savedJpaEntity);
        given(helpfulMarkMapper.toDomain(savedJpaEntity)).willReturn(savedHelpfulMark);

        // when
        HelpfulMark result = helpfulMarkAdapter.save(newHelpfulMark);

        // then
        assertSame(savedHelpfulMark, result);
        assertEquals(new HelpfulMarkId(HELPFUL_MARK_ID), result.getId());
        then(helpfulMarkRepository).should().save(newJpaEntity);
    }

    @Test
    @DisplayName("정보 ID와 회원 ID를 원시값으로 변환해 '도움이 되는 정보에요' 삭제를 요청한다")
    void deleteByInfoIdAndMemberId() {
        // when
        helpfulMarkAdapter.deleteByInfoIdAndMemberId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // then
        then(helpfulMarkRepository).should().deleteByInfoIdAndMemberId(INFO_ID, MEMBER_ID);
    }

    private HelpfulMarkJpaEntity jpaEntity(Long helpfulMarkId) {
        HelpfulMarkJpaEntity jpaEntity = HelpfulMarkJpaEntity.builder()
                .infoId(INFO_ID)
                .memberId(MEMBER_ID)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", helpfulMarkId);
        return jpaEntity;
    }
}
