package com.vaultweb.passwordmanager.backend.model;

import java.time.LocalDateTime;

import com.vaultweb.passwordmanager.backend.model.dtos.PasswordEntryDto;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.vaultweb.passwordmanager.backend.security.AttributeEncryptor;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public PasswordEntry(PasswordEntryDto dto) {
        this.name = dto.getName();
        this.username = dto.getUsername();
        this.password = dto.getPassword();
        this.url = dto.getUrl();
        this.notes = dto.getNotes();
    }
}
