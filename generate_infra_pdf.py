#!/usr/bin/env python3
"""S-Class Backend Infrastructure PDF Generator"""

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm, cm
from reportlab.lib.colors import HexColor, white, black
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT, TA_CENTER
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    PageBreak, HRFlowable
)
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import os
import glob

# ── Font Registration ──
def register_fonts():
    """Register Korean-compatible fonts."""
    font_dirs = [
        "/System/Library/Fonts",
        "/Library/Fonts",
        os.path.expanduser("~/Library/Fonts"),
    ]

    # Try Apple Gothic first (macOS built-in Korean font)
    candidates = [
        ("AppleGothic", "AppleSDGothicNeo.ttc"),
        ("AppleGothic", "AppleGothic.ttf"),
    ]

    for font_name, font_file in candidates:
        for d in font_dirs:
            path = os.path.join(d, font_file)
            if os.path.exists(path):
                try:
                    pdfmetrics.registerFont(TTFont(font_name, path, subfontIndex=0))
                    pdfmetrics.registerFont(TTFont(font_name + "-Bold", path, subfontIndex=3 if font_file.endswith(".ttc") else 0))
                    return font_name
                except Exception:
                    try:
                        pdfmetrics.registerFont(TTFont(font_name, path))
                        pdfmetrics.registerFont(TTFont(font_name + "-Bold", path))
                        return font_name
                    except Exception:
                        continue

    # Fallback: search for any .ttf/.ttc with "Gothic" or "Gothic" in name
    for d in font_dirs:
        if not os.path.isdir(d):
            continue
        for f in os.listdir(d):
            if "Gothic" in f and (f.endswith(".ttf") or f.endswith(".ttc")):
                path = os.path.join(d, f)
                try:
                    pdfmetrics.registerFont(TTFont("KoreanFont", path, subfontIndex=0))
                    pdfmetrics.registerFont(TTFont("KoreanFont-Bold", path, subfontIndex=0))
                    return "KoreanFont"
                except Exception:
                    continue

    return "Helvetica"


FONT = register_fonts()
FONT_BOLD = FONT + "-Bold" if FONT != "Helvetica" else "Helvetica-Bold"

# ── Colors ──
PRIMARY = HexColor("#1a237e")      # Deep navy
ACCENT = HexColor("#0d47a1")       # Blue
SECTION_BG = HexColor("#e8eaf6")   # Light indigo bg
TABLE_HEADER = HexColor("#283593") # Indigo
TABLE_ALT = HexColor("#f5f5f5")    # Light gray
BORDER = HexColor("#bdbdbd")       # Gray border
HIGHLIGHT = HexColor("#e3f2fd")    # Light blue
TEXT_DARK = HexColor("#212121")
TEXT_MID = HexColor("#424242")
TEXT_LIGHT = HexColor("#757575")

# ── Styles ──
def build_styles():
    styles = getSampleStyleSheet()

    styles.add(ParagraphStyle(
        "DocTitle", fontName=FONT_BOLD, fontSize=24, leading=30,
        textColor=PRIMARY, alignment=TA_CENTER, spaceAfter=4*mm
    ))
    styles.add(ParagraphStyle(
        "DocSubtitle", fontName=FONT, fontSize=11, leading=14,
        textColor=TEXT_LIGHT, alignment=TA_CENTER, spaceAfter=10*mm
    ))
    styles.add(ParagraphStyle(
        "SectionTitle", fontName=FONT_BOLD, fontSize=15, leading=20,
        textColor=PRIMARY, spaceBefore=8*mm, spaceAfter=4*mm,
        borderPadding=(2*mm, 0, 2*mm, 0)
    ))
    styles.add(ParagraphStyle(
        "SubSection", fontName=FONT_BOLD, fontSize=11, leading=15,
        textColor=ACCENT, spaceBefore=5*mm, spaceAfter=2*mm
    ))
    styles.add(ParagraphStyle(
        "BodyText2", fontName=FONT, fontSize=9, leading=13,
        textColor=TEXT_DARK, spaceAfter=2*mm
    ))
    styles.add(ParagraphStyle(
        "BulletItem", fontName=FONT, fontSize=9, leading=13,
        textColor=TEXT_MID, leftIndent=10*mm, bulletIndent=5*mm,
        spaceAfter=1*mm
    ))
    styles.add(ParagraphStyle(
        "TableCell", fontName=FONT, fontSize=8, leading=11,
        textColor=TEXT_DARK
    ))
    styles.add(ParagraphStyle(
        "TableHeader", fontName=FONT_BOLD, fontSize=8, leading=11,
        textColor=white
    ))
    styles.add(ParagraphStyle(
        "CodeText", fontName="Courier", fontSize=8, leading=11,
        textColor=TEXT_DARK, leftIndent=5*mm
    ))
    styles.add(ParagraphStyle(
        "Footer", fontName=FONT, fontSize=7, leading=9,
        textColor=TEXT_LIGHT, alignment=TA_CENTER
    ))
    return styles


