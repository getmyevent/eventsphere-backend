package com.eventsphere.modules.connection;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.connection.dto.ConnectionDto;
import com.eventsphere.modules.connection.dto.SendConnectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConnectionService {

    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

    private final ConnectionRepository connectionRepository;
    private final AttendeeRepository attendeeRepository;

    public ConnectionService(ConnectionRepository connectionRepository, AttendeeRepository attendeeRepository) {
        this.connectionRepository = connectionRepository;
        this.attendeeRepository = attendeeRepository;
    }

    public ConnectionDto sendConnectionRequest(String eventId, String viewerId, SendConnectionRequest request) {
        String targetId = request.getTargetAttendeeId();
        if (viewerId.equals(targetId)) {
            throw new BusinessException("Cannot connect with yourself");
        }

        AttendeeEntity targetAttendee = attendeeRepository.findByIdAndEventId(targetId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee not found in this event: " + targetId));

        String userLow = viewerId.compareTo(targetId) <= 0 ? viewerId : targetId;
        String userHigh = viewerId.compareTo(targetId) <= 0 ? targetId : viewerId;

        Optional<ConnectionEntity> existingOpt = connectionRepository.findByEventIdAndUserLowAndUserHigh(eventId, userLow, userHigh);

        ConnectionEntity connection;
        if (existingOpt.isPresent()) {
            connection = existingOpt.get();
            if ("connected".equalsIgnoreCase(connection.getStatus())) {
                log.info("Attendees {} and {} are already connected", viewerId, targetId);
                return toConnectionDto(connection, viewerId, targetAttendee);
            }

            // If existing is pending
            if (connection.getSenderId().equals(viewerId)) {
                log.info("Connection request from {} to {} is already pending", viewerId, targetId);
                return toConnectionDto(connection, viewerId, targetAttendee);
            }

            // Mutual intent: target had previously sent a request to viewer. This auto-accepts!
            log.info("Mutual connection intent detected: auto-accepting connection between {} and {}", viewerId, targetId);
            connection.setStatus("connected");
            connection = connectionRepository.save(connection);
        } else {
            String connId = "conn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            String source = (request.getSource() != null && !request.getSource().isBlank()) ? request.getSource().trim() : "direct";

            connection = new ConnectionEntity(connId, eventId, viewerId, targetId, "pending", source);
            connection = connectionRepository.save(connection);
            log.info("Sent connection request {} from {} to {}", connId, viewerId, targetId);
        }

        return toConnectionDto(connection, viewerId, targetAttendee);
    }

    public ConnectionDto acceptConnection(String eventId, String viewerId, String connectionId) {
        ConnectionEntity connection = connectionRepository.findById(connectionId)
                .filter(c -> c.getEventId().equals(eventId))
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found: " + connectionId));

        if (!connection.getReceiverId().equals(viewerId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the recipient can accept this connection request");
        }

        connection.setStatus("connected");
        ConnectionEntity saved = connectionRepository.save(connection);
        log.info("Accepted connection request {} by recipient {}", connectionId, viewerId);

        String otherPersonId = connection.getSenderId();
        AttendeeEntity otherPerson = attendeeRepository.findById(otherPersonId).orElse(null);

        return toConnectionDto(saved, viewerId, otherPerson);
    }

    public void removeConnection(String eventId, String viewerId, String connectionId) {
        ConnectionEntity connection = connectionRepository.findById(connectionId)
                .filter(c -> c.getEventId().equals(eventId))
                .orElse(null);

        if (connection == null) {
            String userLow = viewerId.compareTo(connectionId) <= 0 ? viewerId : connectionId;
            String userHigh = viewerId.compareTo(connectionId) <= 0 ? connectionId : viewerId;
            connection = connectionRepository.findByEventIdAndUserLowAndUserHigh(eventId, userLow, userHigh)
                    .orElseThrow(() -> new ResourceNotFoundException("Connection not found: " + connectionId));
        }

        if (!connection.getSenderId().equals(viewerId) && !connection.getReceiverId().equals(viewerId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Access denied: you are not a party to this connection");
        }

        connectionRepository.delete(connection);
        log.info("Removed connection {} by attendee {}", connectionId, viewerId);
    }

    @Transactional(readOnly = true)
    public List<ConnectionDto> getConnections(String eventId, String viewerId, String statusFilter) {
        List<ConnectionEntity> list = connectionRepository.findAllByAttendee(eventId, viewerId);

        if (statusFilter != null && !statusFilter.isBlank()) {
            list = list.stream()
                    .filter(c -> c.getStatus().equalsIgnoreCase(statusFilter.trim()))
                    .collect(Collectors.toList());
        }

        Set<String> otherIds = list.stream()
                .map(c -> c.getSenderId().equals(viewerId) ? c.getReceiverId() : c.getSenderId())
                .collect(Collectors.toSet());

        Map<String, AttendeeEntity> attendeeMap = attendeeRepository.findAllById(otherIds).stream()
                .collect(Collectors.toMap(AttendeeEntity::getId, a -> a));

        return list.stream()
                .map(c -> {
                    String otherId = c.getSenderId().equals(viewerId) ? c.getReceiverId() : c.getSenderId();
                    AttendeeEntity other = attendeeMap.get(otherId);
                    return toConnectionDto(c, viewerId, other);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean areConnected(String eventId, String userA, String userB) {
        if (userA == null || userB == null || userA.equals(userB)) return false;
        String userLow = userA.compareTo(userB) <= 0 ? userA : userB;
        String userHigh = userA.compareTo(userB) <= 0 ? userB : userA;

        return connectionRepository.findByEventIdAndUserLowAndUserHigh(eventId, userLow, userHigh)
                .map(c -> "connected".equalsIgnoreCase(c.getStatus()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public ConnectionStatusInfo getConnectionStatusInfo(String eventId, String viewerId, String targetId) {
        if (viewerId == null || targetId == null || viewerId.equals(targetId)) {
            return new ConnectionStatusInfo("none", null);
        }

        String userLow = viewerId.compareTo(targetId) <= 0 ? viewerId : targetId;
        String userHigh = viewerId.compareTo(targetId) <= 0 ? targetId : viewerId;

        Optional<ConnectionEntity> connOpt = connectionRepository.findByEventIdAndUserLowAndUserHigh(eventId, userLow, userHigh);
        if (connOpt.isEmpty()) {
            return new ConnectionStatusInfo("none", null);
        }

        ConnectionEntity c = connOpt.get();
        if ("connected".equalsIgnoreCase(c.getStatus())) {
            return new ConnectionStatusInfo("connected", c.getId());
        }

        // Status is pending
        if (c.getSenderId().equals(viewerId)) {
            return new ConnectionStatusInfo("pending", c.getId());
        } else {
            return new ConnectionStatusInfo("incoming_request", c.getId());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, ConnectionStatusInfo> getBatchConnectionStatusInfo(String eventId, String viewerId) {
        if (viewerId == null) return Map.of();
        List<ConnectionEntity> list = connectionRepository.findAllByAttendee(eventId, viewerId);
        Map<String, ConnectionStatusInfo> map = new HashMap<>();
        for (ConnectionEntity c : list) {
            String otherId = c.getSenderId().equals(viewerId) ? c.getReceiverId() : c.getSenderId();
            if ("connected".equalsIgnoreCase(c.getStatus())) {
                map.put(otherId, new ConnectionStatusInfo("connected", c.getId()));
            } else if (c.getSenderId().equals(viewerId)) {
                map.put(otherId, new ConnectionStatusInfo("pending", c.getId()));
            } else {
                map.put(otherId, new ConnectionStatusInfo("incoming_request", c.getId()));
            }
        }
        return map;
    }

    public static class ConnectionStatusInfo {
        private final String status;
        private final String connectionId;

        public ConnectionStatusInfo(String status, String connectionId) {
            this.status = status;
            this.connectionId = connectionId;
        }

        public String getStatus() { return status; }
        public String getConnectionId() { return connectionId; }
    }

    private ConnectionDto toConnectionDto(ConnectionEntity conn, String viewerId, AttendeeEntity otherPerson) {
        boolean isSender = conn.getSenderId().equals(viewerId);
        String otherPersonId = isSender ? conn.getReceiverId() : conn.getSenderId();
        String type = isSender ? "sent" : "received";

        String name = otherPerson != null ? otherPerson.getName() : "Unknown Attendee";
        String designation = otherPerson != null && otherPerson.getDesignation() != null ? otherPerson.getDesignation() : "";
        String company = otherPerson != null && otherPerson.getCompany() != null ? otherPerson.getCompany() : "";
        String avatar = otherPerson != null ? otherPerson.getAvatarUrl() : null;

        return new ConnectionDto(
                conn.getId(),
                otherPersonId,
                name,
                designation,
                company,
                avatar,
                conn.getStatus(),
                type,
                conn.getSource(),
                conn.getUpdatedAt().toString()
        );
    }
}
