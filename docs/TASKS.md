# MVP 開発タスク一覧

## 概要

MVP スコープ: **認証 + サービス台帳 + インシデント管理 + ダッシュボード**（アラート Webhook は v1.1 以降）

タスクは **Claude Code 向け**（複雑な設計判断・多ファイル変更・設定ファイル整備）と
**Codex 向け**（明確な仕様がある CRUD 実装・UI コンポーネント）に分類しています。

---

## Phase 0: 環境セットアップ

### [CC-001] Claude Code向け — バックエンド Spring Boot プロジェクト初期化
**概要**: Spring Initializr でプロジェクト生成、pom.xml 依存関係設定、ディレクトリ構成整備  
**完了条件**:
- `./mvnw spring-boot:run` でアプリが起動する
- `/actuator/health` が 200 を返す
- `application.yml` に profile (`local`, `test`, `prod`) が設定されている

**依存関係**: なし  
**見積もり**: 1.5h

---

### [CC-002] Claude Code向け — フロントエンド Next.js プロジェクト初期化
**概要**: Next.js 14 App Router + TypeScript + Tailwind CSS + shadcn/ui セットアップ  
**完了条件**:
- `npm run dev` で `localhost:3000` が表示される
- `npm run lint`, `npm run type-check` がエラーなし
- `src/app/layout.tsx`, `src/app/page.tsx` が存在する

**依存関係**: なし  
**見積もり**: 1h

---

### [CC-003] Claude Code向け — Docker Compose 完成
**概要**: `docker-compose.yml` に全サービス（db, backend, frontend, pgadmin）を定義し、ローカルで一発起動できるようにする  
**完了条件**:
- `docker compose up` だけで全サービスが起動する
- バックエンドが DB に接続できる（ヘルスチェック通過）
- `.env.example` が揃っている

**依存関係**: CC-001, CC-002  
**見積もり**: 1h

---

### [CC-004] Claude Code向け — Flyway マイグレーション設計
**概要**: `docs/ERD.md` のスキーマを Flyway マイグレーションファイルに変換する  
**完了条件**:
- `V1__init_schema.sql` で全テーブル・インデックス・Enum を作成できる
- `V2__seed_data.sql` でローカル開発用初期データ（ユーザー 3 名・チーム 2 つ・サービス 3 件）が投入される
- `./mvnw spring-boot:run` 起動時に自動適用される

**依存関係**: CC-001, CC-003  
**見積もり**: 1.5h

---

### [CC-005] Claude Code向け — GitHub Actions CI パイプライン
**概要**: `.github/workflows/ci.yml` を実装する  
**完了条件**:
- Push 時に frontend lint/type-check/test が実行される
- Push 時に backend `mvn verify` が PostgreSQL service container 付きで実行される
- main ブランチへの merge 時にビルド成功通知が出る

**依存関係**: CC-001, CC-002  
**見積もり**: 1h

---

## Phase 1: 認証・認可

### [CC-006] Claude Code向け — JWT 認証基盤（Spring Security）
**概要**: JwtAuthFilter・JwtTokenProvider・CustomUserDetailsService・SecurityConfig を実装する  
**完了条件**:
- `POST /api/v1/auth/login` でアクセストークン + リフレッシュトークンが返る
- 無効トークンで保護エンドポイントに 401 が返る
- `@PreAuthorize("hasRole('ADMIN')")` が機能する
- 単体テストが 80% カバレッジ

**依存関係**: CC-004  
**見積もり**: 3h

---

### [CDX-001] Codex向け — POST /api/v1/auth/login エンドポイント実装
**概要**: `AuthController`, `AuthService`, `AuthRequest`/`AuthResponse` DTO の実装  
**仕様**: `docs/API.md` の認証セクション参照  
**完了条件**:
- 正しい認証情報で 200 + トークン返却
- 不正な認証情報で 401 返却
- `@SpringBootTest` による統合テスト

**依存関係**: CC-006  
**見積もり**: 1.5h

---