def make_table(headers, rows, col_widths=None):
    """Create a styled table."""
    s = build_styles()
    header_cells = [Paragraph(h, s["TableHeader"]) for h in headers]
    data = [header_cells]
    for row in rows:
        data.append([Paragraph(str(c), s["TableCell"]) for c in row])

    if col_widths is None:
        col_widths = [170*mm / len(headers)] * len(headers)

    t = Table(data, colWidths=col_widths, repeatRows=1)

    style_cmds = [
        ("BACKGROUND", (0, 0), (-1, 0), TABLE_HEADER),
        ("TEXTCOLOR", (0, 0), (-1, 0), white),
        ("ALIGN", (0, 0), (-1, -1), "LEFT"),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("FONTNAME", (0, 0), (-1, -1), FONT),
        ("FONTSIZE", (0, 0), (-1, -1), 8),
        ("TOPPADDING", (0, 0), (-1, -1), 3*mm),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3*mm),
        ("LEFTPADDING", (0, 0), (-1, -1), 3*mm),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3*mm),
        ("GRID", (0, 0), (-1, -1), 0.5, BORDER),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [white, TABLE_ALT]),
    ]
    t.setStyle(TableStyle(style_cmds))
    return t


def section_divider():
    return HRFlowable(width="100%", thickness=0.5, color=BORDER, spaceBefore=2*mm, spaceAfter=2*mm)


