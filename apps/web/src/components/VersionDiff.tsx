import type { SourceRecordView } from '../api/types';
import { formatDelta } from '../format';

interface VersionDiffProps {
  revisions: SourceRecordView[];
}

export function VersionDiff({ revisions }: VersionDiffProps) {
  const sorted = [...revisions].sort(
    (a, b) => a.sourceRevision - b.sourceRevision,
  );
  if (sorted.length < 2) return null;

  return (
    <div className="version-diff">
      <h3>版本差异</h3>
      <ol>
        {sorted.slice(1).map((rev, i) => {
          const prev = sorted[i];
          const delta = rev.scoreTenths - prev.scoreTenths;
          return (
            <li key={rev.sourceRevision}>
              rev{prev.sourceRevision} {prev.scoreDisplay} → rev
              {rev.sourceRevision} {rev.scoreDisplay}{' '}
              <span className={`delta ${delta < 0 ? 'delta-down' : 'delta-up'}`}>
                Δ{formatDelta(delta)}
              </span>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
