# 달빛운세

생년월일과 오늘 날짜의 60갑자를 계산해 개인화된 오늘의 운세를 보여주는 오프라인 Android 앱입니다.

## 주요 기능

- 종합 점수와 총운
- 애정운, 금전운, 학업·성장운, 이동·여행운
- 행운의 방향, 색, 숫자, 띠, 아이템, 별자리
- 오늘의 스타일 제안
- 기기 내부 프로필 저장

로그인, 서버 통신, 전문가 상담, 사용자 커뮤니티는 포함하지 않습니다.

## 실행

1. Android Studio에서 이 폴더를 엽니다.
2. JDK 17과 Android SDK 35를 선택합니다.
3. `app` 실행 구성을 Android 기기 또는 에뮬레이터에서 실행합니다.

## APK 빌드

### Android Studio

1. `DakbitFortune` 폴더를 엽니다.
2. Gradle 동기화가 끝날 때까지 기다립니다.
3. 메뉴에서 **Build > Build Bundle(s) / APK(s) > Build APK(s)**를 선택합니다.

### Windows 명령행

PowerShell에서 프로젝트 폴더로 이동한 뒤 실행합니다.

```powershell
cd C:\projects\Fortune\DakbitFortune
.\gradlew.bat assembleDebug
```

생성되는 디버그 APK:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 데이터 갱신

원본 `TodayFortune.sql`을 수정했다면 다음 명령으로 앱 자산을 다시 생성합니다.

```powershell
python .\tools\build_assets.py
```

운세 데이터는 2015년 작성 자료이므로 공개 배포 전 문구 현대화와 사용 권리 확인이 필요합니다.
