# backend-auth

Team9 중앙 인증용 Envoy external authorization service입니다.

- 공개 조회 API는 token 없이 허용합니다.
- 보호 API는 Cognito JWT를 검증합니다.
- Cognito `sub`를 login-service의 internal mapping API로 내부 `userId`에 매핑합니다.
- 허용된 요청에는 `X-Auth-Gateway`, `X-Cognito-Sub`, `X-User-Id`, `X-User-Name`, `X-User-Email`을 반환합니다.

## Local test

```bash
./gradlew test
```

## Environment

| Name | Description |
| --- | --- |
| `COGNITO_ISSUER_URI` | Cognito issuer URI |
| `COGNITO_CLIENT_ID` | Cognito app client id |
| `LOGIN_SERVICE_BASE_URL` | Internal login-service base URL |
| `LOGIN_SERVICE_INTERNAL_TOKEN` | Shared token for login-service internal lookup |
| `AUTH_PUBLIC_ROUTES` | Comma-separated public route policies, for example `GET /api/room/rooms` |
