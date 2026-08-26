import type { DossierResponse } from '../types';
import { StatusBadge } from './StatusBadge';

interface CurrentFactProps {
  dossier: DossierResponse;
}

export function CurrentFact({ dossier }: CurrentFactProps) {
  const activeEntries = dossier.sealed ? dossier.sealedEntries : dossier.sources;
  const scores = activeEntries.map((e) => e.scoreValue);
  const uniqueScores = [...new Set(scores)];
  const agreedValue = uniqueScores.length === 1 ? uniqueScores[0] : null;
  const expectedScale = dossier.expectedScale;

  return (
    <div>
      <dl className="fact-grid" aria-label="事实标识">
        <dt>省份</dt>
        <dd>{dossier.provinceCode}</dd>
        <dt>年份</dt>
        <dd>{dossier.admissionYear}</dd>
        <dt>科类</dt>
        <dd>{dossier.subjectCategory}</dd>
        <dt>院校</dt>
        <dd>{dossier.schoolCode}</dd>
        <dt>专业组</dt>
        <dd>{dossier.majorGroupCode}</dd>
        <dt>期望量表</dt>
        <dd>{expectedScale}</dd>
        <dt>卷宗号</dt>
        <dd>{dossier.dossierId.slice(0, 8)}</dd>
      </dl>

      <div className="current-value" aria-live="polite">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
          <span style={{ color: 'var(--text-muted)', fontSize: 12 }}>当前核验结论</span>
          <StatusBadge status={dossier.status} />
        </div>
        {agreedValue != null ? (
          <div className="score" aria-label={`一致最高分 ${(agreedValue / 10).toFixed(1)}`}>
            {(agreedValue / 10).toFixed(1)}
          </div>
        ) : uniqueScores.length > 1 ? (
          <div>
            <div style={{ fontSize: 12, color: 'var(--err)', marginBottom: 4 }}>数值不一致：</div>
            <div style={{ fontFamily: 'ui-monospace, monospace', fontWeight: 600 }}>
              {uniqueScores.map((s) => (s / 10).toFixed(1)).join(' / ')}
            </div>
          </div>
        ) : (
          <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>暂无来源数据</div>
        )}
      </div>
    </div>
  );
}
