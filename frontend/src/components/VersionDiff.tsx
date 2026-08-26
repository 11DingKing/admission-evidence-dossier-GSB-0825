import type { SourceRecordResponse } from '../types';

interface VersionDiffProps {
  history: SourceRecordResponse[];
}

interface RevisionGroup {
  sourceId: string;
  revisions: SourceRecordResponse[];
}

export function VersionDiff({ history }: VersionDiffProps) {
  const groups = new Map<string, SourceRecordResponse[]>();
  for (const s of history) {
    const arr = groups.get(s.sourceId) ?? [];
    arr.push(s);
    groups.set(s.sourceId, arr);
  }

  const entries: RevisionGroup[] = [];
  for (const [sourceId, revisions] of groups) {
    revisions.sort((a, b) => a.sourceRevision - b.sourceRevision);
    entries.push({ sourceId, revisions });
  }

  if (entries.length === 0) {
    return <div className="state-box">暂无版本记录</div>;
  }

  return (
    <div className="diff-list" aria-label="版本差异">
      {entries.map((group) => {
        const latest = group.revisions[group.revisions.length - 1];
        return (
          <div className="diff-entry" key={group.sourceId}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
              <strong>{group.sourceId}</strong>
              <span className="rev-tag">当前 r{latest.sourceRevision}</span>
            </div>
            {group.revisions.length === 1 ? (
              <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                仅一个版本，值 {(latest.scoreValue / 10).toFixed(1)}
              </div>
            ) : (
              group.revisions.slice(0, -1).map((rev, idx) => {
                const next = group.revisions[idx + 1];
                const changed = rev.scoreValue !== next.scoreValue;
                return (
                  <div key={rev.sourceRevision} className="rev-row" style={{ fontSize: 12 }}>
                    <span>
                      r{rev.sourceRevision}
                      {changed ? (
                        <>
                          {' '}<span className="old-val">{(rev.scoreValue / 10).toFixed(1)}</span>
                          {' → '}<span className="new-val">{(next.scoreValue / 10).toFixed(1)}</span>
                        </>
                      ) : (
                        <> ：{(rev.scoreValue / 10).toFixed(1)}（无变化）</>
                      )}
                    </span>
                  </div>
                );
              })
            )}
          </div>
        );
      })}
    </div>
  );
}
