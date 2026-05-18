'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { DashboardShell } from '@/components/DashboardShell';
import { getService } from '@/lib/api';
import type { ServiceResponse, ServiceStatus, ServiceTier } from '@/types/api';

export default function ServiceDetailPage() {
  const params = useParams<{ id: string }>();
  const [service, setService] = useState<ServiceResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadService() {
      setIsLoading(true);
      setError(null);

      try {
        const response = await getService(params.id);

        if (isMounted) {
          setService(response);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(loadError instanceof Error ? loadError.message : 'Unable to load service');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadService();

    return () => {
      isMounted = false;
    };
  }, [params.id]);

  return (
    <DashboardShell
      title={service?.name ?? 'Service detail'}
      eyebrow="Service Catalog"
      actions={
        service ? (
          <Link
            href={`/dashboard/services/${service.id}/edit`}
            className="rounded-md bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300"
          >
            Edit
          </Link>
        ) : null
      }
    >
      <section className="mx-auto max-w-4xl px-6 py-8">
        {isLoading ? (
          <p className="rounded-lg border border-slate-800 bg-slate-900 p-6 text-sm text-slate-400">
            Loading service...
          </p>
        ) : error ? (
          <p className="rounded-lg border border-red-900 bg-red-950/60 p-6 text-sm text-red-200">
            {error}
          </p>
        ) : service ? (
          <div className="grid gap-5">
            <div className="rounded-lg border border-slate-800 bg-slate-900 p-6">
              <div className="flex flex-wrap gap-3">
                <span className="rounded-full bg-slate-800 px-3 py-1 text-sm text-slate-300">
                  {formatTier(service.tier)}
                </span>
                <span className={statusBadgeClass(service.status)}>
                  {formatStatus(service.status)}
                </span>
              </div>
              <p className="mt-5 text-sm leading-6 text-slate-300">
                {service.description || 'No description provided.'}
              </p>
            </div>

            <div className="grid gap-5 md:grid-cols-2">
              <DetailItem label="Team" value={service.team?.name ?? 'Unassigned'} />
              <DetailItem
                label="Open incidents"
                value={String(service.openIncidentCount)}
              />
              <DetailItem label="Endpoint URL" value={service.endpointUrl || 'Not set'} />
              <DetailItem
                label="Repository URL"
                value={service.repositoryUrl || 'Not set'}
              />
            </div>
          </div>
        ) : null}
      </section>
    </DashboardShell>
  );
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900 p-5">
      <p className="text-sm text-slate-400">{label}</p>
      <p className="mt-2 break-words text-sm font-medium text-slate-100">{value}</p>
    </div>
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
  const base = 'rounded-full px-3 py-1 text-sm font-medium';

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
