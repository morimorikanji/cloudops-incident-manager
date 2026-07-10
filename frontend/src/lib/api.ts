import { getToken } from '@/lib/auth';
import type {
  LoginRequest,
  LoginResponse,
  PageResponse,
  ServiceRequest,
  ServiceResponse,
  ServiceStatus,
  ServiceTier,
  UpdateServiceRequest,
} from '@/types/api';

export const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080/api/v1';

interface ApiRequestOptions extends RequestInit {
  auth?: boolean;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function apiRequest<T>(
  path: string,
  { auth = true, headers, body, ...options }: ApiRequestOptions = {},
): Promise<T> {
  const requestHeaders = new Headers(headers);

  if (body && !requestHeaders.has('Content-Type')) {
    requestHeaders.set('Content-Type', 'application/json');
  }

  if (auth) {
    const token = getToken();

    if (token) {
      requestHeaders.set('Authorization', `Bearer ${token}`);
    }
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    body,
    headers: requestHeaders,
  });

  if (!response.ok) {
    throw new ApiError(await getErrorMessage(response), response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function login(credentials: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    auth: false,
    body: JSON.stringify(credentials),
  });
}

export interface ServiceFilters {
  status?: ServiceStatus;
  tier?: ServiceTier;
  page?: number;
  size?: number;
}

export function getServices(
  filters: ServiceFilters = {},
): Promise<PageResponse<ServiceResponse>> {
  const params = new URLSearchParams();

  if (filters.status) {
    params.set('status', filters.status);
  }

  if (filters.tier) {
    params.set('tier', filters.tier);
  }

  params.set('page', String(filters.page ?? 0));
  params.set('size', String(filters.size ?? 20));

  return apiRequest<PageResponse<ServiceResponse>>(`/services?${params.toString()}`);
}

export function getService(id: string): Promise<ServiceResponse> {
  return apiRequest<ServiceResponse>(`/services/${id}`);
}

export function createService(request: ServiceRequest): Promise<ServiceResponse> {
  return apiRequest<ServiceResponse>('/services', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function updateService(
  id: string,
  request: UpdateServiceRequest,
): Promise<ServiceResponse> {
  return apiRequest<ServiceResponse>(`/services/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  });
}

async function getErrorMessage(response: Response): Promise<string> {
  try {
    const data = (await response.json()) as { message?: string; error?: string };
    return data.message ?? data.error ?? 'Request failed';
  } catch {
    return 'Request failed';
  }
}
