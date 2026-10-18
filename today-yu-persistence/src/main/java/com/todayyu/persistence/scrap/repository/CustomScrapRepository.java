package com.todayyu.persistence.scrap.repository;

import java.util.List;

public interface CustomScrapRepository {
    List<Long> findInfoIdsByMemberIdAndInfoIds(Long memberId, List<Long> infoIds);
}
