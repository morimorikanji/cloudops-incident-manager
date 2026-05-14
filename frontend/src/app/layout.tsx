import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'CloudOps Incident Manager',
  description: 'クラウド運用チーム向けインシデント管理システム',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ja">
      <body>{children}</body>
    </html>
  );
}
