package com.todayyu.application.inquiry.dto.query;

import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Builder;

@Builder
public record GetInquiriesQuery(
        MemberId memberId,
        InquiryCursor cursor,
        int size
) {
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    public static final int MIN_SIZE = 1;
    
    public GetInquiriesQuery {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 비어있을 수 없습니다.");
        }
        
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException("조회 크기는 " + MIN_SIZE + " 이상 " + MAX_SIZE + " 이하여야 합니다.");
        }
    }
}
