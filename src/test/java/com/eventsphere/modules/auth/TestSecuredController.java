package com.eventsphere.modules.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestSecuredController {

    @GetMapping("/exhibitor/protected-test")
    public ResponseEntity<String> exhibitorOnly() {
        return ResponseEntity.ok("exhibitor-ok");
    }

    @GetMapping("/organizer/protected-test")
    public ResponseEntity<String> organizerOnly() {
        return ResponseEntity.ok("organizer-ok");
    }
}
