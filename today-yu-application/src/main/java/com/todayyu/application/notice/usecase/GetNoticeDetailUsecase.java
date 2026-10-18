package com.todayyu.application.notice.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.notice.dto.query.GetNoticeDetailQuery;
import com.todayyu.application.notice.dto.result.NoticeDetailResult;
import com.todayyu.application.notice.error.BusinessNoticeErrorType;
import com.todayyu.application.notice.port.read.ReadNoticePort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.notice.entity.Notice;
import lombok.RequiredArgsConstructor;

@Usecase
@RequiredArgsConstructor
public class GetNoticeDetailUsecase {
    private final ReadNoticePort readNoticePort;

    /**
     * 공지 상세 조회 메서드
     *
     * @param query 공지 상세 조회 요청 DTO
     * @return 공지 상세 정보
     * @throws CustomException 공지 상세 조회에 실패한 경우
     * <ul>
     *     <li>{@link BusinessNoticeErrorType#NOT_FOUND} - 공지가 존재하지 않을 경우</li>
     * </ul>
     */
    public NoticeDetailResult getNoticeDetail(GetNoticeDetailQuery query) {
        Notice notice = readNoticePort.readById(query.id())
                .orElseThrow(() -> new CustomException(BusinessNoticeErrorType.NOT_FOUND));
        
        return NoticeDetailResult.from(notice);
    }
}
