// TypeScript types for API responses — expand as each endpoint is implemented

export type UserRole = 'ADMIN' | 'OPERATOR' | 'VIEWER';

export interface AuthUser {
  id?: number | string;
  email?: string;
  name?: string;
  role?: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user?: AuthUser;
}

export type ServiceStatus = 'OPERATIONAL' | 'DEGRADED' | 'DOWN' | 'MAINTENANCE';
export type ServiceTier = 'TIER1' | 'TIER2' | 'TIER3';

export interface TeamInfo {
  id: string;
  name: string;
}

export interface ServiceResponse {
  id: string;
  name: string;
  description?: string | null;
  team?: TeamInfo | null;
  tier: ServiceTier;
  status: ServiceStatus;
  endpointUrl?: string | null;
  repositoryUrl?: string | null;
  openIncidentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceRequest {
  name: string;
  description?: string;
  teamId?: string;
  tier: ServiceTier;
  endpointUrl?: string;
  repositoryUrl?: string;
}

export interface UpdateServiceRequest {
  name?: string;
  description?: string;
  teamId?: string;
  tier?: ServiceTier;
  status?: ServiceStatus;
  endpointUrl?: string;
  repositoryUrl?: string;
}

export type IncidentSeverity = 'P1' | 'P2' | 'P3' | 'P4';
export type IncidentStatus = 'OPEN' | 'INVESTIGATING' | 'MITIGATED' | 'RESOLVED' | 'CLOSED';

export type AlertSeverity = 'CRITICAL' | 'WARNING' | 'INFO';
export type AlertStatus = 'FIRING' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page?: number;
  number?: number;
  size: number;
}
