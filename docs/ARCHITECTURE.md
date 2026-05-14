# アーキテクチャ設計

## 1. システム概要

CloudOps Incident Manager は、クラウド運用チーム向けの障害対応支援 Web アプリです。
フロントエンドとバックエンドを分離した **SPA + REST API** 構成を採用し、
AWS 上では ECS Fargate によるコンテナ運用、RDS PostgreSQL でデータを永続化します。

---

## 2. コンポーネント構成

```
┌──────────────────────────────────────────────────────────────────────┐
│                           AWS Cloud                                  │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  VPC (10.0.0.0/16)                                              │ │
│  │                                                                  │ │
│  │  ┌──────────────────────────────────────────────────────────┐   │ │
│  │  │  Public Subnet (AZ-a / AZ-c)                             │   │ │
│  │  │                                                           │   │ │
│  │  │   ┌─────────────────────────────────────────────────┐    │   │ │
│  │  │   │  Application Load Balancer (ALB)                │    │   │ │
│  │  │   │  - /api/*  → Backend Target Group               │    │   │ │
│  │  │   │  - /*      → Frontend Target Group              │    │   │ │
│  │  │   └──────────────┬───────────────────┬──────────────┘    │   │ │
│  │  └──────────────────┼───────────────────┼───────────────────┘   │ │
│  │                     │                   │                        │ │
│  │  ┌──────────────────┼───────────────────┼───────────────────┐   │ │
│  │  │  Private Subnet (AZ-a / AZ-c)        │                   │   │ │
│  │  │                  │                   │                    │   │ │
│  │  │   ┌──────────────▼──────┐  ┌─────────▼────────────────┐  │   │ │
│  │  │   │  ECS Service        │  │  ECS Service             │  │   │ │
│  │  │   │  (Spring Boot API)  │  │  (Next.js Frontend)      │  │   │ │
│  │  │   │  Port: 8080         │  │  Port: 3000              │  │   │ │
│  │  │   └──────────┬──────────┘  └──────────────────────────┘  │   │ │
│  │  │              │ JDBC                                        │   │ │
│  │  │   ┌──────────▼──────────┐                                 │   │ │
│  │  │   │  RDS PostgreSQL 16  │                                 │   │ │
│  │  │   │  (Multi-AZ)         │                                 │   │ │
│  │  │   └─────────────────────┘                                 │   │ │
│  │  └───────────────────────────────────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ECR (コンテナレジストリ)  │  Secrets Manager (JWT秘密鍵 / DB接続情報)   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. ローカル開発構成

```
docker-compose.yml
┌─────────────────────────────────────────┐
│                                         │
│  frontend (Next.js)   :3000             │
│       ↓ HTTP /api/*                     │
│  backend (Spring Boot) :8080            │
│       ↓ JDBC                            │
│  db (PostgreSQL)      :5432             │
│  pgadmin              :5050             │
│                                         │
└─────────────────────────────────────────┘
```

---

## 4. 認証・認可フロー

```
Client                 Backend (Spring Boot)              DB
  │                          │                            │
  │  POST /api/v1/auth/login │                            │
  │  { email, password }     │                            │
  │─────────────────────────>│                            │
  │                          │  SELECT user WHERE email   │
  │                          │───────────────────────────>│
  │                          │<───────────────────────────│
  │                          │  BCrypt verify             │
  │                          │  Sign JWT (access + refresh)
  │  { accessToken,          │                            │
  │    refreshToken }        │                            │
  │<─────────────────────────│                            │
  │                          │                            │
  │  GET /api/v1/incidents   │                            │
  │  Authorization: Bearer … │                            │
  │─────────────────────────>│                            │
  │                          │  JwtAuthFilter: verify     │
  │                          │  Extract role → RBAC check │
  │  200 OK { … }            │                            │
  │<─────────────────────────│                            │
```

### ロール定義 (RBAC)

| ロール | 説明 | 主な権限 |
|--------|------|----------|
| `ADMIN` | システム管理者 | 全操作 + ユーザー管理 |
| `OPERATOR` | 運用担当者 | インシデント起票・更新・アラート操作 |
| `VIEWER` | 参照専用 | 全リソースの読み取りのみ |

---

## 5. バックエンド層構成

```
HTTP Request
     ↓
[Controller Layer]  — リクエスト/レスポンス変換、入力バリデーション
     ↓
[Service Layer]     — ビジネスロジック、トランザクション境界
     ↓
[Repository Layer]  — JPA リポジトリ、クエリ定義
     ↓
[Entity / DB]       — PostgreSQL
```

パッケージ構成:

```
com.cloudops.incidentmanager
├── controller/      # REST コントローラ
├── service/         # ビジネスロジック
├── repository/      # Spring Data JPA リポジトリ
├── model/           # JPA エンティティ
├── dto/             # リクエスト/レスポンス DTO (Java Records)
├── security/        # JWT フィルター・UserDetailsService・RBAC 設定
└── config/          # Spring Security・OpenAPI・CORS 設定
```

---

## 6. フロントエンド層構成

Next.js 14 App Router を採用し、Server Components / Client Components を使い分けます。

```
frontend/src/
├── app/                  # App Router ルート
│   ├── (auth)/           # 認証不要ページ (login)
│   ├── (dashboard)/      # 認証済みページ群
│   │   ├── page.tsx      # ダッシュボード
│   │   ├── incidents/    # インシデント一覧・詳細
│   │   ├── services/     # サービス台帳
│   │   ├── alerts/       # アラート一覧
│   │   └── settings/     # 設定（管理者のみ）
│   └── api/              # Next.js Route Handlers (BFF)
├── components/
│   ├── ui/               # shadcn/ui ベースコンポーネント
│   ├── incidents/        # インシデント関連コンポーネント
│   ├── services/         # サービス関連コンポーネント
│   └── dashboard/        # ダッシュボードウィジェット
├── hooks/                # カスタムフック (useSWR ラッパー等)
├── lib/
│   ├── api.ts            # API クライアント (fetch ラッパー)
│   └── auth.ts           # 認証ヘルパー
└── types/                # TypeScript 型定義
```

---

## 7. データフロー：インシデント起票

```
1. Operator が UI でインシデント起票フォームを送信
2. Next.js (Client) → POST /api/v1/incidents (Bearer token 付き)
3. JwtAuthFilter でトークン検証 + OPERATOR ロール確認
4. IncidentController → IncidentService.create()
5. IncidentRepository.save() → PostgreSQL に INSERT
6. 201 Created レスポンス返却
7. フロントエンドがキャッシュを invalidate → 一覧を再取得
```

---

## 8. アラート受信フロー

```
Prometheus / CloudWatch
        ↓ Webhook POST
POST /api/v1/alerts/webhook
        ↓
AlertWebhookController
        ↓ 正規化
AlertService.processIncoming()
        ↓ 重複チェック・重大度判定
alerts テーブルに保存
        ↓ 重大度が CRITICAL の場合
自動インシデント起票 (P1/P2)
        ↓ (将来) Slack / PagerDuty 通知
```

---

## 9. CI/CD パイプライン

```
GitHub Push
    ↓
┌────────────────────────────────────┐
│  GitHub Actions: CI Workflow       │
│                                    │
│  [frontend-check]                  │
│    npm run lint                    │
│    npm run type-check              │
│    npm run test                    │
│                                    │
│  [backend-test]                    │
│    mvn verify                      │
│    (uses postgres service)         │
│                                    │
│  [build-images] (main branch のみ) │
│    docker build frontend           │
│    docker build backend            │
│    docker push to ECR              │
│                                    │
│  [deploy] (main branch のみ)       │
│    terraform apply                 │
│    ecs update-service              │
└────────────────────────────────────┘
```

---

## 10. 技術選定の理由

| 技術 | 選定理由 |
|------|----------|
| Next.js App Router | SSR/SSG/CSR を柔軟に切り替えられる。Server Components によるパフォーマンス最適化 |
| Java 21 + Spring Boot 3 | エンタープライズ標準。Virtual Threads (Project Loom) で高スループット |
| PostgreSQL | ACID 準拠、JSON 型サポート、運用実績が豊富 |
| JWT | ステートレス認証でスケールアウトに有利 |
| Terraform | AWS リソースをコードで管理。チーム開発・環境複製が容易 |
| Flyway | DB スキーマのバージョン管理。本番適用も安全 |
