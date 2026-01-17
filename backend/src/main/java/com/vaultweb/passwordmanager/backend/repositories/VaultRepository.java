package com.vaultweb.passwordmanager.backend.repositories;

import com.vaultweb.passwordmanager.backend.model.Vault;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultRepository extends JpaRepository<Vault, Long> {
  Optional<Vault> findByOwnerId(Long ownerId);

  boolean existsByOwnerId(Long ownerId);
}
