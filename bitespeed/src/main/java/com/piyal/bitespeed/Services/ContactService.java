package com.piyal.bitespeed.Services;

import com.piyal.bitespeed.DTO.IdentifyResponse;

public interface ContactService {
  IdentifyResponse identify(String email, String phoneNumber);
}
