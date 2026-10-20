package com.todayyu.application.member.usecase;

import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.dto.query.GetMyInfoQuery;
import com.todayyu.application.member.dto.result.MyInfoResult;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Usecase
@RequiredArgsConstructor
public class GetMyInfoUsecase {
    private final ReadMemberPort readMemberPort;

    /**
     * 로그인한 회원 본인의 정보 조회 메서드
     *
     * @param query 내 정보 조회 요청 DTO
     * @return 회원 ID, 닉네임, 이메일
     * @throws CustomException 내 정보 조회에 실패한 경우
     * <ul>
     *     <li>{@link BusinessMemberErrorType#NOT_FOUND} - 회원이 존재하지 않을 경우</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public MyInfoResult getMyInfo(GetMyInfoQuery query) {
        MemberId memberId = query.memberId();
        
        Member member = readMemberPort.readByMemberId(memberId)
                .orElseThrow(() -> new CustomException(BusinessMemberErrorType.NOT_FOUND));
        
        return MyInfoResult.from(member);
    }
}
