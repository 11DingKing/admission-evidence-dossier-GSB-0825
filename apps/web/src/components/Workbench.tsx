import type { ScoreScale } from '../api/types';
import { formatDateTime, statusLabel } from '../format';
import { useViewport } from '../hooks/useViewport';
import { DOSSIERS, useWorkbench } from '../hooks/useWorkbench';
import { CurrentFact } from './CurrentFact';
import { SealBanner } from './SealBanner';
import { SourceDrawer } from './SourceDrawer';
import { SourceQueue } from './SourceQueue';
import { EmptyState, LoadErrorAlert, LoadingPanel, PartialFailureAlert } from './states';

export function Workbench() {
  const wb = useWorkbench();
  const viewport = useViewport();
  const sealMode = wb.seal !== null;

  const viewSources = wb.seal
    ? wb.seal.snapshot.sources
    : wb.detail?.sources ?? [];
  const viewStatus = wb.seal ? wb.seal.snapshot.status : wb.detail?.status;
  const viewFactKey = wb.seal ? wb.seal.factKey : wb.detail?.factKey;
  const expectedScale: ScoreScale =
    wb.seal || !wb.detail
      ? viewFactKey?.subjectCategory === 'ART_COMPOSITE'
        ? 'ART_COMPOSITE_TENTH'
        : 'GAOKAO_750_TENTH'
      : wb.detail.expectedScale;

  const seals = wb.detail?.seals ?? [];
  const failedCount =
    wb.report?.results.filter((r) => r.outcome === 'ERROR').length ?? 0;

  const drawerSource = viewSources.find(
    (s) => s.sourceId === wb.drawerSourceId,
  ) ?? null;

  return (
    <div className={`workbench viewport-${viewport}`}>
      <header className="topbar">
        <h1>投档来源卷宗核验</h1>
        <div className="controls">
          <label className="ctrl">
            <span className="ctrl-label">卷宗</span>
            <select
              aria-label="选择卷宗"
              value={wb.dossierId}
              disabled={sealMode}
              onChange={(e) => wb.setDossierId(e.target.value)}
            >
              {DOSSIERS.map((d) => (
                <option key={d.dossierId} value={d.dossierId}>
                  {d.name}
                </option>
              ))}
            </select>
          </label>
          <label className="ctrl">
            <span className="ctrl-label">as_of（留空为当前）</span>
            <input
              type="date"
              aria-label="as_of 日期"
              value={wb.asOfInput}
              disabled={sealMode}
              onChange={(e) => wb.setAsOfInput(e.target.value)}
            />
          </label>
          {!sealMode && (
            <>
              <button
                type="button"
                className="btn-primary"
                disabled={wb.verifying || wb.loading}
                onClick={() => void wb.verify()}
              >
                {wb.verifying ? '核验中…' : '核验来源'}
              </button>
              <button
                type="button"
                disabled={wb.sealing || wb.loading}
                onClick={() => void wb.sealDossier()}
              >
                {wb.sealing ? '封存中…' : '封存卷宗'}
              </button>
            </>
          )}
          {sealMode && (
            <button type="button" className="btn-primary" onClick={wb.backToCurrent}>
              返回当前视图
            </button>
          )}
        </div>
      </header>

      <section className="seal-list" aria-label="封存记录">
        <h2>封存记录</h2>
        {seals.length === 0 ? (
          <span className="muted">暂无封存记录</span>
        ) : (
          <ul>
            {seals.map((s) => (
              <li key={s.sealId}>
                <button
                  type="button"
                  aria-pressed={wb.seal?.sealId === s.sealId}
                  onClick={() => void wb.viewSeal(s.sealId)}
                >
                  封存于 {formatDateTime(s.sealedAt)} · {statusLabel(s.status)}
                  {s.hasNewerEvidence ? ' · 之后有新证据' : ''}
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      {sealMode && wb.seal && <SealBanner seal={wb.seal} />}

      {wb.loadError && <LoadErrorAlert message={wb.loadError} />}
      {!sealMode && failedCount > 0 && (
        <PartialFailureAlert count={failedCount} />
      )}

      {wb.loading ? (
        <LoadingPanel />
      ) : wb.loadError && !wb.detail ? null : (
        <main className="panels">
          <SourceQueue
            sources={viewSources}
            busy={wb.verifying}
            onOpenSource={(id, trigger) => void wb.openDrawer(id, trigger)}
          />
          {viewStatus === 'NO_EVIDENCE' || !viewFactKey ? (
            <EmptyState />
          ) : (
            <CurrentFact
              factKey={viewFactKey}
              expectedScale={expectedScale}
              status={viewStatus ?? 'NO_EVIDENCE'}
              sources={viewSources}
            />
          )}
        </main>
      )}

      <SourceDrawer
        open={wb.drawerSourceId !== null}
        source={drawerSource}
        chain={wb.chain}
        loading={wb.chainLoading}
        onClose={wb.closeDrawer}
      />
    </div>
  );
}
