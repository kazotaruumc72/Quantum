package com.wynvers.quantum.orders;

/**
 * Statut d'un ordre
 */
public enum OrderStatus {
    ACTIVE,      // Ordre actif, en attente
    COMPLETED,   // Ordre complété
    CANCELLED,   // Ordre annulé
    EXPIRED;     // Ordre expiré
}
