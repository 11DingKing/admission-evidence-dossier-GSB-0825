import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type {
  DossierDetail,
  FetchReport,
  RevisionChain,
  SealView,
  SourceRecordView,
  SourceState,
} from '../api/types';
import { Workbench } from './Workbench';

vi.mock('../api/client', () => ({
  getDossier: vi.fn(),
  fetchSources: vi.fn(),
  createSeal: vi.fn(),
  getSeal: vi.fn(),
  getRevisions: vi.fn(),
}));

import * as client from '../api/client';

const SZ_DOSSIER = '11111111-1111-4111-8111-111111111111';
const GD = 'GD_EDU_EXAM';
const SZPU = 'SZPU_ADMISSION';
const CHSI = 'CHSI_GGKG';

const GD_REV1: SourceRecordView = {
  sourceId: GD,
  sourceName: '广东省教育考试院',
  sourceRevision: 1,
  scoreTenths: 6100,
  scoreDisplay: '610.0',
  scoreScale: 'GAOKAO_750_TENTH',
  sourceDocNo: '粤招办〔2025〕38号',
  effectiveFrom: '2025-07-15',
  effectiveTo: null,
  recordedAt: '2025-07-16T09:00:00Z',
  freshness: 'FRESH',
  contentHash: 'sha256:gd-rev1-hash',
  rawText:
    '广东省2025年普通高校招生本科批次投档情况公告（第1号）：深圳职业技术大学物理类专业组203投档最高分610.0分。',
  origin: 'FETCH',
  superseded: false,
};

const GD_REV2: SourceRecordView = {
  ...GD_REV1,
  sourceRevision: 2,
  scoreTenths: 6090,
  scoreDisplay: '609.0',
  sourceDocNo: '粤招办〔2025〕42号',
  effectiveFrom: '2025-07-24',
  recordedAt: '2025-07-25T09:00:00Z',
  contentHash: 'sha256:gd-rev2-hash',
  rawText:
    '广东省2025年普通高校招生本科批次投档情况公告（更正）：深圳职业技术大学物理类专业组203投档最高分更正为609.0分。',
};

const SZPU_REC: SourceRecordView = {
  sourceId: SZPU,
  sourceName: '深圳职业技术大学本科招生网',
  sourceRevision: 1,
  scoreTenths: 6090,
  scoreDisplay: '609.0',
  scoreScale: 'GAOKAO_750_TENTH',
  sourceDocNo: '深职大招〔2025〕17号',
  effectiveFrom: '2025-07-18',
  effectiveTo: null,
  recordedAt: '2025-07-22T03:00:00Z',
  freshness: 'FRESH',
  contentHash: 'sha256:szpu-hash',
  rawText:
    '深圳职业技术大学2025年广东省本科批投档分数线公告：物理类专业组203投档最高分609.0分。',
  origin: 'FETCH',
  superseded: false,
};

function state(
  sourceId: string,
  sourceName: string,
  over: Partial<SourceState> = {},
): SourceState {
  return {
    sourceId,
    sourceName,
    loadState: 'MISSING',
    errorCode: null,
    errorMessage: null,
    current: null,
    revisionCount: 0,
    ...over,
  };
}

const GD_CONSISTENT = state(GD, '广东省教育考试院', {
  loadState: 'OK',
  current: GD_REV2,
  revisionCount: 2,
});
const SZPU_OK = state(SZPU, '深圳职业技术大学本科招生网', {
  loadState: 'OK',
  current: SZPU_REC,
  revisionCount: 1,
});
const CHSI_MISSING = state(CHSI, '阳光高考信息平台');
const CHSI_ERROR = state(CHSI, '阳光高考信息平台', {
  loadState: 'ERROR',
  errorCode: 'FACT_KEY_MISMATCH',
  errorMessage: '发布事实与卷宗 fact_key 不一致',
});

const GD_CONFLICT = state(GD, '广东省教育考试院', {
  loadState: 'OK',
  current: GD_REV1,
  revisionCount: 1,
});

function makeDetail(over: Partial<DossierDetail> = {}): DossierDetail {
  return {
    dossierId: SZ_DOSSIER,
    factKey: {
      provinceCode: '44',
      admissionYear: 2025,
      subjectCategory: 'PHYSICS',
      schoolCode: '11113',
      majorGroupCode: '203',
    },
    factKeyHash: 'sha256:fact-shenzhen',
    expectedScale: 'GAOKAO_750_TENTH',
    status: 'CONSISTENT',
    asOf: '2025-08-01T00:00:00Z',
    sources: [GD_CONSISTENT, SZPU_OK, CHSI_MISSING],
    evidenceChain: [],
    seals: [],
    ...over,
  };
}

