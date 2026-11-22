package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.dto.InfoRegistrationRequestSortType;
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
     * 회원 ID({@link MemberId})를 통해 해당 회원이 요청한 모든 정보 등록 요청을 조회하는 메서드
     *
     * @param memberId 정보 등록 요청자 ID
     * @return 해당 회원의 정보 등록 요청 목록 (요청 상태와 무관하게 전부)
     */
    @Override
    public List<InfoRegistrationRequest> readAllByMemberId(MemberId memberId) {
        return infoRegistrationRequestRepository.findAllByRequesterId(memberId.getValue()).stream()
                .map(infoRegistrationRequestMapper::toDomain)
                .toList();
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
    
    /**
     * 정보 등록 요청 ID({@link InfoRegistrationRequestId}) 목록에 해당하는 정보 등록 요청을 일괄 삭제하는 메서드
     *
     * <p> 영속성 컨텍스트를 거치지 않고 단일 DELETE 쿼리로 삭제하며, 목록이 비어있으면 쿼리를 실행하지 않는다. </p>
     *
     * @param ids 삭제할 정보 등록 요청 ID 목록
     */
    @Override
    public void deleteByIds(List<InfoRegistrationRequestId> ids) {
        if (ids.isEmpty()) return;
        
        List<Long> requestIds = ids.stream().map(id -> id.getValue()).toList();
        infoRegistrationRequestRepository.deleteAllByIdInBatch(requestIds);
    }
    
    /**
     * 비식별화 처리된 정보 등록 요청 목록을 저장하는 메서드
     *
     * <p> 요청 ID로 JPA 엔티티를 조회한 후 연락 이메일을 제거하며, 더티 체킹을 통해 갱신된다.
     *     목록이 비어있으면 조회하지 않는다. </p>
     *
     * @param requests 도메인에서 비식별화({@link InfoRegistrationRequest#anonymize()}) 처리한 정보 등록 요청 목록
     * @throws IllegalStateException 비식별화 처리하지 않은 정보 등록 요청이 포함된 경우
     */
    @Override
    public void anonymizeAll(List<InfoRegistrationRequest> requests) {
        if (requests.isEmpty()) return;
        
        if (!requests.stream().allMatch(request -> request.isAnonymized())) {
            throw new IllegalStateException("비식별화 처리하지 않은 정보 등록 요청이 존재합니다.");
        }
        
        List<Long> ids = requests.stream()
                .map(request -> request.getId().getValue())
                .toList();
        
        infoRegistrationRequestRepository.findAllById(ids)
                .forEach(request -> request.anonymize());
    }
}
