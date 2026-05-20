# 📲 NFC 명함 앱 (Android Native - Kotlin + Jetpack Compose)

NFC로 명함을 교환하고 로컬에 저장 관리하는 안드로이드 앱입니다.

---

## 🚀 APK 받는 법 (GitHub Actions 자동 빌드)

> **코드 한 줄 안 짜도 됩니다. GitHub에 올리기만 하면 APK 자동 생성됩니다.**

### 1단계 — GitHub 레포 만들기
```
GitHub.com → New Repository → "NFCCard" 생성
```

### 2단계 — 이 폴더 전체 업로드
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_NAME/NFCCard.git
git push -u origin main
```

### 3단계 — APK 다운로드
```
GitHub 레포 → Actions 탭
→ "Build & Test APK" 워크플로우 클릭
→ 완료된 실행 클릭
→ 하단 Artifacts → "NFCCard-release-apk" 다운로드
```

### 4단계 — 폰에 설치
1. 다운받은 ZIP 압축 해제 → `.apk` 파일
2. 카카오톡/USB/드라이브 등으로 폰에 전송
3. 폰에서 파일 탭 → 설정 → **출처를 알 수 없는 앱 허용**
4. apk 파일 탭 → 설치

---

## 📁 프로젝트 구조

```
NFCCard/
├── .github/workflows/build.yml        ← GitHub Actions (테스트→APK 자동빌드)
├── app/
│   ├── build.gradle                   ← 앱 의존성
│   └── src/main/
│       ├── AndroidManifest.xml        ← NFC 권한 포함
│       └── java/com/nfccard/
│           ├── MainActivity.kt        ← NFC 포그라운드 디스패치 처리
│           ├── NFCCardApp.kt          ← Application 클래스
│           ├── data/
│           │   └── CardRepository.kt  ← DataStore 로컬 저장 + 모델
│           ├── utils/
│           │   └── NFCManager.kt      ← NFC 읽기/쓰기 유틸
│           └── ui/
│               ├── NFCCardApp.kt      ← 테마 + 네비게이션
│               ├── components/
│               │   └── CardView.kt    ← 명함 카드 UI
│               └── screens/
│                   ├── MyCardScreen.kt
│                   ├── NFCScreen.kt
│                   └── ContactsScreen.kt
└── app/src/test/
    └── CardSerializationTest.kt       ← 단위 테스트 6개
```

---

## 📱 주요 기능

| 화면 | 기능 |
|---|---|
| 내 명함 | 이름/직함/회사/연락처 입력, 6가지 색상, DataStore 저장 |
| NFC 교환 | **보내기**: 내 명함을 NFC NDEF 태그에 쓰기<br>**받기**: NFC 태그 읽어서 자동 저장 |
| 받은 명함 | 목록/검색/상세/삭제 |

---

## ✅ 자동 테스트 항목 (build.yml에서 실행)

- NFC 페이로드 직렬화/역직렬화
- 빈 필드 처리
- 페이로드 크기 (NFC 1KB 제한)
- 잘못된 JSON 기본값 처리
- 이름 필수값 검증
- ID 고유성

테스트 실패 시 APK 빌드를 중단합니다.

---

## ⚠️ NFC 사용 조건

- Android 8.0 (API 26) 이상
- NFC 지원 기기 (없으면 앱에서 안내 표시)
- 설정 → 연결 → NFC 활성화
