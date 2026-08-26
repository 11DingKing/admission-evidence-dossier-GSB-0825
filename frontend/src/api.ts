import type {
  BatchSourceResponse,
  CreateDossierRequest,
  DossierResponse,
  FactKey,
  SourceRecordRequest,
  SourceRecordResponse,
} from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options?.headers ?? {}) },
    ...options,
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`API ${res.status}: ${body || res.statusText}`);
  }
  if (res.status === 207) {
    return res.json() as Promise<T>;
  }
  return res.json() as Promise<T>;
}

export function createDossier(req: CreateDossierRequest): Promise<DossierResponse> {
  return request<DossierResponse>('/dossiers', {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export function getDossier(dossierId: string): Promise<DossierResponse> {
  return request<DossierResponse>(`/dossiers/${dossierId}`);
}

export function listDossiers(key: FactKey): Promise<DossierResponse[]> {
  const params = new URLSearchParams({
    provinceCode: key.provinceCode,
    admissionYear: String(key.admissionYear),
    subjectCategory: key.subjectCategory,
    schoolCode: key.schoolCode,
    majorGroupCode: key.majorGroupCode,
  });
  return request<DossierResponse[]>(`/dossiers?${params}`);
}

export function sealDossier(dossierId: string, asOf: string): Promise<DossierResponse> {
  return request<DossierResponse>(`/dossiers/${dossierId}/seal`, {
    method: 'POST',
    body: JSON.stringify({ asOf }),
  });
}

export function submitSourcesToDossier(
  dossierId: string,
  sources: SourceRecordRequest[],
): Promise<BatchSourceResponse> {
  return request<BatchSourceResponse>(`/dossiers/${dossierId}/sources/batch`, {
    method: 'POST',
    body: JSON.stringify({ sources }),
  });
}

export function getSourceHistory(key: FactKey): Promise<SourceRecordResponse[]> {
  const params = new URLSearchParams({
    provinceCode: key.provinceCode,
    admissionYear: String(key.admissionYear),
    subjectCategory: key.subjectCategory,
    schoolCode: key.schoolCode,
    majorGroupCode: key.majorGroupCode,
  });
  return request<SourceRecordResponse[]>(`/sources/history?${params}`);
}
