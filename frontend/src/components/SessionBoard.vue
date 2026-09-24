<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { cancelPlayer, fetchSessions, registerPlayer } from "../api/client";
import type { SessionActionResult, SessionView } from "../types";

const sessions = ref<SessionView[]>([]);
const selectedId = ref<number | null>(null);
const playerName = ref("");
const loading = ref(false);
const submitting = ref(false);
const loadError = ref("");
const lastFeedback = ref<{ type: "success" | "info" | "warning"; text: string } | null>(null);

let pollTimer: ReturnType<typeof setInterval> | null = null;

const selectedSession = computed<SessionView | null>(
  () => sessions.value.find((item) => item.id === selectedId.value) ?? null,
);

function startTimeLabel(session: SessionView): string {
  const start = new Date(session.startTime);
  if (Number.isNaN(start.getTime())) {
    return "";
  }
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${start.getMonth() + 1}月${start.getDate()}日 ${pad(start.getHours())}:${pad(start.getMinutes())}`;
}

async function loadSessions(showLoading = false) {
  if (showLoading) {
    loading.value = true;
  }
  try {
    const data = await fetchSessions();
    sessions.value = data;
    if (selectedId.value === null && data.length > 0) {
      selectedId.value = data[0].id;
    }
    loadError.value = "";
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : "场次加载失败";
  } finally {
    if (showLoading) {
      loading.value = false;
    }
  }
}

function applyResult(result: SessionActionResult) {
  const index = sessions.value.findIndex((item) => item.id === result.session.id);
  if (index >= 0) {
    sessions.value[index] = result.session;
  } else {
    sessions.value.push(result.session);
  }
  lastFeedback.value = {
    type: result.session.full && result.status === "WAITING" ? "warning" : "success",
    text: result.message,
  };
}

async function submit(action: "register" | "cancel") {
  if (selectedId.value === null) {
    ElMessage.warning("请先选择一个场次");
    return;
  }
  const name = playerName.value.trim();
  if (!name) {
    ElMessage.warning("请输入玩家昵称");
    return;
  }
  submitting.value = true;
  try {
    const result =
      action === "register"
        ? await registerPlayer(selectedId.value, name)
        : await cancelPlayer(selectedId.value, name);
    applyResult(result);
    ElMessage.success(result.message);
  } catch (error) {
    const text = error instanceof Error ? error.message : "操作失败";
    lastFeedback.value = { type: "warning", text };
    ElMessage.warning(text);
    await loadSessions();
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  void loadSessions(true);
  // 多人同时在前台操作时，定时刷新保证看到的座位 / 候补人数是最新的
  pollTimer = setInterval(() => void loadSessions(), 10000);
});

onBeforeUnmount(() => {
  if (pollTimer !== null) {
    clearInterval(pollTimer);
  }
});
</script>

<template>
  <section class="work-panel session-board" v-loading="loading">
    <div class="panel-head">
      <div>
        <h2>场次报名 · 候补补位</h2>
        <p>选择场次办理报名或退局，占座、候补人数与最近一次补位结果立即刷新。</p>
      </div>
      <el-button @click="loadSessions(true)">刷新场次</el-button>
    </div>

    <el-alert
      v-if="loadError"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
      class="board-alert"
    />

    <el-radio-group v-model="selectedId" class="session-tabs">
      <el-radio-button
        v-for="session in sessions"
        :key="session.id"
        :value="session.id"
      >
        {{ session.title }}
      </el-radio-button>
    </el-radio-group>

    <div v-if="selectedSession" class="session-detail">
      <div class="seat-summary">
        <div class="seat-progress">
          <div class="seat-numbers">
            <span class="seat-free">{{ selectedSession.freeSeats }} 个空位</span>
            <span class="seat-total">容量 {{ selectedSession.capacity }} · 已坐 {{ selectedSession.seatedCount }}</span>
          </div>
          <el-progress
            :percentage="Math.round((selectedSession.seatedCount / selectedSession.capacity) * 100)"
            :status="selectedSession.full ? 'exception' : undefined"
            :stroke-width="14"
          />
        </div>
        <el-tag :type="selectedSession.full ? 'danger' : 'success'" size="large" effect="dark">
          {{ selectedSession.full ? "已满员 · 候补中" : "可报名占座" }}
        </el-tag>
        <el-tag type="warning" size="large" effect="plain">
          候补 {{ selectedSession.waitingCount }} 人
        </el-tag>
      </div>

      <div class="action-row">
        <el-input
          v-model="playerName"
          placeholder="输入玩家昵称，如：小明"
          maxlength="80"
          clearable
          class="player-input"
          @keyup.enter="submit('register')"
        />
        <el-button
          type="primary"
          :loading="submitting"
          @click="submit('register')"
        >
          报名
        </el-button>
        <el-button
          type="danger"
          plain
          :loading="submitting"
          @click="submit('cancel')"
        >
          退局
        </el-button>
      </div>

      <el-alert
        v-if="lastFeedback"
        :title="lastFeedback.text"
        :type="lastFeedback.type"
        show-icon
        :closable="false"
        class="board-alert"
      />

      <div class="info-grid">
        <article class="info-card">
          <header>
            <strong>已占座玩家（{{ selectedSession.seatedPlayers.length }}）</strong>
            <span>{{ startTimeLabel(selectedSession) }} 开场</span>
          </header>
          <div class="tag-cloud">
            <el-tag
              v-for="name in selectedSession.seatedPlayers"
              :key="name"
              type="primary"
              effect="light"
              class="player-tag"
            >
              {{ name }}
            </el-tag>
            <span v-if="selectedSession.seatedPlayers.length === 0" class="empty-hint">
              还没有人报名
            </span>
          </div>
        </article>

        <article class="info-card">
          <header>
            <strong>候补队列（{{ selectedSession.waitingPlayers.length }}）</strong>
            <span>按报名先后自动补位</span>
          </header>
          <div class="tag-cloud">
            <el-tag
              v-for="player in selectedSession.waitingPlayers"
              :key="player.playerName"
              type="warning"
              effect="plain"
              class="player-tag"
            >
              {{ player.position }}. {{ player.playerName }}
            </el-tag>
            <span v-if="selectedSession.waitingPlayers.length === 0" class="empty-hint">
              暂无候补
            </span>
          </div>
        </article>
      </div>

      <article class="promotion-card">
        <header>
          <strong>最近一次补位结果</strong>
        </header>
        <template v-if="selectedSession.lastPromotionNote">
          <p class="promotion-note">{{ selectedSession.lastPromotionNote }}</p>
          <small v-if="selectedSession.lastPromotedAt">
            补位玩家：{{ selectedSession.lastPromotedPlayer ?? "无" }} ·
            时间：{{ new Date(selectedSession.lastPromotedAt).toLocaleTimeString() }}
          </small>
        </template>
        <p v-else class="empty-hint">还没有发生过退局补位。</p>
      </article>
    </div>
  </section>
</template>
