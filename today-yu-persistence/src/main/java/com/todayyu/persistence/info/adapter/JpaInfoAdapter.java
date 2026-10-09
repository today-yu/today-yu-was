package com.todayyu.persistence.info.adapter;

import com.todayyu.application.info.port.read.ReadInfoPort;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.info.mapper.InfoMapper;
import com.todayyu.persistence.info.repository.InfoJpaRepository;
import lombok.RequiredArgsConstructor;

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
}
