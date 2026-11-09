package com.application.eurder2.repository;

import com.application.eurder2.domain.Item;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    @Transactional
    default Item updateOrInsert(Item entity) {
        return save(entity);
    }
}
