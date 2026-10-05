package com.todayyu.application.auth.dto;

import com.todayyu.domain.auth.vo.TokenId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;

import java.time.Instant;

public record AccessTokenClaims(
        MemberId memberId,
        TokenId tokenId,
        Role role,
        Instant expiresAt
) { }
