package com.todayyu.apis.user.inquiry.dto.request;

import com.todayyu.application.inquiry.dto.command.UpdateInquiryCommand;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.inquiry.vo.InquiryContent;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.inquiry.vo.InquiryTitle;
import com.todayyu.domain.member.vo.MemberId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateInquiryRequest(
        @NotBlank(message = "제목은 비어있을 수 없습니다.")
        String title,
        
        @NotBlank(message = "내용은 비어있을 수 없습니다.")
        String content,
        
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Pattern(regexp = Email.REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 양식이 잘못되었습니다.")
        String contactEmail
) {
    public UpdateInquiryCommand toCommand(Long inquiryId, MemberId memberId) {
        return UpdateInquiryCommand.builder()
                .id(new InquiryId(inquiryId))
                .title(new InquiryTitle(title))
                .content(new InquiryContent(content))
                .contactEmail(new Email(contactEmail))
                .memberId(memberId)
                .build();
    }
}
