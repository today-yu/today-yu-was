package com.todayyu.application.auth.dto;

import com.todayyu.domain.member.vo.MemberId;

import java.time.Instant;

public record RefreshTokenClaims(
        MemberId memberId,
        Instant expiresAt
) { }
