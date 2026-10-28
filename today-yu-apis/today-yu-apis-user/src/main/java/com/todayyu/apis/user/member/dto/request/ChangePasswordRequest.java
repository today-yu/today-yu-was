package com.todayyu.apis.user.member.dto.request;

import com.todayyu.application.member.dto.command.ChangePasswordCommand;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
        @Size(
                min = Password.MIN_LENGTH, max = Password.MAX_LENGTH,
                message = "비밀번호는 최소 " + Password.MIN_LENGTH + "자 이상, 최대 " + Password.MAX_LENGTH + "자 이하여야 합니다."
        )
        String currentPassword,
        
        @NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
        @Size(
                min = Password.MIN_LENGTH, max = Password.MAX_LENGTH,
                message = "비밀번호는 최소 " + Password.MIN_LENGTH + "자 이상, 최대 " + Password.MAX_LENGTH + "자 이하여야 합니다."
        )
        String newPassword
) {
    public ChangePasswordCommand toCommand(MemberId memberId) {
        return new ChangePasswordCommand(memberId, new Password(currentPassword), new Password(newPassword));
    }
}