def build_pdf(output_path):
    s = build_styles()

    doc = SimpleDocTemplate(
        output_path, pagesize=A4,
        leftMargin=20*mm, rightMargin=20*mm,
        topMargin=20*mm, bottomMargin=20*mm
    )

    story = []

    # ── Title ──
    story.append(Spacer(1, 15*mm))
    story.append(Paragraph("S-Class Backend", s["DocTitle"]))
    story.append(Paragraph("Infrastructure Overview", ParagraphStyle(
        "SubTitle2", parent=s["DocTitle"], fontSize=18, textColor=ACCENT
    )))
    story.append(Spacer(1, 5*mm))
    story.append(Paragraph("AWS ap-northeast-1  |  Spring Boot 4.0.3  |  Kotlin  |  Terraform", s["DocSubtitle"]))
    story.append(Spacer(1, 5*mm))
    story.append(section_divider())

    # ── 1. Overview ──
    story.append(Paragraph("1. Architecture Overview", s["SectionTitle"]))
    story.append(Paragraph(
        "3개의 독립 API 서비스(Supporters, LMS, Backoffice)를 AWS App Runner에 배포하는 "
        "멀티 서비스 아키텍처. Terraform으로 인프라를 관리하며, GitHub Actions를 통한 CI/CD 파이프라인 구축.",
        s["BodyText2"]
    ))

    story.append(make_table(
        ["Component", "Technology", "Purpose"],
        [
            ["Compute", "AWS App Runner", "컨테이너 기반 API 서비스 배포"],
            ["Database", "Amazon RDS (MySQL 8.0)", "관계형 데이터 저장소"],
            ["Storage", "Amazon S3", "파일 업로드/다운로드 (Presigned URL)"],
            ["Network", "VPC + NAT Instance", "Private 네트워크 + 비용 최적화 외부 통신"],
            ["IaC", "Terraform", "인프라 코드 관리 (S3 State + DynamoDB Lock)"],
            ["CI/CD", "GitHub Actions", "빌드/테스트/배포 자동화"],
            ["Monitoring", "CloudWatch + X-Ray", "메트릭, 알람, 분산 추적"],
            ["Secrets", "AWS SSM Parameter Store", "시크릿 중앙 관리"],
            ["DNS", "Route 53", "도메인 관리 (aura.co.kr)"],
            ["Container Registry", "Amazon ECR", "Docker 이미지 저장소"],
        ],
        col_widths=[35*mm, 50*mm, 85*mm]
    ))

    # ── 2. Network ──
    story.append(Paragraph("2. Network Architecture", s["SectionTitle"]))

    story.append(Paragraph("VPC Configuration", s["SubSection"]))
    story.append(make_table(
        ["Resource", "Configuration"],
        [
            ["VPC CIDR", "10.0.0.0/16"],
            ["Public Subnets", "10.0.1.0/24 (ap-northeast-1a), 10.0.2.0/24 (ap-northeast-1c)"],
            ["Private Subnets", "10.0.10.0/24, 10.0.11.0/24"],
            ["NAT Solution", "NAT Instance (t4g.nano) - ~$3/month vs Gateway ~$45/month"],
            ["S3 Access", "VPC Gateway Endpoint (free, NAT 경유 불필요)"],
            ["App Runner", "VPC Connector로 Private Subnet 접근"],
        ],
        col_widths=[45*mm, 125*mm]
    ))

    story.append(Paragraph("Security Groups", s["SubSection"]))
    story.append(make_table(
        ["Security Group", "Inbound", "Outbound"],
        [
            ["NAT Instance SG", "HTTP/HTTPS from Private Subnets", "Unrestricted"],
            ["App Runner SG", "N/A (관리형)", "HTTPS (OAuth), MySQL 3306 to RDS"],
            ["RDS SG", "MySQL 3306 from App Runner SG only", "N/A"],
        ],
        col_widths=[40*mm, 65*mm, 65*mm]
    ))

    # ── 3. Compute ──
    story.append(Paragraph("3. Compute (App Runner)", s["SectionTitle"]))

    story.append(make_table(
        ["Service", "Dev CPU/Mem", "Prod CPU/Mem", "Instances", "Port"],
        [
            ["supporters-api", "512m / 1GB", "1024m / 2GB", "1-2 / 2-4", "8080"],
            ["lms-api", "512m / 1GB", "1024m / 2GB", "1-2 / 2-4", "8080"],
            ["backoffice-api", "512m / 1GB", "1024m / 2GB", "1-2 / 2-4", "8080"],
        ],
        col_widths=[35*mm, 30*mm, 32*mm, 28*mm, 20*mm]
    ))

    story.append(Spacer(1, 3*mm))
    for item in [
        "Max Concurrency: 100 per instance",
        "Auto Deployment: Disabled (CD 워크플로우에서 수동 트리거)",
        "VPC Egress: Private Subnet -> NAT Instance -> External",
        "X-Ray Observability: Enabled",
    ]:
        story.append(Paragraph(f"\u2022 {item}", s["BulletItem"]))

    # ── 4. Database ──
    story.append(Paragraph("4. Database (RDS MySQL 8.0)", s["SectionTitle"]))
    story.append(make_table(
        ["Parameter", "Value"],
        [
            ["Instance Class", "db.t4g.micro (dev/prod 공유)"],
            ["Storage", "20GB ~ 50GB Auto Scaling (gp3)"],
            ["Encryption", "AES-256 (at rest)"],
            ["Multi-AZ", "Disabled (Single-AZ)"],
            ["Public Access", "No (Private Subnet only)"],
            ["Backup Retention", "7 days"],
            ["Databases", "sclass_dev, sclass_prod (환경별 분리)"],
            ["Connection Pool", "HikariCP (max: 5, idle: 2)"],
            ["DDL Mode", "update (auto-schema)"],
        ],
        col_widths=[45*mm, 125*mm]
    ))

    # ── 5. Storage ──
    story.append(Paragraph("5. Storage (S3)", s["SectionTitle"]))
    story.append(make_table(
        ["Parameter", "Value"],
        [
            ["Bucket", "sclass-<env>-files"],
            ["Public Access", "All Block (완전 비공개)"],
            ["Versioning", "Enabled"],
            ["Encryption", "AES-256 Server-Side"],
            ["CORS", "GET, PUT, POST from configured origins"],
            ["Upload Method", "Presigned URL (클라이언트 직접 업로드)"],
            ["Local Dev", "MinIO (docker-compose, port 9000/9001)"],
        ],
        col_widths=[45*mm, 125*mm]
    ))

    story.append(PageBreak())

    # ── 6. Secrets ──
    story.append(Paragraph("6. Secrets Management (SSM Parameter Store)", s["SectionTitle"]))
    story.append(Paragraph(
        "모든 시크릿은 /sclass/&lt;env&gt;/ 경로에 SecureString으로 저장. "
        "ParameterStorePropertySource를 통해 Spring 기동 전 자동 로딩.",
        s["BodyText2"]
    ))
    story.append(make_table(
        ["Category", "Parameters"],
        [
            ["Database", "DATASOURCE_USERNAME, DATASOURCE_PASSWORD"],
            ["Authentication", "JWT_SECRET_KEY, TOKEN_ENCRYPTION_KEY"],
            ["OAuth", "GOOGLE_CLIENT_ID, KAKAO_CLIENT_ID, KAKAO_APP_ID"],
            ["Email", "SMTP_USERNAME, SMTP_PASSWORD"],
            ["Messaging", "ALIMTALK_ACCESS_KEY, ALIMTALK_SERVICE_ID, ALIMTALK_SECRET_KEY"],
        ],
        col_widths=[35*mm, 135*mm]
    ))

    # ── 7. Monitoring ──
    story.append(Paragraph("7. Monitoring & Observability", s["SectionTitle"]))

    story.append(Paragraph("CloudWatch Dashboard", s["SubSection"]))
    story.append(make_table(
        ["Metric", "Description", "Alarm Threshold"],
        [
            ["JVM Heap Memory", "힙 메모리 사용량", "-"],
            ["CPU Usage", "프로세스 CPU 사용률 (%)", "> 80% (2 consecutive periods)"],
            ["HikariCP Connections", "활성 DB 커넥션 수", "> 4 (pool max: 5)"],
            ["HTTP Request Count", "HTTP 요청 수", "-"],
            ["HTTP Avg Latency", "평균 응답 시간 (ms)", "-"],
            ["HTTP P99 Latency", "P99 응답 시간 (ms)", "-"],
        ],
        col_widths=[40*mm, 65*mm, 65*mm]
    ))

    story.append(Spacer(1, 3*mm))
    for item in [
        "Namespace: SClass/<ServiceName>",
        "Metrics Step: 60 seconds",
        "X-Ray: App Runner 분산 추적 활성화",
        "Missing data: non-breaching 처리",
    ]:
        story.append(Paragraph(f"\u2022 {item}", s["BulletItem"]))

    # ── 8. CI/CD ──
    story.append(Paragraph("8. CI/CD Pipelines", s["SectionTitle"]))
    story.append(make_table(
        ["Workflow", "Trigger", "Description"],
        [
            ["ci.yml", "PR / Manual", "ktlint + Build + Test + Kover Coverage"],
            ["cd.yml", "develop/main push", "변경 모듈 감지 -> Docker Build -> ECR Push -> App Runner Deploy"],
            ["deploy.yml", "Manual dispatch", "특정 서비스/환경 선택 배포"],
            ["infra.yml", "develop/main push\n(infra/env/** 변경)", "Terraform Init + Apply (auto-approve)"],
        ],
        col_widths=[30*mm, 40*mm, 100*mm]
    ))

    story.append(Spacer(1, 3*mm))
    story.append(Paragraph("Deployment Strategy", s["SubSection"]))
    for item in [
        "변경 감지 기반 선택적 배포: Common/Domain 변경 시 전체, 개별 API 변경 시 해당 서비스만",
        "release/* -> main 머지 시 3개 서비스 전체 배포",
        "Docker 이미지 태그: git SHA + latest",
        "ECR Lifecycle: 최근 5개 이미지만 보관",
    ]:
        story.append(Paragraph(f"\u2022 {item}", s["BulletItem"]))

    # ── 9. External Integrations ──
    story.append(Paragraph("9. External Integrations", s["SectionTitle"]))
    story.append(make_table(
        ["Service", "Provider", "Purpose", "Conditional"],
        [
            ["OAuth", "Google, Kakao", "소셜 로그인 (ID Token 검증)", "Always"],
            ["SMTP", "Gmail (smtp.gmail.com:587)", "이메일 인증 코드 발송", "email.smtp.enabled"],
            ["Alimtalk", "NCP SENS API", "카카오 알림톡 본인인증", "alimtalk.enabled"],
            ["S3", "AWS S3 / MinIO", "파일 업로드/다운로드", "Always"],
            ["CloudWatch", "AWS CloudWatch", "메트릭 수집/알람", "Prod only"],
        ],
        col_widths=[28*mm, 45*mm, 52*mm, 35*mm]
    ))

    # ── 10. DNS ──
    story.append(Paragraph("10. DNS & Routing (Route 53)", s["SectionTitle"]))
    for item in [
        "Hosted Zone: aura.co.kr",
        "Pattern: <service>.<env>.aura.co.kr -> App Runner CNAME",
        "TTL: 300 seconds",
        "ACM 인증서: 자동 DNS 검증",
    ]:
        story.append(Paragraph(f"\u2022 {item}", s["BulletItem"]))

    # ── 11. Cost Optimization ──
    story.append(Paragraph("11. Cost Optimization", s["SectionTitle"]))
    story.append(make_table(
        ["Strategy", "Savings", "Detail"],
        [
            ["NAT Instance vs Gateway", "~$42/month", "t4g.nano (~$3) vs NAT Gateway (~$45)"],
            ["S3 VPC Gateway Endpoint", "Free", "NAT 경유 없이 S3 직접 접근"],
            ["ECR Lifecycle Policy", "Storage cost", "최근 5개 이미지만 유지"],
            ["RDS Single-AZ", "~50% RDS cost", "Multi-AZ 비활성화"],
            ["Selective Metrics Export", "CloudWatch cost", "필요한 메트릭만 선별 전송"],
        ],
        col_widths=[48*mm, 32*mm, 90*mm]
    ))

    # ── 12. Security ──
    story.append(Paragraph("12. Security", s["SectionTitle"]))
    for item in [
        "Secrets: SSM Parameter Store + GitHub Secrets (코드 커밋 금지)",
        "RDS: Private Subnet, Security Group 격리, At-rest 암호화",
        "S3: Block Public Access 전체 활성화, Server-Side 암호화",
        "IAM: 최소 권한 원칙 (서비스별 Role 분리)",
        "Network: NAT Instance로 Egress 제어",
        "HTTPS: App Runner TLS Termination",
        "Token: AES-256-GCM 암호화 (Access Token), DB 저장 (Refresh Token)",
    ]:
        story.append(Paragraph(f"\u2022 {item}", s["BulletItem"]))

    # ── 13. Local Dev ──
    story.append(Paragraph("13. Local Development", s["SectionTitle"]))
    story.append(make_table(
        ["Component", "Local Setup"],
        [
            ["Database", "MySQL localhost:13306"],
            ["S3 Storage", "MinIO (docker-compose) localhost:9000"],
            ["Profile", "local (MinIO + Local DB)"],
            ["Test Profile", "H2 In-Memory Database"],
        ],
        col_widths=[45*mm, 125*mm]
    ))

    # ── Footer ──
    story.append(Spacer(1, 10*mm))
    story.append(section_divider())
    story.append(Paragraph("S-Class Backend Infrastructure Overview | Generated 2025", s["Footer"]))

    doc.build(story)
    print(f"PDF generated: {output_path}")


if __name__ == "__main__":
    output = os.path.join(os.path.dirname(os.path.abspath(__file__)), "S-Class_Infrastructure_Overview.pdf")
    build_pdf(output)
