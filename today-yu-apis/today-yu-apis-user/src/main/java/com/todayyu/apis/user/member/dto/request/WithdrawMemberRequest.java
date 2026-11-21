package com.todayyu.apis.user.member.dto.request;

import com.todayyu.application.member.dto.command.WithdrawMemberCommand;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Password;
import jakarta.validation.constraints.Size;

public record WithdrawMemberRequest(
        @Size(
                min = Password.MIN_LENGTH, max = Password.MAX_LENGTH,
                message = "비밀번호는 최소 " + Password.MIN_LENGTH + "자 이상, 최대 " + Password.MAX_LENGTH + "자 이하여야 합니다."
        )
        String password
) {
    public WithdrawMemberCommand toCommand(MemberId memberId) {
        Password passwordCommand = (password == null || password.isBlank()) ? null : new Password(password);
        return new WithdrawMemberCommand(memberId, passwordCommand);
    }
}
