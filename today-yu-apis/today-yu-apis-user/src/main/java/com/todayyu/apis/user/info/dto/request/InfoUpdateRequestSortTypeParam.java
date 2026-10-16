package com.todayyu.apis.user.info.dto.request;

import com.todayyu.application.info.port.dto.InfoUpdateRequestSortType;

public enum InfoUpdateRequestSortTypeParam {
    LATEST,
    OLDEST,
    ;
    
    public InfoUpdateRequestSortType toDomain() {
        return switch (this) {
            case LATEST -> InfoUpdateRequestSortType.LATEST;
            case OLDEST -> InfoUpdateRequestSortType.OLDEST;
        };
    }
}