### [CDX-002] Codex向け — フロントエンド認証フロー
**概要**: ログインページ・認証コンテキスト・トークン管理・プロテクトルートの実装  
**完了条件**:
- `/login` でメール・パスワード入力 → バックエンドに POST
- トークンを `httpOnly cookie` または `sessionStorage` に保存
- 未認証状態で `/` にアクセスすると `/login` にリダイレクト
- ログアウトボタンでトークン削除 → `/login` にリダイレクト

**依存関係**: CDX-001, CC-002  
**見積もり**: 2h

---

## Phase 2: サービス台帳

### [CDX-003] Codex向け — サービス台帳 REST API (CRUD)
**概要**: `ServiceController`, `ServiceService`, `ServiceRepository`, `Service` エンティティ、DTO 実装  
**仕様**: `docs/API.md` の Services セクション参照  
**完了条件**:
- GET / POST / PUT / DELETE `/api/v1/services` が動作する
- ページネーション・フィルタリング（status, tier, teamId）が動作する
- バリデーションエラー時に 400 が返る
- 統合テスト（ServiceControllerTest）が存在する

**依存関係**: CC-004, CC-006  
**見積もり**: 2h

---

### [CDX-004] Codex向け — サービス台帳 UI（一覧・詳細・フォーム）
**概要**: `/services` ページのサービス一覧テーブル・詳細ページ・作成/編集フォームの実装  
**完了条件**:
- ステータスバッジ（色分け）が表示される
- ティア・ステータスでフィルタリングできる
- モーダルまたは別ページで作成・編集フォームが開く
- ADMIN / OPERATOR のみ作成・編集ボタンが表示される

**依存関係**: CDX-002, CDX-003  
**見積もり**: 3h

---

## Phase 3: インシデント管理

### [CDX-005] Codex向け — インシデント管理 REST API (CRUD + ステータス変更)
**概要**: `IncidentController`, `IncidentService`, `IncidentRepository`, `Incident` エンティティ、DTO 実装  
**仕様**: `docs/API.md` の Incidents セクション参照  
**完了条件**:
- GET / POST `/api/v1/incidents` が動作する
- `PATCH /api/v1/incidents/{id}/status` でステータス変更 + 対応履歴自動記録
- `PATCH /api/v1/incidents/{id}/assignee` で担当者変更
- `resolved_at` から MTTR 計算ロジックがある
- フィルタリング（status, severity, serviceId）が動作する

**依存関係**: CDX-003, CC-006  
**見積もり**: 3h

---

### [CDX-006] Codex向け — 対応履歴 API
**概要**: `IncidentUpdateController`, `IncidentUpdate` エンティティ・DTO の実装  
**完了条件**:
- `GET /api/v1/incidents/{id}/updates` で時系列リスト取得
- `POST /api/v1/incidents/{id}/updates` でコメント追加
- ステータス変更・担当者変更時に自動的に履歴レコードが作成される

**依存関係**: CDX-005  
**見積もり**: 1.5h

---

### [CC-007] Claude Code向け — インシデント一覧・詳細 UI（設計考慮あり）
**概要**: インシデントの一覧テーブル、詳細ページ（対応履歴タイムライン含む）の実装  
**完了条件**:
- `/incidents` で一覧表示（ステータス・重大度・担当者でフィルタ）
- `/incidents/[id]` で詳細表示（サービス情報・タイムライン）
- タイムラインはスクロール可能な縦型レイアウト
- コメント投稿フォームが末尾にある
- 重大度 P1 は赤、P2 はオレンジ等の視覚的区別

**依存関係**: CDX-005, CDX-006, CDX-002  
**見積もり**: 3h

---

### [CDX-007] Codex向け — インシデント起票フォーム
**概要**: インシデント新規作成モーダルフォームの実装  
**完了条件**:
- タイトル・説明・サービス選択・重大度・発生日時を入力できる
- バリデーション（タイトル必須、重大度必須）
- 作成成功後に一覧を更新する

**依存関係**: CC-007  
**見積もり**: 1.5h

---

## Phase 4: ダッシュボード

