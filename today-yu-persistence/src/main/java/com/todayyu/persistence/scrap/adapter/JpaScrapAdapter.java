package com.todayyu.persistence.scrap.adapter;

import com.todayyu.application.scrap.port.read.ReadScrapPort;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import com.todayyu.domain.scrap.vo.ScrapId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;
import com.todayyu.persistence.scrap.mapper.ScrapMapper;
import com.todayyu.persistence.scrap.repository.ScrapJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaScrapAdapter implements ReadScrapPort, SaveScrapPort {
    private final ScrapMapper scrapMapper;
    private final ScrapJpaRepository scrapRepository;
    
    /**
     * 정보 ID({@link InfoId})와 회원 ID({@link MemberId})를 통해 회원이 해당 정보를 스크랩했는지 조회하는 메서드
     *
     * @param infoId   정보 ID 객체
     * @param memberId 회원 ID 객체
     * @return 스크랩했으면 {@code true}, 아니면 {@code false}
     */
    @Override
    public boolean existsByInfoIdAndMemberId(InfoId infoId, MemberId memberId) {
        return scrapRepository.existsByInfoIdAndMemberId(infoId.getValue(), memberId.getValue());
    }
    
    /**
     * 회원 ID({@link MemberId})와 정보 ID({@link InfoId}) 목록을 통해 회원이 스크랩한 정보 ID를 조회하는 메서드
     *
     * <p> 정보 ID 목록 중 회원이 스크랩한 정보 ID만 반환한다. </p>
     *
     * @param memberId 회원 ID 객체
     * @param infoIds  정보 ID 객체 목록
     * @return 회원이 스크랩한 정보 ID 집합
     */
    @Override
    public Set<InfoId> readScrappedInfoIds(MemberId memberId, List<InfoId> infoIds) {
        List<Long> scrappedInfoIds = scrapRepository
                .findInfoIdsByMemberIdAndInfoIds(memberId.getValue(), infoIds.stream().map(InfoId::getValue).toList());
        
        return scrappedInfoIds.stream()
                .map(InfoId::new)
                .collect(Collectors.toSet());
    }
    
    /**
     * 스크랩을 저장하는 메서드
     *
     * @param scrap 스크랩 객체
     * @return 저장 후 {@link ScrapId}가 부여된 스크랩 객체
     */
    @Override
    public Scrap save(Scrap scrap) {
        ScrapJpaEntity savedJpaEntity = scrapRepository.save(scrapMapper.toJpaEntity(scrap));
        
        return scrapMapper.toDomain(savedJpaEntity);
    }
    
    /**
     * 정보 ID({@link InfoId})와 회원 ID({@link MemberId})에 해당하는 스크랩을 삭제하는 메서드
     *
     * <p> 해당하는 스크랩이 없으면 아무것도 삭제하지 않는다. </p>
     *
     * @param infoId   정보 ID 객체
     * @param memberId 회원 ID 객체
     */
    @Override
    public void deleteByInfoIdAndMemberId(InfoId infoId, MemberId memberId) {
        scrapRepository.deleteByInfoIdAndMemberId(infoId.getValue(), memberId.getValue());
    }
}
