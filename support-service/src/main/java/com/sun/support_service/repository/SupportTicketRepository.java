package com.sun.support_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sun.support_service.entity.SupportTicket;
import com.sun.support_service.entity.TicketStatus;
import com.sun.support_service.entity.TicketCategory;
import com.sun.support_service.entity.TicketPriority;


@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
	
	// User's own tickets — most recent first
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // User's own ticket by ticket number — for security check
    Optional<SupportTicket> findByTicketNumberAndUserId(String ticketNumber, Long userId);
    
    // Admin: lookup any ticket by number
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    
    // Admin: filter by status
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);
    
    // Admin: filter by category
    List<SupportTicket> findByCategoryOrderByCreatedAtDesc(TicketCategory category);
    
    // Admin: filter by priority
    List<SupportTicket> findByPriorityOrderByCreatedAtDesc(TicketPriority priority);
    
    // Count for ticket number generation
    long countByTicketNumberStartingWith(String prefix);
}
