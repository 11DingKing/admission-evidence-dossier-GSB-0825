import type { DossierResponse, SourceRecordResponse } from '../types';

export function makeSource(overrides: Partial<SourceRecordResponse> = {}): SourceRecordResponse {
  return {
    sourceId: 'SOURCE_A',
    sourceRevision: 1,
    scoreValue: 6090,
    scoreScale: 'PHYSICS_750_T1',
    originalScoreText: '609.0',
    effectiveFrom: '2025-07-10T00:00:00Z',
    effectiveTo: null,
    recordedAt: '2025-07-15T10:00:00Z',
    sourceDocumentNo: '粤招办[2025]12号',
    originalText: '深圳职业技术大学 普通物理类 投档最高分 609.0',
    contentHash: 'abc123def4567890abc123def4567890abc123def4567890abc123def4567890',
    freshness: 'FRESH',
    isLatestRevision: true,
    ...overrides,
  };
}

export function makeDossier(overrides: Partial<DossierResponse> = {}): DossierResponse {
  return {
    dossierId: 'test-dossier-id-1234',
    provinceCode: '44',
    admissionYear: 2025,
    subjectCategory: 'PHYSICS',
    schoolCode: '11113',
    majorGroupCode: '001',
    expectedScale: 'PHYSICS_750_T1',
    status: 'CONSISTENT',
    asOf: null,
    sealed: false,
    sealedAt: null,
    createdAt: '2025-07-15T09:00:00Z',
    updatedAt: '2025-07-15T10:00:00Z',
    expired: false,
    sources: [
      makeSource({ sourceId: 'SOURCE_A', sourceRevision: 2, scoreValue: 6090 }),
      makeSource({ sourceId: 'SOURCE_B', sourceRevision: 1, scoreValue: 6090,
        sourceDocumentNo: '粤招办[2025]13号', contentHash: 'b'.repeat(64) }),
    ],
    sealedEntries: [],
    ...overrides,
  };
}
