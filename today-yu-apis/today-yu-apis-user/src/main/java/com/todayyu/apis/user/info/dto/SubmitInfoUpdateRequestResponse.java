package com.todayyu.apis.user.info.dto;

import com.todayyu.application.info.dto.SubmitInfoUpdateRequestResult;

public record SubmitInfoUpdateRequestResponse(
        Long infoUpdateRequestId
) {
    public static SubmitInfoUpdateRequestResponse from(SubmitInfoUpdateRequestResult result) {
        return new SubmitInfoUpdateRequestResponse(result.id().getValue());
    }
}