const GD_CHAIN: RevisionChain = {
  sourceId: GD,
  revisions: [
    { ...GD_REV1, superseded: true, deltaTenths: null },
    { ...GD_REV2, deltaTenths: -10 },
  ],
};

async function tabTo(user: ReturnType<typeof userEvent.setup>, el: HTMLElement) {
  for (let i = 0; i < 25; i += 1) {
    if (document.activeElement === el) return;
    await user.tab();
  }
  throw new Error('tab 未能到达目标控件');
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('卷宗核验工作台', () => {
  it('键盘流程：Tab+Enter 核验来源 → Tab+Enter 打开原文抽屉 → Esc 关闭并归还焦点', async () => {
    vi.mocked(client.getDossier).mockResolvedValue(makeDetail());
    vi.mocked(client.fetchSources).mockResolvedValue({
      dossierId: SZ_DOSSIER,
      asOf: '2025-08-01T00:00:00Z',
      dossierStatus: 'CONSISTENT',
      results: [
        { sourceId: GD, outcome: 'LOADED', record: GD_REV2, errorCode: null, errorMessage: null },
        { sourceId: SZPU, outcome: 'LOADED', record: SZPU_REC, errorCode: null, errorMessage: null },
      ],
    } satisfies FetchReport);
    vi.mocked(client.getRevisions).mockResolvedValue(GD_CHAIN);

    const user = userEvent.setup();
    render(<Workbench />);

    const verifyBtn = await screen.findByRole('button', { name: '核验来源' });

    await tabTo(user, verifyBtn);
    await user.keyboard('{Enter}');
    await waitFor(() => expect(client.fetchSources).toHaveBeenCalledTimes(1));

    const sourceBtn = screen.getByRole('button', {
      name: '查看 广东省教育考试院 原文',
    });
    await tabTo(user, sourceBtn);
    await user.keyboard('{Enter}');

    const dialog = await screen.findByRole('dialog');
    expect(
      within(dialog).getByText('粤招办〔2025〕42号'),
    ).toBeInTheDocument();
    expect(
      within(dialog).getByText('sha256:gd-rev2-hash'),
    ).toBeInTheDocument();

    await user.keyboard('{Escape}');
    await waitFor(() =>
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument(),
    );
    expect(sourceBtn).toHaveFocus();
  });

  it('原文抽屉展示文号、contentHash、rawText 与版本差异', async () => {
    vi.mocked(client.getDossier).mockResolvedValue(makeDetail());
    vi.mocked(client.getRevisions).mockResolvedValue(GD_CHAIN);

    const user = userEvent.setup();
    render(<Workbench />);

    await user.click(
      await screen.findByRole('button', {
        name: '查看 广东省教育考试院 原文',
      }),
    );

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('粤招办〔2025〕42号')).toBeInTheDocument();
    expect(within(dialog).getByText('sha256:gd-rev2-hash')).toBeInTheDocument();
    expect(
      within(dialog).getByText(/投档情况公告（更正）/),
    ).toBeInTheDocument();
    expect(within(dialog).getByText('已被取代')).toBeInTheDocument();
    expect(within(dialog).getByText(/Δ-1\.0/)).toBeInTheDocument();
  });

  it('部分失败：单个来源 FACT_KEY_MISMATCH 时保留已有证据、标记缺口并出现 alert', async () => {
    const initial = makeDetail();
    const afterError = makeDetail({
      sources: [GD_CONSISTENT, SZPU_OK, CHSI_ERROR],
    });
    vi.mocked(client.getDossier)
      .mockResolvedValueOnce(initial)
      .mockResolvedValue(afterError);
    vi.mocked(client.fetchSources).mockResolvedValue({
      dossierId: SZ_DOSSIER,
      asOf: '2025-08-01T00:00:00Z',
      dossierStatus: 'CONSISTENT',
      results: [
        { sourceId: GD, outcome: 'SUPERSEDED', record: GD_REV2, errorCode: null, errorMessage: null },
        { sourceId: SZPU, outcome: 'SUPERSEDED', record: SZPU_REC, errorCode: null, errorMessage: null },
        { sourceId: CHSI, outcome: 'ERROR', record: null, errorCode: 'FACT_KEY_MISMATCH', errorMessage: '发布事实与卷宗 fact_key 不一致' },
      ],
    });

    const user = userEvent.setup();
    render(<Workbench />);

    expect(await screen.findByText('广东省教育考试院')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '核验来源' }));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('1 个来源抓取失败');
    expect(alert).toHaveTextContent('已保留');

    expect(screen.getByText('广东省教育考试院')).toBeInTheDocument();
    expect(screen.getByText('深圳职业技术大学本科招生网')).toBeInTheDocument();
    expect(screen.getByText('阳光高考信息平台')).toBeInTheDocument();

    expect(
      screen.getAllByText('609.0（普通类750分制）'),
    ).toHaveLength(2);
    expect(screen.getByText(/失败-缺口/)).toBeInTheDocument();
  });

  it('状态切换：CONFLICT 后再次核验变为 CONSISTENT（rev1 610.0 → rev2 609.0）', async () => {
    const conflict = makeDetail({
      status: 'CONFLICT',
      sources: [GD_CONFLICT, SZPU_OK, CHSI_MISSING],
    });
    const consistent = makeDetail();
    vi.mocked(client.getDossier)
      .mockResolvedValueOnce(conflict)
      .mockResolvedValue(consistent);
    vi.mocked(client.fetchSources).mockResolvedValue({
      dossierId: SZ_DOSSIER,
      asOf: '2025-08-01T00:00:00Z',
      dossierStatus: 'CONSISTENT',
      results: [
        { sourceId: GD, outcome: 'LOADED', record: GD_REV2, errorCode: null, errorMessage: null },
        { sourceId: SZPU, outcome: 'SUPERSEDED', record: SZPU_REC, errorCode: null, errorMessage: null },
      ],
    });

    const user = userEvent.setup();
    render(<Workbench />);

    expect(await screen.findByText('冲突')).toBeInTheDocument();
    expect(screen.getByText('610.0（普通类750分制）')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '核验来源' }));

    expect(await screen.findByText('一致')).toBeInTheDocument();
    await waitFor(() =>
      expect(screen.queryByText('610.0（普通类750分制）')).not.toBeInTheDocument(),
    );
    expect(
      screen.getAllByText('609.0（普通类750分制）'),
    ).toHaveLength(2);
  });

  it('DOM 顺序：来源队列在当前事实区之前', async () => {
    vi.mocked(client.getDossier).mockResolvedValue(makeDetail());
    render(<Workbench />);

    const queue = await screen.findByRole('heading', { name: '来源队列' });
    const fact = screen.getByRole('heading', { name: '当前事实' });
    expect(
      queue.compareDocumentPosition(fact) & Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });

  it('过期卷宗：查看 hasNewerEvidence 的封存快照时显示 status 横幅且只读', async () => {
    vi.mocked(client.getDossier).mockResolvedValue(
      makeDetail({
        seals: [
          {
            sealId: 'aaaaaaaa-0000-4000-8000-000000000001',
            asOf: '2025-07-20T12:00:00Z',
            status: 'CONFLICT',
            sealedAt: '2025-07-20T12:05:00Z',
            contentHash: 'sha256:seal-snapshot',
            hasNewerEvidence: true,
          },
        ],
      }),
    );
    const sealView: SealView = {
      sealId: 'aaaaaaaa-0000-4000-8000-000000000001',
      dossierId: SZ_DOSSIER,
      asOf: '2025-07-20T12:00:00Z',
      sealedAt: '2025-07-20T12:05:00Z',
      status: 'CONFLICT',
      contentHash: 'sha256:seal-snapshot',
      hasNewerEvidence: true,
      factKey: {
        provinceCode: '44',
        admissionYear: 2025,
        subjectCategory: 'PHYSICS',
        schoolCode: '11113',
        majorGroupCode: '203',
      },
      snapshot: {
        status: 'CONFLICT',
        sources: [GD_CONFLICT, SZPU_OK, CHSI_MISSING],
        evidenceChain: [],
      },
    };
    vi.mocked(client.getSeal).mockResolvedValue(sealView);

    const user = userEvent.setup();
    render(<Workbench />);

    await user.click(
      await screen.findByRole('button', { name: /封存于/ }),
    );

    const banner = await screen.findByRole('status');
    expect(banner).toHaveTextContent('该封存卷宗之后已出现新证据，快照内容保持不变');

    expect(
      screen.queryByRole('button', { name: '核验来源' }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: '封存卷宗' }),
    ).not.toBeInTheDocument();
    const back = screen.getByRole('button', { name: '返回当前视图' });
    expect(back).toBeInTheDocument();

    await user.click(back);
    expect(
      await screen.findByRole('button', { name: '核验来源' }),
    ).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });
});
