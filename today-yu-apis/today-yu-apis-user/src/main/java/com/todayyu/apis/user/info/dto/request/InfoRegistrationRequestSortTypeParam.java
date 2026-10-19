package com.todayyu.apis.user.info.dto.request;

import com.todayyu.application.info.dto.InfoRegistrationRequestSortType;

public enum InfoRegistrationRequestSortTypeParam {
    LATEST,
    OLDEST,
    ;
    
    public InfoRegistrationRequestSortType toDomain() {
        return switch (this) {
            case LATEST -> InfoRegistrationRequestSortType.LATEST;
            case OLDEST -> InfoRegistrationRequestSortType.OLDEST;
        };
    }
}
