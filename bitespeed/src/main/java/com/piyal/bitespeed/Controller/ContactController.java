package com.piyal.bitespeed.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piyal.bitespeed.DTO.IdentifyRequest;
import com.piyal.bitespeed.DTO.IdentifyResponse;
import com.piyal.bitespeed.Services.ContactServiceImpl;

@RestController
@RequestMapping("/identify")
public class ContactController {

  @Autowired
  private ContactServiceImpl contactService;

  @PostMapping
  public ResponseEntity<IdentifyResponse> identify(@RequestBody IdentifyRequest request) {
    return ResponseEntity.ok(
        contactService.identify(request.getEmail(), request.getPhoneNumber()));
  }
}
