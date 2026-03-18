package com.sun.support_service.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * SupportTicket — a user's customer support request.
 *
 * Table: support_tickets (owned by this service)
 */

@Entity
@Table(name = "support_tickets", indexes = {
		@Index(name = "idx_ticket_user_id", columnList = "userId"),
		@Index(name = "idx_ticket_number", columnList = "ticketNumber", unique = true),
		@Index(name = "idx_ticket_status", columnList = "status"),
		@Index(name = "idx_ticket_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ── Ownership
	
	@Column(nullable = false)
	private Long userId;
	
	@Column(nullable = false, length = 150)
	private String userEmail;
	
	// ── Ticket Identity
	
	@Column(nullable = false, unique = true, length = 50)
    private String ticketNumber;        // generated e.g. TKT-202501-000001
 
	// ── Request Details
	
	@Column(nullable = false, length = 200)
    private String subject;
 
    @Column(nullable = false, length = 3000)
    private String description;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;
    
    
    /**
     * Optional: user can reference a related policy or claim number.
     * Helps the support agent locate the relevant context immediately.
     */
    @Column(length = 50)
    private String referenceNumber;     // e.g. POL-202501-000001 or CLM-202501-000001
    
    // ── Admin Fields
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;
 
    @Column(length = 2000)
    private String adminNotes;          // internal notes visible to admin only
 
    @Column(length = 2000)
    private String resolutionNotes;     // resolution message shown to user
 
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    
    
    // ── Audit
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
 
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
}
