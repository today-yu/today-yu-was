package com.todayyu.apis.user.info.dto.response;

import com.todayyu.application.info.dto.result.SubmitInfoRegistrationRequestResult;

public record SubmitInfoRegistrationRequestResponse(
        Long infoRegistrationRequestId
) {
    public static SubmitInfoRegistrationRequestResponse from(SubmitInfoRegistrationRequestResult result) {
        return new SubmitInfoRegistrationRequestResponse(result.id().getValue());
    }
}
