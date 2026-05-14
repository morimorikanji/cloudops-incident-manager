// TypeScript types for API responses — expand as each endpoint is implemented

export type UserRole = 'ADMIN' | 'OPERATOR' | 'VIEWER';

export type ServiceStatus = 'OPERATIONAL' | 'DEGRADED' | 'DOWN' | 'MAINTENANCE';
export type ServiceTier = 'TIER1' | 'TIER2' | 'TIER3';

export type IncidentSeverity = 'P1' | 'P2' | 'P3' | 'P4';
export type IncidentStatus = 'OPEN' | 'INVESTIGATING' | 'MITIGATED' | 'RESOLVED' | 'CLOSED';

export type AlertSeverity = 'CRITICAL' | 'WARNING' | 'INFO';
export type AlertStatus = 'FIRING' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
