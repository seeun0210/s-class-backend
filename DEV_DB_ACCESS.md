# 로컬에서 Dev DB 접속 가이드

Dev RDS는 private subnet에 있으므로, shared NAT instance를 경유한 SSM Session Manager 포트 포워딩으로 접속합니다.

## 사전 준비

```bash
brew install --cask session-manager-plugin
```

AWS CLI 자격증명과 SSM 권한이 먼저 준비되어 있어야 합니다.

## 접속 방법

### 1. SSM 터널 열기

터미널에서 아래 명령을 실행한 뒤 세션을 유지합니다.

```bash
aws ssm start-session \
  --region ap-northeast-2 \
  --target <NAT_INSTANCE_ID> \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["<RDS_ENDPOINT>"],"portNumber":["3306"],"localPortNumber":["13306"]}'
```

### 2. DB 접속

| 항목 | 값 |
| --- | --- |
| Host | `127.0.0.1` |
| Port | `13306` |
| Database | `sclass_dev` |
| Username | 팀 내부 공유 참고 |
| Password | 팀 내부 공유 참고 |

### 3. 예시

**MySQL CLI**

```bash
mysql -h 127.0.0.1 -P 13306 -u <username> -p'<password>' sclass_dev
```

**Spring Boot 설정**

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:13306/sclass_dev
    username: <username>
    password: <password>
```

## 참고

- 터널을 닫으면 DB 연결도 함께 종료됩니다.
- 로컬 기본 설정도 `127.0.0.1:13306/sclass_dev`를 기준으로 맞춰져 있습니다.
- 운영/개발 RDS 엔드포인트와 NAT instance ID는 Terraform output 또는 팀 내부 공유값을 사용하면 됩니다.
