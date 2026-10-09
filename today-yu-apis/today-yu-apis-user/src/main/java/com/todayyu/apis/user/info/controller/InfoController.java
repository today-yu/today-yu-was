package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.info.dto.InfoDetailResponse;
import com.todayyu.application.info.dto.GetInfoDetailQuery;
import com.todayyu.application.info.dto.InfoDetailResult;
import com.todayyu.application.info.usecase.GetInfoDetailUsecase;
import com.todayyu.domain.info.vo.InfoId;
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
    private final GetInfoDetailUsecase getInfoDetailUsecase;
    
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
