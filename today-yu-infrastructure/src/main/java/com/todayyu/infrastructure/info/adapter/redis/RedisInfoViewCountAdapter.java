package com.todayyu.infrastructure.info.adapter.redis;

import com.todayyu.application.info.port.save.SaveInfoViewCountPort;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.infrastructure.common.annotation.Adapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

@Slf4j
@Adapter
@RequiredArgsConstructor
public class RedisInfoViewCountAdapter implements SaveInfoViewCountPort {
    private final RedisInfoViewCountStore infoViewStore;

    /**
     * Redis에 정보 조회수 증가분을 누적하는 메서드
     *
     * <p> 조회수 증가는 부가 기능이므로, Redis 장애({@link DataAccessException})로 실패하더라도
     *     예외를 던지지 않고 로그만 남긴다. </p>
     *
     * @param infoId 정보 ID
     */
    @Override
    public void increase(InfoId infoId) {
        try {
            infoViewStore.increase(String.valueOf(infoId.getValue()));
        } catch (DataAccessException e) {
            log.warn(
                    "[Error/Redis] Redis 조회수 증가에 실패하였습니다. (infoId: {} / message: {})",
                    infoId.getValue(),
                    e.getMessage()
            );
        }
    }
}
