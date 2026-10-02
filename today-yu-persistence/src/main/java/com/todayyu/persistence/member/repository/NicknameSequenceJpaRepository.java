package com.todayyu.persistence.member.repository;

import com.todayyu.domain.member.vo.NicknameModifier;
import com.todayyu.persistence.member.entity.NicknameSequenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NicknameSequenceJpaRepository extends JpaRepository<NicknameSequenceJpaEntity, NicknameModifier> {
    @Modifying
    @Query(value = """
            INSERT INTO nickname_sequence (modifier, last_number)
            VALUES (:modifier, 1)
            ON DUPLICATE KEY UPDATE last_number = last_number + 1
            """, nativeQuery = true)
    void increase(@Param(value = "modifier") String modifier);
    
    @Query(value = "SELECT ns.last_number FROM nickname_sequence ns WHERE ns.modifier = :modifier", nativeQuery = true)
    long findLastNumber(@Param(value = "modifier") String modifier);
}
