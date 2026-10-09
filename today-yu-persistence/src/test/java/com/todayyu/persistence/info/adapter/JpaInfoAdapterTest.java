package com.todayyu.persistence.info.adapter;

import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
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
