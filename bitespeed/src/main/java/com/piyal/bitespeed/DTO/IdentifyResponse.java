package com.piyal.bitespeed.DTO;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentifyResponse {
  private ContactResponse contact;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ContactResponse {
    private Long primaryContactId;
    private Set<String> emails;
    private Set<String> phoneNumbers;
    private List<Long> secondaryContactIds;
  }
}
