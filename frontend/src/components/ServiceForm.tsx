'use client';

import { FormEvent, useState } from 'react';
import type {
  ServiceRequest,
  ServiceResponse,
  ServiceStatus,
  ServiceTier,
  UpdateServiceRequest,
} from '@/types/api';

const tierOptions: ServiceTier[] = ['TIER1', 'TIER2', 'TIER3'];
const statusOptions: ServiceStatus[] = ['OPERATIONAL', 'DEGRADED', 'DOWN', 'MAINTENANCE'];

interface ServiceFormProps {
  initialService?: ServiceResponse;
  mode: 'create' | 'edit';
  onSubmit: (values: ServiceRequest | UpdateServiceRequest) => Promise<void>;
}

export function ServiceForm({ initialService, mode, onSubmit }: ServiceFormProps) {
  const [name, setName] = useState(initialService?.name ?? '');
  const [description, setDescription] = useState(initialService?.description ?? '');
  const [tier, setTier] = useState<ServiceTier>(initialService?.tier ?? 'TIER2');
  const [status, setStatus] = useState<ServiceStatus>(
    initialService?.status ?? 'OPERATIONAL',
  );
  const [teamId, setTeamId] = useState(initialService?.team?.id ?? '');
  const [endpointUrl, setEndpointUrl] = useState(initialService?.endpointUrl ?? '');
  const [repositoryUrl, setRepositoryUrl] = useState(initialService?.repositoryUrl ?? '');
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSaving(true);

    const values = compactRequest({
      name,
      description,
      tier,
      status: mode === 'edit' ? status : undefined,
      teamId,
      endpointUrl,
      repositoryUrl,
    });

    try {
      await onSubmit(values);
    } catch (submitError) {
      setError(
        submitError instanceof Error ? submitError.message : 'Unable to save service',
      );
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="grid gap-5 rounded-lg border border-slate-800 bg-slate-900 p-6"
    >
      <div>
        <label className="block text-sm font-medium text-slate-200" htmlFor="name">
          Name
        </label>
        <input
          id="name"
          required
          value={name}
          onChange={(event) => setName(event.target.value)}
          className="mt-2 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-400"
          placeholder="Payments API"
        />
      </div>

      <div>
        <label
          className="block text-sm font-medium text-slate-200"
          htmlFor="description"
        >
          Description
        </label>
        <textarea
          id="description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          className="mt-2 min-h-24 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-400"
          placeholder="What this service owns"
        />
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        <div>
          <label className="block text-sm font-medium text-slate-200" htmlFor="tier">
            Tier
          </label>
          <select
            id="tier"
            value={tier}
            onChange={(event) => setTier(event.target.value as ServiceTier)}
            className="mt-2 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition focus:border-cyan-400"
          >
            {tierOptions.map((option) => (
              <option key={option} value={option}>
                {formatTier(option)}
              </option>
            ))}
          </select>
        </div>

        {mode === 'edit' ? (
          <div>
            <label
              className="block text-sm font-medium text-slate-200"
              htmlFor="status"
            >
              Status
            </label>
            <select
              id="status"
              value={status}
              onChange={(event) => setStatus(event.target.value as ServiceStatus)}
              className="mt-2 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition focus:border-cyan-400"
            >
              {statusOptions.map((option) => (
                <option key={option} value={option}>
                  {formatStatus(option)}
                </option>
              ))}
            </select>
          </div>
        ) : null}
      </div>

      <div>
        <label className="block text-sm font-medium text-slate-200" htmlFor="teamId">
          Team ID
        </label>
        <input
          id="teamId"
          value={teamId}
          onChange={(event) => setTeamId(event.target.value)}
          className="mt-2 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-400"
          placeholder="Optional UUID"
        />
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        <div>
          <label
            className="block text-sm font-medium text-slate-200"
            htmlFor="endpointUrl"
          >
            Endpoint URL
          </label>
          <input
            id="endpointUrl"
            type="url"
            value={endpointUrl}
            onChange={(event) => setEndpointUrl(event.target.value)}
            className="mt-2 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-400"
            placeholder="https://api.example.com"
          />
        </div>

        <div>
          <label
            className="block text-sm font-medium text-slate-200"
            htmlFor="repositoryUrl"
          >
            Repository URL
          </label>
          <input
            id="repositoryUrl"
            type="url"
            value={repositoryUrl}
            onChange={(event) => setRepositoryUrl(event.target.value)}
            className="mt-2 w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-400"
            placeholder="https://github.com/example/service"
          />
        </div>
      </div>

      {error ? (
        <p className="rounded-md border border-red-900 bg-red-950/60 px-3 py-2 text-sm text-red-200">
          {error}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={isSaving}
        className="w-full rounded-md bg-cyan-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300 disabled:cursor-not-allowed disabled:bg-slate-700 disabled:text-slate-400 md:w-fit"
      >
        {isSaving ? 'Saving...' : mode === 'create' ? 'Create service' : 'Save changes'}
      </button>
    </form>
  );
}

function compactRequest(
  values: ServiceRequest & { status?: ServiceStatus },
): ServiceRequest | UpdateServiceRequest {
  return Object.fromEntries(
    Object.entries(values).map(([key, value]) => [
      key,
      typeof value === 'string' ? value.trim() || undefined : value,
    ]),
  ) as ServiceRequest | UpdateServiceRequest;
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
