package com.sun.policy_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic message response used for endpoints that only need to
 * return a success flag and a message.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
	
	private boolean success;
	private String message;
	
	// ── Static factory helpers
	
	public static MessageResponse of(String message) {
		return MessageResponse.builder()
				.success(true)
				.message(message)
				.build();
	}

	public static MessageResponse error(String message) {
		return MessageResponse.builder()
				.success(false)
				.message(message)
				.build();
	}
}
