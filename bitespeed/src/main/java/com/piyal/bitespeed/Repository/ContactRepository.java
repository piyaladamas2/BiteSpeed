package com.piyal.bitespeed.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.piyal.bitespeed.Entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

  List<Contact> findByPhoneNumberOrEmail(String phoneNumber, String email);

  List<Contact> findByLinkedId(int linkedId);
}
