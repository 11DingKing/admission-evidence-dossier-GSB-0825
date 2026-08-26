import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SourceDrawer } from '../components/SourceDrawer';
import { makeSource } from './fixtures';

describe('SourceDrawer evidence drawer', () => {
  it('选中来源时打开抽屉并显示原文', () => {
    const source = makeSource({ originalText: '投档最高分 609.0 的原文内容' });
    render(<SourceDrawer source={source} onClose={() => {}} />);

    expect(screen.getByRole('dialog', { name: /来源 SOURCE_A 原文/ })).toBeInTheDocument();
    expect(screen.getByRole('article', { name: '来源原文内容' })).toHaveTextContent('投档最高分 609.0 的原文内容');
    expect(screen.getByText('粤招办[2025]12号')).toBeInTheDocument();
  });

  it('按 Escape 键关闭抽屉', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    const source = makeSource();

    render(<SourceDrawer source={source} onClose={onClose} />);
    await user.keyboard('{Escape}');
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('点击关闭按钮关闭抽屉', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    const source = makeSource();

    render(<SourceDrawer source={source} onClose={onClose} />);
    await user.click(screen.getByRole('button', { name: '关闭抽屉' }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('source 为 null 时不渲染抽屉', () => {
    const { container } = render(<SourceDrawer source={null} onClose={() => {}} />);
    expect(container.firstChild).toBeNull();
  });
});
