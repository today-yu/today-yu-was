package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.repository.HelpfulMarkJpaRepository;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaHelpfulMarkAdapter implements ReadHelpfulMarkPort {
    private final HelpfulMarkJpaRepository helpfulMarkRepository;

    /**
     * 정보 ID({@link InfoId})를 통해 해당 정보의 '도움이 되는 정보에요' 수를 조회해 반환하는 메서드
     *
     * @param infoId 정보 ID 객체
     * @return '도움이 되는 정보에요' 수
     */
    @Override
    public long countByInfoId(InfoId infoId) {
        return helpfulMarkRepository.countByInfoId(infoId.getValue());
    }

    /**
     * 정보 ID({@link InfoId})와 회원 ID({@link MemberId})를 통해 회원이 해당 정보에 '도움이 되는 정보에요'를 눌렀는지 조회하는 메서드
     *
     * @param infoId 정보 ID 객체
     * @param memberId 회원 ID 객체
     * @return '도움이 되는 정보에요'를 눌렀으면 {@code true}, 아니면 {@code false}
     */
    @Override
    public boolean existsByInfoIdAndMemberId(InfoId infoId, MemberId memberId) {
        return helpfulMarkRepository.existsByInfoIdAndMemberId(infoId.getValue(), memberId.getValue());
    }
}
