package com.todayyu.apis.user.scrap.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.common.dto.CursorResponse;
import com.todayyu.apis.user.scrap.dto.request.GetScrapsRequest;
import com.todayyu.apis.user.scrap.dto.response.ScrapCursorResponse;
import com.todayyu.apis.user.scrap.dto.response.ScrapResponse;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.scrap.dto.ScrapCursor;
import com.todayyu.application.scrap.dto.command.CancelScrapCommand;
import com.todayyu.application.scrap.dto.command.ScrapCommand;
import com.todayyu.application.scrap.dto.result.ScrapResult;
import com.todayyu.application.scrap.usecase.CancelScrapUsecase;
import com.todayyu.application.scrap.usecase.GetScrapsUsecase;
import com.todayyu.application.scrap.usecase.ScrapUsecase;
import com.todayyu.domain.info.vo.InfoId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infos")
@RequiredArgsConstructor
public class ScrapController {
    private final GetScrapsUsecase getScrapsUsecase;
    private final ScrapUsecase scrapUsecase;
    private final CancelScrapUsecase cancelScrapUsecase;
    
    @GetMapping("/scraps")
    public ResponseEntity<?> getScraps(
            @Valid GetScrapsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CursorResult<ScrapResult, ScrapCursor> result
                = getScrapsUsecase.getScraps(request.toQuery(userDetails.getMemberId()));
        
        return ResponseEntity.ok(
                SuccessResponse.from(
                        "스크랩 목록 조회에 성공하였습니다.",
                        CursorResponse.of(
                                result,
                                ScrapResponse::from,
                                ScrapCursorResponse::from
                        )
                )
        );
    }
    
    @PutMapping("/{infoId}/scrap")
    public ResponseEntity<?> scrap(
            @PathVariable(name = "infoId") Long infoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        scrapUsecase.scrap(new ScrapCommand(new InfoId(infoId), userDetails.getMemberId()));
        
        return ResponseEntity.ok(SuccessResponse.from("스크랩에 성공하였습니다."));
    }

    @DeleteMapping("/{infoId}/scrap")
    public ResponseEntity<?> cancelScrap(
            @PathVariable(name = "infoId") Long infoId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cancelScrapUsecase.cancelScrap(new CancelScrapCommand(new InfoId(infoId), userDetails.getMemberId()));

        return ResponseEntity.ok(SuccessResponse.from("스크랩 해제에 성공하였습니다."));
    }
}
