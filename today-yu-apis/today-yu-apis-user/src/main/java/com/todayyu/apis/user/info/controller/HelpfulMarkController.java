package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.info.dto.command.MarkHelpfulCommand;
import com.todayyu.application.info.usecase.MarkHelpfulUsecase;
import com.todayyu.domain.info.vo.InfoId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infos")
@RequiredArgsConstructor
public class HelpfulMarkController {
    private final MarkHelpfulUsecase markHelpfulUsecase;
    
    @PutMapping("/{infoId}/helpful-mark")
    public ResponseEntity<?> markHelpful(
            @PathVariable(name = "infoId") Long infoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        markHelpfulUsecase.markHelpful(new MarkHelpfulCommand(new InfoId(infoId), userDetails.getMemberId()));
        
        return ResponseEntity.ok(SuccessResponse.from("'도움이 되는 정보에요' 표시에 성공하였습니다."));
    }
}
