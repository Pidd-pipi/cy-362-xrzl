import { API_BASE_URL } from "../constants/app";
import type { OverviewResponse, SessionActionResult, SessionView } from "../types";

async function parseJsonOrThrow(response: Response): Promise<unknown> {
  const payload = (await response.json().catch(() => null)) as
    | { message?: string }
    | null;
  if (!response.ok) {
    throw new Error(payload?.message || `请求失败（${response.status}）`);
  }
  return payload;
}

export async function fetchOverview(): Promise<OverviewResponse> {
  const response = await fetch(`${API_BASE_URL}/overview`, {
    headers: { Accept: "application/json" },
  });

  if (!response.ok) {
    throw new Error(`Overview request failed: ${response.status}`);
  }

  return response.json() as Promise<OverviewResponse>;
}

export async function fetchSessions(): Promise<SessionView[]> {
  const response = await fetch(`${API_BASE_URL}/sessions`, {
    headers: { Accept: "application/json" },
  });
  return (await parseJsonOrThrow(response)) as SessionView[];
}

export async function registerPlayer(
  sessionId: number,
  playerName: string,
): Promise<SessionActionResult> {
  const response = await fetch(`${API_BASE_URL}/sessions/${sessionId}/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify({ playerName }),
  });
  return (await parseJsonOrThrow(response)) as SessionActionResult;
}

export async function cancelPlayer(
  sessionId: number,
  playerName: string,
): Promise<SessionActionResult> {
  const response = await fetch(`${API_BASE_URL}/sessions/${sessionId}/cancel`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify({ playerName }),
  });
  return (await parseJsonOrThrow(response)) as SessionActionResult;
}
