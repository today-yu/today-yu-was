package com.todayyu.apis.user.info.dto;

import com.todayyu.application.info.dto.command.SubmitInfoUpdateRequestCommand;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoUpdateRequestContent;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record SubmitInfoUpdateRequestRequest(
        
        @NotBlank(message = "정보 수정 요청 내용은 비어있을 수 없습니다.")
        String content,
        
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Pattern(regexp = Email.REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 양식이 잘못되었습니다.")
        String contactEmail,
        
        @NotNull(message = "수정 정보 확인 시간은 비어있을 수 없습니다.")
        LocalDateTime observedAt
) {
    public SubmitInfoUpdateRequestCommand toCommand(Long infoId, MemberId requesterId) {
        return new SubmitInfoUpdateRequestCommand(
                requesterId,
                new InfoId(infoId),
                new InfoUpdateRequestContent(content),
                new Email(contactEmail),
                observedAt
        );
    }
}
