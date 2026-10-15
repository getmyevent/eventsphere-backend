package com.eventsphere.modules.connection;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.connection.dto.ConnectionDto;
import com.eventsphere.modules.connection.dto.SendConnectionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping
    public ResponseEntity<ConnectionDto> sendConnectionRequest(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @Valid @RequestBody SendConnectionRequest request
    ) {
        ConnectionDto dto = connectionService.sendConnectionRequest(
                principal.getEventId(),
                principal.getAttendeeId(),
                request
        );
        return ResponseEntity.created(URI.create("/api/v1/connections/" + dto.getId())).body(dto);
    }

    @PostMapping("/{connectionId}/accept")
    public ResponseEntity<ConnectionDto> acceptConnection(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String connectionId
    ) {
        ConnectionDto dto = connectionService.acceptConnection(
                principal.getEventId(),
                principal.getAttendeeId(),
                connectionId
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{connectionId}")
    public ResponseEntity<Void> removeConnection(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String connectionId
    ) {
        connectionService.removeConnection(
                principal.getEventId(),
                principal.getAttendeeId(),
                connectionId
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDto>> getConnections(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String status
    ) {
        List<ConnectionDto> list = connectionService.getConnections(
                principal.getEventId(),
                principal.getAttendeeId(),
                status
        );
        return ResponseEntity.ok(list);
    }
}
