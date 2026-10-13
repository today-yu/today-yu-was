package com.todayyu.apis.user.member.dto;

import com.todayyu.application.member.dto.command.SignupCommand;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Pattern(regexp = Email.REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 양식이 잘못되었습니다.")
        String email,
        
        
        @NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
        @Size(
                min = Password.MIN_LENGTH, max = Password.MAX_LENGTH,
                message = "비밀번호는 최소 " + Password.MIN_LENGTH + "자 이상, 최대 " + Password.MAX_LENGTH + "자 이하여야 합니다."
        )
        String password
) {
    public SignupCommand toCommand() {
        return new SignupCommand(new Email(email), new Password(password));
    }
}
