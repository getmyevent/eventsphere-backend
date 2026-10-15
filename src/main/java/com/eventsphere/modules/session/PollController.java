package com.eventsphere.modules.session;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.session.dto.PollDto;
import com.eventsphere.modules.session.dto.VotePollRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PollDto> getPoll(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        PollDto poll = pollService.getPollById(id, principal.getAttendeeId());
        return ResponseEntity.ok(poll);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<PollDto> vote(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody VotePollRequest request
    ) {
        PollDto updated = pollService.vote(id, request, principal.getAttendeeId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/vote")
    public ResponseEntity<PollDto> resetVote(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        PollDto updated = pollService.resetVote(id, principal.getAttendeeId());
        return ResponseEntity.ok(updated);
    }
}
