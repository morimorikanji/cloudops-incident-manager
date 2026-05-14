# CLAUDE.md — CloudOps Incident Manager

Claude Code がこのプロジェクトで作業する際の設計方針・規約です。

## プロジェクト概要

クラウド運用チーム向けインシデント管理 Web アプリ。
- **Frontend**: Next.js 14 (App Router) + TypeScript
- **Backend**: Java 21 + Spring Boot 3.x
- **DB**: PostgreSQL 16 (Flyway マイグレーション)
- **Auth**: JWT + RBAC (ADMIN / OPERATOR / VIEWER)

詳細は `docs/ARCHITECTURE.md` を参照。

---

## ディレクトリ構成

```
cloudops-incident-manager/
├── frontend/               # Next.js アプリ
│   ├── src/app/            # App Router ページ
│   ├── src/components/     # UI コンポーネント
│   ├── src/hooks/          # カスタムフック
│   ├── src/lib/            # API クライアント・ユーティリティ
│   └── src/types/          # TypeScript 型定義
├── backend/
│   └── src/main/java/com/cloudops/incidentmanager/
│       ├── controller/     # REST コントローラ
│       ├── service/        # ビジネスロジック
│       ├── repository/     # Spring Data JPA
│       ├── model/          # JPA エンティティ
│       ├── dto/            # リクエスト/レスポンス DTO
│       ├── security/       # JWT・Spring Security
│       └── config/         # Bean 設定
├── infra/terraform/        # AWS インフラ定義
├── docs/                   # 設計ドキュメント
└── .github/workflows/      # CI/CD
```

---

## よく使うコマンド

### フロントエンド

```bash
cd frontend
npm install          # 依存インストール
npm run dev          # 開発サーバー起動 (:3000)
npm run build        # プロダクションビルド
npm run lint         # ESLint
npm run type-check   # TypeScript 型チェック
npm run test         # Vitest
```

### バックエンド

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local   # 起動
./mvnw test                                                 # 全テスト
./mvnw verify                                               # 統合テスト含む
./mvnw spotbugs:check                                       # 静的解析
```

### インフラ

```bash
cd infra/terraform/environments/dev
terraform init
terraform plan
terraform apply
terraform destroy
```

### Docker Compose

```bash
docker compose up -d         # バックグラウンド起動
docker compose logs -f       # ログ追跡
docker compose down          # 停止
docker compose down -v       # 停止 + ボリューム削除
```

---

## バックエンド実装規約

### パッケージ名
`com.cloudops.incidentmanager`

### エンティティ
- すべての ID は `UUID` (PostgreSQL の `gen_random_uuid()`)
- `created_at` / `updated_at` は `@CreationTimestamp` / `@UpdateTimestamp`
- Enum は DB では `VARCHAR`、Java では `@Enumerated(EnumType.STRING)`

### DTO
- Java 21 の **Record** を使用する
- リクエスト DTO: `XxxRequest`, レスポンス DTO: `XxxResponse`
- バリデーションは `javax.validation` アノテーション

### サービス層
- `@Transactional` はサービス層に付ける（コントローラには付けない）
- ビジネス例外は `com.cloudops.incidentmanager.exception` に定義

### テスト
- 単体テスト: JUnit 5 + Mockito
- 統合テスト: `@SpringBootTest` + Testcontainers (PostgreSQL)
- カバレッジ目標: サービス層 80% 以上

---

## フロントエンド実装規約

### コンポーネント設計
- `src/components/ui/` に shadcn/ui ベースの汎用コンポーネント
- 機能ドメイン別に `src/components/incidents/`, `src/components/services/` を分ける
- Server Components を優先し、インタラクションが必要な部分のみ `"use client"` を付ける

### データフェッチ
- SWR を使用: `const { data, error, isLoading } = useSWR('/api/v1/incidents', fetcher)`
- API クライアントは `src/lib/api.ts` に集約する
- エラーハンドリングは `src/components/ui/ErrorBoundary` で統一

### 型定義
- API レスポンスの型は `src/types/api.ts` に定義する
- バックエンドの DTO 名に対応させる（例: `IncidentResponse`）

### 認証
- JWT アクセストークンは `sessionStorage` に保存
- リフレッシュトークンは `httpOnly cookie` に保存（XSS 対策）
- 認証状態は `src/lib/auth.ts` の React Context で管理

---

## 設計上の制約

- **ロール制御の徹底**: フロントエンドでもロールに応じて UI を出し分ける（ただし、真のセキュリティはバックエンドで担保）
- **ページネーション必須**: 一覧系 API は必ず `page`, `size` パラメータを持つ
- **論理削除**: サービス・インシデントは物理削除しない（`deleted_at` カラムを追加するか、ステータスを `CLOSED` にする）
- **N+1 防止**: JPA の関連エンティティは `JOIN FETCH` または `@EntityGraph` で明示的にフェッチする

---

## 実装タスクの参照先

`docs/TASKS.md` に MVP タスクが `[CC-XXX]`（Claude Code 向け）と `[CDX-XXX]`（Codex 向け）に分類されています。
作業前にタスク ID と完了条件を確認してください。

---

## Git ブランチ運用

```
main          ← プロダクション相当（保護ブランチ）
develop       ← 開発統合ブランチ
feature/CC-XXX-task-name   ← Claude Code 作業ブランチ
feature/CDX-XXX-task-name  ← Codex 作業ブランチ
```

コミットメッセージ形式: `feat(CC-006): JWT認証フィルター実装`
