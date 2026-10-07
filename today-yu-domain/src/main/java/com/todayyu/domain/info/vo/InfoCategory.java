package com.todayyu.domain.info.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InfoCategory {
    ACADEMIC("학사"),
    STUDY("학업"),
    CAREER("취업"),
    SCHOLARSHIP("장학"),
    FACILITY("시설"),
    WELFARE("복지"),
    FOOD_AND_CAFE("맛집·카페"),
    ;
    
    private final String value;
}
