package com.piyal.bitespeed.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Contact {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private String phoneNumber;
  private String email;
  private int linkedId;
  private String linkPrecedence;
  private LocalDateTime createdAT;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
