import { act, renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { useViewport } from './useViewport';

function resizeTo(width: number) {
  act(() => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: width,
    });
    window.dispatchEvent(new Event('resize'));
  });
}

describe('useViewport', () => {
  it('360px 时为 narrow，1280px 时为 wide，并监听 resize', async () => {
    resizeTo(1024);
    const { result } = renderHook(() => useViewport());
    expect(result.current).toBe('wide');

    resizeTo(360);
    await waitFor(() => expect(result.current).toBe('narrow'));

    resizeTo(1280);
    await waitFor(() => expect(result.current).toBe('wide'));
  });
});
