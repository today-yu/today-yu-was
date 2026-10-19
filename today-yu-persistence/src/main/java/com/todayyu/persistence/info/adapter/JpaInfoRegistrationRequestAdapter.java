package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSortType;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestSummaryView;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestView;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.application.info.port.save.SaveInfoRegistrationRequestPort;
import com.todayyu.domain.info.entity.InfoRegistrationRequest;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestSummaryViewProjection;
import com.todayyu.persistence.info.entity.InfoRegistrationRequestJpaEntity;
import com.todayyu.persistence.info.mapper.InfoRegistrationRequestMapper;
import com.todayyu.persistence.info.repository.InfoRegistrationRequestJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoRegistrationRequestAdapter implements ReadInfoRegistrationRequestPort, SaveInfoRegistrationRequestPort {
    private final InfoRegistrationRequestJpaRepository infoRegistrationRequestRepository;
    private final InfoRegistrationRequestMapper infoRegistrationRequestMapper;
    
    /**
     * 정보 등록 요청 ID({@link InfoRegistrationRequestId})를 통해 정보 등록 요청을 조회하는 메서드
     *
     * @param id 정보 등록 요청 ID
     * @return 정보 등록 요청
     */
    @Override
    public Optional<InfoRegistrationRequest> readById(InfoRegistrationRequestId id) {
        return infoRegistrationRequestRepository.findById(id.getValue())
                .map(infoRegistrationRequestMapper::toDomain);
    }
    
    /**
     * 정보 ID({@link InfoId})와 요청 상태({@link InfoRequestStatus})를 통해 정보 등록 요청 목록을
     * 요청자 닉네임과 함께 조회하는 메서드
     *
     * @param infoId 정보 ID 객체
     * @param status 정보 요청 상태
     * @return 요청자 닉네임이 포함된 정보 등록 요청 목록
     */
    @Override
    public List<InfoRegistrationRequestView> readByInfoIdAndStatus(InfoId infoId, InfoRequestStatus status) {
        return infoRegistrationRequestRepository
                .findWithRequesterNicknameByInfoIdAndStatus(infoId.getValue(), status).stream()
                .map(projection -> new InfoRegistrationRequestView(
                        infoRegistrationRequestMapper.toDomain(projection.request()),
                        new Nickname(projection.requesterNickname())
                ))
                .toList();
    }
    
    /**
     * 요청자 ID({@link MemberId}), 카테고리({@link InfoCategory}), 정렬 기준({@link InfoRegistrationRequestSortType})에 따라
     * 커서({@link InfoRegistrationRequestCursor}) 이후의 정보 등록 요청 목록을 조회하는 메서드
     *
     * <p> 카테고리가 {@code null}이면 전체 카테고리를 조회하고, 커서가 {@code null}이면 처음부터 조회한다. </p>
     *
     * @param requesterId 정보 등록 요청자 ID
     * @param category    정보 카테고리
     * @param sortType    정렬 기준
     * @param cursor      마지막으로 조회한 정보 등록 요청의 커서
     * @param limit       최대 조회 개수
     * @return 정보 등록 요청 요약 목록
     */
    @Override
    public List<InfoRegistrationRequestSummaryView> readByCursor(
            MemberId requesterId,
            InfoCategory category,
            InfoRegistrationRequestSortType sortType,
            InfoRegistrationRequestCursor cursor,
            int limit
    ) {
        LocalDateTime cursorRequestedAt = (cursor == null) ? null : cursor.requestedAt();
        Long cursorId = (cursor == null) ? null : cursor.id().getValue();
        
        List<InfoRegistrationRequestSummaryViewProjection> projections = infoRegistrationRequestRepository
                .findByCursor(requesterId.getValue(), category, sortType, cursorRequestedAt, cursorId, limit);
        
        return projections.stream()
                .map(InfoRegistrationRequestSummaryViewProjection::toView)
                .toList();
    }
    
    /**
     * 정보 등록 요청을 저장하는 메서드
     *
     * @param infoRegistrationRequest 정보 등록 요청 객체
     * @return 저장 후 {@link InfoRegistrationRequestId}가 부여된 정보 등록 요청 객체
     */
    @Override
    public InfoRegistrationRequest save(InfoRegistrationRequest infoRegistrationRequest) {
        InfoRegistrationRequestJpaEntity savedRequest = infoRegistrationRequestRepository.save(
                infoRegistrationRequestMapper.toJpaEntity(infoRegistrationRequest)
        );
        
        return infoRegistrationRequestMapper.toDomain(savedRequest);
    }
}
