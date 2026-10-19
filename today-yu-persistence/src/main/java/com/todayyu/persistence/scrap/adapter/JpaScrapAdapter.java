package com.todayyu.persistence.scrap.adapter;

import com.todayyu.application.scrap.dto.ScrapCursor;
import com.todayyu.application.scrap.dto.ScrapSortType;
import com.todayyu.application.scrap.port.dto.ScrapView;
import com.todayyu.application.scrap.port.read.ReadScrapPort;
import com.todayyu.application.scrap.port.save.SaveScrapPort;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.entity.Scrap;
import com.todayyu.domain.scrap.vo.ScrapId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.scrap.dto.ScrapViewProjection;
import com.todayyu.persistence.scrap.entity.ScrapJpaEntity;
import com.todayyu.persistence.scrap.mapper.ScrapMapper;
import com.todayyu.persistence.scrap.repository.ScrapJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
     * 카테고리({@link InfoCategory})와 정렬 기준({@link ScrapSortType})에 따라 커서({@link ScrapCursor}) 이후의 회원이 스크랩한 정보 목록을 조회하는 메서드
     *
     * <p> 카테고리가 {@code null}이면 전체 카테고리를 조회하고, 커서가 {@code null}이면 처음부터 조회한다. </p>
     *
     * @param memberId 회원 ID 객체
     * @param category 정보 카테고리
     * @param sortType 정렬 기준
     * @param cursor   마지막으로 조회한 정보의 커서
     * @param limit    최대 조회 개수
     * @return 스크랩한 정보 요약 목록
     */
    @Override
    public List<ScrapView> readByCursor(
            MemberId memberId,
            InfoCategory category,
            ScrapSortType sortType,
            ScrapCursor cursor,
            int limit
    ) {
        LocalDateTime cursorUpdatedAt = (cursor == null) ? null : cursor.updatedAt();
        Long cursorInfoId = (cursor == null) ? null : cursor.infoId().getValue();
        
        List<ScrapViewProjection> projections = scrapRepository.findByCursor(
                memberId.getValue(), category, sortType, cursorUpdatedAt, cursorInfoId, limit
        );
        
        return projections.stream()
                .map(ScrapViewProjection::toView)
                .toList();
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
