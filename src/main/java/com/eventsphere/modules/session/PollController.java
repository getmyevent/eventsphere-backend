package com.eventsphere.modules.session;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.session.dto.PollDto;
import com.eventsphere.modules.session.dto.VotePollRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @GetMapping("/active")
    public ResponseEntity<PollDto> getActivePoll(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        String attendeeId = principal != null ? principal.getAttendeeId() : null;
        PollDto poll = pollService.getActivePoll(attendeeId);
        return ResponseEntity.ok(poll);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PollDto> getPoll(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        String attendeeId = principal != null ? principal.getAttendeeId() : null;
        PollDto poll = pollService.getPollById(id, attendeeId);
        return ResponseEntity.ok(poll);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<PollDto> vote(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody VotePollRequest request
    ) {
        String attendeeId = principal != null ? principal.getAttendeeId() : null;
        PollDto updated = pollService.vote(id, request, attendeeId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/vote")
    public ResponseEntity<PollDto> resetVote(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        String attendeeId = principal != null ? principal.getAttendeeId() : null;
        PollDto updated = pollService.resetVote(id, attendeeId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamActivePoll(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        String attendeeId = principal != null ? principal.getAttendeeId() : null;
        return pollService.subscribeToActivePoll(attendeeId);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPoll(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        String attendeeId = principal != null ? principal.getAttendeeId() : null;
        return pollService.subscribeToPoll(id, attendeeId);
    }

    @PatchMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<PollDto> updateStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody com.eventsphere.modules.session.dto.UpdatePollStatusRequest request
    ) {
        PollDto updated = pollService.updatePollStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }
}
