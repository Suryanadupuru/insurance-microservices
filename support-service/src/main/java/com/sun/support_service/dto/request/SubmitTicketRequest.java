package com.sun.support_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.sun.support_service.entity.TicketCategory;

/**
 * Request body for POST /support/tickets — submit a support request.
 */

@Data
public class SubmitTicketRequest {

	@NotBlank(message = "Subject is required")
	@Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
	private String subject;

	@NotBlank(message = "Description is required")
	@Size(min = 20, max = 3000, message = "Description must be between 20 and 3000 characters")
	private String description;

	@NotNull(message = "Category is required")
	private TicketCategory category;

	/**
	 * Optional: a policy number (POL-...) or claim number (CLM-...) this request
	 * relates to. Helps the support agent find context faster.
	 */
	@Size(max = 50, message = "Reference number must not exceed 50 characters")
	private String referenceNumber;

}
