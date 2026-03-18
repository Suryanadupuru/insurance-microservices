package com.sun.support_service.exception;

public class TicketNotFoundException extends RuntimeException{

	public TicketNotFoundException(String ticketNumber) {
		super("Ticket not found "+ ticketNumber);
	}

}
