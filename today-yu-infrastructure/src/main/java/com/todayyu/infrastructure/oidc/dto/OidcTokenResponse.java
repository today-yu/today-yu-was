package com.todayyu.infrastructure.oidc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OidcTokenResponse(
        @JsonProperty(value = "id_token")
        String idToken
) {
}
