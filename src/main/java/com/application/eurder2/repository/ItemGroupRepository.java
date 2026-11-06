package com.application.eurder2.repository;

import com.application.eurder2.domain.ItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemGroupRepository extends JpaRepository<ItemGroup, UUID> {
}
