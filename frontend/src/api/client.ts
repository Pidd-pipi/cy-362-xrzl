import { API_BASE_URL } from "../constants/app";
import type { CancelResponse, OverviewResponse, RegisterResponse, SessionView } from "../types";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    ...options,
  });

  const body = (await response.json().catch(() => null)) as ({ message?: string } & T) | null;

  if (!response.ok) {
    throw new Error(body?.message ?? `请求失败（${response.status}）`);
  }

  return body as T;
}

export async function fetchOverview(): Promise<OverviewResponse> {
  return request<OverviewResponse>("/overview");
}

export async function fetchSessions(): Promise<SessionView[]> {
  return request<SessionView[]>("/sessions");
}

export async function registerPlayer(sessionId: number, playerName: string): Promise<RegisterResponse> {
  return request<RegisterResponse>(`/sessions/${sessionId}/register`, {
    method: "POST",
    body: JSON.stringify({ playerName }),
  });
}

export async function cancelPlayer(sessionId: number, playerName: string): Promise<CancelResponse> {
  return request<CancelResponse>(`/sessions/${sessionId}/cancel`, {
    method: "POST",
    body: JSON.stringify({ playerName }),
  });
}
