package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOptionEntity, String> {

    @Query("SELECT po FROM PollOptionEntity po WHERE (po.poll.id = :pollId OR po.pollId = :pollId) AND po.id = :id")
    Optional<PollOptionEntity> findByPollIdAndId(@Param("pollId") String pollId, @Param("id") String id);

    @Query("SELECT po FROM PollOptionEntity po WHERE po.poll.id = :pollId ORDER BY po.sortOrder ASC")
    List<PollOptionEntity> findAllByPollIdOrderBySortOrderAsc(@Param("pollId") String pollId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollOptionEntity po SET po.votes = po.votes + 1 WHERE po.id = :id")
    int incrementVotes(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollOptionEntity po SET po.votes = CASE WHEN po.votes > 0 THEN po.votes - 1 ELSE 0 END WHERE po.id = :id")
    int decrementVotes(@Param("id") String id);
}
