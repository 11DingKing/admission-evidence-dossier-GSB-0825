import { useCallback, useEffect, useRef, useState } from 'react';
import * as client from '../api/client';
import type {
  DossierDetail,
  FetchReport,
  RevisionChain,
  SealView,
} from '../api/types';

export const DOSSIERS = [
  {
    dossierId: '11111111-1111-4111-8111-111111111111',
    name: '深圳职业技术大学',
  },
  {
    dossierId: '22222222-2222-4222-8222-222222222222',
    name: '武汉职业技术大学',
  },
] as const;

export function asOfToIso(dateInput: string): string | undefined {
  if (!dateInput) return undefined;
  return new Date(`${dateInput}T12:00:00`).toISOString();
}

function toMessage(e: unknown, fallback: string): string {
  if (typeof e === 'object' && e !== null && 'message' in e) {
    const message = (e as { message?: unknown }).message;
    if (typeof message === 'string') return message;
  }
  return fallback;
}

export function useWorkbench() {
  const [dossierId, setDossierId] = useState<string>(DOSSIERS[0].dossierId);
  const [asOfInput, setAsOfInput] = useState<string>('');
  const [detail, setDetail] = useState<DossierDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [verifying, setVerifying] = useState<boolean>(false);
  const [sealing, setSealing] = useState<boolean>(false);
  const [report, setReport] = useState<FetchReport | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [seal, setSeal] = useState<SealView | null>(null);
  const [drawerSourceId, setDrawerSourceId] = useState<string | null>(null);
  const [chain, setChain] = useState<RevisionChain | null>(null);
  const [chainLoading, setChainLoading] = useState<boolean>(false);
  const drawerTriggerRef = useRef<HTMLButtonElement | null>(null);

  const reload = useCallback(async (id: string, asOf: string) => {
    setLoading(true);
    setLoadError(null);
    setDrawerSourceId(null);
    try {
      const d = await client.getDossier(id, asOfToIso(asOf));
      setDetail(d);
      setSeal(null);
      setReport(null);
    } catch (e) {
      setLoadError(toMessage(e, '卷宗加载失败'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void reload(dossierId, asOfInput);
  }, [dossierId, asOfInput, reload]);

  const verify = useCallback(async () => {
    setVerifying(true);
    setReport(null);
    try {
      const r = await client.fetchSources(dossierId, {
        asOf: asOfToIso(asOfInput),
      });
      setReport(r);
      const d = await client.getDossier(dossierId, asOfToIso(asOfInput));
      setDetail(d);
    } catch (e) {
      setLoadError(toMessage(e, '核验失败'));
    } finally {
      setVerifying(false);
    }
  }, [dossierId, asOfInput]);

  const sealDossier = useCallback(async () => {
    setSealing(true);
    try {
      await client.createSeal(dossierId, {
        asOf: asOfToIso(asOfInput) ?? new Date().toISOString(),
      });
      const d = await client.getDossier(dossierId, asOfToIso(asOfInput));
      setDetail(d);
    } catch (e) {
      setLoadError(toMessage(e, '封存失败'));
    } finally {
      setSealing(false);
    }
  }, [dossierId, asOfInput]);

  const viewSeal = useCallback(
    async (sealId: string) => {
      const s = await client.getSeal(dossierId, sealId);
      setSeal(s);
      setDrawerSourceId(null);
    },
    [dossierId],
  );

  const backToCurrent = useCallback(() => {
    setSeal(null);
  }, []);

  const openDrawer = useCallback(
    async (sourceId: string, trigger: HTMLButtonElement) => {
      drawerTriggerRef.current = trigger;
      setDrawerSourceId(sourceId);
      setChain(null);
      setChainLoading(true);
      try {
        const c = await client.getRevisions(dossierId, sourceId);
        setChain(c);
      } finally {
        setChainLoading(false);
      }
    },
    [dossierId],
  );

  const closeDrawer = useCallback(() => {
    setDrawerSourceId(null);
    setChain(null);
    setChainLoading(false);
    drawerTriggerRef.current?.focus();
    drawerTriggerRef.current = null;
  }, []);

  return {
    dossierId,
    setDossierId,
    asOfInput,
    setAsOfInput,
    detail,
    loading,
    verifying,
    sealing,
    report,
    loadError,
    seal,
    verify,
    sealDossier,
    viewSeal,
    backToCurrent,
    drawerSourceId,
    chain,
    chainLoading,
    openDrawer,
    closeDrawer,
  };
}

export type WorkbenchApi = ReturnType<typeof useWorkbench>;
