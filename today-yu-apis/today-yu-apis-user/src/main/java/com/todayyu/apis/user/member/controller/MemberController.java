package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.member.dto.request.ChangePasswordRequest;
import com.todayyu.apis.user.member.dto.response.MyInfoResponse;
import com.todayyu.application.member.dto.query.GetMyInfoQuery;
import com.todayyu.application.member.dto.result.MyInfoResult;
import com.todayyu.application.member.usecase.ChangePasswordUsecase;
import com.todayyu.application.member.usecase.GetMyInfoUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final GetMyInfoUsecase getMyInfoUsecase;
    private final ChangePasswordUsecase changePasswordUsecase;
    
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyInfoResult result = getMyInfoUsecase.getMyInfo(new GetMyInfoQuery(userDetails.getMemberId()));
        
        return ResponseEntity.ok(
                SuccessResponse.from("내 정보 조회에 성공하였습니다.", MyInfoResponse.from(result))
        );
    }
    
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        changePasswordUsecase.change(request.toCommand(userDetails.getMemberId()));
        
        return ResponseEntity.ok(SuccessResponse.from("비밀번호 변경에 성공하였습니다."));
    }
}
