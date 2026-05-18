'use client';

import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { DashboardShell } from '@/components/DashboardShell';
import { ServiceForm } from '@/components/ServiceForm';
import { getService, updateService } from '@/lib/api';
import type {
  ServiceRequest,
  ServiceResponse,
  UpdateServiceRequest,
} from '@/types/api';

export default function EditServicePage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
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

  async function handleUpdate(values: ServiceRequest | UpdateServiceRequest) {
    const updatedService = await updateService(params.id, values as UpdateServiceRequest);
    router.replace(`/dashboard/services/${updatedService.id}`);
  }

  return (
    <DashboardShell title="Edit Service" eyebrow="Service Catalog">
      <section className="mx-auto max-w-3xl px-6 py-8">
        {isLoading ? (
          <p className="rounded-lg border border-slate-800 bg-slate-900 p-6 text-sm text-slate-400">
            Loading service...
          </p>
        ) : error ? (
          <p className="rounded-lg border border-red-900 bg-red-950/60 p-6 text-sm text-red-200">
            {error}
          </p>
        ) : service ? (
          <ServiceForm mode="edit" initialService={service} onSubmit={handleUpdate} />
        ) : null}
      </section>
    </DashboardShell>
  );
}
