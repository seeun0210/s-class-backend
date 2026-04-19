# 로컬에서 Dev DB 접속 가이드

SSM Session Manager 포트 포워딩을 통해 로컬에서 Dev RDS에 접속합니다.

## 사전 준비

```bash
brew install --cask session-manager-plugin
```

## 접속 방법

### 1. SSM 터널 열기

터미널에서 아래 명령어를 실행하고 열어둡니다.

```bash
aws ssm start-session \
  --region ap-northeast-2 \
  --target <NAT_INSTANCE_ID> \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["<RDS_ENDPOINT>"],"portNumber":["3306"],"localPortNumber":["13306"]}'
```

### 2. DB 접속

| 항목 | 값 |
|------|-----|
| Host | `127.0.0.1` |
| Port | `13306` |
| Database | `sclass` |
| Username | 팀 내부 공유 참고 |
| Password | 팀 내부 공유 참고 |

**MySQL CLI:**

```bash
mysql -h 127.0.0.1 -P 13306 -u <username> -p'<password>'
```

**Spring Boot (`application-local.yml`):**

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:13306/sclass
    username: <username>
    password: <password>
```

## 참고

- SSM은 IAM 인증 기반이므로 IP가 바뀌어도 접속 가능
- AWS CLI에 자격증명이 설정되어 있어야 함
- 터널을 닫으면 DB 연결도 끊김
