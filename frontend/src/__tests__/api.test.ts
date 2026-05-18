import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiRequest, login } from '@/lib/api';
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
});
