package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.dto.InfoUpdateRequestView;
import com.todayyu.application.info.port.read.ReadInfoUpdateRequestPort;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.mapper.InfoUpdateRequestMapper;
import com.todayyu.persistence.info.repository.InfoUpdateRequestJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoUpdateRequestAdapter implements ReadInfoUpdateRequestPort {
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
}
