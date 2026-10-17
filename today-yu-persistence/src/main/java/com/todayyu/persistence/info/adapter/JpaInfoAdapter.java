package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.port.dto.InfoCursor;
import com.todayyu.application.info.port.dto.InfoSortType;
import com.todayyu.application.info.port.dto.InfoSummaryView;
import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.dto.InfoSummaryViewProjection;
import com.todayyu.persistence.info.mapper.InfoMapper;
import com.todayyu.persistence.info.repository.InfoJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoAdapter implements ReadInfoPort {
    private final InfoJpaRepository infoRepository;
    private final InfoMapper infoMapper;

    /**
     * 정보 ID({@link InfoId})를 통해 정보({@link Info})를 조회해 반환하는 메서드
     *
     * @param id 정보 ID 객체
     * @return 정보 객체
     */
    @Override
    public Optional<Info> readById(InfoId id) {
        return infoRepository.findById(id.getValue())
                .map(infoMapper::toDomain);
    }

    /**
     * 카테고리({@link InfoCategory})와 정렬 기준({@link InfoSortType})에 따라 커서({@link InfoCursor}) 이후의 정보 목록을 조회하는 메서드
     *
     * <p> 카테고리가 {@code null}이면 전체 카테고리를 조회하고, 커서가 {@code null}이면 처음부터 조회한다. </p>
     *
     * @param category 정보 카테고리
     * @param sortType 정렬 기준
     * @param cursor   마지막으로 조회한 정보의 커서
     * @param limit    최대 조회 개수
     * @return 정보 요약 목록
     */
    @Override
    public List<InfoSummaryView> readByCursor(
            InfoCategory category,
            InfoSortType sortType,
            InfoCursor cursor,
            int limit
    ) {
        LocalDateTime cursorUpdatedAt = (cursor == null) ? null : cursor.updatedAt();
        Long cursorId = (cursor == null) ? null : cursor.infoId().getValue();
        
        List<InfoSummaryViewProjection> projections = infoRepository
                .findByCursor(category, sortType, cursorUpdatedAt, cursorId, limit);
        
        return projections.stream()
                .map(InfoSummaryViewProjection::toView)
                .toList();
    }
    
    /**
     * 정보 ID({@link InfoId})를 통해 정보의 존재 여부를 반환하는 메서드
     *
     * @param id 정보 ID 객체
     * @return 정보 존재 여부
     */
    @Override
    public boolean existsById(InfoId id) {
        return infoRepository.existsById(id.getValue());
    }
}
