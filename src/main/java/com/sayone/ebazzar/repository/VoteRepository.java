package com.sayone.ebazzar.repository;

import com.sayone.ebazzar.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity,Long> {
    @Query(value = "select * from vote where review_id = ?2 AND user_id = ?1 ",nativeQuery = true)
    List<VoteEntity> findByUserID(Long Id,Long reviewId);
}
