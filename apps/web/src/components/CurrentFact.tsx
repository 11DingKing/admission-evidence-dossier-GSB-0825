import type {
  DossierStatus,
  FactKey,
  ScoreScale,
  SourceState,
} from '../api/types';
import {
  provinceLabel,
  scaleLabel,
  schoolLabel,
  subjectLabel,
} from '../format';
import { StatusBadge } from './StatusBadge';

interface CurrentFactProps {
  factKey: FactKey;
  expectedScale: ScoreScale;
  status: DossierStatus;
  sources: SourceState[];
}

function agreedScore(sources: SourceState[]): string | null {
  const currents = sources
    .map((s) => s.current)
    .filter((r): r is NonNullable<typeof r> => r !== null);
  if (currents.length === 0) return null;
  const first = currents[0];
  const allAgree = currents.every(
    (r) =>
      r.scoreTenths === first.scoreTenths && r.scoreScale === first.scoreScale,
  );
  return allAgree ? first.scoreDisplay : null;
}

export function CurrentFact({
  factKey,
  expectedScale,
  status,
  sources,
}: CurrentFactProps) {
  const agreed = agreedScore(sources);
  return (
    <section className="current-fact" aria-label="当前事实">
      <h2>当前事实</h2>
      <dl className="fact-grid">
        <dt>省份</dt>
        <dd>{provinceLabel(factKey.provinceCode)}</dd>
        <dt>年份</dt>
        <dd>{factKey.admissionYear}</dd>
        <dt>科类</dt>
        <dd>{subjectLabel(factKey.subjectCategory)}</dd>
        <dt>院校</dt>
        <dd>{schoolLabel(factKey.schoolCode)}</dd>
        <dt>专业组</dt>
        <dd>{factKey.majorGroupCode}</dd>
        <dt>期望量表</dt>
        <dd>{scaleLabel(expectedScale)}</dd>
        <dt>状态</dt>
        <dd>
          <StatusBadge status={status} />
        </dd>
        <dt>一致分</dt>
        <dd className="agreed-score">{agreed ?? '—'}</dd>
      </dl>
    </section>
  );
}
