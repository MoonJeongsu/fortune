# Firebase FCM 일일 푸시 설정 (달빛운세)

앱 내 로컬 알람(`AlarmManager`)과 **별개**로, Firebase Cloud Messaging으로
매일 아침 **한국 시간 07:30**에 랜덤 문구를 보냅니다.

## 1. Firebase 프로젝트

1. [Firebase Console](https://console.firebase.google.com/)에서 프로젝트 생성(또는 기존 사용)
2. Android 앱 추가
   - 패키지명: `com.dakbit.fortune`
3. `google-services.json` 다운로드 후 아래 경로에 저장

```text
DakbitFortune/app/google-services.json
```

참고용 템플릿: `app/google-services.json.example`

4. Cloud Messaging API(V1) 사용 설정 확인

## 2. 앱 동작

- 앱 시작 시 topic `daily_fortune` 구독
- 푸시 수신 채널: `fortune_fcm_daily` (앱 내 알람 채널과 분리)
- 알림 탭 시 오늘 운세 진입 신호(`open_today`)

## 3. Cloud Functions 배포

```powershell
cd C:\projects\Fortune\DakbitFortune
# Firebase 프로젝트: fortune-50b37
npm install -g firebase-tools
firebase login
cd functions
npm install
cd ..
firebase deploy --only functions
```

배포되는 함수:

| 함수 | 역할 |
|------|------|
| `dailyFortunePush` | 매일 07:30(Asia/Seoul) 자동 발송 |
| `sendDailyFortuneNow` | 수동 테스트용 HTTP |

수동 테스트 URL (이미 배포됨):

```text
https://asia-northeast3-fortune-50b37.cloudfunctions.net/sendDailyFortuneNow
```

## 4. 테스트

1. 실제 `google-services.json`으로 디버그 앱 설치
2. 알림 권한 허용
3. `sendDailyFortuneNow` URL 호출 → 기기에서 푸시 확인

## 5. 문구 (서버에서 랜덤)

- 오늘의 운세가 도착했어요! 🌙 달빛이 비추는 당신의 하루는 어떤 모습일까요?
- 오늘 당신에게 행운을 가져다줄 색상은 무엇일까요? 집을 나서기 전 확인해 보세요.
- 오늘 하루, 피해야 할 것과 가까이해야 할 것은? 달빛운세에서 슬쩍 알려드릴게요.
- 달빛이 전해주는 특별한 조언이 도착했습니다. 조용히 확인해 보세요.
- 최근 마음속에 고민이 있으신가요? 달빛운세가 당신의 길을 비춰줄 해답을 준비했어요.
