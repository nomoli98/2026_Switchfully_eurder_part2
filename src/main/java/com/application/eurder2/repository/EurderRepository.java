package com.application.eurder2.repository;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EurderRepository extends JpaRepository<Eurder, UUID> {
    List<Eurder> getByUser(User user);
}
