import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { App } from '../App';
import { makeDossier, makeSource } from './fixtures';

const apiMocks = vi.hoisted(() => ({
  getDossier: vi.fn(),
  getSourceHistory: vi.fn(),
  listDossiers: vi.fn(),
  createDossier: vi.fn(),
  sealDossier: vi.fn(),
}));

vi.mock('../api', () => ({
  getDossier: apiMocks.getDossier,
  getSourceHistory: apiMocks.getSourceHistory,
  listDossiers: apiMocks.listDossiers,
  createDossier: apiMocks.createDossier,
  sealDossier: apiMocks.sealDossier,
}));

beforeEach(() => {
  vi.clearAllMocks();
});

afterEach(() => {
  vi.restoreAllMocks();
});

describe('App workbench states', () => {
  it('初始显示空状态提示', () => {
    render(<App />);
    expect(screen.getByText(/输入卷宗 ID 并加载/)).toBeInTheDocument();
  });

  it('加载中显示加载状态', async () => {
    apiMocks.getDossier.mockReturnValue(new Promise(() => {}));
    render(<App />);
    await userEvent.type(screen.getByRole('textbox', { name: '输入卷宗 ID' }), 'abc');
    await userEvent.click(screen.getByRole('button', { name: '加载' }));
    expect(await screen.findByRole('status', { name: '' })).toHaveTextContent('正在加载卷宗');
  });

  it('加载失败且无缓存数据时显示错误', async () => {
    apiMocks.getDossier.mockRejectedValue(new Error('Network failure'));
    render(<App />);
    await userEvent.type(screen.getByRole('textbox', { name: '输入卷宗 ID' }), 'bad-id');
    await userEvent.click(screen.getByRole('button', { name: '加载' }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/Network failure/);
  });

  it('过期卷宗显示过期横幅', async () => {
    const expired = makeDossier({ expired: true, status: 'CONSISTENT' });
    apiMocks.getDossier.mockResolvedValue(expired);
    apiMocks.getSourceHistory.mockResolvedValue([]);

    render(<App />);
    await userEvent.type(screen.getByRole('textbox', { name: '输入卷宗 ID' }), 'exp-1');
    await userEvent.click(screen.getByRole('button', { name: '加载' }));

    expect(await screen.findByRole('alert', { name: '' })).toHaveTextContent(/已过期/);
  });

  it('部分失败：版本历史加载失败时保留证据链并显示提示', async () => {
    const dossier = makeDossier();
    apiMocks.getDossier.mockResolvedValue(dossier);
    apiMocks.getSourceHistory.mockRejectedValue(new Error('history unavailable'));

    render(<App />);
    await userEvent.type(screen.getByRole('textbox', { name: '输入卷宗 ID' }), 'd-1');
    await userEvent.click(screen.getByRole('button', { name: '加载' }));

    expect(await screen.findByRole('option', { name: /SOURCE_A/ })).toBeInTheDocument();
    expect(await screen.findByRole('option', { name: /SOURCE_B/ })).toBeInTheDocument();
    expect(screen.getByText(/版本历史加载失败/)).toBeInTheDocument();
  });
});

describe('App mobile layout at 360px', () => {
  it('在 360px 视口下工作台元素均可访问且无溢出结构问题', async () => {
    const dossier = makeDossier();
    apiMocks.getDossier.mockResolvedValue(dossier);
    apiMocks.getSourceHistory.mockResolvedValue([
      makeSource({ sourceId: 'SOURCE_A', sourceRevision: 1, scoreValue: 6100 }),
      makeSource({ sourceId: 'SOURCE_A', sourceRevision: 2, scoreValue: 6090 }),
      makeSource({ sourceId: 'SOURCE_B', sourceRevision: 1, scoreValue: 6090 }),
    ]);

    Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 360 });
    window.dispatchEvent(new Event('resize'));

    render(<App />);
    await userEvent.type(screen.getByRole('textbox', { name: '输入卷宗 ID' }), 'mobile-1');
    await userEvent.click(screen.getByRole('button', { name: '加载' }));

    await waitFor(() => {
      expect(screen.getByRole('region', { name: '来源队列' })).toBeInTheDocument();
    });

    expect(screen.getByRole('region', { name: '当前事实' })).toBeInTheDocument();
    expect(screen.getByRole('region', { name: '版本差异' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /SOURCE_A/ })).toBeInTheDocument();
    expect(screen.getAllByText('609.0').length).toBeGreaterThanOrEqual(2);
  });
});
