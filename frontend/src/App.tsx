import { useCallback, useState } from 'react';
import { createDossier, getDossier, getSourceHistory, listDossiers, sealDossier } from './api';
import type {
  DossierResponse,
  FactKey,
  SourceRecordResponse,
} from './types';
import { SourceQueue } from './components/SourceQueue';
import { CurrentFact } from './components/CurrentFact';
import { SourceDrawer } from './components/SourceDrawer';
import { VersionDiff } from './components/VersionDiff';

const SHENZHEN_KEY: FactKey = {
  provinceCode: '44',
  admissionYear: 2025,
  subjectCategory: 'PHYSICS',
  schoolCode: '11113',
  majorGroupCode: '001',
};

interface Gap {
  sourceId: string;
  message: string;
}

type LoadState = 'idle' | 'loading' | 'ready' | 'error';

export function App() {
  const [dossierIdInput, setDossierIdInput] = useState('');
  const [dossier, setDossier] = useState<DossierResponse | null>(null);
  const [history, setHistory] = useState<SourceRecordResponse[]>([]);
  const [state, setState] = useState<LoadState>('idle');
  const [error, setError] = useState<string | null>(null);
  const [partialError, setPartialError] = useState<string | null>(null);
  const [gaps, setGaps] = useState<Gap[]>([]);
  const [selectedSourceId, setSelectedSourceId] = useState<string | null>(null);
  const [sealAsOf, setSealAsOf] = useState('');
  const [sealing, setSealing] = useState(false);

  const activeSources = dossier
    ? dossier.sealed
      ? dossier.sealedEntries.map((e) => ({ ...e, isLatestRevision: true }))
      : dossier.sources
    : [];

  const loadHistory = useCallback(async (key: FactKey) => {
    try {
      const h = await getSourceHistory(key);
      setHistory(h);
      setPartialError(null);
    } catch {
      setHistory([]);
      setPartialError('版本历史加载失败，已保留当前证据链');
    }
  }, []);

  const loadDossier = useCallback(async (id: string) => {
    if (!id.trim()) return;
    setState('loading');
    setError(null);
    setPartialError(null);
    setGaps([]);
    try {
      const d = await getDossier(id.trim());
      setDossier(d);
      const key: FactKey = {
        provinceCode: d.provinceCode,
        admissionYear: d.admissionYear,
        subjectCategory: d.subjectCategory,
        schoolCode: d.schoolCode,
        majorGroupCode: d.majorGroupCode,
      };
      await loadHistory(key);
      setState('ready');
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setState('error');
    }
  }, [loadHistory]);

  const loadShenzhen = useCallback(async () => {
    setState('loading');
    setError(null);
    setPartialError(null);
    setGaps([]);
    try {
      const list = await listDossiers(SHENZHEN_KEY);
      let d: DossierResponse;
      if (list.length > 0) {
        d = list[0];
      } else {
        d = await createDossier({ ...SHENZHEN_KEY, expectedScale: 'PHYSICS_750_T1' });
      }
      setDossier(d);
      setDossierIdInput(d.dossierId);
      await loadHistory(SHENZHEN_KEY);
      setState('ready');
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setState('error');
    }
  }, [loadHistory]);

  const handleSeal = useCallback(async () => {
    if (!dossier || !sealAsOf) return;
    setSealing(true);
    setPartialError(null);
    try {
      const d = await sealDossier(dossier.dossierId, new Date(sealAsOf).toISOString());
      setDossier(d);
      const key: FactKey = {
        provinceCode: d.provinceCode,
        admissionYear: d.admissionYear,
        subjectCategory: d.subjectCategory,
        schoolCode: d.schoolCode,
        majorGroupCode: d.majorGroupCode,
      };
      await loadHistory(key);
    } catch (e) {
      setPartialError(e instanceof Error ? e.message : String(e));
    } finally {
      setSealing(false);
    }
  }, [dossier, sealAsOf, loadHistory]);

  const selectedSource = activeSources.find((s) => s.sourceId === selectedSourceId) ?? null;

  return (
    <div className="app">
      <header className="app-header">
        <h1>投档来源卷宗核验工作台</h1>
        <div className="dossier-loader">
          <label htmlFor="dossier-id-input" className="sr-only">卷宗 ID</label>
          <input
            id="dossier-id-input"
            type="text"
            placeholder="输入卷宗 ID 加载"
            value={dossierIdInput}
            onChange={(e) => setDossierIdInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') loadDossier(dossierIdInput);
            }}
            aria-label="输入卷宗 ID"
          />
          <button onClick={() => loadDossier(dossierIdInput)} disabled={state === 'loading'}>
            加载
          </button>
          <button className="primary" onClick={loadShenzhen} disabled={state === 'loading'}>
            深圳职业技术大学示例
          </button>
        </div>
      </header>

      {state === 'loading' && (
        <div className="panel">
          <div className="state-box" role="status" aria-live="polite">正在加载卷宗…</div>
        </div>
      )}

      {state === 'idle' && (
        <div className="panel">
          <div className="state-box">
            输入卷宗 ID 并加载，或点击「深圳职业技术大学示例」查看核验场景。
          </div>
        </div>
      )}

      {state === 'error' && !dossier && (
        <div className="panel">
          <div className="state-box error" role="alert">
            加载失败：{error}
          </div>
        </div>
      )}

      {state === 'error' && dossier && (
        <>
          <div className="state-box error" role="alert">
            部分数据加载失败：{error}（已保留已加载的证据链）
          </div>
          {renderWorkbench()}
        </>
      )}

      {state === 'ready' && dossier && renderWorkbench()}
    </div>
  );

  function renderWorkbench() {
    if (!dossier) return null;
    return (
      <>
        {dossier.expired && (
          <div className="expired-banner" role="alert">
            此卷宗已过期：封存后有新的来源修订到达，当前结论可能已变化。
          </div>
        )}

        {partialError && (
          <div className="state-box error" role="alert" style={{ marginBottom: 8 }}>
            {partialError}
          </div>
        )}

        <div className="workbench">
          <section className="panel" aria-label="来源队列">
            <div className="panel-header">
              <span>来源队列（{activeSources.length}）</span>
              {dossier.sealed && <span className="badge">已封存</span>}
            </div>
            <div className="panel-body">
              {activeSources.length === 0 && gaps.length === 0 ? (
                <div className="state-box">该卷宗暂无来源记录</div>
              ) : (
                <SourceQueue
                  sources={activeSources}
                  selectedId={selectedSourceId}
                  onSelect={setSelectedSourceId}
                  gaps={gaps}
                />
              )}
            </div>
          </section>

          <section className="panel" aria-label="当前事实">
            <div className="panel-header">
              <span>当前事实</span>
              {!dossier.sealed && (
                <div className="seal-bar">
                  <label htmlFor="seal-asof" className="sr-only">封存时间</label>
                  <input
                    id="seal-asof"
                    type="datetime-local"
                    value={sealAsOf}
                    onChange={(e) => setSealAsOf(e.target.value)}
                    aria-label="封存截止时间"
                  />
                  <button onClick={handleSeal} disabled={sealing || !sealAsOf}>
                    {sealing ? '封存中…' : '按 as_of 封存'}
                  </button>
                </div>
              )}
            </div>
            <div className="panel-body">
              <CurrentFact dossier={dossier} />
            </div>
          </section>

          <section className="panel" aria-label="版本差异" style={{ gridColumn: '1 / -1' }}>
            <div className="panel-header">
              <span>版本差异</span>
              <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                {history.length} 条修订记录
              </span>
            </div>
            <div className="panel-body">
              <VersionDiff history={history} />
            </div>
          </section>
        </div>

        <SourceDrawer source={selectedSource} onClose={() => setSelectedSourceId(null)} />
      </>
    );
  }
}
