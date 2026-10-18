package com.eventsphere.modules.organizer;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.attendee.dto.AttendeeProfileDto;
import com.eventsphere.modules.organizer.dto.*;
import com.eventsphere.modules.session.dto.SessionDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eventsphere.modules.session.PollService;
import com.eventsphere.modules.session.dto.CreatePollRequest;
import com.eventsphere.modules.session.dto.PollDto;
import com.eventsphere.modules.session.dto.UpdatePollStatusRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizer")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerController {

    private final OrganizerService organizerService;
    private final PollService pollService;

    public OrganizerController(OrganizerService organizerService, PollService pollService) {
        this.organizerService = organizerService;
        this.pollService = pollService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<OrganizerMetricsDto> getMetrics(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        OrganizerMetricsDto metrics = organizerService.getMetrics(principal.getEventId());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/attendees/{id}/check-in")
    public ResponseEntity<CheckInResponseDto> toggleCheckIn(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        CheckInResponseDto response = organizerService.toggleCheckIn(principal.getEventId(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attendees/walk-in")
    public ResponseEntity<AttendeeProfileDto> registerWalkIn(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @Valid @RequestBody WalkInRegistrationRequest request
    ) {
        AttendeeProfileDto profile = organizerService.registerWalkIn(principal.getEventId(), request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping(value = "/attendees/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDto> importAttendees(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam("file") MultipartFile file
    ) {
        ImportResultDto result = organizerService.importAttendees(principal.getEventId(), file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attendees")
    public ResponseEntity<List<AttendeeProfileDto>> getAttendees(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean checkedInOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        List<AttendeeProfileDto> list = organizerService.getAttendees(
                principal.getEventId(),
                query,
                category,
                checkedInOnly,
                page,
                size
        );
        long total = organizerService.countAttendees(
                principal.getEventId(),
                query,
                category,
                checkedInOnly
        );
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(list);
    }

    @GetMapping("/exhibitors")
    public ResponseEntity<List<OrganizerExhibitorDto>> getExhibitors(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        List<OrganizerExhibitorDto> list = organizerService.getExhibitors(principal.getEventId());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/attendees/{id}")
    public ResponseEntity<AttendeeProfileDto> updateAttendee(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id,
            @RequestBody com.eventsphere.modules.attendee.dto.UpdateProfileRequest request
    ) {
        AttendeeProfileDto profile = organizerService.updateAttendee(principal.getEventId(), id, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionDto> createSession(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @Valid @RequestBody CreateSessionRequest request
    ) {
        SessionDto session = organizerService.createSession(principal.getEventId(), request);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<SessionDto> updateSession(
            @PathVariable("id") String id,
            @RequestBody UpdateSessionRequest request
    ) {
        SessionDto session = organizerService.updateSession(id, request);
        return ResponseEntity.ok(session);
    }

    @PatchMapping("/sessions/{id}/status")
    public ResponseEntity<SessionDto> updateSessionStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateSessionStatusRequest request
    ) {
        SessionDto session = organizerService.updateSessionStatus(id, request.status());
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable("id") String id) {
        organizerService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/polls")
    public ResponseEntity<List<PollDto>> getPolls(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        List<PollDto> polls = pollService.getAllPolls(principal.getAttendeeId());
        return ResponseEntity.ok(polls);
    }

    @PostMapping("/polls")
    public ResponseEntity<PollDto> createPoll(
            @Valid @RequestBody CreatePollRequest request
    ) {
        PollDto created = pollService.createPoll(request);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/polls/{id}/status")
    public ResponseEntity<PollDto> updatePollStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdatePollStatusRequest request
    ) {
        PollDto updated = pollService.updatePollStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }
}
