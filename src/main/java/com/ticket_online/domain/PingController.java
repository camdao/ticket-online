package com.ticket_online.domain;

import org.springframework.web.bind.annotation.*;

@RestController
public class PingController {

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }
}
