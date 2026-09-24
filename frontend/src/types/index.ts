export interface FeatureItem {
  id: number;
  title: string;
  description: string;
  status: string;
  metric: string;
}

export interface KpiItem {
  label: string;
  value: string;
  trend: string;
  tone: string;
}

export interface OperationRecord {
  key: string;
  name: string;
  owner: string;
  status: string;
  metric: string;
  priority: string;
}

export interface OverviewResponse {
  appName: string;
  appCode: string;
  description: string;
  features: FeatureItem[];
  kpis: KpiItem[];
  records: OperationRecord[];
}

export type RegistrationStatus = "SEATED" | "WAITING";

export interface WaitingPlayer {
  playerName: string;
  position: number;
}

export interface SessionView {
  id: number;
  title: string;
  startTime: string;
  capacity: number;
  seatedCount: number;
  freeSeats: number;
  full: boolean;
  waitingCount: number;
  playerStatus: RegistrationStatus | null;
  waitingPosition: number | null;
  lastPromotedPlayer: string | null;
  lastPromotedAt: string | null;
  lastPromotionNote: string | null;
  seatedPlayers: string[];
  waitingPlayers: WaitingPlayer[];
}

export interface SessionActionResult {
  message: string;
  status: RegistrationStatus;
  waitingPosition: number | null;
  session: SessionView;
}
