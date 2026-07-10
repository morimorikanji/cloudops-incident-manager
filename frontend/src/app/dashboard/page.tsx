import Link from 'next/link';
import { DashboardShell } from '@/components/DashboardShell';

export default function DashboardPage() {
  return (
    <DashboardShell title="Incident Dashboard">
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
            Service catalog data is available from the authenticated dashboard.
          </p>
          <Link
            href="/dashboard/services"
            className="mt-5 inline-flex rounded-md bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300"
          >
            View services
          </Link>
        </div>
      </section>
    </DashboardShell>
  );
}
