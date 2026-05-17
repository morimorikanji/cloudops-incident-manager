-- V1: 全テーブル・インデックス作成

CREATE TABLE users (
    id            UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255)             NOT NULL UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    display_name  VARCHAR(100)             NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE teams (
    id          UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)             NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE user_teams (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, team_id)
);

CREATE TABLE services (
    id              UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)             NOT NULL UNIQUE,
    description     TEXT,
    team_id         UUID                     REFERENCES teams(id),
    tier            VARCHAR(10)              NOT NULL,
    status          VARCHAR(20)              NOT NULL DEFAULT 'OPERATIONAL',
    endpoint_url    VARCHAR(500),
    repository_url  VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE incidents (
    id          UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255)             NOT NULL,
    description TEXT,
    service_id  UUID                     REFERENCES services(id),
    severity    VARCHAR(5)               NOT NULL,
    status      VARCHAR(20)              NOT NULL DEFAULT 'OPEN',
    assignee_id UUID                     REFERENCES users(id),
    created_by  UUID                     NOT NULL REFERENCES users(id),
    started_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE incident_updates (
    id          UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID                     NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    author_id   UUID                     NOT NULL REFERENCES users(id),
    update_type VARCHAR(20)              NOT NULL,
    message     TEXT                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE alerts (
    id              UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id      UUID                     REFERENCES services(id),
    source          VARCHAR(50)              NOT NULL,
    title           VARCHAR(255)             NOT NULL,
    description     TEXT,
    severity        VARCHAR(20)              NOT NULL,
    status          VARCHAR(20)              NOT NULL DEFAULT 'FIRING',
    incident_id     UUID                     REFERENCES incidents(id),
    fired_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- インデックス
CREATE INDEX idx_incidents_status      ON incidents(status);
CREATE INDEX idx_incidents_severity    ON incidents(severity);
CREATE INDEX idx_incidents_service_id  ON incidents(service_id);
CREATE INDEX idx_incidents_assignee_id ON incidents(assignee_id);
CREATE INDEX idx_incidents_started_at  ON incidents(started_at DESC);

CREATE INDEX idx_alerts_status     ON alerts(status);
CREATE INDEX idx_alerts_severity   ON alerts(severity);
CREATE INDEX idx_alerts_service_id ON alerts(service_id);
CREATE INDEX idx_alerts_fired_at   ON alerts(fired_at DESC);

CREATE INDEX idx_incident_updates_incident_id ON incident_updates(incident_id);
CREATE INDEX idx_services_status              ON services(status);
