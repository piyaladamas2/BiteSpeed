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
    List<Contact> matchedContacts = contactRepository.findByPhoneNumberOrEmail(email, phoneNumber);

    if (matchedContacts.isEmpty()) {
      Contact newContact = new Contact();
      newContact.setEmail(email);
      newContact.setPhoneNumber(phoneNumber);
      newContact.setLinkPrecedence("primary");
      newContact.setCreatedAT(LocalDateTime.now());
      newContact.setUpdatedAt(LocalDateTime.now());

      contactRepository.save(newContact);

      return buildResponse(newContact, Collections.emptyList());
    }

    // Find primary contact
    Contact primary = matchedContacts.stream()
        .filter(c -> "primary".equals(c.getLinkPrecedence()))
        .min(Comparator.comparing(Contact::getCreatedAT))
        .orElse(matchedContacts.get(0));

    // If input has new data , create secondary contact
    boolean existingMatch = matchedContacts.stream().anyMatch(
        c -> Objects.equals(c.getEmail(), email) && Objects.equals(c.getPhoneNumber(), phoneNumber));

    if (!existingMatch) {
      Contact secondary = new Contact();
      secondary.setEmail(email);
      secondary.setPhoneNumber(phoneNumber);
      secondary.setLinkedId(primary.getId());
      secondary.setLinkPrecedence("secondary");
      secondary.setCreatedAT(LocalDateTime.now());
      secondary.setUpdatedAt(LocalDateTime.now());
      contactRepository.save(secondary);
      matchedContacts.add(secondary);
    }

    // Normalize data: primary + all linked secondaries
    // To collect the unique IDs of all currently matched contacts. This will help
    // prevent re-adding the same contacts later.

    // It is not used anywhere after being created still it was needed for Duplicate
    // Prevention

    Set<Integer> allContactIds = matchedContacts.stream().map(Contact::getId).collect(Collectors.toSet());
    matchedContacts.addAll(contactRepository.findByLinkedId(primary.getId()));

    return buildResponse(primary, matchedContacts);
  }

  // To improve code clarity, maintainability, and reusability creating a seperate
  // method

  private IdentifyResponse buildResponse(Contact primary, List<Contact> allContacts) {
    Set<String> emails = new HashSet<>();
    Set<String> phoneNumbers = new HashSet<>();
    List<Integer> secondaryIds = new ArrayList<>();

    for (Contact cont : allContacts) {
      if (cont.getEmail() != null)
        emails.add(cont.getEmail());
      if (cont.getPhoneNumber() != null)
        phoneNumbers.add(cont.getPhoneNumber());
      if (!Objects.equals(cont.getId(), primary.getId())) {
        secondaryIds.add(cont.getId());
      }
    }

    IdentifyResponse.ContactResponse contactData = new IdentifyResponse.ContactResponse();
    contactData.setPrimaryContactId(primary.getId());
    contactData.setEmails(emails);
    contactData.setPhoneNumbers(phoneNumbers);
    contactData.setSecondaryContactIds(secondaryIds);

    IdentifyResponse response = new IdentifyResponse();
    response.setContact(contactData);
    return response;
  }

}
