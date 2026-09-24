<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { cancelPlayer, fetchSessions, registerPlayer } from "../api/client";
import type { SessionView } from "../types";

const sessions = ref<SessionView[]>([]);
const selectedSessionId = ref<number | null>(null);
const playerName = ref("");
const loading = ref(false);
const submitting = ref(false);
const loadError = ref("");
const feedback = ref<{ tone: "success" | "warning" | "info"; text: string } | null>(null);

const currentSession = computed(
  () => sessions.value.find((session) => session.id === selectedSessionId.value) ?? null,
);

const seatDots = computed(() => {
  const session = currentSession.value;
  if (!session) {
    return [];
  }
  return Array.from({ length: session.capacity }, (_, index) => index < session.seatedCount);
});

async function loadSessions(showLoading = true) {
  if (showLoading) {
    loading.value = true;
  }
  try {
    const data = await fetchSessions();
    sessions.value = data;
    loadError.value = "";
    if (selectedSessionId.value === null && data.length > 0) {
      selectedSessionId.value = data[0].id;
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : "场次加载失败";
  } finally {
    loading.value = false;
  }
}

function trimName(): string | null {
  const name = playerName.value.trim();
  if (!name) {
    ElMessage.warning("请填写玩家昵称");
    return null;
  }
  return name;
}

async function handleRegister() {
  const session = currentSession.value;
  const name = trimName();
  if (!session || !name) {
    return;
  }
  submitting.value = true;
  try {
    const result = await registerPlayer(session.id, name);
    mergeSession(result.session);
    feedback.value = {
      tone: result.success ? (result.status === "SEATED" ? "success" : "warning") : "info",
      text: result.message,
    };
    if (result.success) {
      playerName.value = "";
    }
  } catch (error) {
    feedback.value = {
      tone: "warning",
      text: error instanceof Error ? error.message : "报名失败，请稍后重试",
    };
  } finally {
    submitting.value = false;
  }
}

async function handleCancel() {
  const session = currentSession.value;
  const name = trimName();
  if (!session || !name) {
    return;
  }
  submitting.value = true;
  try {
    const result = await cancelPlayer(session.id, name);
    mergeSession(result.session);
    feedback.value = {
      tone: result.promotedPlayer ? "success" : "info",
      text: result.message,
    };
    playerName.value = "";
  } catch (error) {
    feedback.value = {
      tone: "warning",
      text: error instanceof Error ? error.message : "退局失败，请稍后重试",
    };
  } finally {
    submitting.value = false;
  }
}

function mergeSession(updated: SessionView) {
  const index = sessions.value.findIndex((session) => session.id === updated.id);
  if (index >= 0) {
    sessions.value[index] = updated;
  }
}

onMounted(() => loadSessions());
</script>

<template>
  <section class="work-panel session-board">
    <div class="board-head">
      <div>
        <h2>场次报名 · 候补补位</h2>
        <p>前台在此为玩家办理报名和退局，座位实时占用，满员后自动按序候补。</p>
      </div>
      <el-button :loading="loading" @click="loadSessions()">刷新场次</el-button>
    </div>

    <el-alert v-if="loadError" type="error" :closable="false" :title="loadError" class="board-alert" />

    <template v-else>
      <el-select
        v-model="selectedSessionId"
        placeholder="请选择场次"
        size="large"
        class="session-select"
      >
        <el-option
          v-for="session in sessions"
          :key="session.id"
          :label="`${session.title}（${session.startTime}）`"
          :value="session.id"
        />
      </el-select>

      <div v-if="currentSession" class="board-body" v-loading="loading">
        <div class="session-meta">
          <span class="meta-chip">剧本：{{ currentSession.scriptName }}</span>
          <span class="meta-chip">DM：{{ currentSession.dmName }}</span>
          <span class="meta-chip">开场：{{ currentSession.startTime }}</span>
        </div>

        <div class="seat-stat-grid">
          <div class="seat-stat" :class="{ full: currentSession.remainingSeats === 0 }">
            <span class="seat-stat-label">座位</span>
            <strong>{{ currentSession.seatedCount }} / {{ currentSession.capacity }}</strong>
            <span class="seat-stat-sub">
              {{ currentSession.remainingSeats > 0 ? `剩余 ${currentSession.remainingSeats} 个空位` : "已满员" }}
            </span>
            <div class="seat-dots">
              <i v-for="(taken, index) in seatDots" :key="index" :class="{ taken }" />
            </div>
          </div>
          <div class="seat-stat waiting">
            <span class="seat-stat-label">候补人数</span>
            <strong>{{ currentSession.waitingCount }}</strong>
            <span class="seat-stat-sub">
              {{ currentSession.waitingCount > 0 ? "有人退局即按序补位" : "暂无候补" }}
            </span>
          </div>
        </div>

        <el-alert
          v-if="currentSession.lastEvent"
          type="info"
          :closable="false"
          class="board-alert"
          :title="`最近一次补位结果：${currentSession.lastEvent}`"
        />

        <div class="action-bar">
          <el-input
            v-model="playerName"
            size="large"
            placeholder="输入玩家昵称"
            maxlength="80"
            clearable
            class="player-input"
            @keyup.enter="handleRegister"
          />
          <el-button type="primary" size="large" :loading="submitting" @click="handleRegister">
            报名
          </el-button>
          <el-button size="large" :loading="submitting" @click="handleCancel">退局</el-button>
        </div>

        <el-alert
          v-if="feedback"
          :type="feedback.tone"
          :closable="false"
          class="board-alert"
          :title="feedback.text"
        />

        <div class="roster-grid">
          <div class="roster">
            <h3>已入座（{{ currentSession.seatedPlayers.length }}）</h3>
            <p v-if="currentSession.seatedPlayers.length === 0" class="roster-empty">暂无入座玩家</p>
            <el-tag
              v-for="player in currentSession.seatedPlayers"
              :key="player.playerName"
              type="primary"
              size="large"
              class="roster-tag"
            >
              {{ player.playerName }}
            </el-tag>
          </div>
          <div class="roster">
            <h3>候补队列（{{ currentSession.waitingPlayers.length }}）</h3>
            <p v-if="currentSession.waitingPlayers.length === 0" class="roster-empty">暂无候补玩家</p>
            <el-tag
              v-for="player in currentSession.waitingPlayers"
              :key="player.playerName"
              type="warning"
              size="large"
              effect="plain"
              class="roster-tag"
            >
              #{{ player.position }} {{ player.playerName }}（前面 {{ player.aheadCount }} 人）
            </el-tag>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>
