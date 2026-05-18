import { AuthGuard } from '@/components/AuthGuard';
import { LogoutButton } from '@/components/LogoutButton';

export default function DashboardPage() {
  return (
    <AuthGuard>
      <main className="min-h-screen bg-slate-950 text-slate-100">
        <header className="border-b border-slate-800 bg-slate-950/80">
          <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
            <div>
              <p className="text-sm font-medium text-cyan-300">CloudOps</p>
              <h1 className="text-2xl font-semibold tracking-tight">
                Incident Dashboard
              </h1>
            </div>
            <LogoutButton />
          </div>
        </header>

        <section className="mx-auto grid max-w-6xl gap-4 px-6 py-8 md:grid-cols-3">
          <div className="rounded-lg border border-slate-800 bg-slate-900 p-5">
            <p className="text-sm text-slate-400">Open incidents</p>
            <p className="mt-3 text-4xl font-semibold">0</p>
          </div>
          <div className="rounded-lg border border-slate-800 bg-slate-900 p-5">
            <p className="text-sm text-slate-400">Services degraded</p>
            <p className="mt-3 text-4xl font-semibold">0</p>
          </div>
          <div className="rounded-lg border border-slate-800 bg-slate-900 p-5">
            <p className="text-sm text-slate-400">Active alerts</p>
            <p className="mt-3 text-4xl font-semibold">0</p>
          </div>
        </section>

        <section className="mx-auto max-w-6xl px-6 pb-10">
          <div className="rounded-lg border border-slate-800 bg-slate-900 p-6">
            <h2 className="text-lg font-semibold">Operations overview</h2>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">
              You are signed in. Protected API requests from the frontend will
              include the saved bearer token.
            </p>
          </div>
        </section>
      </main>
    </AuthGuard>
  );
}
