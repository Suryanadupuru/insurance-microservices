package com.sun.support_service.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.sun.support_service.entity.*;

/**
 * Full support ticket response.
 * adminNotes is excluded from user-facing responses — filtered in controller.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
	
	private Long id;
    private String ticketNumber;
 
    // Ownership
    private Long userId;
    private String userEmail;
 
    // Request details
    private String subject;
    private String description;
    private TicketCategory category;
    private String referenceNumber;
 
    // Status
    private TicketStatus status;
    private String statusLabel;
    private TicketPriority priority;
 
    // Resolution
    private String adminNotes;          // null for user-facing responses
    private String resolutionNotes;
 
    // Dates
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
