package com.eventsphere.modules.session;

import com.eventsphere.modules.session.dto.PollDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PollStreamService {

    private static final Logger log = LoggerFactory.getLogger(PollStreamService.class);

    // pollId -> Set of active SseEmitters
    private final Map<String, Set<SseEmitter>> pollEmitters = new ConcurrentHashMap<>();

    /**
     * Subscribe a client to real-time live poll updates via Server-Sent Events.
     * Pushes current snapshot immediately on connection.
     */
    public SseEmitter subscribe(String pollId, PollDto initialSnapshot) {
        // 30 minute stream duration
        SseEmitter emitter = new SseEmitter(1_800_000L);

        Set<SseEmitter> emitters = pollEmitters.computeIfAbsent(
                pollId,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>())
        );
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(pollId, emitter));
        emitter.onTimeout(() -> removeEmitter(pollId, emitter));
        emitter.onError(e -> removeEmitter(pollId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("poll-init")
                    .data(initialSnapshot));
        } catch (IOException e) {
            log.debug("Failed to deliver initial poll snapshot for {}: {}", pollId, e.getMessage());
            removeEmitter(pollId, emitter);
        }

        return emitter;
    }

    /**
     * Broadcast live poll vote changes to all connected listeners in real time.
     */
    public void broadcast(String pollId, PollDto updatedSnapshot) {
        Set<SseEmitter> emitters = pollEmitters.get(pollId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("poll-update")
                        .data(updatedSnapshot));
            } catch (Exception e) {
                removeEmitter(pollId, emitter);
            }
        }
    }

    private void removeEmitter(String pollId, SseEmitter emitter) {
        Set<SseEmitter> emitters = pollEmitters.get(pollId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                pollEmitters.remove(pollId, Collections.emptySet());
            }
        }
    }

    /**
     * Keepalive comment frames every 15 seconds to prevent browser/NAT/proxy timeout.
     */
    @Scheduled(fixedRate = 15_000L)
    public void sendHeartbeat() {
        if (pollEmitters.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Set<SseEmitter>> entry : pollEmitters.entrySet()) {
            String pollId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (Exception e) {
                    removeEmitter(pollId, emitter);
                }
            }
        }
    }

    public int getActiveSubscriberCount(String pollId) {
        Set<SseEmitter> emitters = pollEmitters.get(pollId);
        return emitters != null ? emitters.size() : 0;
    }
}
