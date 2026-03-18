package com.sun.support_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sun.support_service.dto.response.TicketResponse;
import com.sun.support_service.dto.request.*;
import com.sun.support_service.service.SupportService;
import com.sun.support_service.dto.response.MessageResponse;
import com.sun.support_service.entity.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * SupportController
 *
 * Base path: /support
 * Via gateway: /api/support (StripPrefix=1 → /support)
 */

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {
	
	private final SupportService supportService;
	
	// ── USER ENDPOINTS
	
	@Tag(name = "Support — User")
    @PostMapping("/tickets")
    @Operation(
        summary     = "Submit a support request",
        description = """
            Create a new support ticket.
            Optionally include a referenceNumber (policy or claim number)
            to help the support team locate your context.
            """
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> submitTicket(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String userEmail,
            @Valid @RequestBody SubmitTicketRequest request) {
 
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.submit(userId, userEmail, request));
    }
	
	@Tag(name = "Support — User")
    @GetMapping("/tickets/my")
    @Operation(summary = "List all my support tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @RequestHeader("X-User-Id") Long userId) {
 
        return ResponseEntity.ok(supportService.getMyTickets(userId));
    }
	
	@Tag(name = "Support — User")
    @GetMapping("/tickets/my/{ticketNumber}")
    @Operation(summary = "Get one of my tickets by ticket number")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> getMyTicket(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String ticketNumber) {
 
        return ResponseEntity.ok(supportService.getMyTicket(userId, ticketNumber));
    }
	
	@Tag(name = "Support — User")
    @PutMapping("/tickets/my/{ticketNumber}/close")
    @Operation(
        summary     = "Close a resolved ticket",
        description = "Users can close a ticket once it has been RESOLVED by the support team."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> closeTicket(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String ticketNumber) {
 
        return ResponseEntity.ok(supportService.closeTicket(userId, ticketNumber));
    }
	
	// ── ADMIN ENDPOINTS

	@Tag(name = "Support — Admin")
	@GetMapping("/tickets")
	@Operation(summary = "List all support tickets — Admin only", 
			description = "Filter by status, category, or priority using optional query params.")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<TicketResponse>> getAllTickets(@RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) TicketCategory category,
			@RequestParam(required = false) TicketPriority priority) {

		return ResponseEntity.ok(supportService.getAllTickets(status, category, priority));
	}
	
	@Tag(name = "Support — Admin")
	@GetMapping("/tickets/{ticketNumber}")
	@Operation(summary = "Get any ticket by ticket number — Admin only")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<TicketResponse> getAnyTicket(@PathVariable String ticketNumber) {
		return ResponseEntity.ok(supportService.getAnyTicket(ticketNumber));
	}
	
	@Tag(name = "Support — Admin")
    @PutMapping("/tickets/{ticketNumber}/status")
    @Operation(
        summary     = "Update ticket status — Admin only",
        description = """
            Allowed transitions:
              OPEN        → IN_PROGRESS
              IN_PROGRESS → RESOLVED  (requires resolutionNotes)
              RESOLVED    → CLOSED
              CLOSED      → (terminal)
            
            Priority can be updated at any status change.
            """
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable String ticketNumber,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
 
        return ResponseEntity.ok(supportService.updateStatus(ticketNumber, request));
    }

}
