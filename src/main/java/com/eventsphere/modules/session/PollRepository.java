package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<PollEntity, String> {

    Optional<PollEntity> findBySessionId(String sessionId);

    List<PollEntity> findAllBySessionIdOrderByCreatedAtDesc(String sessionId);

    @Query("SELECT p FROM PollEntity p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<PollEntity> findActivePolls();

    @Query("SELECT p FROM PollEntity p WHERE p.status = 'open' ORDER BY p.createdAt DESC")
    List<PollEntity> findAllOpenOrderByCreatedAtDesc();

    @Query("SELECT p FROM PollEntity p ORDER BY p.createdAt DESC")
    List<PollEntity> findAllOrderByCreatedAtDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollEntity p SET p.active = CASE WHEN p.id = :id THEN true ELSE false END")
    int setActivePoll(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollEntity p SET p.active = false")
    int deactivateAllPolls();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollEntity p SET p.totalVotes = p.totalVotes + 1 WHERE p.id = :id")
    int incrementTotalVotes(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PollEntity p SET p.totalVotes = CASE WHEN p.totalVotes > 0 THEN p.totalVotes - 1 ELSE 0 END WHERE p.id = :id")
    int decrementTotalVotes(@Param("id") String id);
}
