package com.vaultweb.passwordmanager.backend.repositories;

import com.vaultweb.passwordmanager.backend.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findAllByOwnerId(Long ownerId);

  Optional<Category> findByIdAndOwnerId(Long id, Long ownerId);
}
