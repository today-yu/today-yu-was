package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoUpdateRequestCursor;
import com.todayyu.application.info.dto.InfoUpdateRequestSortType;
import com.todayyu.application.info.port.dto.InfoUpdateRequestSummaryView;
import com.todayyu.application.info.port.dto.InfoUpdateRequestView;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.application.info.port.save.SaveInfoUpdateRequestPort;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.dto.InfoUpdateRequestSummaryViewProjection;
import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;
import com.todayyu.persistence.info.mapper.InfoUpdateRequestMapper;
import com.todayyu.persistence.info.repository.InfoUpdateRequestJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoUpdateRequestAdapter implements ReadInfoUpdateRequestPort, SaveInfoUpdateRequestPort {
    private final InfoUpdateRequestJpaRepository infoUpdateRequestRepository;
    private final InfoUpdateRequestMapper infoUpdateRequestMapper;
    
    /**
     * 정보 수정 요청 ID({@link InfoUpdateRequestId})를 통해 정보 수정 요청을 조회하는 메서드
     *
     * @param id 정보 수정 요청 ID
     * @return 정보 수정 요청
     */
    @Override
    public Optional<InfoUpdateRequest> readById(InfoUpdateRequestId id) {
        return infoUpdateRequestRepository.findById(id.getValue())
                .map(infoUpdateRequestMapper::toDomain);
    }
    
    /**
     * 정보 ID({@link InfoId})와 요청 상태({@link InfoRequestStatus})를 통해 정보 수정 요청 목록을
     * 요청자 닉네임과 함께 조회하는 메서드
     *
     * @param infoId 정보 ID 객체
     * @param status 정보 요청 상태
     * @return 요청자 닉네임이 포함된 정보 수정 요청 목록
     */
    @Override
    public List<InfoUpdateRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status) {
        return infoUpdateRequestRepository
                .findWithRequesterNicknameByInfoIdAndStatus(infoId.getValue(), status).stream()
                .map(projection -> new InfoUpdateRequestView(
                        infoUpdateRequestMapper.toDomain(projection.request()),
                        new Nickname(projection.requesterNickname())
                ))
                .toList();
    }
    
    /**
     * 요청자 ID({@link MemberId}), 카테고리({@link InfoCategory}), 정렬 기준({@link InfoUpdateRequestSortType})에 따라
     * 커서({@link InfoUpdateRequestCursor}) 이후의 정보 수정 요청 목록을 조회하는 메서드
     *
     * <p> 각 정보 수정 요청의 카테고리·제목은 연결된 정보의 카테고리·제목이다. </p>
     *
     * <p> 카테고리가 {@code null}이면 전체 카테고리를 조회하고, 커서가 {@code null}이면 처음부터 조회한다. </p>
     *
     * @param requesterId 정보 수정 요청자 ID
     * @param category    정보 카테고리
     * @param sortType    정렬 기준
     * @param cursor      마지막으로 조회한 정보 수정 요청의 커서
     * @param limit       최대 조회 개수
     * @return 정보 수정 요청 요약 목록
     */
    @Override
    public List<InfoUpdateRequestSummaryView> readByCursor(
            MemberId requesterId,
            InfoCategory category,
            InfoUpdateRequestSortType sortType,
            InfoUpdateRequestCursor cursor,
            int limit
    ) {
        LocalDateTime cursorRequestedAt = (cursor == null) ? null : cursor.requestedAt();
        Long cursorId = (cursor == null) ? null : cursor.id().getValue();
        
        List<InfoUpdateRequestSummaryViewProjection> projections = infoUpdateRequestRepository
                .findByCursor(requesterId.getValue(), category, sortType, cursorRequestedAt, cursorId, limit);
        
        return projections.stream()
                .map(InfoUpdateRequestSummaryViewProjection::toView)
                .toList();
    }
    
    /**
     * 정보 수정 요청을 저장하는 메서드
     *
     * @param infoUpdateRequest 정보 수정 요청 객체
     * @return 저장 후 {@link InfoUpdateRequestId}가 부여된 정보 수정 요청 객체
     */
    @Override
    public InfoUpdateRequest save(InfoUpdateRequest infoUpdateRequest) {
        InfoUpdateRequestJpaEntity savedJpaEntity
                = infoUpdateRequestRepository.save(infoUpdateRequestMapper.toJpaEntity(infoUpdateRequest));
        
        return infoUpdateRequestMapper.toDomain(savedJpaEntity);
    }
}
