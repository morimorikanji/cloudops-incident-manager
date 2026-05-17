-- V2: ローカル開発用初期データ
-- パスワード: Admin1234! / Operator1234! / Viewer1234!

INSERT INTO teams (id, name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Platform Team',    'インフラ・プラットフォーム担当チーム'),
    ('22222222-2222-2222-2222-222222222222', 'Application Team', 'アプリケーション開発担当チーム');

INSERT INTO users (id, email, password_hash, display_name, role) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'admin@example.com',
     '$2b$10$NubqW.ex/NYDkJ9DH5Y1F.RRg3Q7W0SrqCO/g7qmwYDPvcN9F.14.',
     'Admin User',    'ADMIN'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'operator@example.com',
     '$2b$10$uzKlwx0Pvf3gjtGyhLa9Cuo92f.o2D8eejQy0K.CXssI85ropVHRO',
     'Operator User', 'OPERATOR'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc',
     'viewer@example.com',
     '$2b$10$GQoxS29Gh9ZDScTtnBw/jue1rzkXKHDbd9Og9uzfnHX8.f8apPTn2',
     'Viewer User',   'VIEWER');

INSERT INTO user_teams (user_id, team_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222');

INSERT INTO services (name, description, team_id, tier, status, endpoint_url) VALUES
    ('API Gateway',
     'エントリポイント API ゲートウェイ',
     '11111111-1111-1111-1111-111111111111',
     'TIER1', 'OPERATIONAL',
     'https://api.example.com'),
    ('Auth Service',
     '認証・認可サービス',
     '11111111-1111-1111-1111-111111111111',
     'TIER1', 'OPERATIONAL',
     'https://auth.example.com'),
    ('Notification Service',
     '通知配信サービス',
     '22222222-2222-2222-2222-222222222222',
     'TIER2', 'OPERATIONAL',
     'https://notify.example.com');
