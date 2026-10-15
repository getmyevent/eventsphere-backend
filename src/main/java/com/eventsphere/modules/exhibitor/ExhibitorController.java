package com.eventsphere.modules.exhibitor;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.exhibitor.dto.*;
import com.eventsphere.modules.exhibitor.model.LeadStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exhibitor")
@PreAuthorize("hasAnyRole('EXHIBITOR', 'ORGANIZER')")
public class ExhibitorController {

    private final ExhibitorService exhibitorService;

    public ExhibitorController(ExhibitorService exhibitorService) {
        this.exhibitorService = exhibitorService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ExhibitorStatsDto> getStats(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        return ResponseEntity.ok(exhibitorService.getStats(resolvedId));
    }

    @GetMapping("/attendees/search")
    public ResponseEntity<List<AttendeeCandidateDto>> searchCandidates(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        List<AttendeeCandidateDto> candidates = exhibitorService.searchCandidates(
                principal.getEventId(),
                resolvedId,
                query,
                role,
                page,
                size
        );
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/leads")
    public ResponseEntity<List<LeadDto>> getLeads(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId,
            @RequestParam(required = false) LeadStatus status
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        return ResponseEntity.ok(exhibitorService.getLeads(resolvedId, status));
    }

    @PostMapping("/leads")
    public ResponseEntity<LeadDto> captureLead(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId,
            @Valid @RequestBody CaptureLeadRequest request
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        LeadDto lead = exhibitorService.captureLead(resolvedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lead);
    }

    @PatchMapping("/leads/{leadId}")
    public ResponseEntity<LeadDto> updateLeadStatus(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String leadId,
            @RequestParam(required = false) String exhibitorId,
            @Valid @RequestBody UpdateLeadStatusRequest request
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        LeadDto lead = exhibitorService.updateLeadStatus(resolvedId, leadId, request);
        return ResponseEntity.ok(lead);
    }

    @GetMapping("/leads/export")
    public ResponseEntity<byte[]> exportLeadsCsv(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        byte[] csvBytes = exhibitorService.exportLeadsCsv(resolvedId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"booth-leads-" + resolvedId + ".csv\"");
        headers.setContentLength(csvBytes.length);

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getProducts(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        return ResponseEntity.ok(exhibitorService.getProducts(resolvedId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDto> getProductById(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String productId,
            @RequestParam(required = false) String exhibitorId
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        return ResponseEntity.ok(exhibitorService.getProductById(resolvedId, productId));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        ProductDto created = exhibitorService.createProduct(resolvedId, request);
        return ResponseEntity.created(URI.create("/api/v1/exhibitor/products/" + created.getId())).body(created);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String productId,
            @RequestParam(required = false) String exhibitorId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        return ResponseEntity.ok(exhibitorService.updateProduct(resolvedId, productId, request));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String productId,
            @RequestParam(required = false) String exhibitorId
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        exhibitorService.deleteProduct(resolvedId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/brochure")
    public ResponseEntity<ExhibitorStatsDto> updateBrochure(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @RequestParam(required = false) String exhibitorId,
            @RequestBody UpdateBrochureRequest request
    ) {
        String resolvedId = resolveExhibitorId(principal, exhibitorId);
        return ResponseEntity.ok(exhibitorService.updateBrochure(resolvedId, request));
    }

    private String resolveExhibitorId(EventSphereUserPrincipal principal, String explicitExhibitorId) {
        if (principal == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthenticated user");
        }

        if (principal.getRole() == UserRole.ROLE_ORGANIZER) {
            if (explicitExhibitorId != null && !explicitExhibitorId.isBlank()) {
                return explicitExhibitorId;
            }
            if (principal.getExhibitorId() != null && !principal.getExhibitorId().isBlank()) {
                return principal.getExhibitorId();
            }
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Organizer must specify an exhibitorId parameter");
        }

        String exhibitorId = principal.getExhibitorId();
        if (exhibitorId == null || exhibitorId.isBlank()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Authenticated user is not linked to an exhibitor booth");
        }

        if (explicitExhibitorId != null && !explicitExhibitorId.isBlank() && !explicitExhibitorId.equals(exhibitorId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Cross-tenant access is prohibited: cannot access another exhibitor booth");
        }

        return exhibitorId;
    }
}
