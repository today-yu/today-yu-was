package com.todayyu.persistence.member.adapter;

import com.todayyu.domain.member.vo.NicknameModifier;
import com.todayyu.persistence.member.repository.NicknameSequenceJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class JpaNicknameAdapterTest {
    @Mock
    private NicknameSequenceJpaRepository nicknameSequenceRepository;

    @InjectMocks
    private JpaNicknameAdapter nicknameAdapter;

    @Test
    @DisplayName("수식어의 시퀀스를 증가시킨 뒤 증가된 번호를 조회해 반환한다")
    void issueNext() {
        // given
        NicknameModifier modifier = NicknameModifier.KIND;
        given(nicknameSequenceRepository.findLastNumber(modifier.name())).willReturn(3L);

        // when
        long number = nicknameAdapter.issueNext(modifier);

        // then
        assertEquals(3L, number);

        InOrder inOrder = inOrder(nicknameSequenceRepository);
        inOrder.verify(nicknameSequenceRepository).increase(modifier.name());
        inOrder.verify(nicknameSequenceRepository).findLastNumber(modifier.name());
    }
}
