package com.todayyu.apis.user.notice.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.common.dto.CursorResponse;
import com.todayyu.apis.user.notice.dto.request.GetNoticesRequest;
import com.todayyu.apis.user.notice.dto.response.NoticeCursorResponse;
import com.todayyu.apis.user.notice.dto.response.NoticeDetailResponse;
import com.todayyu.apis.user.notice.dto.response.NoticeSummaryResponse;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.notice.dto.query.GetNoticeDetailQuery;
import com.todayyu.application.notice.dto.result.NoticeDetailResult;
import com.todayyu.application.notice.dto.result.NoticeSummaryResult;
import com.todayyu.application.notice.port.dto.NoticeCursor;
import com.todayyu.application.notice.usecase.GetNoticeDetailUsecase;
import com.todayyu.application.notice.usecase.GetNoticesUsecase;
import com.todayyu.domain.notice.vo.NoticeId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final GetNoticeDetailUsecase getNoticeDetailUsecase;
    private final GetNoticesUsecase getNoticesUsecase;
    
    @GetMapping
    public ResponseEntity<?> getNotices(@Valid GetNoticesRequest request) {
        CursorResult<NoticeSummaryResult, NoticeCursor> result = getNoticesUsecase.getNotices(request.toQuery());
        
        return ResponseEntity.ok(
                SuccessResponse.from(
                        "공지 조회에 성공하였습니다.",
                        CursorResponse.of(result, NoticeSummaryResponse::from, NoticeCursorResponse::from)
                )
        );
    }
    
    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable(name = "noticeId") Long noticeId) {
        NoticeDetailResult result
                = getNoticeDetailUsecase.getNoticeDetail(new GetNoticeDetailQuery(new NoticeId(noticeId)));
        
        return ResponseEntity.ok(SuccessResponse.from(
                "공지 상세 조회에 성공하였습니다.",
                NoticeDetailResponse.from(result)
        ));
    }
}
