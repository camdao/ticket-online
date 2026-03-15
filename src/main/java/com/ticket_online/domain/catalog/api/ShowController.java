package com.ticket_online.domain.catalog.api;

import com.ticket_online.domain.catalog.application.ShowService;
import com.ticket_online.domain.catalog.dto.FindShowResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
public class ShowController {
    private final ShowService showService;

    @PostMapping
    public ResponseEntity<List<FindShowResponse>> findAllShow() {
        return ResponseEntity.ok(showService.findAllShow());
    }
}
