package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.NicknamePort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.application.member.util.NicknameModifierGenerator;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.NicknameModifier;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class SocialSignupUsecase {
    private final SaveMemberPort saveMemberPort;
    private final NicknamePort nicknamePort;
    
    /**
     * 소셜 회원가입 메서드
     *
     * <p> 인증 제공자 내 사용자 식별 정보({@link SocialIdentity})로 소셜 계정({@link SocialAccount})을 가진 회원을 저장한다. </p>
     *
     * @param identity 인증 제공자 내 사용자 식별 정보
     * @return 저장된 회원 {@link Member} 객체
     */
    @Transactional
    public Member signup(SocialIdentity identity) {
        Nickname nickname = generateNickname();
        Member member = Member.withoutId(null, nickname, Role.MEMBER, SocialAccount.withoutId(identity));

        return saveMemberPort.save(member);
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
