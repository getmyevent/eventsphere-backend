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

    // pollId -> Set of active SseEmitters for that specific poll
    private final Map<String, Set<SseEmitter>> pollEmitters = new ConcurrentHashMap<>();

    // Global active poll subscribers (clients listening to whichever poll is currently live)
    private final Set<SseEmitter> activeStreamEmitters = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Subscribe a client to a specific poll via Server-Sent Events.
     */
    public SseEmitter subscribe(String pollId, PollDto initialSnapshot) {
        SseEmitter emitter = new SseEmitter(1_800_000L); // 30 min duration

        Set<SseEmitter> emitters = pollEmitters.computeIfAbsent(
                pollId,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>())
        );
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(pollId, emitter));
        emitter.onTimeout(() -> removeEmitter(pollId, emitter));
        emitter.onError(e -> removeEmitter(pollId, emitter));

        if (initialSnapshot != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("poll-init")
                        .data(initialSnapshot));
            } catch (IOException e) {
                log.debug("Failed to deliver initial poll snapshot for {}: {}", pollId, e.getMessage());
                removeEmitter(pollId, emitter);
            }
        }

        return emitter;
    }

    /**
     * Subscribe a client to the global active live poll stream.
     * When any new poll is created or status changes, this stream pushes it immediately.
     */
    public SseEmitter subscribeActive(PollDto initialSnapshot) {
        SseEmitter emitter = new SseEmitter(1_800_000L);
        activeStreamEmitters.add(emitter);

        emitter.onCompletion(() -> activeStreamEmitters.remove(emitter));
        emitter.onTimeout(() -> activeStreamEmitters.remove(emitter));
        emitter.onError(e -> activeStreamEmitters.remove(emitter));

        if (initialSnapshot != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("poll-init")
                        .data(initialSnapshot));
            } catch (IOException e) {
                log.debug("Failed to deliver initial active poll snapshot: {}", e.getMessage());
                activeStreamEmitters.remove(emitter);
            }
        }

        return emitter;
    }

    /**
     * Broadcast live poll vote changes to all connected listeners in real time.
     */
    public void broadcast(String pollId, PollDto updatedSnapshot) {
        // 1. Send to poll-specific subscribers
        Set<SseEmitter> emitters = pollEmitters.get(pollId);
        if (emitters != null) {
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

        // 2. Also send to all active stream subscribers
        for (SseEmitter emitter : activeStreamEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("poll-update")
                        .data(updatedSnapshot));
            } catch (Exception e) {
                activeStreamEmitters.remove(emitter);
            }
        }
    }

    /**
     * Broadcast a brand new live poll to all connected audience devices.
     */
    public void broadcastNewPoll(PollDto newPoll) {
        log.info("Broadcasting new live poll {} to {} active audience subscribers", newPoll.id(), activeStreamEmitters.size());
        for (SseEmitter emitter : activeStreamEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("poll-init")
                        .data(newPoll));
            } catch (Exception e) {
                activeStreamEmitters.remove(emitter);
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
        // Heartbeat for poll-specific emitters
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

        // Heartbeat for global active emitters
        for (SseEmitter emitter : activeStreamEmitters) {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
                activeStreamEmitters.remove(emitter);
            }
        }
    }

    public int getActiveSubscriberCount(String pollId) {
        Set<SseEmitter> emitters = pollEmitters.get(pollId);
        return (emitters != null ? emitters.size() : 0) + activeStreamEmitters.size();
    }
}
