package com.sun.support_service.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.support_service.dto.request.SubmitTicketRequest;
import com.sun.support_service.dto.request.UpdateTicketStatusRequest;
import com.sun.support_service.dto.response.*;
import com.sun.support_service.repository.SupportTicketRepository;
import com.sun.support_service.entity.*;
import com.sun.support_service.exception.InvalidTicketStatusTransitionException;
import com.sun.support_service.exception.TicketNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SupportService : Customer Support.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupportService {

	private final SupportTicketRepository ticketRepository;

	// ── Submit Ticket

	public TicketResponse submit(Long userId, String userEmail, SubmitTicketRequest request) {
		SupportTicket ticket = SupportTicket.builder()
				.userId(userId)
				.userEmail(userEmail)
				.ticketNumber(generateTicketNumber())
				.subject(request.getSubject())
				.description(request.getDescription())
				.category(request.getCategory())
				.referenceNumber(request.getReferenceNumber())
				.priority(TicketPriority.MEDIUM) // default; admin can reprioritize
				.status(TicketStatus.OPEN).build();

		SupportTicket saved = ticketRepository.save(ticket);
		log.info("Support ticket submitted: ticketNumber={}, userId={}", saved.getTicketNumber(), userId);

		return toUserResponse(saved);
	}

	// ── User: Read Own Tickets

	@Transactional(readOnly = true)
	public List<TicketResponse> getMyTickets(Long userId) {
		return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream().map(this::toUserResponse).toList();
	}

	@Transactional(readOnly = true)
	public TicketResponse getMyTicket(Long userId, String ticketNumber) {
		SupportTicket ticket = ticketRepository.findByTicketNumberAndUserId(ticketNumber, userId)
				.orElseThrow(() -> new TicketNotFoundException(ticketNumber));
		return toUserResponse(ticket);
	}

	// ── User: Close a Resolved Ticket

	public MessageResponse closeTicket(Long userId, String ticketNumber) {
		SupportTicket ticket = ticketRepository.findByTicketNumberAndUserId(ticketNumber, userId)
				.orElseThrow(() -> new TicketNotFoundException(ticketNumber));

		if (ticket.getStatus() != TicketStatus.RESOLVED) {
			throw new InvalidTicketStatusTransitionException(
					"Only RESOLVED tickets can be closed by the user. "
					+ "Current status: " + ticket.getStatus() + ".");
		}

		ticket.setStatus(TicketStatus.CLOSED);
		ticket.setClosedAt(LocalDateTime.now());
		ticketRepository.save(ticket);

		log.info("Ticket closed by user: ticketNumber={}, userId={}", ticketNumber, userId);
		return MessageResponse.of("Ticket " + ticketNumber + " has been closed. Thank you for your feedback.");
	}

	// ── Admin: Read All Tickets

	@Transactional(readOnly = true)
	public List<TicketResponse> getAllTickets(TicketStatus status, TicketCategory category, TicketPriority priority) {
		// Apply whichever filter was provided; if none, return all
		if (status != null) {
			return ticketRepository.findByStatusOrderByCreatedAtDesc(status).stream().map(this::toAdminResponse)
					.toList();
		}
		if (category != null) {
			return ticketRepository.findByCategoryOrderByCreatedAtDesc(category).stream().map(this::toAdminResponse)
					.toList();
		}
		if (priority != null) {
			return ticketRepository.findByPriorityOrderByCreatedAtDesc(priority).stream().map(this::toAdminResponse)
					.toList();
		}
		return ticketRepository.findAll().stream().map(this::toAdminResponse).toList();
	}

	@Transactional(readOnly = true)
	public TicketResponse getAnyTicket(String ticketNumber) {
		SupportTicket ticket = ticketRepository.findByTicketNumber(ticketNumber)
				.orElseThrow(() -> new TicketNotFoundException(ticketNumber));
		return toAdminResponse(ticket);
	}
	
	// ── Admin: Update Status

	public TicketResponse updateStatus(String ticketNumber, UpdateTicketStatusRequest request) {
		SupportTicket ticket = ticketRepository.findByTicketNumber(ticketNumber)
				.orElseThrow(() -> new TicketNotFoundException(ticketNumber));

		validateStatusTransition(ticket.getStatus(), request.getStatus());

		// Require resolutionNotes when resolving
		if (request.getStatus() == TicketStatus.RESOLVED) {
			if (request.getResolutionNotes() == null || request.getResolutionNotes().isBlank()) {
				throw new InvalidTicketStatusTransitionException(
						"resolutionNotes is required when resolving a ticket.");
			}
			ticket.setResolutionNotes(request.getResolutionNotes());
			ticket.setResolvedAt(LocalDateTime.now());
		}

		if (request.getStatus() == TicketStatus.CLOSED) {
			ticket.setClosedAt(LocalDateTime.now());
		}

		ticket.setStatus(request.getStatus());
		if (request.getPriority() != null)
			ticket.setPriority(request.getPriority());
		if (request.getAdminNotes() != null)
			ticket.setAdminNotes(request.getAdminNotes());

		SupportTicket updated = ticketRepository.save(ticket);
		log.info("Ticket status updated: ticketNumber={}, newStatus={}", ticketNumber, updated.getStatus());

		return toAdminResponse(updated);
	}
	
	// ── Private Helpers
	
	/**
	 * Enforces allowed status transitions:
	 *  OPEN → IN_PROGRESS
	 *  IN_PROGRESS →RESOLVED 
	 *  RESOLVED → CLOSED
	 *  CLOSED → (terminal)
	 */
	private void validateStatusTransition(TicketStatus current, TicketStatus next) {
		boolean valid = switch (current) {
		case OPEN -> next == TicketStatus.IN_PROGRESS;
		case IN_PROGRESS -> next == TicketStatus.RESOLVED;
		case RESOLVED -> next == TicketStatus.CLOSED;
		case CLOSED -> false;
		};

		if (!valid) {
			throw new InvalidTicketStatusTransitionException(
					"Cannot transition ticket from " + current + " to " + next + ".");
		}
	}

	/**
	 * Generates a unique ticket number: TKT-YYYYMM-XXXXXX e.g. TKT-202501-000001
	 */
	private String generateTicketNumber() {
		String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
		String prefix = "TKT-" + yearMonth + "-";
		long count = ticketRepository.countByTicketNumberStartingWith(prefix) + 1;
		return prefix + String.format("%06d", count);
	}

	private String statusLabel(TicketStatus status) {
		return switch (status) {
		case OPEN -> "Open";
		case IN_PROGRESS -> "In Progress";
		case RESOLVED -> "Resolved";
		case CLOSED -> "Closed";
		};
	}

	// ── Response Mapping

	/** User-facing response — adminNotes excluded. */
	private TicketResponse toUserResponse(SupportTicket t) {
		return TicketResponse.builder()
				.id(t.getId())
				.ticketNumber(t.getTicketNumber())
				.userId(t.getUserId())
				.userEmail(t.getUserEmail())
				.subject(t.getSubject())
				.description(t.getDescription())
				.category(t.getCategory())
				.referenceNumber(t.getReferenceNumber())
				.status(t.getStatus())
				.statusLabel(statusLabel(t.getStatus()))
				.priority(t.getPriority())
				.resolutionNotes(t.getResolutionNotes()) // adminNotes intentionally null for user view
				.resolvedAt(t.getResolvedAt())
				.closedAt(t.getClosedAt())
				.createdAt(t.getCreatedAt())
				.updatedAt(t.getUpdatedAt())
				.build();
	}

	/** Admin response — includes adminNotes. */
	private TicketResponse toAdminResponse(SupportTicket t) {
		return TicketResponse.builder()
				.id(t.getId())
				.ticketNumber(t.getTicketNumber())
				.userId(t.getUserId())
				.userEmail(t.getUserEmail())
				.subject(t.getSubject())
				.description(t.getDescription())
				.category(t.getCategory())
				.referenceNumber(t.getReferenceNumber())
				.status(t.getStatus())
				.statusLabel(statusLabel(t.getStatus()))
				.priority(t.getPriority())
				.adminNotes(t.getAdminNotes())
				.resolutionNotes(t.getResolutionNotes())
				.resolvedAt(t.getResolvedAt())
				.closedAt(t.getClosedAt())
				.createdAt(t.getCreatedAt())
				.updatedAt(t.getUpdatedAt())
				.build();
	}

}
