package com.todayyu.persistence.scrap.adapter;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import com.todayyu.domain.scrap.vo.ScrapId;
import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;
import com.todayyu.persistence.scrap.mapper.ScrapMapper;
import com.todayyu.persistence.scrap.repository.ScrapJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JpaScrapAdapterTest {
    private static final Long SCRAP_ID = 100L;
    private static final Long INFO_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final LocalDateTime SCRAPPED_AT = LocalDateTime.of(2026, 10, 18, 9, 0);

    @Mock
    private ScrapMapper scrapMapper;

    @Mock
    private ScrapJpaRepository scrapRepository;

    @InjectMocks
    private JpaScrapAdapter scrapAdapter;

    @Test
    @DisplayName("회원이 정보를 스크랩했으면 true를 반환한다")
    void existsByInfoIdAndMemberId() {
        // given
        given(scrapRepository.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(true);

        // when
        boolean exists = scrapAdapter.existsByInfoIdAndMemberId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("회원이 정보를 스크랩하지 않았으면 false를 반환한다")
    void existsByInfoIdAndMemberIdWithoutScrap() {
        // given
        given(scrapRepository.existsByInfoIdAndMemberId(INFO_ID, MEMBER_ID)).willReturn(false);

        // when
        boolean exists = scrapAdapter.existsByInfoIdAndMemberId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // then
        assertFalse(exists);
    }

    @Test
    @DisplayName("회원 ID와 정보 ID 목록을 원시값으로 변환해 조회하고, 회원이 스크랩한 정보 ID 집합을 반환한다")
    void readScrappedInfoIds() {
        // given
        given(scrapRepository.findInfoIdsByMemberIdAndInfoIds(MEMBER_ID, List.of(1L, 2L, 3L)))
                .willReturn(List.of(1L, 3L));

        // when
        Set<InfoId> scrappedInfoIds = scrapAdapter.readScrappedInfoIds(
                new MemberId(MEMBER_ID),
                List.of(new InfoId(1L), new InfoId(2L), new InfoId(3L))
        );

        // then
        assertEquals(Set.of(new InfoId(1L), new InfoId(3L)), scrappedInfoIds);
    }

    @Test
    @DisplayName("회원이 스크랩한 정보가 없으면 빈 집합을 반환한다")
    void readScrappedInfoIdsWithoutScrap() {
        // given
        given(scrapRepository.findInfoIdsByMemberIdAndInfoIds(MEMBER_ID, List.of(1L, 2L)))
                .willReturn(List.of());

        // when
        Set<InfoId> scrappedInfoIds = scrapAdapter.readScrappedInfoIds(
                new MemberId(MEMBER_ID),
                List.of(new InfoId(1L), new InfoId(2L))
        );

        // then
        assertTrue(scrappedInfoIds.isEmpty());
    }

    @Test
    @DisplayName("조회 결과에 중복된 정보 ID가 있으면 하나로 반환한다")
    void readScrappedInfoIdsWithDuplicate() {
        // given
        given(scrapRepository.findInfoIdsByMemberIdAndInfoIds(MEMBER_ID, List.of(1L)))
                .willReturn(List.of(1L, 1L));

        // when
        Set<InfoId> scrappedInfoIds = scrapAdapter.readScrappedInfoIds(
                new MemberId(MEMBER_ID),
                List.of(new InfoId(1L))
        );

        // then
        assertEquals(Set.of(new InfoId(1L)), scrappedInfoIds);
    }

    @Test
    @DisplayName("스크랩을 JPA 엔티티로 변환해 저장하고, ID가 부여된 스크랩을 반환한다")
    void save() {
        // given
        Scrap newScrap = Scrap.withoutId(new InfoId(INFO_ID), new MemberId(MEMBER_ID), SCRAPPED_AT);
        ScrapJpaEntity newJpaEntity = jpaEntity(null);
        ScrapJpaEntity savedJpaEntity = jpaEntity(SCRAP_ID);
        Scrap savedScrap = Scrap.withId(new ScrapId(SCRAP_ID), new InfoId(INFO_ID), new MemberId(MEMBER_ID), SCRAPPED_AT);

        given(scrapMapper.toJpaEntity(newScrap)).willReturn(newJpaEntity);
        given(scrapRepository.save(newJpaEntity)).willReturn(savedJpaEntity);
        given(scrapMapper.toDomain(savedJpaEntity)).willReturn(savedScrap);

        // when
        Scrap result = scrapAdapter.save(newScrap);

        // then
        assertSame(savedScrap, result);
        assertEquals(new ScrapId(SCRAP_ID), result.getId());
        then(scrapRepository).should().save(newJpaEntity);
    }

    @Test
    @DisplayName("정보 ID와 회원 ID를 원시값으로 변환해 스크랩 삭제를 요청한다")
    void deleteByInfoIdAndMemberId() {
        // when
        scrapAdapter.deleteByInfoIdAndMemberId(new InfoId(INFO_ID), new MemberId(MEMBER_ID));

        // then
        then(scrapRepository).should().deleteByInfoIdAndMemberId(INFO_ID, MEMBER_ID);
    }

    private ScrapJpaEntity jpaEntity(Long scrapId) {
        ScrapJpaEntity jpaEntity = ScrapJpaEntity.builder()
                .infoId(INFO_ID)
                .memberId(MEMBER_ID)
                .scrappedAt(SCRAPPED_AT)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", scrapId);
        return jpaEntity;
    }
}
