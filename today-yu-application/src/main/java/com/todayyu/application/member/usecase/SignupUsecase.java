package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.NicknamePort;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.dto.command.SignupCommand;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.application.member.util.NicknameModifierGenerator;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.NicknameModifier;
import com.todayyu.domain.member.vo.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class SignupUsecase {
    private final ReadMemberPort readMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordPort passwordPort;
    private final NicknamePort nicknamePort;
    
    /**
     * 회원가입 메서드
     *
     * @param command 회원가입 요청 DTO
     * @throws CustomException - 이미 존재하는 이메일일 경우
     */
    @Transactional
    public void signup(SignupCommand command) {
        if (readMemberPort.existsByEmail(command.email())) {
            throw new CustomException(BusinessMemberErrorType.DUPLICATED_EMAIL);
        }
        
        EncodedPassword encodedPassword = passwordPort.encode(command.password());
        LocalAccount account = LocalAccount.withoutId(encodedPassword);
        Nickname nickname = generateNickname();
        
        Member newMember = Member.withoutId(command.email(), nickname, Role.MEMBER, account);
        
        saveMemberPort.save(newMember);
    }
    
    /**
     * 닉네임 생성 메서드
     *
     * <p> 닉네임 수식어({@link NicknameModifier})와 해당 수식어의 마지막 가입 번호를 통해 닉네임을 생성한다. </p>
     *
     * @return 닉네임 객체
     */
    private Nickname generateNickname() {
        NicknameModifier modifier = NicknameModifierGenerator.generate();
        long next = nicknamePort.issueNext(modifier);
        return Nickname.from(modifier, next);
    }
}
