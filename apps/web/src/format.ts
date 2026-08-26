import type {
  DossierStatus,
  Freshness,
  ScoreScale,
  SubjectCategory,
} from './api/types';

const PROVINCE_NAMES: Record<string, string> = {
  '44': '广东',
  '42': '湖北',
};

const SCHOOL_NAMES: Record<string, string> = {
  '11113': '深圳职业技术大学',
  '10834': '武汉职业技术大学',
};

export function provinceLabel(code: string): string {
  const name = PROVINCE_NAMES[code];
  return name ? `${name}（${code}）` : code;
}

export function schoolLabel(code: string): string {
  const name = SCHOOL_NAMES[code];
  return name ? `${name}（${code}）` : code;
}

export function subjectLabel(subject: SubjectCategory): string {
  switch (subject) {
    case 'PHYSICS':
      return '物理类';
    case 'HISTORY':
      return '历史类';
    case 'ART_COMPOSITE':
      return '艺术综合';
  }
}

export function scaleLabel(scale: ScoreScale): string {
  return scale === 'GAOKAO_750_TENTH' ? '普通类750分制' : '艺术综合分制';
}

export function statusLabel(status: DossierStatus): string {
  switch (status) {
    case 'NO_EVIDENCE':
      return '无证据';
    case 'SINGLE_SOURCE':
      return '单来源';
    case 'CONSISTENT':
      return '一致';
    case 'CONFLICT':
      return '冲突';
    case 'SCALE_CONFLICT':
      return '量表冲突';
  }
}

export function freshnessLabel(freshness: Freshness): string {
  switch (freshness) {
    case 'FRESH':
      return '有效';
    case 'UPCOMING':
      return '未生效';
    case 'EXPIRED':
      return '已过期';
  }
}

export function formatDelta(tenths: number): string {
  const sign = tenths > 0 ? '+' : '';
  return `${sign}${(tenths / 10).toFixed(1)}`;
}

export function formatDate(iso: string): string {
  return iso ? iso.slice(0, 10) : '—';
}

export function formatDateTime(iso: string): string {
  return iso ? iso.replace('T', ' ').slice(0, 16) : '—';
}
