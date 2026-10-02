package com.todayyu.domain.member.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NicknameModifier {
    KIND("착한"),
    GENTLE("다정한"),
    GENEROUS("너그러운"),
    TRUSTWORTHY("믿음직한"),
    WARM("따뜻한"),
    MILD("온화한"),
    FRIENDLY("친절한"),
    AMIABLE("상냥한"),
    COOL("멋진"),
    BRAVE("용감한"),
    DEPENDABLE("듬직한"),
    PRETTY("예쁜"),
    LOVELY("사랑스러운"),
    CUTE("귀여운"),
    CHEERFUL("명랑한"),
    SUNNY("해맑은"),
    LIVELY("활기찬"),
    POSITIVE("긍정적인"),
    HAPPY("행복한"),
    PASSIONATE("열정적인"),
    DILIGENT("성실한"),
    ;
    
    private final String value;
    
    
}
