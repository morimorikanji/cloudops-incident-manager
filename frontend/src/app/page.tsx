export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-gray-950 text-white">
      <div className="text-center">
        <h1 className="text-4xl font-bold tracking-tight text-white">
          CloudOps Incident Manager
        </h1>
        <p className="mt-4 text-lg text-gray-400">
          クラウド運用チーム向けインシデント管理システム
        </p>
        <div className="mt-8 flex gap-4 justify-center">
          <span className="rounded-full bg-green-900 px-3 py-1 text-sm text-green-300">
            API: 起動中
          </span>
          <span className="rounded-full bg-blue-900 px-3 py-1 text-sm text-blue-300">
            開発中
          </span>
        </div>
      </div>
    </main>
  );
}
