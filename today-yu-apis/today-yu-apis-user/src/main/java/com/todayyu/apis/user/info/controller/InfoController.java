package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.dto.CursorResponse;
import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.info.dto.GetInfosRequest;
import com.todayyu.apis.user.info.dto.InfoCursorResponse;
import com.todayyu.apis.user.info.dto.InfoDetailResponse;
import com.todayyu.apis.user.info.dto.InfoSummaryResponse;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.GetInfoDetailQuery;
import com.todayyu.application.info.dto.InfoCursor;
import com.todayyu.application.info.dto.InfoDetailResult;
import com.todayyu.application.info.dto.InfoSummaryResult;
import com.todayyu.application.info.usecase.GetInfoDetailUsecase;
import com.todayyu.application.info.usecase.GetInfosUsecase;
import com.todayyu.domain.info.vo.InfoId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infos")
@RequiredArgsConstructor
public class InfoController {
    private final GetInfosUsecase getInfosUsecase;
    private final GetInfoDetailUsecase getInfoDetailUsecase;
    
    @GetMapping
    public ResponseEntity<?> getInfos(@Valid GetInfosRequest request) {
        CursorResult<InfoSummaryResult, InfoCursor> result = getInfosUsecase.getInfos(request.toQuery());
        
        return ResponseEntity.ok(
                SuccessResponse.from(
                        "정보 조회에 성공하였습니다.",
                        CursorResponse.of(result, InfoSummaryResponse::from, InfoCursorResponse::from)
                )
        );
    }
    
    @GetMapping("/{infoId}")
    public ResponseEntity<?> getInfoDetail(
            @PathVariable(name = "infoId") Long infoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetInfoDetailQuery query = new GetInfoDetailQuery(new InfoId(infoId), userDetails.getMemberId());
        InfoDetailResult result = getInfoDetailUsecase.getInfoDetail(query);
        
        return ResponseEntity.ok(
                SuccessResponse.from("정보 상세 조회에 성공하였습니다.", InfoDetailResponse.from(result))
        );
    }
}
