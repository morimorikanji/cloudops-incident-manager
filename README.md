# CloudOps Incident Manager

[![CI](https://github.com/morikanji/cloudops-incident-manager/actions/workflows/ci.yml/badge.svg)](https://github.com/morikanji/cloudops-incident-manager/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?logo=springboot)
![Next.js](https://img.shields.io/badge/Next.js-14-black?logo=next.js)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Terraform](https://img.shields.io/badge/Terraform-AWS-purple?logo=terraform)

クラウド上で稼働するサービスの運用を想定した、**インシデント管理 Web アプリケーション**です。
サービス台帳・インシデント管理・アラート受信・対応履歴・ダッシュボード・認証/権限管理を一元化し、
SRE/運用チームの障害対応フローを支援します。

---

## 開発目的

本プロジェクトは以下を実証するポートフォリオです。

- **実務レベルのシステム設計**: マイクロサービス指向の REST API + フロントエンド分離構成
- **クラウドネイティブ運用**: AWS ECS/RDS + Terraform による IaC、GitHub Actions CI/CD
- **セキュリティ設計**: JWT 認証 + RBAC による役割ベースアクセス制御
- **エンタープライズ Java**: Spring Boot 3 / Java 21 (Virtual Threads, Records)
- **モダンフロントエンド**: Next.js 14 App Router + TypeScript + Server Components

---

## 主な機能

| 機能 | 概要 |
|------|------|
| **サービス台帳** | サービス一覧・詳細・ステータス管理 (Operational / Degraded / Down) |
| **インシデント管理** | 起票・重大度分類 (P1〜P4)・担当者アサイン・ステータス管理 |
| **アラート受信** | Webhook 経由で Prometheus / CloudWatch アラートを受信・集約 |
| **対応履歴** | インシデントごとのタイムライン・コメント・ステータス変更ログ |
| **ダッシュボード** | 稼働中インシデント数・MTTR・アラートトレンドのサマリービュー |
| **認証・権限管理** | JWT 認証、Admin / Operator / Viewer の 3 ロール RBAC |

---

## 技術スタック

### Frontend
- **Next.js 14** (App Router) + **TypeScript**
- **Tailwind CSS** + **shadcn/ui** コンポーネント
- **SWR** / React Query によるデータフェッチ
- **Recharts** によるグラフ描画

### Backend
- **Java 21** + **Spring Boot 3.x**
- **Spring Security** (JWT フィルター + RBAC)
- **Spring Data JPA** + **Hibernate**
- **Flyway** によるスキーママイグレーション

### Database
- **PostgreSQL 16**

### Infrastructure
- **Docker Compose** (ローカル開発)
- **AWS** (ECS Fargate + RDS PostgreSQL + ALB + ECR)
- **Terraform** による IaC
- **GitHub Actions** CI/CD

---

## アーキテクチャ概要

```
┌─────────────────────────────────────────────────────────────┐
│                         Internet                            │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS
             ┌───────────▼────────────┐
             │   AWS ALB (Load Balancer)│
             └───────┬────────┬───────┘
                     │        │
          ┌──────────▼──┐  ┌──▼──────────┐
          │  Next.js    │  │ Spring Boot  │
          │  (ECS)      │  │  API (ECS)   │
          └─────────────┘  └──────┬───────┘
                                  │ JDBC
                          ┌───────▼───────┐
                          │  PostgreSQL   │
                          │  (RDS)        │
                          └───────────────┘
```

詳細は [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照。

---

## ディレクトリ構成

```
cloudops-incident-manager/
├── frontend/          # Next.js アプリケーション
├── backend/           # Spring Boot アプリケーション
├── infra/
│   └── terraform/     # AWS インフラ定義
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API.md
│   ├── ERD.md
│   └── TASKS.md
├── .github/
│   └── workflows/     # CI/CD パイプライン
├── docker-compose.yml
└── CLAUDE.md
```

---

## ローカル起動方法

### 前提条件

- Docker Desktop 4.x 以上
- Node.js 20 以上
- Java 21 以上

### 手順

```bash
# 1. リポジトリをクローン
git clone https://github.com/morikanji/cloudops-incident-manager.git
cd cloudops-incident-manager

# 2. 環境変数を設定
cp frontend/.env.example frontend/.env.local
cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml

# 3. Docker Compose でバックエンド + DB を起動
docker compose up -d db

# 4. バックエンドを起動
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 5. フロントエンドを起動（別ターミナル）
cd frontend
npm install
npm run dev

# 6. ブラウザでアクセス
open http://localhost:3000
```

### デフォルト認証情報

| ロール | メール | パスワード |
|--------|--------|-----------|
| Admin | admin@example.com | password |
| Operator | operator@example.com | password |
| Viewer | viewer@example.com | password |

---

## API ドキュメント

バックエンド起動後、以下で Swagger UI にアクセスできます。

```
http://localhost:8080/swagger-ui.html
```

REST API 仕様の詳細は [docs/API.md](docs/API.md) を参照。

---

## データベース設計

ER 図は [docs/ERD.md](docs/ERD.md) を参照。

---

## CI/CD パイプライン

```
Push → GitHub Actions
         ├── Lint & Type Check (frontend)
         ├── Unit Test (backend)
         ├── Integration Test (backend + PostgreSQL)
         ├── Build Docker Images
         └── [main branch] Deploy to AWS ECS
```

---

## 今後の拡張予定

- [ ] PagerDuty / Slack 通知連携
- [ ] on-call スケジュール管理
- [ ] SLO/SLI メトリクス可視化
- [ ] 過去インシデントの AI サマリー生成 (Claude API)
- [ ] Kubernetes (EKS) 対応
- [ ] 多言語対応 (i18n)

---

## ライセンス

[MIT](LICENSE)

---

## 作者

**Kanji Mori** — 情報理工学部 3 回生  
SIer / クラウド系企業を志望し、実務レベルの開発経験を積むために本プロジェクトを開発しています。
