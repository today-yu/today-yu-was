package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.port.read.ReadInfoViewCountPort;
import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.mapper.InfoViewCountMapper;
import com.todayyu.persistence.info.repository.InfoViewCountJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInfoViewCountAdapter implements ReadInfoViewCountPort {
    private final InfoViewCountJpaRepository infoViewCountRepository;
    private final InfoViewCountMapper infoViewCountMapper;

    /**
     * 정보 ID({@link InfoId})를 통해 정보 조회수({@link InfoViewCount})를 조회해 반환하는 메서드
     *
     * @param infoId 정보 ID 객체
     * @return 정보 조회수 객체
     */
    @Override
    public Optional<InfoViewCount> readByInfoId(InfoId infoId) {
        return infoViewCountRepository.findByInfoId(infoId.getValue())
                .map(infoViewCountMapper::toDomain);
    }
}
