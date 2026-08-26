import { useEffect, useState } from 'react';

export type Viewport = 'narrow' | 'wide';

const NARROW_BREAKPOINT = 720;

function readViewport(): Viewport {
  return window.innerWidth < NARROW_BREAKPOINT ? 'narrow' : 'wide';
}

export function useViewport(): Viewport {
  const [viewport, setViewport] = useState<Viewport>(readViewport);

  useEffect(() => {
    const onResize = () => setViewport(readViewport());
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  return viewport;
}
