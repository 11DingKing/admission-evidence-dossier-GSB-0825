import { useEffect, useRef } from 'react';
import type { RevisionChain, SourceState } from '../api/types';
import { formatDate } from '../format';
import { VersionDiff } from './VersionDiff';

interface SourceDrawerProps {
  open: boolean;
  source: SourceState | null;
  chain: RevisionChain | null;
  loading: boolean;
  onClose: () => void;
}

export function SourceDrawer({
  open,
  source,
  chain,
  loading,
  onClose,
}: SourceDrawerProps) {
  const closeButtonRef = useRef<HTMLButtonElement | null>(null);

  useEffect(() => {
    if (!open) return;
    closeButtonRef.current?.focus();
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.stopPropagation();
        onClose();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [open, onClose]);

  if (!open || !source) return null;

  const current = source.current;
  const revisions = chain
    ? [...chain.revisions].sort((a, b) => a.sourceRevision - b.sourceRevision)
    : [];

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <div
        className="drawer"
        role="dialog"
        aria-modal="true"
        aria-label={`${source.sourceName} 来源原文`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="drawer-head">
          <h2>{source.sourceName} · 来源原文</h2>
          <button
            type="button"
            ref={closeButtonRef}
            aria-label="关闭"
            onClick={onClose}
          >
            关闭
          </button>
        </div>

        {current ? (
          <dl className="drawer-meta">
            <dt>来源文号</dt>
            <dd>{current.sourceDocNo}</dd>
            <dt>内容哈希</dt>
            <dd className="hash">{current.contentHash}</dd>
            <dt>有效期</dt>
            <dd>
              {formatDate(current.effectiveFrom)} 至{' '}
              {current.effectiveTo ? formatDate(current.effectiveTo) : '长期有效'}
            </dd>
            <dt>当前修订</dt>
            <dd>
              rev{current.sourceRevision} · {current.scoreDisplay}
            </dd>
          </dl>
        ) : (
          <p>该来源尚未抓取到记录。</p>
        )}

        {current && (
          <div className="raw-text">
            <h3>原文</h3>
            <pre>{current.rawText}</pre>
          </div>
        )}

        <div className="revision-list">
          <h3>该来源修订记录</h3>
          {loading && <p aria-busy="true">正在加载修订记录…</p>}
          {!loading && chain && (
            <ul>
              {revisions.map((r) => (
                <li key={r.sourceRevision} className={r.superseded ? 'rev-superseded' : ''}>
                  rev{r.sourceRevision} · {r.scoreDisplay} · {r.sourceDocNo} ·{' '}
                  {formatDate(r.recordedAt)}
                  {r.superseded && <span className="tag-superseded">已被取代</span>}
                </li>
              ))}
            </ul>
          )}
          {!loading && chain && <VersionDiff revisions={revisions} />}
        </div>
      </div>
    </div>
  );
}
