package com.todayyu.apis.user.info.dto.request;

import com.todayyu.application.info.dto.command.SubmitInfoRegistrationRequestCommand;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.info.vo.InfoRegistrationRequestContent;
import com.todayyu.domain.info.vo.InfoRegistrationRequestTitle;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SubmitInfoRegistrationRequestRequest(
        @NotNull(message = "정보 카테고리는 비어있을 수 없습니다.")
        InfoCategoryParam category,
        
        @NotBlank(message = "정보 등록 요청 제목은 비어있을 수 없습니다.")
        String title,
        
        @NotBlank(message = "정보 등록 요청 내용은 비어있을 수 없습니다.")
        String content,
        
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Pattern(regexp = Email.REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 양식이 잘못되었습니다.")
        String contactEmail
) {
    public SubmitInfoRegistrationRequestCommand toCommand(MemberId requesterId) {
        return new SubmitInfoRegistrationRequestCommand(
                requesterId,
                category.toDomain(),
                new InfoRegistrationRequestTitle(title),
                new InfoRegistrationRequestContent(content),
                new Email(contactEmail)
        );
    }
}
