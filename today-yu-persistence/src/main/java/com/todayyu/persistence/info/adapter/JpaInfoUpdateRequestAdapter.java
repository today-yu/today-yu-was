package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoUpdateRequestView;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.application.info.port.save.SaveInfoUpdateRequestPort;
import com.todayyu.domain.info.entity.InfoUpdateRequest;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.info.vo.InfoUpdateRequestId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.entity.InfoUpdateRequestJpaEntity;
import com.todayyu.persistence.info.mapper.InfoUpdateRequestMapper;
import com.todayyu.persistence.info.repository.InfoUpdateRequestJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoUpdateRequestAdapter implements ReadInfoUpdateRequestPort, SaveInfoUpdateRequestPort {
    private final InfoUpdateRequestJpaRepository infoUpdateRequestRepository;
    private final InfoUpdateRequestMapper infoUpdateRequestMapper;

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
     * 정보 수정 요청을 저장하는 메서드
     *
     * @param infoUpdateRequest 정보 수정 요청 객체
     * @return 저장 후 {@link InfoUpdateRequestId}가 부여된 정보 등록 요청 객체
     */
    @Override
    public InfoUpdateRequest save(InfoUpdateRequest infoUpdateRequest) {
        InfoUpdateRequestJpaEntity savedJpaEntity
                = infoUpdateRequestRepository.save(infoUpdateRequestMapper.toJpaEntity(infoUpdateRequest));
        
        return infoUpdateRequestMapper.toDomain(savedJpaEntity);
    }
}
