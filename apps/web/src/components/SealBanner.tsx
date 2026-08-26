import type { SealView } from '../api/types';
import { formatDateTime } from '../format';

export function SealBanner({ seal }: { seal: SealView }) {
  return (
    <div className="seal-banner-wrap">
      <div className="seal-info">
        <span className="badge seal-readonly">只读快照</span>
        <span>封存时刻：{formatDateTime(seal.asOf)}</span>
        <span>封存操作：{formatDateTime(seal.sealedAt)}</span>
        <span className="hash">快照哈希：{seal.contentHash}</span>
      </div>
      {seal.hasNewerEvidence && (
        <div className="seal-banner" role="status">
          该封存卷宗之后已出现新证据，快照内容保持不变
        </div>
      )}
    </div>
  );
}
