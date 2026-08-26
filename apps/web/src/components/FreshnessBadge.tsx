import type { Freshness } from '../api/types';
import { freshnessLabel } from '../format';

export function FreshnessBadge({ value }: { value: Freshness }) {
  return <span className={`badge fresh-${value}`}>{freshnessLabel(value)}</span>;
}
