package com.todayyu.persistence.info.repository;

import com.todayyu.persistence.info.dto.HelpfulMarkCountProjection;

import java.util.List;

public interface CustomHelpfulMarkRepository {
    List<HelpfulMarkCountProjection> countByInfoIds(List<Long> infoIds);
}
