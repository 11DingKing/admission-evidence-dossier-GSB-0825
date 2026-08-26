import { useEffect, useRef } from 'react';
import type { SourceRecordResponse, DossierEntryResponse } from '../types';
import { FreshnessBadge } from './StatusBadge';

interface SourceDrawerProps {
  source: SourceRecordResponse | DossierEntryResponse | null;
  onClose: () => void;
}

export function SourceDrawer({ source, onClose }: SourceDrawerProps) {
  const closeRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (source) {
      closeRef.current?.focus();
    }
  }, [source]);

  useEffect(() => {
    if (!source) return;
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [source, onClose]);

  if (!source) return null;

  return (
    <div
      className="drawer-overlay"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label={`来源 ${source.sourceId} 原文`}
    >
      <div className="drawer" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-header">
          <h2>来源原文 — {source.sourceId}</h2>
          <button ref={closeRef} onClick={onClose} aria-label="关闭抽屉">
            关闭
          </button>
        </div>
        <div className="drawer-body">
          <dl>
            <dt>来源 ID</dt>
            <dd>{source.sourceId}</dd>
            <dt>修订版本</dt>
            <dd>r{source.sourceRevision}</dd>
            <dt>最高分</dt>
            <dd>{(source.scoreValue / 10).toFixed(1)}</dd>
            <dt>分数口径</dt>
            <dd>{source.scoreScale}</dd>
            <dt>原始分数文本</dt>
            <dd>{source.originalScoreText ?? '—'}</dd>
            <dt>生效时间</dt>
            <dd>{new Date(source.effectiveFrom).toLocaleString('zh-CN')}</dd>
            <dt>失效时间</dt>
            <dd>{source.effectiveTo ? new Date(source.effectiveTo).toLocaleString('zh-CN') : '—'}</dd>
            <dt>记录时间</dt>
            <dd>{new Date(source.recordedAt).toLocaleString('zh-CN')}</dd>
            <dt>来源文号</dt>
            <dd>{source.sourceDocumentNo ?? '—'}</dd>
            <dt>内容哈希</dt>
            <dd style={{ fontSize: 11 }}>{source.contentHash.slice(0, 24)}…</dd>
            <dt>新鲜度</dt>
            <dd><FreshnessBadge freshness={source.freshness} /></dd>
          </dl>
          <div style={{ fontWeight: 600, marginBottom: 6, fontSize: 13 }}>原文内容</div>
          <div className="original-text" role="article" aria-label="来源原文内容">
            {source.originalText ?? '（无原文）'}
          </div>
        </div>
      </div>
    </div>
  );
}
