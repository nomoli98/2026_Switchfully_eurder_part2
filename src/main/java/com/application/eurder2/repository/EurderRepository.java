package com.application.eurder2.repository;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EurderRepository extends JpaRepository<Eurder, UUID> {
    @EntityGraph(attributePaths = {"itemGroups", "itemGroups.item"})
    List<Eurder> findByUserOrderByOrderDateDesc(User user);

    @EntityGraph(attributePaths = {"itemGroups", "itemGroups.item"})
    Optional<Eurder> findByIdAndUser(UUID id, User user);

    @EntityGraph(attributePaths = {"user", "itemGroups", "itemGroups.item"})
    @Query("""
        SELECT e FROM Eurder e
        WHERE EXISTS (
            SELECT ig FROM ItemGroup ig
            WHERE ig.eurder = e
              AND ig.shippingDate = :date
        )
        """)
    List<Eurder> findAllWithItemGroupShippingOn(@Param("date") LocalDate date);
}