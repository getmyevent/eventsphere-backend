package com.eventsphere.modules.exhibitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findAllByExhibitorIdOrderByCreatedAtAsc(String exhibitorId);

    Optional<ProductEntity> findByIdAndExhibitorId(String id, String exhibitorId);

    boolean existsByIdAndExhibitorId(String id, String exhibitorId);

    long countByExhibitorId(String exhibitorId);
}
