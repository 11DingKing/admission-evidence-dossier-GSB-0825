import type {
  ApiError,
  DossierDetail,
  FetchReport,
  RevisionChain,
  SealView,
} from './types';

const BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const { headers, ...rest } = init ?? {};
  const res = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    headers: { 'Content-Type': 'application/json', ...headers },
  });
  if (!res.ok) {
    let body: { errorCode?: string; message?: string } | null = null;
    try {
      body = (await res.json()) as { errorCode?: string; message?: string };
    } catch {
      body = null;
    }
    throw {
      errorCode: body?.errorCode ?? 'HTTP_ERROR',
      message: body?.message ?? `请求失败（HTTP ${res.status}）`,
    } satisfies ApiError;
  }
  return (await res.json()) as T;
}

export function getDossier(dossierId: string, asOf?: string): Promise<DossierDetail> {
  const query = asOf ? `?as_of=${encodeURIComponent(asOf)}` : '';
  return request<DossierDetail>(`/dossiers/${dossierId}${query}`);
}

export function fetchSources(
  dossierId: string,
  body: { asOf?: string },
): Promise<FetchReport> {
  return request<FetchReport>(`/dossiers/${dossierId}/sources/fetch`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function createSeal(
  dossierId: string,
  body: { asOf: string },
): Promise<SealView> {
  return request<SealView>(`/dossiers/${dossierId}/seals`, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

export function getSeal(dossierId: string, sealId: string): Promise<SealView> {
  return request<SealView>(`/dossiers/${dossierId}/seals/${sealId}`);
}

export function getRevisions(
  dossierId: string,
  sourceId: string,
): Promise<RevisionChain> {
  return request<RevisionChain>(
    `/dossiers/${dossierId}/sources/${sourceId}/revisions`,
  );
}
