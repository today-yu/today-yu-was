package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.member.dto.request.ChangePasswordRequest;
import com.todayyu.apis.user.member.dto.request.WithdrawMemberRequest;
import com.todayyu.apis.user.member.dto.response.MyInfoResponse;
import com.todayyu.application.member.dto.command.WithdrawMemberCommand;
import com.todayyu.application.member.dto.query.GetMyInfoQuery;
import com.todayyu.application.member.dto.result.MyInfoResult;
import com.todayyu.application.member.usecase.ChangePasswordUsecase;
import com.todayyu.application.member.usecase.GetMyInfoUsecase;
import com.todayyu.application.member.usecase.WithdrawMemberUsecase;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final GetMyInfoUsecase getMyInfoUsecase;
    private final ChangePasswordUsecase changePasswordUsecase;
    private final WithdrawMemberUsecase withdrawMemberUsecase;
    
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
    
    @PostMapping("/me/withdrawal")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody(required = false) WithdrawMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberId memberId = userDetails.getMemberId();
        WithdrawMemberCommand command = (request == null)
                ? new WithdrawMemberCommand(memberId, null)
                : request.toCommand(memberId);
        
        withdrawMemberUsecase.withdraw(command);
    
        return ResponseEntity.ok(SuccessResponse.from("회원 탈퇴에 성공하였습니다."));
    }
}
