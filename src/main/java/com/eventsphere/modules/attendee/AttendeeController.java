package com.eventsphere.modules.attendee;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.attendee.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendees")
public class AttendeeController {

    private final AttendeeService attendeeService;

    public AttendeeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    @GetMapping
    public ResponseEntity<List<AttendeeProfileDto>> getDirectory(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean savedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        List<AttendeeProfileDto> list = attendeeService.getDirectory(
                principal.getEventId(),
                principal.getAttendeeId(),
                query,
                category,
                savedOnly,
                page,
                size
        );
        return ResponseEntity.ok(list);
    }

    @GetMapping("/me")
    public ResponseEntity<AttendeeProfileDto> getMyProfile(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        AttendeeProfileDto profile = attendeeService.getAttendeeProfile(
                principal.getEventId(),
                principal.getAttendeeId(),
                principal.getAttendeeId()
        );
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<AttendeeProfileDto> updateMyProfile(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        AttendeeProfileDto updated = attendeeService.updateMyProfile(
                principal.getEventId(),
                principal.getAttendeeId(),
                request
        );
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/privacy")
    public ResponseEntity<PrivacyConsentDto> updateMyPrivacy(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @Valid @RequestBody UpdatePrivacyRequest request
    ) {
        PrivacyConsentDto privacy = attendeeService.updateMyPrivacy(
                principal.getAttendeeId(),
                request
        );
        return ResponseEntity.ok(privacy);
    }

    @GetMapping("/{attendeeId}")
    public ResponseEntity<AttendeeProfileDto> getAttendeeProfile(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String attendeeId
    ) {
        AttendeeProfileDto profile = attendeeService.getAttendeeProfile(
                principal.getEventId(),
                principal.getAttendeeId(),
                attendeeId
        );
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{attendeeId}/save")
    public ResponseEntity<Map<String, Boolean>> savePerson(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String attendeeId
    ) {
        attendeeService.savePerson(principal.getAttendeeId(), attendeeId);
        return ResponseEntity.ok(Map.of("saved", true));
    }

    @DeleteMapping("/{attendeeId}/save")
    public ResponseEntity<Map<String, Boolean>> unsavePerson(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String attendeeId
    ) {
        attendeeService.unsavePerson(principal.getAttendeeId(), attendeeId);
        return ResponseEntity.ok(Map.of("saved", false));
    }

    @GetMapping("/saved")
    public ResponseEntity<List<AttendeeProfileDto>> getSavedPeople(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        List<AttendeeProfileDto> list = attendeeService.getSavedPeople(
                principal.getEventId(),
                principal.getAttendeeId()
        );
        return ResponseEntity.ok(list);
    }
}
