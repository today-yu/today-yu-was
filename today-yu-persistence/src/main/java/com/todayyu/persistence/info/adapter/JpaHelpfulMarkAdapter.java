package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.port.read.ReadHelpfulMarkPort;
import com.todayyu.application.info.port.save.SaveHelpfulMarkPort;
import com.todayyu.domain.info.entity.HelpfulMark;
import com.todayyu.domain.info.vo.HelpfulMarkId;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.entity.HelpfulMarkJpaEntity;
import com.todayyu.persistence.info.mapper.HelpfulMarkMapper;
import com.todayyu.persistence.info.repository.HelpfulMarkJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaHelpfulMarkAdapter implements ReadHelpfulMarkPort, SaveHelpfulMarkPort {
    private final HelpfulMarkJpaRepository helpfulMarkRepository;
    private final HelpfulMarkMapper helpfulMarkMapper;
    
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
     * @param infoId   정보 ID 객체
     * @param memberId 회원 ID 객체
     * @return '도움이 되는 정보에요'를 눌렀으면 {@code true}, 아니면 {@code false}
     */
    @Override
    public boolean existsByInfoIdAndMemberId(InfoId infoId, MemberId memberId) {
        return helpfulMarkRepository.existsByInfoIdAndMemberId(infoId.getValue(), memberId.getValue());
    }
    
    /**
     * 정보 ID({@link InfoId}) 목록을 통해 각 정보의 '도움이 되는 정보에요' 수를 조회해 반환하는 메서드
     *
     * <p> 정보 ID별로 '도움이 되는 정보에요' 수를 집계하므로, '도움이 되는 정보에요'가 없는 정보는 결과에 포함되지 않는다. </p>
     *
     * @param infoIds 정보 ID 객체 목록
     * @return 정보 ID별 '도움이 되는 정보에요' 수
     */
    @Override
    public Map<InfoId, Long> countByInfoIds(List<InfoId> infoIds) {
        if (infoIds.isEmpty()) {
            return Map.of();
        }
        
        return helpfulMarkRepository.countByInfoIds(infoIds.stream().map(InfoId::getValue).toList()).stream()
                .collect(Collectors.toMap(
                        projection -> new InfoId(projection.infoId()),
                        projection -> projection.count()
                ));
    }
    
    /**
     * '도움이 되는 정보에요' 엔티티를 저장하는 메서드
     *
     * @param helpfulMark '도움이 되는 정보에요' 객체
     * @return 저장 후 {@link HelpfulMarkId}가 부여된 '도움이 되는 정보에요' 객체
     */
    @Override
    public HelpfulMark save(HelpfulMark helpfulMark) {
        HelpfulMarkJpaEntity savedJpaEntity = helpfulMarkRepository.save(helpfulMarkMapper.toJpaEntity(helpfulMark));
        
        return helpfulMarkMapper.toDomain(savedJpaEntity);
    }
    
    /**
     * 정보 ID({@link InfoId})와 회원 ID({@link MemberId})에 해당하는 '도움이 되는 정보에요'를 삭제하는 메서드
     *
     * <p> 해당하는 '도움이 되는 정보에요'가 없으면 아무것도 삭제하지 않는다. </p>
     *
     * @param infoId   정보 ID 객체
     * @param memberId 회원 ID 객체
     */
    @Override
    public void deleteByInfoIdAndMemberId(InfoId infoId, MemberId memberId) {
        helpfulMarkRepository.deleteByInfoIdAndMemberId(infoId.getValue(), memberId.getValue());
    }
}
