import type { DossierStatus } from '../api/types';
import { statusLabel } from '../format';

export function StatusBadge({ status }: { status: DossierStatus }) {
  return <span className={`badge status-${status}`}>{statusLabel(status)}</span>;
}
