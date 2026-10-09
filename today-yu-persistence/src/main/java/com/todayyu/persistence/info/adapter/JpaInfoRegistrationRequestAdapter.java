package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoRegistrationRequestView;
import com.todayyu.application.info.port.read.ReadInfoRegistrationRequestPort;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.mapper.InfoRegistrationRequestMapper;
import com.todayyu.persistence.info.repository.InfoRegistrationRequestJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoRegistrationRequestAdapter implements ReadInfoRegistrationRequestPort {
    private final InfoRegistrationRequestJpaRepository infoRegistrationRequestRepository;
    private final InfoRegistrationRequestMapper infoRegistrationRequestMapper;
    
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
}
