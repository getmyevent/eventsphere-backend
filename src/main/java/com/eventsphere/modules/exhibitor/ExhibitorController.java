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

    @GetMapping({"/stats", "/{exhibitorId}/stats"})
    public ResponseEntity<ExhibitorStatsDto> getStats(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        return ResponseEntity.ok(exhibitorService.getStats(resolvedId));
    }

    @GetMapping({"/attendees/search", "/{exhibitorId}/attendees/search", "/candidates", "/{exhibitorId}/candidates"})
    public ResponseEntity<List<AttendeeCandidateDto>> searchCandidates(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        String effectiveQuery = (query != null && !query.isBlank()) ? query : search;
        List<AttendeeCandidateDto> candidates = exhibitorService.searchCandidates(
                principal.getEventId(),
                resolvedId,
                effectiveQuery,
                role,
                page,
                size
        );
        return ResponseEntity.ok(candidates);
    }

    @GetMapping({"/leads", "/{exhibitorId}/leads"})
    public ResponseEntity<List<LeadDto>> getLeads(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @RequestParam(required = false) LeadStatus status
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        return ResponseEntity.ok(exhibitorService.getLeads(resolvedId, status));
    }

    @PostMapping({"/leads", "/{exhibitorId}/leads"})
    public ResponseEntity<LeadDto> captureLead(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @Valid @RequestBody CaptureLeadRequest request
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        LeadDto lead = exhibitorService.captureLead(resolvedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lead);
    }

    @PatchMapping({"/leads/{leadId}", "/leads/{leadId}/status", "/{exhibitorId}/leads/{leadId}", "/{exhibitorId}/leads/{leadId}/status"})
    public ResponseEntity<LeadDto> updateLeadStatus(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @PathVariable String leadId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @Valid @RequestBody UpdateLeadStatusRequest request
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        LeadDto lead = exhibitorService.updateLeadStatus(resolvedId, leadId, request);
        return ResponseEntity.ok(lead);
    }

    @GetMapping({"/leads/export", "/{exhibitorId}/leads/export"})
    public ResponseEntity<byte[]> exportLeadsCsv(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        byte[] csvBytes = exhibitorService.exportLeadsCsv(resolvedId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"booth-leads-" + resolvedId + ".csv\"");
        headers.setContentLength(csvBytes.length);

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @GetMapping({"/products", "/{exhibitorId}/products"})
    public ResponseEntity<List<ProductDto>> getProducts(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        return ResponseEntity.ok(exhibitorService.getProducts(resolvedId));
    }

    @GetMapping({"/products/{productId}", "/{exhibitorId}/products/{productId}"})
    public ResponseEntity<ProductDto> getProductById(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @PathVariable String productId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        return ResponseEntity.ok(exhibitorService.getProductById(resolvedId, productId));
    }

    @PostMapping({"/products", "/{exhibitorId}/products"})
    public ResponseEntity<ProductDto> createProduct(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        ProductDto created = exhibitorService.createProduct(resolvedId, request);
        return ResponseEntity.created(URI.create("/api/v1/exhibitor/products/" + created.getId())).body(created);
    }

    @PutMapping({"/products/{productId}", "/{exhibitorId}/products/{productId}"})
    public ResponseEntity<ProductDto> updateProduct(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @PathVariable String productId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        return ResponseEntity.ok(exhibitorService.updateProduct(resolvedId, productId, request));
    }

    @DeleteMapping({"/products/{productId}", "/{exhibitorId}/products/{productId}"})
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @PathVariable String productId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
        exhibitorService.deleteProduct(resolvedId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping({"/brochure", "/{exhibitorId}/brochure"})
    public ResponseEntity<ExhibitorStatsDto> updateBrochure(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable(name = "exhibitorId", required = false) String exhibitorId,
            @RequestParam(name = "exhibitorId", required = false) String paramExhibitorId,
            @RequestBody UpdateBrochureRequest request
    ) {
        String explicitId = (exhibitorId != null && !exhibitorId.isBlank()) ? exhibitorId : paramExhibitorId;
        String resolvedId = resolveExhibitorId(principal, explicitId);
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
