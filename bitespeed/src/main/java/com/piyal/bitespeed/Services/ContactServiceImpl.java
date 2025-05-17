package com.piyal.bitespeed.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.piyal.bitespeed.DTO.IdentifyResponse;
import com.piyal.bitespeed.Entity.Contact;
import com.piyal.bitespeed.Repository.ContactRepository;

@Service
public class ContactServiceImpl implements ContactService {

  // injecting ContactRepository to interact with database
  @Autowired
  private ContactRepository contactRepository;

  // This method is for handelling incoming request

  @Override
  public IdentifyResponse identify(String email, String phoneNumber) {

    // Step 1: Find all contacts matching either email or phoneNumber
    List<Contact> matchedContacts = contactRepository.findByPhoneNumberOrEmail(email, phoneNumber);

    // Step 2: If no contacts found, create a new primary
    if (matchedContacts.isEmpty()) {
      Contact newContact = new Contact();
      newContact.setEmail(email);
      newContact.setPhoneNumber(phoneNumber);
      newContact.setLinkPrecedence("primary");
      newContact.setCreatedAT(LocalDateTime.now());
      newContact.setUpdatedAt(LocalDateTime.now());

      contactRepository.save(newContact);
      // Since it's a new contact, no secondary contacts exist yet
      return buildResponse(newContact, Collections.emptyList());
    }

    // Step 3: Among matched contacts, identify the earliest-created PRIMARY contact
    // This will be treated as the canonical primary contact

    Contact primary = matchedContacts.stream()
        .filter(c -> "primary".equals(c.getLinkPrecedence()))
        .min(Comparator.comparing(Contact::getCreatedAT))
        .orElse(matchedContacts.get(0)); // fallback in case no primary is found

    // Step 4: Check if the incoming email and phone combination already exists
    boolean existingMatch = matchedContacts.stream().anyMatch(
        c -> Objects.equals(c.getEmail(), email) && Objects.equals(c.getPhoneNumber(), phoneNumber));

    // Step 5: If the exact combination does not exist, create a new SECONDARY
    // contact
    if (!existingMatch) {
      Contact secondary = new Contact();
      secondary.setEmail(email);
      secondary.setPhoneNumber(phoneNumber);
      secondary.setLinkedId(primary.getId()); // Link to the identified primary
      secondary.setLinkPrecedence("secondary");
      secondary.setCreatedAT(LocalDateTime.now());
      secondary.setUpdatedAt(LocalDateTime.now());

      contactRepository.save(secondary);
      matchedContacts.add(secondary); // Include in the list for response building
    }

    // Step 6: Normalize data by including secondaries explicitly linked to the
    // primary
    // This ensures we have the complete set of all related contacts

    // Optional step to collect all contact IDs (not currently used but could help
    // with deduplication if expanded)

    Set<Integer> allContactIds = matchedContacts.stream().map(Contact::getId).collect(Collectors.toSet());

    // Add all secondaries linked to the primary that might not be already included

    matchedContacts.addAll(contactRepository.findByLinkedId(primary.getId()));

    // Step 7: Build and return the IdentifyResponse with full contact linkage
    return buildResponse(primary, matchedContacts);
  }

  /**
   * To improve code clarity, maintainability, and reusability creating a seperate
   * Helper method to build the IdentifyResponse with all collected emails,
   * phoneNumbers, and secondaryContactIds.
   */

  private IdentifyResponse buildResponse(Contact primary, List<Contact> allContacts) {
    Set<String> emails = new HashSet<>();
    Set<String> phoneNumbers = new HashSet<>();
    List<Integer> secondaryIds = new ArrayList<>();

    for (Contact cont : allContacts) {
      if (cont.getEmail() != null)
        emails.add(cont.getEmail());
      if (cont.getPhoneNumber() != null)
        phoneNumbers.add(cont.getPhoneNumber());

      // If the contact is not the primary, collect it as a secondary
      if (!Objects.equals(cont.getId(), primary.getId())) {
        secondaryIds.add(cont.getId());
      }
    }

    // Creating the nested response object
    IdentifyResponse.ContactResponse contactData = new IdentifyResponse.ContactResponse();
    contactData.setPrimaryContactId(primary.getId());
    contactData.setEmails(emails);
    contactData.setPhoneNumbers(phoneNumbers);
    contactData.setSecondaryContactIds(secondaryIds);

    // Final response object wrapping the contact data
    IdentifyResponse response = new IdentifyResponse();
    response.setContact(contactData);
    return response;
  }

}
