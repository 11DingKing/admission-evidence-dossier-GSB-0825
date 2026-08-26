export function LoadingPanel() {
  return (
    <div className="loading-panel" aria-busy="true" aria-live="polite">
      <div className="skeleton-line skeleton-wide" />
      <div className="skeleton-line" />
      <div className="skeleton-line skeleton-narrow" />
      <p className="loading-text">正在加载卷宗视图…</p>
    </div>
  );
}

export function EmptyState() {
  return (
    <section className="empty-state" aria-label="空状态">
      <h2>尚未抓取任何来源</h2>
      <p>
        当前卷宗还没有任何来源证据。点击顶栏「核验来源」按钮抓取绑定来源；
        抓取成功后此处将显示核验事实与一致分。
      </p>
    </section>
  );
}

export function PartialFailureAlert({ count }: { count: number }) {
  return (
    <div className="alert-partial" role="alert">
      {count} 个来源抓取失败，已保留其他来源证据，失败来源在队列中标记为缺口。
    </div>
  );
}

export function LoadErrorAlert({ message }: { message: string }) {
  return <div className="alert-error" role="alert">{message}</div>;
}
