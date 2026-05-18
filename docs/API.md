# API 仕様

ベース URL: `http://localhost:8080/api/v1`

認証が必要なエンドポイントには `Authorization: Bearer <accessToken>` ヘッダーが必要です。

---

## 認証 (Auth)

> CC-006 で実装済み。JWT はデフォルト 1 時間有効。リフレッシュトークンは 7 日有効。

### POST /auth/login
ログイン。アクセストークンとリフレッシュトークンを返します。

**実装クラス**: `AuthController` → `AuthService` → `JwtTokenProvider`

**Request Body**
```json
{
  "email": "admin@example.com",
  "password": "Admin1234!"
}
```

| フィールド | 型 | 必須 | バリデーション |
|-----------|-----|------|---------------|
| `email` | string | ✓ | RFC 5322 メール形式 |
| `password` | string | ✓ | 空文字不可 |

**Response 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "admin@example.com",
    "displayName": "Admin User",
    "role": "ADMIN"
  }
}
```

**Response 400** — バリデーションエラー（email 形式不正・必須項目欠落）

**Response 401**
```json
{ "error": "Unauthorized", "message": "Invalid credentials" }
```

**開発用テストアカウント** (V2 シードデータ)

| email | password | role |
|-------|----------|------|
| `admin@example.com` | `Admin1234!` | ADMIN |
| `operator@example.com` | `Operator1234!` | OPERATOR |
| `viewer@example.com` | `Viewer1234!` | VIEWER |

**取得したトークンの使い方**
```bash
curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/v1/incidents
```

---

### POST /auth/refresh
アクセストークンを更新します。

**Request Body**
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

**Response 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

---

### POST /auth/logout
ログアウト（サーバー側でリフレッシュトークンを無効化）。

**Response 204** No Content

---

## ユーザー管理 (Users)

> 必要ロール: `ADMIN`（一覧・作成・削除）、`OPERATOR`/`VIEWER`（自分のプロフィール参照）

### GET /users
ユーザー一覧取得。

**Query Parameters**

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `page` | integer | ページ番号（0 始まり、デフォルト: 0） |
| `size` | integer | 1 ページのサイズ（デフォルト: 20） |
| `role` | string | `ADMIN` / `OPERATOR` / `VIEWER` でフィルタ |

**Response 200**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "admin@example.com",
      "displayName": "Admin User",
      "role": "ADMIN",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### POST /users
ユーザー作成。

**Request Body**
```json
{
  "email": "new@example.com",
  "displayName": "New User",
  "password": "securepassword",
  "role": "OPERATOR"
}
```

**Response 201**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "email": "new@example.com",
  "displayName": "New User",
  "role": "OPERATOR",
  "createdAt": "2024-06-01T10:00:00Z"
}
```

---

## サービス台帳 (Services)

### GET /services
サービス一覧取得。

**Query Parameters**

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `status` | string | `OPERATIONAL` / `DEGRADED` / `DOWN` / `MAINTENANCE` |
| `tier` | string | `TIER1` / `TIER2` / `TIER3` |
| `teamId` | UUID | チーム ID でフィルタ |

