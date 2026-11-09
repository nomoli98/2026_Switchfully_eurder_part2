package com.application.eurder2.repository;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EurderRepository extends JpaRepository<Eurder, UUID> {
    @EntityGraph(attributePaths = {"itemGroups", "itemGroups.item"})
    List<Eurder> findByUserOrderByOrderDateDesc(User user);

    @EntityGraph(attributePaths = {"itemGroups", "itemGroups.item"})
    Optional<Eurder> findByIdAndUser(UUID id, User user);
}