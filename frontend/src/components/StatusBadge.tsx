import type { DossierStatus, Freshness } from '../types';

const STATUS_LABELS: Record<DossierStatus, string> = {
  PENDING: '待核验',
  CONSISTENT: '一致',
  CONFLICT: '冲突',
  SCALE_CONFLICT: '量表冲突',
};

const FRESHNESS_LABELS: Record<Freshness, string> = {
  FRESH: '新鲜',
  STALE: '陈旧',
  EXPIRED: '过期',
};

export function StatusBadge({ status }: { status: DossierStatus }) {
  return (
    <span className={`badge badge-${status}`} role="status" aria-label={`卷宗状态：${STATUS_LABELS[status]}`}>
      {STATUS_LABELS[status]}
    </span>
  );
}

export function FreshnessBadge({ freshness }: { freshness: Freshness }) {
  return (
    <span className={`badge badge-${freshness}`} aria-label={`数据新鲜度：${FRESHNESS_LABELS[freshness]}`}>
      {FRESHNESS_LABELS[freshness]}
    </span>
  );
}