**Response 200**
```json
{
  "content": [
    {
      "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Payment Service",
      "description": "決済処理サービス",
      "team": { "id": "...", "name": "Platform Team" },
      "tier": "TIER1",
      "status": "OPERATIONAL",
      "endpointUrl": "https://payment.internal",
      "repositoryUrl": "https://github.com/org/payment-service",
      "openIncidentCount": 0,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-06-01T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### GET /services/{id}
サービス詳細取得。

**Response 200**: 上記 content 配列の 1 要素と同じ構造 + 直近インシデント一覧

---

### POST /services
サービス登録。必要ロール: `ADMIN` / `OPERATOR`

**Request Body**
```json
{
  "name": "Inventory Service",
  "description": "在庫管理サービス",
  "teamId": "team-uuid",
  "tier": "TIER2",
  "endpointUrl": "https://inventory.internal",
  "repositoryUrl": "https://github.com/org/inventory-service"
}
```

**Response 201**: 作成されたサービスオブジェクト

---

### PUT /services/{id}
サービス更新。必要ロール: `ADMIN` / `OPERATOR`

**Response 200**: 更新後のサービスオブジェクト

---

### DELETE /services/{id}
サービス削除（論理削除）。必要ロール: `ADMIN`

**Response 204** No Content

---

## インシデント管理 (Incidents)

### GET /incidents
インシデント一覧取得。

**Query Parameters**

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `status` | string | `OPEN` / `INVESTIGATING` / `MITIGATED` / `RESOLVED` / `CLOSED` |
| `severity` | string | `P1` / `P2` / `P3` / `P4` |
| `serviceId` | UUID | サービス ID でフィルタ |
| `assigneeId` | UUID | 担当者 ID でフィルタ |
| `from` | ISO8601 | 開始日時（以降） |
| `to` | ISO8601 | 終了日時（以前） |

**Response 200**
```json
{
  "content": [
    {
      "id": "inc-uuid-001",
      "title": "Payment Service レイテンシ急上昇",
      "description": "p99 レイテンシが 5 秒を超過",
      "service": { "id": "...", "name": "Payment Service" },
      "severity": "P1",
      "status": "INVESTIGATING",
      "assignee": { "id": "...", "displayName": "Taro Yamada" },
      "createdBy": { "id": "...", "displayName": "Bot" },
      "startedAt": "2024-06-01T09:00:00Z",
      "resolvedAt": null,
      "createdAt": "2024-06-01T09:00:00Z",
      "updatedAt": "2024-06-01T09:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### GET /incidents/{id}
インシデント詳細取得（対応履歴含む）。

---

### POST /incidents
インシデント起票。必要ロール: `ADMIN` / `OPERATOR`

**Request Body**
```json
{
  "title": "API サーバー 5xx エラー急増",
  "description": "本番環境で 500 エラーが急増しています",
  "serviceId": "service-uuid",
  "severity": "P1",
  "startedAt": "2024-06-01T09:00:00Z"
}
```

**Response 201**: 作成されたインシデントオブジェクト

---

### PATCH /incidents/{id}/status
ステータス変更。必要ロール: `ADMIN` / `OPERATOR`

**Request Body**
```json
{
  "status": "RESOLVED",
  "resolvedAt": "2024-06-01T11:00:00Z",
  "comment": "根本原因: 設定変更ミス。ロールバックで解消"
}
```

**Response 200**: 更新後のインシデントオブジェクト

---

### PATCH /incidents/{id}/assignee
担当者変更。必要ロール: `ADMIN` / `OPERATOR`

**Request Body**
```json
{ "assigneeId": "user-uuid" }
```

---

### GET /incidents/{id}/updates
対応履歴一覧取得。

**Response 200**
```json
[
  {
    "id": "update-uuid",
    "incidentId": "inc-uuid-001",
    "author": { "id": "...", "displayName": "Taro Yamada" },
    "updateType": "STATUS_CHANGE",
    "message": "ステータスを OPEN → INVESTIGATING に変更",
    "createdAt": "2024-06-01T09:15:00Z"
  }
]
```

---

### POST /incidents/{id}/updates
対応履歴追加（コメント）。必要ロール: `ADMIN` / `OPERATOR`

**Request Body**
```json
{
  "updateType": "COMMENT",
  "message": "ログを調査中。DB クエリのスロークエリが疑われる"
}
```

**Response 201**: 作成された対応履歴オブジェクト

---

## アラート (Alerts)

### GET /alerts
アラート一覧取得。

**Query Parameters**

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `status` | string | `FIRING` / `ACKNOWLEDGED` / `RESOLVED` |
| `severity` | string | `CRITICAL` / `WARNING` / `INFO` |
| `serviceId` | UUID | サービス ID でフィルタ |

**Response 200**
```json
{
  "content": [
    {
      "id": "alert-uuid",
      "service": { "id": "...", "name": "Payment Service" },
      "source": "prometheus",
      "title": "HighLatency",
      "description": "p99 latency > 2s for 5 minutes",
      "severity": "CRITICAL",
      "status": "FIRING",
      "linkedIncident": null,
      "firedAt": "2024-06-01T08:55:00Z",
      "acknowledgedAt": null,
      "resolvedAt": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### POST /alerts/webhook
外部アラートソース（Prometheus Alertmanager、AWS CloudWatch）からの Webhook 受信。

**Request Body（Prometheus Alertmanager 形式）**
```json
{
  "version": "4",
  "alerts": [
    {
      "status": "firing",
      "labels": {
        "alertname": "HighLatency",
        "severity": "critical",
        "service": "payment-service"
      },
      "annotations": {
        "description": "p99 latency > 2s for 5 minutes"
      },
      "startsAt": "2024-06-01T08:55:00Z"
    }
  ]
}
```

**Response 202** Accepted

---

### PATCH /alerts/{id}/acknowledge
アラートを確認済みにする。必要ロール: `ADMIN` / `OPERATOR`

**Response 200**: 更新後のアラートオブジェクト

---

### PATCH /alerts/{id}/resolve
アラートを解決済みにする。必要ロール: `ADMIN` / `OPERATOR`

**Response 200**: 更新後のアラートオブジェクト

---

## ダッシュボード (Dashboard)

### GET /dashboard/summary
ダッシュボードのサマリー情報取得。

**Response 200**
```json
{
  "openIncidents": {
    "total": 3,
    "p1": 1,
    "p2": 2,
    "p3": 0,
    "p4": 0
  },
  "firingAlerts": {
    "total": 5,
    "critical": 2,
    "warning": 3
  },
  "serviceHealth": {
    "operational": 12,
    "degraded": 1,
    "down": 1,
    "maintenance": 0
  },
  "mttr": {
    "last7days": 3600,
    "last30days": 4200
  }
}
```

---

### GET /dashboard/incidents/trend
直近 N 日のインシデント件数トレンド。

**Query Parameters**

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `days` | integer | 集計日数（デフォルト: 30） |

**Response 200**
```json
[
  { "date": "2024-05-01", "opened": 2, "resolved": 1 },
  { "date": "2024-05-02", "opened": 0, "resolved": 2 }
]
```

---

## 共通エラーレスポンス

```json
{
  "error": "エラー種別",
  "message": "詳細メッセージ",
  "timestamp": "2024-06-01T09:00:00Z",
  "path": "/api/v1/incidents"
}
```

| HTTP ステータス | 意味 |
|----------------|------|
| 400 | バリデーションエラー |
| 401 | 未認証（トークン無効・期限切れ） |
| 403 | 権限不足 |
| 404 | リソースが存在しない |
| 409 | 競合（重複作成等） |
| 500 | サーバー内部エラー |
