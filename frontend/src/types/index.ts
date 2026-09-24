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

export interface PlayerView {
  playerName: string;
}

export interface WaitingPlayerView {
  playerName: string;
  position: number;
  aheadCount: number;
}

export interface SessionView {
  id: number;
  title: string;
  scriptName: string;
  dmName: string;
  startTime: string;
  capacity: number;
  seatedCount: number;
  remainingSeats: number;
  waitingCount: number;
  lastEvent: string;
  seatedPlayers: PlayerView[];
  waitingPlayers: WaitingPlayerView[];
}

export interface RegisterResponse {
  success: boolean;
  status: "SEATED" | "WAITING";
  message: string;
  aheadCount: number;
  session: SessionView;
}

export interface CancelResponse {
  success: boolean;
  message: string;
  promotedPlayer: string | null;
  session: SessionView;
}
