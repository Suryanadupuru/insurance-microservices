package com.sun.claims_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
	
	private boolean success;
	private String message;
	
	public static MessageResponse of(String message) {
		return MessageResponse.builder().success(true).message(message).build();
	}

}
