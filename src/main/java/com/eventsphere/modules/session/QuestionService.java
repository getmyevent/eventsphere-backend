package com.eventsphere.modules.session;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.modules.session.dto.CreateQuestionRequest;
import com.eventsphere.modules.session.dto.QuestionDto;
import com.eventsphere.modules.session.dto.UpvoteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final SessionRepository sessionRepository;
    private final SessionQuestionRepository questionRepository;
    private final QuestionUpvoteRepository upvoteRepository;

    public QuestionService(
            SessionRepository sessionRepository,
            SessionQuestionRepository questionRepository,
            QuestionUpvoteRepository upvoteRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.upvoteRepository = upvoteRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(String sessionId, String currentAttendeeId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        List<SessionQuestionEntity> questions = questionRepository.findBySessionIdOrderByUpvotesDescCreatedAtAsc(sessionId);

        Set<String> upvotedQuestionIds = (currentAttendeeId != null)
                ? upvoteRepository.findByIdAttendeeId(currentAttendeeId).stream()
                .map(u -> u.getId().getQuestionId())
                .collect(Collectors.toSet())
                : Set.of();

        return questions.stream()
                .map(q -> mapToDto(q, upvotedQuestionIds.contains(q.getId())))
                .toList();
    }

    @Transactional
    public QuestionDto submitQuestion(
            String sessionId,
            CreateQuestionRequest request,
            String attendeeId,
            String attendeeName
    ) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (request.text() == null || request.text().trim().isBlank()) {
            throw new BusinessException("Question text cannot be blank");
        }

        String questionId = "q_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        boolean isAnon = request.isAnonymous();
        String displayName = isAnon ? "Anonymous Attendee" : (attendeeName != null ? attendeeName : "Attendee");

        SessionQuestionEntity question = new SessionQuestionEntity();
        question.setId(questionId);
        question.setSessionId(sessionId);
        question.setAttendeeId(attendeeId);
        question.setText(request.text().trim());
        question.setAnonymous(isAnon);
        question.setAuthorNameDisplay(displayName);
        question.setStatus("Submitted");
        question.setUpvotes(0); // Starts at 0 per architectural invariant

        questionRepository.save(question);

        // Update session questions count
        session.setQuestionsCount(session.getQuestionsCount() + 1);
        sessionRepository.save(session);

        log.info("Submitted question {} in session {} by attendee {}", questionId, sessionId, attendeeId);

        return mapToDto(question, false);
    }

    @Transactional
    public UpvoteResponse toggleUpvote(String questionId, String currentAttendeeId) {
        SessionQuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));

        boolean alreadyUpvoted = upvoteRepository.existsByIdQuestionIdAndIdAttendeeId(questionId, currentAttendeeId);

        if (alreadyUpvoted) {
            upvoteRepository.deleteByIdQuestionIdAndIdAttendeeId(questionId, currentAttendeeId);
            int newCount = Math.max(0, question.getUpvotes() - 1);
            question.setUpvotes(newCount);
            questionRepository.save(question);

            log.info("Attendee {} removed upvote on question {}", currentAttendeeId, questionId);
            return new UpvoteResponse(questionId, newCount, false, "Upvote removed");
        } else {
            upvoteRepository.save(new QuestionUpvoteEntity(questionId, currentAttendeeId));
            int newCount = question.getUpvotes() + 1;
            question.setUpvotes(newCount);
            questionRepository.save(question);

            log.info("Attendee {} upvoted question {}", currentAttendeeId, questionId);
            return new UpvoteResponse(questionId, newCount, true, "Question upvoted");
        }
    }

    private QuestionDto mapToDto(SessionQuestionEntity q, boolean hasUpvoted) {
        String authorName = q.isAnonymous() ? "Anonymous Attendee" : q.getAuthorNameDisplay();
        String authorId = q.isAnonymous() ? "usr-anon" : q.getAttendeeId();
        String createdAtStr = DateTimeFormatter.ISO_INSTANT.format(q.getCreatedAt());

        return new QuestionDto(
                q.getId(),
                q.getSessionId(),
                q.getText(),
                authorName,
                authorId,
                q.isAnonymous(),
                q.getStatus(),
                q.getUpvotes(),
                hasUpvoted,
                createdAtStr
        );
    }
}
