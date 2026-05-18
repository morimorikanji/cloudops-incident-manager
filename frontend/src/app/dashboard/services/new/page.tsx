'use client';

import { useRouter } from 'next/navigation';
import { DashboardShell } from '@/components/DashboardShell';
import { ServiceForm } from '@/components/ServiceForm';
import { createService } from '@/lib/api';
import type { ServiceRequest, UpdateServiceRequest } from '@/types/api';

export default function NewServicePage() {
  const router = useRouter();

  async function handleCreate(values: ServiceRequest | UpdateServiceRequest) {
    const service = await createService(values as ServiceRequest);
    router.replace(`/dashboard/services/${service.id}`);
  }

  return (
    <DashboardShell title="Create Service" eyebrow="Service Catalog">
      <section className="mx-auto max-w-3xl px-6 py-8">
        <ServiceForm mode="create" onSubmit={handleCreate} />
      </section>
    </DashboardShell>
  );
}
