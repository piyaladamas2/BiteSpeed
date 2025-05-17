package com.piyal.bitespeed.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.piyal.bitespeed.DTO.IdentifyResponse;
import com.piyal.bitespeed.Entity.Contact;
import com.piyal.bitespeed.Exception.InvalidContactException;
import com.piyal.bitespeed.Repository.ContactRepository;

@Service
public class ContactServiceImpl implements ContactService {

  // injecting ContactRepository to interact with database
  @Autowired
  private ContactRepository contactRepository;

  @Override
  public IdentifyResponse identify(String email, String phoneNumber) {

    // Handelling null entry
    if ((email == null || email.isBlank()) && (phoneNumber == null || phoneNumber.isBlank())) {
      throw new InvalidContactException("Email or Phone Number must be provided");
    }

    // 1. Find existing contacts by phone or email
    List<Contact> matchedContacts = contactRepository.findByPhoneNumberOrEmail(phoneNumber, email);

    if (matchedContacts.isEmpty()) {
      // No match found, create a new primary contact
      Contact newContact = Contact.builder()
          .email(email)
          .phoneNumber(phoneNumber)
          .linkedId(0)
          .linkPrecedence("primary")
          .createdAT(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();

      contactRepository.save(newContact);

      return IdentifyResponse.builder()
          .contact(IdentifyResponse.ContactResponse.builder()
              .primaryContactId(newContact.getId())
              .emails(Stream.of(email).filter(Objects::nonNull).collect(Collectors.toSet()))
              .phoneNumbers(Stream.of(phoneNumber).filter(Objects::nonNull).collect(Collectors.toSet()))
              .secondaryContactIds(new ArrayList<>())
              .build())
          .build();
    }

    // 2. Determine primary contact (earliest created with linkPrecedence "primary")
    Contact primaryContact = matchedContacts.stream()
        .filter(c -> "primary".equalsIgnoreCase(c.getLinkPrecedence()))
        .min(Comparator.comparing(Contact::getCreatedAT))
        .orElse(matchedContacts.get(0));

    // 3. Make all other contacts secondary and link to primary
    List<Contact> secondaryContacts = new ArrayList<>();
    for (Contact c : matchedContacts) {
      if (c.getId() != primaryContact.getId()) {
        if (!"secondary".equalsIgnoreCase(c.getLinkPrecedence()) ||
            c.getLinkedId() != primaryContact.getId()) {
          c.setLinkPrecedence("secondary");
          c.setLinkedId(primaryContact.getId());
          c.setUpdatedAt(LocalDateTime.now());
          contactRepository.save(c);
        }
        secondaryContacts.add(c);
      }
    }

    // 4. Check if the incoming email/phone combination is new
    boolean exists = matchedContacts.stream()
        .anyMatch(c -> Objects.equals(c.getEmail(), email) && Objects.equals(c.getPhoneNumber(), phoneNumber));

    if (!exists) {
      // Create new secondary contact linked to primary
      Contact newSecondary = Contact.builder()
          .email(email)
          .phoneNumber(phoneNumber)
          .linkedId(primaryContact.getId())
          .linkPrecedence("secondary")
          .createdAT(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();
      contactRepository.save(newSecondary);
      secondaryContacts.add(newSecondary);
    }

    // 5. Gather all related contacts for response (primary + secondaries)
    Set<Contact> allContacts = new HashSet<>();
    allContacts.add(primaryContact);
    allContacts.addAll(contactRepository.findByLinkedId(primaryContact.getId()));

    Set<String> emails = allContacts.stream()
        .map(Contact::getEmail)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    Set<String> phoneNumbers = allContacts.stream()
        .map(Contact::getPhoneNumber)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    List<Integer> secondaryIds = allContacts.stream()
        .filter(c -> c.getId() != primaryContact.getId())
        .map(Contact::getId)
        .collect(Collectors.toList());

    // 6. Build and return response
    IdentifyResponse.ContactResponse response = IdentifyResponse.ContactResponse.builder()
        .primaryContactId(primaryContact.getId())
        .emails(emails)
        .phoneNumbers(phoneNumbers)
        .secondaryContactIds(secondaryIds)
        .build();

    return IdentifyResponse.builder()
        .contact(response)
        .build();
  }

}
