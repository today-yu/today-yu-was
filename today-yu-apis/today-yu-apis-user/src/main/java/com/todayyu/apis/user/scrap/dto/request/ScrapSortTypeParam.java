package com.todayyu.apis.user.scrap.dto.request;

import com.todayyu.application.scrap.dto.ScrapSortType;

public enum ScrapSortTypeParam {
    LATEST,
    OLDEST,
    ;
    
    public ScrapSortType toDomain() {
        return switch (this) {
            case LATEST -> ScrapSortType.LATEST;
            case OLDEST -> ScrapSortType.OLDEST;
        };
    }
}
