package com.todayyu.persistence.info.adapter;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.info.repository.HelpfulMarkJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JpaHelpfulMarkAdapterTest {
    private static final Long INFO_ID = 1L;
    private static final Long MEMBER_ID = 2L;

    @Mock
    private HelpfulMarkJpaRepository helpfulMarkRepository;

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
}
