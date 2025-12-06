package com.vaultweb.passwordmanager.backend.model;

import com.vaultweb.passwordmanager.backend.model.dtos.PasswordEntryDto;
import com.vaultweb.passwordmanager.backend.security.AttributeEncryptor;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "password_entries")
public class PasswordEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name (service/site) is required")
  @Size(max = 100)
  private String name;

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Password is required")
  @Convert(converter = AttributeEncryptor.class)
  private String password;

  private String url;

  @Column(length = 500)
  private String notes;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  @ToString.Exclude
  private Category category;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

  public PasswordEntry(PasswordEntryDto dto) {
    this.name = dto.getName();
    this.username = dto.getUsername();
    this.password = dto.getPassword();
    this.url = dto.getUrl();
    this.notes = dto.getNotes();
  }
}
