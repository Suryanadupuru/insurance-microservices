package com.sun.support_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.sun.support_service.entity.*;

/**
 * Request body for PUT /support/tickets/{ticketNumber}/status — admin update.
 */

@Data
public class UpdateTicketStatusRequest {
	
	@NotNull(message = "Status is required")
    private TicketStatus status;
 
    /** Optional — admin can reprioritize at any point. */
    private TicketPriority priority;
 
    /** Internal notes — visible to admin only. */
    private String adminNotes;
 
    /**
     * Resolution message shown to the user.
     * Required when status = RESOLVED.
     */
    private String resolutionNotes;

}
