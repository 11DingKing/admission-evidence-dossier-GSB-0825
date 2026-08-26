export type SubjectCategory = 'PHYSICS' | 'HISTORY' | 'ART_COMPOSITE';

export type ScoreScale = 'GAOKAO_750_TENTH' | 'ART_COMPOSITE_TENTH';

export type Freshness = 'FRESH' | 'UPCOMING' | 'EXPIRED';

export type DossierStatus =
  | 'NO_EVIDENCE'
  | 'SINGLE_SOURCE'
  | 'CONSISTENT'
  | 'CONFLICT'
  | 'SCALE_CONFLICT';

export type LoadState = 'OK' | 'ERROR' | 'MISSING';

export type FetchOutcome = 'LOADED' | 'SUPERSEDED' | 'ERROR';

export type RecordOrigin = 'FETCH' | 'MANUAL';

export interface FactKey {
  provinceCode: string;
  admissionYear: number;
  subjectCategory: SubjectCategory;
  schoolCode: string;
  majorGroupCode: string;
}

export interface SourceRecordView {
  sourceId: string;
  sourceName: string | null;
  sourceRevision: number;
  scoreTenths: number;
  scoreDisplay: string;
  scoreScale: ScoreScale;
  sourceDocNo: string;
  effectiveFrom: string;
  effectiveTo: string | null;
  recordedAt: string;
  freshness: Freshness;
  contentHash: string;
  rawText: string;
  origin: RecordOrigin;
  superseded: boolean;
}

export interface SourceState {
  sourceId: string;
  sourceName: string;
  loadState: LoadState;
  errorCode: string | null;
  errorMessage: string | null;
  current: SourceRecordView | null;
  revisionCount: number;
}

export interface SealSummary {
  sealId: string;
  asOf: string;
  status: DossierStatus;
  sealedAt: string;
  contentHash: string;
  hasNewerEvidence: boolean;
}

export interface DossierDetail {
  dossierId: string;
  factKey: FactKey;
  factKeyHash: string;
  expectedScale: ScoreScale;
  status: DossierStatus;
  asOf: string;
  sources: SourceState[];
  evidenceChain: SourceRecordView[];
  seals: SealSummary[];
}

export interface FetchResult {
  sourceId: string;
  outcome: FetchOutcome;
  record: SourceRecordView | null;
  errorCode: string | null;
  errorMessage: string | null;
}

export interface FetchReport {
  dossierId: string;
  asOf: string;
  dossierStatus: DossierStatus;
  results: FetchResult[];
}

export interface RevisionView extends SourceRecordView {
  deltaTenths: number | null;
}

export interface RevisionChain {
  sourceId: string;
  revisions: RevisionView[];
}

export interface SealSnapshot {
  status: DossierStatus;
  sources: SourceState[];
  evidenceChain: SourceRecordView[];
}

export interface SealView extends SealSummary {
  dossierId: string;
  factKey: FactKey;
  snapshot: SealSnapshot;
}

export interface ApiError {
  errorCode: string;
  message: string;
}
