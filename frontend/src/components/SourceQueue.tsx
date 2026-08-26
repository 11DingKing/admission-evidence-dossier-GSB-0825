import { useEffect, useRef } from 'react';
import type { SourceRecordResponse } from '../types';
import { FreshnessBadge } from './StatusBadge';

interface SourceQueueProps {
  sources: SourceRecordResponse[];
  selectedId: string | null;
  onSelect: (sourceId: string) => void;
  gaps?: Array<{ sourceId: string; message: string }>;
}

export function SourceQueue({ sources, selectedId, onSelect, gaps = [] }: SourceQueueProps) {
  const listRef = useRef<HTMLUListElement>(null);
  const itemRefs = useRef<Map<string, HTMLLIElement>>(new Map());

  const orderedIds = sources.map((s) => s.sourceId);

  useEffect(() => {
    if (selectedId) {
      itemRefs.current.get(selectedId)?.focus();
    }
  }, [selectedId]);

  function handleKeyDown(e: React.KeyboardEvent, sourceId: string) {
    const idx = orderedIds.indexOf(sourceId);
    if (e.key === 'ArrowDown' || e.key === 'ArrowRight') {
      e.preventDefault();
      if (idx < orderedIds.length - 1) {
        onSelect(orderedIds[idx + 1]);
      }
    } else if (e.key === 'ArrowUp' || e.key === 'ArrowLeft') {
      e.preventDefault();
      if (idx > 0) {
        onSelect(orderedIds[idx - 1]);
      }
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      onSelect(sourceId);
    }
  }

  return (
    <ul
      ref={listRef}
      className="source-list"
      role="listbox"
      aria-label="来源队列"
    >
      {sources.map((source) => (
        <li
          key={source.sourceId}
          ref={(el) => {
            if (el) itemRefs.current.set(source.sourceId, el);
            else itemRefs.current.delete(source.sourceId);
          }}
          className={`source-item${selectedId === source.sourceId ? ' selected' : ''}`}
          role="option"
          tabIndex={selectedId === source.sourceId ? 0 : -1}
          aria-selected={selectedId === source.sourceId}
          aria-current={selectedId === source.sourceId ? 'true' : undefined}
          onClick={() => onSelect(source.sourceId)}
          onKeyDown={(e) => handleKeyDown(e, source.sourceId)}
        >
          <span className="source-id">{source.sourceId}</span>
          <span className="source-score" aria-label={`最高分 ${(source.scoreValue / 10).toFixed(1)}`}>
            {(source.scoreValue / 10).toFixed(1)}
          </span>
          <span className="source-meta">
            <span className="rev-tag" aria-label={`修订版本 ${source.sourceRevision}`}>
              r{source.sourceRevision}
            </span>
            <FreshnessBadge freshness={source.freshness} />
          </span>
        </li>
      ))}
      {gaps.map((gap) => (
        <li key={gap.sourceId} className="gap-row" role="status">
          缺口：{gap.sourceId} — {gap.message}
        </li>
      ))}
    </ul>
  );
}
