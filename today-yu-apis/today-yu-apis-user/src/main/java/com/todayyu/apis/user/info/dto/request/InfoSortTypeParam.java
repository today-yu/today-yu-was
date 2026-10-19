package com.todayyu.apis.user.info.dto.request;

import com.todayyu.application.info.dto.InfoSortType;

public enum InfoSortTypeParam {
    LATEST,
    OLDEST;
    
    public InfoSortType toDomain() {
        return switch(this) {
            case LATEST -> InfoSortType.LATEST;
            case OLDEST -> InfoSortType.OLDEST;
        };
    }
}
