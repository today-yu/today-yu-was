package com.todayyu.application.scrap.port.save;

import com.todayyu.domain.scrap.entity.Scrap;

public interface SaveScrapPort {
    Scrap save(Scrap scrap);
}
