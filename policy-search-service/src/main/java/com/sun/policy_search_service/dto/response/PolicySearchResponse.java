package com.sun.policy_search_service.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated search result returned to the client.
 *
 * Wraps the list of matching policies with pagination metadata
 * so the frontend can render page controls without a second request.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicySearchResponse {
	
	private List<PolicyProductResponse> policies;

    /** Total number of policies matching the search criteria (across all pages). */
    private long totalElements;

    /** Total number of pages based on the requested page size. */
    private int totalPages;

    /** Current page number (0-based). */
    private int currentPage;

    /** Number of results in this page. */
    private int pageSize;

    /** Whether this is the first page. */
    private boolean first;

    /** Whether this is the last page. */
    private boolean last;

}
