'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import { DashboardShell } from '@/components/DashboardShell';
import { getServices } from '@/lib/api';
import type { ServiceResponse, ServiceStatus, ServiceTier } from '@/types/api';

const statusOptions: ServiceStatus[] = ['OPERATIONAL', 'DEGRADED', 'DOWN', 'MAINTENANCE'];
const tierOptions: ServiceTier[] = ['TIER1', 'TIER2', 'TIER3'];

export default function ServicesPage() {
  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [status, setStatus] = useState<ServiceStatus | ''>('');
  const [tier, setTier] = useState<ServiceTier | ''>('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const filters = useMemo(
    () => ({
      status: status || undefined,
      tier: tier || undefined,
    }),
    [status, tier],
  );

  useEffect(() => {
    let isMounted = true;

    async function loadServices() {
      setIsLoading(true);
      setError(null);

      try {
        const response = await getServices(filters);

        if (isMounted) {
          setServices(response.content);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(loadError instanceof Error ? loadError.message : 'Unable to load services');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadServices();

    return () => {
      isMounted = false;
    };
  }, [filters]);

  return (
    <DashboardShell
      title="Service Catalog"
      actions={
        <Link
          href="/dashboard/services/new"
          className="rounded-md bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300"
        >
          New service
        </Link>
      }
    >
      <section className="mx-auto max-w-6xl px-6 py-8">
        <div className="mb-5 grid gap-4 md:grid-cols-2">
          <label className="text-sm font-medium text-slate-300">
            Status
            <select
              value={status}
              onChange={(event) => setStatus(event.target.value as ServiceStatus | '')}
              className="mt-2 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-slate-100 outline-none transition focus:border-cyan-400"
            >
              <option value="">All statuses</option>
              {statusOptions.map((option) => (
                <option key={option} value={option}>
                  {formatStatus(option)}
                </option>
              ))}
            </select>
          </label>

          <label className="text-sm font-medium text-slate-300">
            Tier
            <select
              value={tier}
              onChange={(event) => setTier(event.target.value as ServiceTier | '')}
              className="mt-2 w-full rounded-md border border-slate-700 bg-slate-900 px-3 py-2 text-slate-100 outline-none transition focus:border-cyan-400"
            >
              <option value="">All tiers</option>
              {tierOptions.map((option) => (
                <option key={option} value={option}>
                  {formatTier(option)}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="overflow-hidden rounded-lg border border-slate-800 bg-slate-900">
          {isLoading ? (
            <p className="p-6 text-sm text-slate-400">Loading services...</p>
          ) : error ? (
            <p className="p-6 text-sm text-red-200">{error}</p>
          ) : services.length === 0 ? (
            <p className="p-6 text-sm text-slate-400">
              No services found for the selected filters.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-800 text-sm">
                <thead className="bg-slate-950/60 text-left text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Name</th>
                    <th className="px-4 py-3 font-semibold">Tier</th>
                    <th className="px-4 py-3 font-semibold">Status</th>
                    <th className="px-4 py-3 font-semibold">Team</th>
                    <th className="px-4 py-3 font-semibold">Endpoint URL</th>
                    <th className="px-4 py-3 text-right font-semibold">Open incidents</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {services.map((service) => (
                    <tr key={service.id} className="hover:bg-slate-800/50">
                      <td className="px-4 py-3 font-medium text-slate-100">
                        <Link
                          href={`/dashboard/services/${service.id}`}
                          className="transition hover:text-cyan-300"
                        >
                          {service.name}
                        </Link>
                      </td>
                      <td className="px-4 py-3 text-slate-300">{formatTier(service.tier)}</td>
                      <td className="px-4 py-3">
                        <span className={statusBadgeClass(service.status)}>
                          {formatStatus(service.status)}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-slate-300">
                        {service.team?.name ?? 'Unassigned'}
                      </td>
                      <td className="max-w-xs truncate px-4 py-3 text-slate-300">
                        {service.endpointUrl ? (
                          <a
                            href={service.endpointUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="transition hover:text-cyan-300"
                          >
                            {service.endpointUrl}
                          </a>
                        ) : (
                          'Not set'
                        )}
                      </td>
                      <td className="px-4 py-3 text-right text-slate-300">
                        {service.openIncidentCount}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>
    </DashboardShell>
  );
}

function formatStatus(status: ServiceStatus): string {
  return status
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

function formatTier(tier: ServiceTier): string {
  return tier.replace('TIER', 'Tier ');
}

function statusBadgeClass(status: ServiceStatus): string {
  const base = 'rounded-full px-2 py-1 text-xs font-medium';

  switch (status) {
    case 'OPERATIONAL':
      return `${base} bg-emerald-950 text-emerald-300`;
    case 'DEGRADED':
      return `${base} bg-amber-950 text-amber-300`;
    case 'DOWN':
      return `${base} bg-red-950 text-red-300`;
    case 'MAINTENANCE':
      return `${base} bg-sky-950 text-sky-300`;
  }
}
