const ACCESS_TOKEN_KEY = 'cloudops.accessToken';

function getSessionStorage(): Storage | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return window.sessionStorage;
}

export function saveToken(token: string): void {
  getSessionStorage()?.setItem(ACCESS_TOKEN_KEY, token);
}

export function getToken(): string | null {
  return getSessionStorage()?.getItem(ACCESS_TOKEN_KEY) ?? null;
}

export function removeToken(): void {
  getSessionStorage()?.removeItem(ACCESS_TOKEN_KEY);
}

export function isAuthenticated(): boolean {
  return Boolean(getToken());
}
