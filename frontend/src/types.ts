export type DossierStatus = 'PENDING' | 'CONSISTENT' | 'CONFLICT' | 'SCALE_CONFLICT';
export type Freshness = 'FRESH' | 'STALE' | 'EXPIRED';

export interface FactKey {
  provinceCode: string;
  admissionYear: number;
  subjectCategory: string;
  schoolCode: string;
  majorGroupCode: string;
}

export interface SourceRecordResponse {
  sourceId: string;
  sourceRevision: number;
  scoreValue: number;
  scoreScale: string;
  originalScoreText: string | null;
  effectiveFrom: string;
  effectiveTo: string | null;
  recordedAt: string;
  sourceDocumentNo: string | null;
  originalText: string | null;
  contentHash: string;
  freshness: Freshness;
  isLatestRevision: boolean;
}

export interface DossierEntryResponse {
  sourceId: string;
  sourceRevision: number;
  scoreValue: number;
  scoreScale: string;
  originalScoreText: string | null;
  effectiveFrom: string;
  effectiveTo: string | null;
  recordedAt: string;
  sourceDocumentNo: string | null;
  originalText: string | null;
  contentHash: string;
  freshness: Freshness;
}

export interface SourceError {
  index: number;
  sourceId: string;
  errorCode: string;
  message: string;
}

export interface DossierResponse {
  dossierId: string;
  provinceCode: string;
  admissionYear: number;
  subjectCategory: string;
  schoolCode: string;
  majorGroupCode: string;
  expectedScale: string;
  status: DossierStatus;
  asOf: string | null;
  sealed: boolean;
  sealedAt: string | null;
  createdAt: string;
  updatedAt: string;
  expired: boolean;
  sources: SourceRecordResponse[];
  sealedEntries: DossierEntryResponse[];
}

export interface BatchSourceResponse {
  accepted: SourceRecordResponse[];
  errors: SourceError[];
  totalCount: number;
  successCount: number;
  failureCount: number;
  partialFailure: boolean;
}

export interface CreateDossierRequest extends FactKey {
  expectedScale: string;
}

export interface SourceRecordRequest extends FactKey {
  sourceId: string;
  sourceRevision: number;
  scoreValue: number;
  scoreScale: string;
  originalScoreText?: string | null;
  effectiveFrom: string;
  effectiveTo?: string | null;
  recordedAt: string;
  sourceDocumentNo?: string | null;
  originalText?: string | null;
}
