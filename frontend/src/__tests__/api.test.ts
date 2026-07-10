import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiRequest, createService, getServices, login, updateService } from '@/lib/api';
import { saveToken } from '@/lib/auth';

describe('api client', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('attaches the saved bearer token to authenticated requests', async () => {
    saveToken('access-token');
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );

    await apiRequest<{ ok: boolean }>('/incidents');

    const headers = fetchMock.mock.calls[0][1]?.headers as Headers;
    expect(headers.get('Authorization')).toBe('Bearer access-token');
  });

  it('does not attach authorization to login requests', async () => {
    saveToken('access-token');
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          accessToken: 'new-access-token',
          refreshToken: 'refresh-token',
          tokenType: 'Bearer',
          expiresIn: 3600,
        }),
        {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        },
      ),
    );

    await login({ email: 'admin@example.com', password: 'password' });

    const headers = fetchMock.mock.calls[0][1]?.headers as Headers;
    expect(headers.get('Authorization')).toBeNull();
    expect(headers.get('Content-Type')).toBe('application/json');
  });

  it('requests services with filters', async () => {
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          content: [],
          totalElements: 0,
          totalPages: 0,
          number: 0,
          size: 20,
        }),
        {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        },
      ),
    );

    await getServices({ status: 'OPERATIONAL', tier: 'TIER1' });

    expect(fetchMock.mock.calls[0][0]).toContain(
      '/services?status=OPERATIONAL&tier=TIER1&page=0&size=20',
    );
  });

  it('sends service create and update requests as JSON', async () => {
    const fetchMock = vi
      .spyOn(global, 'fetch')
      .mockResolvedValueOnce(serviceResponse())
      .mockResolvedValueOnce(serviceResponse());

    await createService({ name: 'Payments API', tier: 'TIER1' });
    await updateService('service-id', { status: 'DEGRADED' });

    expect(fetchMock.mock.calls[0][1]?.method).toBe('POST');
    expect(fetchMock.mock.calls[0][1]?.body).toBe(
      JSON.stringify({ name: 'Payments API', tier: 'TIER1' }),
    );
    expect(fetchMock.mock.calls[1][1]?.method).toBe('PATCH');
    expect(fetchMock.mock.calls[1][1]?.body).toBe(
      JSON.stringify({ status: 'DEGRADED' }),
    );
  });
});

function serviceResponse(): Response {
  return new Response(
    JSON.stringify({
      id: 'service-id',
      name: 'Payments API',
      tier: 'TIER1',
      status: 'OPERATIONAL',
      openIncidentCount: 0,
      createdAt: '2026-05-18T00:00:00Z',
      updatedAt: '2026-05-18T00:00:00Z',
    }),
    {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    },
  );
}
