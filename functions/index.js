const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");
const { logger } = require("firebase-functions");

initializeApp();

const TOPIC = "daily_fortune";

const MESSAGES = [
  "오늘의 운세가 도착했어요! 🌙 달빛이 비추는 당신의 하루는 어떤 모습일까요?",
  "오늘 당신에게 행운을 가져다줄 색상은 무엇일까요? 집을 나서기 전 확인해 보세요.",
  "오늘 하루, 피해야 할 것과 가까이해야 할 것은? 달빛운세에서 슬쩍 알려드릴게요.",
  "달빛이 전해주는 특별한 조언이 도착했습니다. 조용히 확인해 보세요.",
  "최근 마음속에 고민이 있으신가요? 달빛운세가 당신의 길을 비춰줄 해답을 준비했어요.",
];

function pickRandomMessage() {
  return MESSAGES[Math.floor(Math.random() * MESSAGES.length)];
}

async function sendDailyFortunePush() {
  const body = pickRandomMessage();
  const messageId = await getMessaging().send({
    topic: TOPIC,
    notification: {
      title: "달빛 운세",
      body,
    },
    data: {
      title: "달빛 운세",
      body,
      open_today: "true",
    },
    android: {
      priority: "high",
      notification: {
        channelId: "fortune_fcm_daily",
        clickAction: "OPEN_TODAY_FORTUNE",
      },
    },
  });
  logger.info("Daily fortune FCM sent", { messageId, body });
  return { messageId, body };
}

/**
 * 매일 한국 시간 07:30에 topic `daily_fortune`으로 랜덤 문구 발송.
 * Cloud Scheduler가 Functions에 포함된 스케줄로 생성됩니다.
 */
exports.dailyFortunePush = onSchedule(
  {
    schedule: "30 7 * * *",
    timeZone: "Asia/Seoul",
    region: "asia-northeast3",
  },
  async () => {
    await sendDailyFortunePush();
  },
);

/**
 * 수동 테스트용 HTTP 엔드포인트.
 * Firebase Console에서 함수 URL로 GET/POST 하면 즉시 1회 발송합니다.
 */
exports.sendDailyFortuneNow = onRequest(
  {
    region: "asia-northeast3",
  },
  async (_req, res) => {
    try {
      const result = await sendDailyFortunePush();
      res.status(200).json({ ok: true, ...result });
    } catch (error) {
      logger.error("Failed to send daily fortune FCM", error);
      res.status(500).json({ ok: false, error: String(error) });
    }
  },
);
