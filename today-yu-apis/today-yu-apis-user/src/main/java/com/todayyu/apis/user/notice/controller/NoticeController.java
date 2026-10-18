package com.todayyu.apis.user.notice.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.notice.dto.response.NoticeDetailResponse;
import com.todayyu.application.notice.dto.query.GetNoticeDetailQuery;
import com.todayyu.application.notice.dto.result.NoticeDetailResult;
import com.todayyu.application.notice.usecase.GetNoticeDetailUsecase;
import com.todayyu.domain.notice.vo.NoticeId;
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
