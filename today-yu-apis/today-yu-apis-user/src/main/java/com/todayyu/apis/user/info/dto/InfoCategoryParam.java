package com.todayyu.apis.user.info.dto;

import com.todayyu.domain.info.vo.InfoCategory;

public enum InfoCategoryParam {
    ACADEMIC,
    STUDY,
    CAREER,
    SCHOLARSHIP,
    FACILITY,
    WELFARE,
    FOOD_AND_CAFE;
    
    public InfoCategory toDomain() {
        return switch (this) {
            case ACADEMIC -> InfoCategory.ACADEMIC;
            case STUDY -> InfoCategory.STUDY;
            case CAREER -> InfoCategory.CAREER;
            case SCHOLARSHIP -> InfoCategory.SCHOLARSHIP;
            case FACILITY -> InfoCategory.FACILITY;
            case WELFARE -> InfoCategory.WELFARE;
            case FOOD_AND_CAFE -> InfoCategory.FOOD_AND_CAFE;
        };
    }
}
