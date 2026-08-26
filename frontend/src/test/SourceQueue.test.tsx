import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SourceQueue } from '../components/SourceQueue';
import { makeSource } from './fixtures';

describe('SourceQueue keyboard navigation', () => {
  it('通过方向键在来源之间移动焦点并触发选择', async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();
    const sources = [
      makeSource({ sourceId: 'SOURCE_A' }),
      makeSource({ sourceId: 'SOURCE_B' }),
      makeSource({ sourceId: 'SOURCE_C' }),
    ];

    render(
      <SourceQueue
        sources={sources}
        selectedId="SOURCE_A"
        onSelect={onSelect}
      />,
    );

    const listbox = screen.getByRole('listbox', { name: '来源队列' });
    expect(listbox).toBeInTheDocument();

    const optionA = screen.getByRole('option', { name: /SOURCE_A/ });
    expect(optionA).toHaveAttribute('aria-selected', 'true');

    optionA.focus();
    await user.keyboard('{ArrowDown}');
    expect(onSelect).toHaveBeenCalledWith('SOURCE_B');
  });

  it('在第一个来源按向上键不会越界', async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();
    const sources = [makeSource({ sourceId: 'SOURCE_A' })];

    render(
      <SourceQueue sources={sources} selectedId="SOURCE_A" onSelect={onSelect} />,
    );

    const option = screen.getByRole('option', { name: /SOURCE_A/ });
    option.focus();
    await user.keyboard('{ArrowUp}');
    expect(onSelect).not.toHaveBeenCalled();
  });

  it('按 Enter 键触发来源选择以打开抽屉', async () => {
    const user = userEvent.setup();
    const onSelect = vi.fn();
    const sources = [makeSource({ sourceId: 'SOURCE_A' })];

    render(
      <SourceQueue sources={sources} selectedId="SOURCE_A" onSelect={onSelect} />,
    );

    const option = screen.getByRole('option', { name: /SOURCE_A/ });
    option.focus();
    await user.keyboard('{Enter}');
    expect(onSelect).toHaveBeenCalledWith('SOURCE_A');
  });

  it('显示部分失败的缺口行，同时保留已加载来源', () => {
    const sources = [makeSource({ sourceId: 'SOURCE_A' })];
    const gaps = [{ sourceId: 'SOURCE_D', message: 'FACT_KEY_MISMATCH' }];

    render(
      <SourceQueue sources={sources} selectedId="SOURCE_A" onSelect={() => {}} gaps={gaps} />,
    );

    expect(screen.getByRole('option', { name: /SOURCE_A/ })).toBeInTheDocument();
    expect(screen.getByText(/缺口：SOURCE_D/)).toBeInTheDocument();
    expect(screen.getByText(/FACT_KEY_MISMATCH/)).toBeInTheDocument();
  });
});
