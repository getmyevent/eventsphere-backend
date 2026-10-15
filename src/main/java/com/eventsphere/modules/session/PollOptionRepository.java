package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOptionEntity, String> {

    @Query("SELECT po FROM PollOptionEntity po WHERE po.poll.id = :pollId AND po.id = :id")
    Optional<PollOptionEntity> findByPollIdAndId(@Param("pollId") String pollId, @Param("id") String id);

    @Query("SELECT po FROM PollOptionEntity po WHERE po.poll.id = :pollId ORDER BY po.sortOrder ASC")
    List<PollOptionEntity> findAllByPollIdOrderBySortOrderAsc(@Param("pollId") String pollId);
}
