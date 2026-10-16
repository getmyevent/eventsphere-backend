package com.eventsphere.modules.session;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.session.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final QuestionService questionService;
    private final PollService pollService;

    public SessionController(
            SessionService sessionService,
            QuestionService questionService,
            PollService pollService
    ) {
        this.sessionService = sessionService;
        this.questionService = questionService;
        this.pollService = pollService;
    }

    @GetMapping
    public ResponseEntity<List<SessionDto>> getSessions(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String track,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") Boolean bookmarkedOnly
    ) {
        List<SessionDto> sessions = sessionService.getSessions(
                principal.getEventId(),
                day,
                track,
                status,
                search,
                bookmarkedOnly,
                principal.getAttendeeId()
        );
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> getSessionById(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        SessionDto session = sessionService.getSessionById(id, principal.getAttendeeId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<BookmarkResponse> toggleBookmark(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        BookmarkResponse response = sessionService.toggleBookmark(id, principal.getAttendeeId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestions(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        List<QuestionDto> questions = questionService.getQuestions(id, principal.getAttendeeId());
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<QuestionDto> submitQuestion(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionDto question = questionService.submitQuestion(
                id,
                request,
                principal.getAttendeeId(),
                principal.getName()
        );
        return ResponseEntity.ok(question);
    }

    @RequestMapping(value = "/questions/{questionId}/upvote", method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<UpvoteResponse> toggleUpvote(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("questionId") String questionId
    ) {
        UpvoteResponse response = questionService.toggleUpvote(questionId, principal.getAttendeeId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/polls")
    public ResponseEntity<List<PollDto>> getSessionPolls(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable("id") String id
    ) {
        List<PollDto> polls = pollService.getPollsForSession(id, principal.getAttendeeId());
        return ResponseEntity.ok(polls);
    }
}
