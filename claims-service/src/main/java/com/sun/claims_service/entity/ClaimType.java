package com.sun.claims_service.entity;

/**
 * Type of incident being claimed.
 * Loosely mapped to policy types but users choose the specific incident.
 */

public enum ClaimType {
	
	MEDICAL,            // hospital, surgery, outpatient
    ACCIDENT,           // vehicle or personal accident
    PROPERTY_DAMAGE,    // home or vehicle damage
    THEFT,              // vehicle or home theft
    NATURAL_DISASTER,   // flood, earthquake, storm
    DEATH,              // life policy claim
    TRAVEL_EMERGENCY,   // medical emergency abroad, trip cancellation
    OTHER			  // any other type of claim not categorized above

}
