# 다가치

> 프로젝트와 스터디를 할 동료를 찾는 웹 서비스

## 📌 프로젝트 소개

다가치(Dagachi)는 개발자들이 팀 프로젝트와 스터디 그룹을 쉽게 찾고 참여할 수 있도록 돕는 플랫폼입니다.

## 🛠 기술 스택

### Frontend
- React 18
- TypeScript
- Vite

### Backend
- Spring Boot 3.5
- Java 17
- Gradle

### DevOps
- Docker & Docker Compose
- Nginx

## 🚀 시작하기

### 사전 요구사항

- Docker & Docker Compose
- (로컬 개발 시) Node.js 22+, Java 17+

### Docker Compose로 실행

```bash
cd dagachi

docker-compose up --build
```

### 로컬 개발 환경

#### Backend

```bash
cd dagachi_be
./gradlew bootRun
```

#### Frontend

```bash
cd dagachi_fe
npm install
npm run dev
```

## 📁 프로젝트 구조

```
dagachi/
├── dagachi_be/          # Spring Boot 백엔드
├── dagachi_fe/          # React 프론트엔드
└── docker-compose.yml   # Docker Compose 설정
```


FE: `./dagachi_fe/.env.production`


## ⚙️ 환경변수 설정
BE: `./.env.fe`

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.
