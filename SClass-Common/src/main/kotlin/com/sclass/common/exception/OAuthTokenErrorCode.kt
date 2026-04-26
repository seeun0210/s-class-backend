package com.sclass.common.exception

enum class OAuthTokenErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    OAUTH_TOKEN_AUDIENCE_MISMATCH("OAUTH_010", "OAuth 토큰의 대상 애플리케이션이 일치하지 않습니다", 401),
    OAUTH_TOKEN_VALIDATION_FAILED("OAUTH_011", "OAuth 토큰 검증에 실패했습니다", 502),
    GOOGLE_TOKEN_EXCHANGE_FAILED("OAUTH_012", "Google 토큰 교환에 실패했습니다", 400),
    GOOGLE_TOKEN_REFRESH_FAILED("OAUTH_013", "Google 토큰 갱신에 실패했습니다", 401),
    GOOGLE_REFRESH_TOKEN_MISSING("OAUTH_014", "Google에서 refresh token이 발급되지 않았습니다", 400),
    GOOGLE_IDENTITY_SCOPE_MISSING("OAUTH_016", "Google 계정 이메일 조회를 위한 email scope가 필요합니다", 400),
    GOOGLE_CALENDAR_SCOPE_MISSING("OAUTH_017", "Google Calendar 연동을 위한 calendar.events scope가 필요합니다", 400),
    GOOGLE_OAUTH_PROVIDER_UNAVAILABLE("OAUTH_018", "Google OAuth 서버가 일시적으로 응답하지 않습니다", 503),
    GOOGLE_OAUTH_STATE_INVALID("OAUTH_019", "Google OAuth state가 유효하지 않습니다", 400),
    GOOGLE_CALENDAR_REQUEST_FAILED("OAUTH_020", "Google Calendar 요청에 실패했습니다", 400),
    GOOGLE_CALENDAR_UNAUTHORIZED("OAUTH_021", "Google Calendar 인증에 실패했습니다", 401),
    GOOGLE_DRIVE_SCOPE_MISSING("OAUTH_024", "Google Meet 녹화 파일 조회를 위한 drive.meet.readonly scope가 필요합니다", 400),
    GOOGLE_CALENDAR_CENTRAL_DISABLED("OAUTH_025", "중앙 Google Calendar 연동이 비활성화되어 있습니다", 503),
}
