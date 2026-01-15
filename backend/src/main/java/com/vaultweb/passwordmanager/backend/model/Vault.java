package com.vaultweb.passwordmanager.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_vaults")
public class Vault {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "owner_id", nullable = false, unique = true)
  private Long ownerId;

  @Column(name = "kdf_salt", nullable = false, length = 128)
  private String kdfSalt;

  @Column(name = "kdf_iterations", nullable = false)
  private Integer kdfIterations;

  @Column(name = "wrapped_dek", nullable = false, length = 1024)
  private String wrappedDek;

  @Column(name = "verifier", nullable = false, length = 128)
  private String verifier;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