### [CDX-008] Codex向け — ダッシュボード API
**概要**: `DashboardController`, `DashboardService` の実装  
**仕様**: `docs/API.md` の Dashboard セクション参照  
**完了条件**:
- `GET /api/v1/dashboard/summary` でサマリーが返る
- `GET /api/v1/dashboard/incidents/trend` で日別集計が返る
- クエリが N+1 問題を起こしていない（JPA で JOIN FETCH か QueryDSL 使用）

**依存関係**: CDX-005  
**見積もり**: 2h

---

### [CC-008] Claude Code向け — ダッシュボード UI（グラフ・ウィジェット設計）
**概要**: ダッシュボードページの KPI ウィジェット群・トレンドチャートの実装  
**完了条件**:
- オープンインシデント数（重大度別）の数値カードが表示される
- 発火中アラート数（重大度別）の数値カードが表示される
- サービス稼働状況の内訳バーが表示される
- 直近 30 日のインシデント件数折れ線グラフが表示される（Recharts）
- データは 30 秒ごとに自動更新される（SWR の refreshInterval）

**依存関係**: CDX-008, CDX-002  
**見積もり**: 3h

---

## Phase 5: 品質・仕上げ

### [CC-009] Claude Code向け — バックエンド例外ハンドリング統一
**概要**: `GlobalExceptionHandler` (@RestControllerAdvice) で全例外を統一レスポンス形式に変換  
**完了条件**:
- `docs/API.md` の共通エラーレスポンス形式に準拠
- バリデーションエラーのフィールド別詳細が返る
- 未知例外でスタックトレースが漏洩しない

**依存関係**: CDX-001 〜 CDX-008  
**見積もり**: 1.5h

---

### [CDX-009] Codex向け — OpenAPI (Swagger) ドキュメント整備
**概要**: springdoc-openapi を使い全エンドポイントに @Operation / @ApiResponse アノテーションを付与  
**完了条件**:
- `http://localhost:8080/swagger-ui.html` で全エンドポイントが表示される
- Bearer 認証の設定がある
- 主要エンドポイントのリクエスト/レスポンスサンプルが表示される

**依存関係**: CC-009  
**見積もり**: 1.5h

---

### [CC-010] Claude Code向け — Terraform AWS 基盤構築
**概要**: `infra/terraform/` の VPC・ECS・RDS・ALB モジュールを実装する  
**完了条件**:
- `terraform plan` がエラーなく実行できる
- dev 環境の ECS Fargate + RDS (t3.micro) が構築できる
- Secrets Manager に JWT 秘密鍵・DB パスワードが格納される
- `terraform destroy` で全リソースが削除できる

**依存関係**: CC-003  
**見積もり**: 4h

---

## タスク依存グラフ（MVP）

```
CC-001 ─┬─ CC-003 ─── CC-004 ─┬─ CC-006 ─── CDX-001 ─── CDX-002
         │                    │
CC-002 ─┘                    └─ CDX-003 ─── CDX-004
                                          └─ CDX-005 ─┬─ CDX-006 ─── CC-007 ─── CDX-007
                                                      └─ CDX-008 ─── CC-008

CC-001 + CC-002 ──── CC-005 (CI)
```

---

## 優先実装順序（推奨）

| 順番 | タスク | 理由 |
|------|--------|------|
| 1 | CC-001, CC-002 | 並列実施可。以降の全タスクの前提 |
| 2 | CC-003, CC-004 | DB スキーマが固まると設計ブレが減る |
| 3 | CC-006, CDX-001 | 認証基盤なしに API テストができない |
| 4 | CDX-002 | 認証済みフロントエンドの土台 |
| 5 | CDX-003, CDX-004 | サービス台帳はシンプルで学習コスト低い |
| 6 | CDX-005, CDX-006 | コアビジネスロジック |
| 7 | CC-007, CDX-007 | フロント最重要ページ |
| 8 | CDX-008, CC-008 | ダッシュボードで全体を可視化 |
| 9 | CC-009, CDX-009 | 品質・ドキュメント整備 |
| 10 | CC-010 | AWS デプロイ（最後に一気に） |
