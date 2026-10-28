package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.command.ChangePasswordCommand;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class ChangePasswordUsecase {
    private final PasswordPort passwordPort;
    private final ReadMemberPort readMemberPort;
    private final SaveMemberPort saveMemberPort;
    
    /**
     * 로그인한 회원의 비밀번호를 변경하는 메서드
     *
     * <p> 현재 비밀번호가 일치하는 경우에만 새 비밀번호를 암호화하여 변경한다. </p>
     *
     * @param command 비밀번호 변경 요청 DTO
     * @throws CustomException 비밀번호 변경에 실패한 경우
     * <ul>
     *     <li>{@link BusinessMemberErrorType#NOT_FOUND} - 회원이 존재하지 않는 경우</li>
     *     <li>{@link BusinessMemberErrorType#NO_LOCAL_ACCOUNT} - 로컬 계정이 존재하지 않는 경우</li>
     *     <li>{@link BusinessMemberErrorType#CURRENT_PASSWORD_MISMATCH} - 현재 비밀번호가 일치하지 않는 경우</li>
     * </ul>
     */
    @Transactional
    public void change(ChangePasswordCommand command) {
        MemberId memberId = command.memberId();
        Password currentPassword = command.currentPassword();
        Password newPassword = command.newPassword();
        
        Member member = readMemberPort.readByMemberId(memberId)
                .orElseThrow(() -> new CustomException(BusinessMemberErrorType.NOT_FOUND));
        
        EncodedPassword encodedPassword = member.getEncodedPassword()
                .orElseThrow(() -> new CustomException(BusinessMemberErrorType.NO_LOCAL_ACCOUNT));
        
        if (!passwordPort.matches(currentPassword, encodedPassword)) {
            throw new CustomException(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH);
        }
        
        EncodedPassword encodedNewPassword = passwordPort.encode(newPassword);
        member.changePassword(encodedNewPassword);
        
        saveMemberPort.update(member);
    }
}
