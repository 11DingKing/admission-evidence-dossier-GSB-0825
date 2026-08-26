import type { LoadState, SourceState } from '../api/types';
import { scaleLabel } from '../format';
import { FreshnessBadge } from './FreshnessBadge';

interface SourceQueueProps {
  sources: SourceState[];
  busy: boolean;
  onOpenSource: (sourceId: string, trigger: HTMLButtonElement) => void;
}

function loadStateText(loadState: LoadState): string {
  switch (loadState) {
    case 'OK':
      return '✓ 已核验';
    case 'ERROR':
      return '✗ 失败-缺口';
    case 'MISSING':
      return '○ 未抓取';
  }
}

export function SourceQueue({ sources, busy, onOpenSource }: SourceQueueProps) {
  return (
    <section className="source-queue" aria-label="来源队列" aria-busy={busy}>
      <h2>来源队列</h2>
      <div className="src-row src-head" role="row">
        <span>来源名称</span>
        <span>当前值</span>
        <span>修订数</span>
        <span>有效期</span>
        <span>抓取状态</span>
        <span>操作</span>
      </div>
      <ul className="src-list">
        {sources.map((s) => (
          <li className="src-row" key={s.sourceId}>
            <span className="cell-name">{s.sourceName}</span>
            <span className="cell-value">
              {s.current ? (
                <>
                  {s.current.scoreDisplay}（{scaleLabel(s.current.scoreScale)}）
                  {s.loadState === 'ERROR' && (
                    <em className="retained-tag">已保留</em>
                  )}
                </>
              ) : (
                '—'
              )}
            </span>
            <span className="cell-rev">{s.revisionCount}</span>
            <span className="cell-fresh">
              {s.current ? <FreshnessBadge value={s.current.freshness} /> : '—'}
            </span>
            <span className={`cell-load load-${s.loadState}`}>
              {loadStateText(s.loadState)}
            </span>
            <span className="cell-act">
              <button
                type="button"
                aria-label={`查看 ${s.sourceName} 原文`}
                disabled={!s.current}
                onClick={(e) => onOpenSource(s.sourceId, e.currentTarget)}
              >
                原文
              </button>
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
