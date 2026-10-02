package com.todayyu.persistence.member.adapter;

import com.todayyu.application.member.port.NicknamePort;
import com.todayyu.domain.member.vo.NicknameModifier;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.member.repository.NicknameSequenceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaNicknameAdapter implements NicknamePort {
    private final NicknameSequenceJpaRepository nicknameSequenceRepository;
    
    /**
     * 닉네임 수식어 별로 다음 닉네임 번호를 발급하는 메서드
     *
     * <p> 수식어의 시퀀스가 없으면 1로 생성하고, 있으면 1 증가시킨 뒤 번호를 조회한다. </p>
     *
     * @param modifier 닉네임 수식어
     * @return 다음 닉네임 번호
     */
    @Override
    @Transactional
    public long issueNext(NicknameModifier modifier) {
        nicknameSequenceRepository.increase(modifier.name());
        return nicknameSequenceRepository.findLastNumber(modifier.name());
    }
}
