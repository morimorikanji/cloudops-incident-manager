'use client';

import { useRouter } from 'next/navigation';
import { removeToken } from '@/lib/auth';

export function LogoutButton() {
  const router = useRouter();

  function handleLogout() {
    removeToken();
    router.replace('/login');
  }

  return (
    <button
      type="button"
      onClick={handleLogout}
      className="rounded-md border border-slate-700 px-3 py-2 text-sm font-medium text-slate-200 transition hover:border-slate-500 hover:bg-slate-800"
    >
      Log out
    </button>
  );
}
