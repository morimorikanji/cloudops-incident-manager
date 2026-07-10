import Link from 'next/link';
import { type ReactNode } from 'react';
import { AuthGuard } from '@/components/AuthGuard';
import { LogoutButton } from '@/components/LogoutButton';

interface DashboardShellProps {
  children: ReactNode;
  title: string;
  eyebrow?: string;
  actions?: ReactNode;
}

export function DashboardShell({
  children,
  title,
  eyebrow = 'CloudOps',
  actions,
}: DashboardShellProps) {
  return (
    <AuthGuard>
      <main className="min-h-screen bg-slate-950 text-slate-100">
        <header className="border-b border-slate-800 bg-slate-950/80">
          <div className="mx-auto flex max-w-6xl flex-col gap-4 px-6 py-5 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm font-medium text-cyan-300">{eyebrow}</p>
              <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
              <nav className="mt-3 flex gap-4 text-sm text-slate-400">
                <Link className="transition hover:text-slate-100" href="/dashboard">
                  Dashboard
                </Link>
                <Link
                  className="transition hover:text-slate-100"
                  href="/dashboard/services"
                >
                  Services
                </Link>
              </nav>
            </div>
            <div className="flex items-center gap-3">
              {actions}
              <LogoutButton />
            </div>
          </div>
        </header>

        {children}
      </main>
    </AuthGuard>
  );
}
