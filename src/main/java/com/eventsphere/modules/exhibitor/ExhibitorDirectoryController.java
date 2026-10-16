package com.eventsphere.modules.exhibitor;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.exhibitor.dto.ProductDto;
import com.eventsphere.modules.exhibitor.dto.PublicExhibitorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exhibitors")
public class ExhibitorDirectoryController {

    private final ExhibitorService exhibitorService;

    public ExhibitorDirectoryController(ExhibitorService exhibitorService) {
        this.exhibitorService = exhibitorService;
    }

    @GetMapping
    public ResponseEntity<List<PublicExhibitorDto>> getExhibitors(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        List<PublicExhibitorDto> list = exhibitorService.getPublicExhibitors(principal.getEventId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicExhibitorDto> getExhibitor(
            @PathVariable String id
    ) {
        PublicExhibitorDto exhibitor = exhibitorService.getPublicExhibitor(id);
        return ResponseEntity.ok(exhibitor);
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductDto>> getProducts(
            @PathVariable String id
    ) {
        List<ProductDto> products = exhibitorService.getPublicProducts(id);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/products/{id}/enquiry")
    public ResponseEntity<Map<String, Boolean>> recordProductEnquiry(
            @AuthenticationPrincipal EventSphereUserPrincipal principal,
            @PathVariable String id
    ) {
        exhibitorService.recordProductEnquiry(id, principal.getAttendeeId());
        return ResponseEntity.ok(Map.of("success", true));
    }
}
