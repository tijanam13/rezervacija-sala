import type { ReactNode } from "react";

interface AuthLayoutProps {
  naslov: string;
  podnaslov?: string;
  children: ReactNode;
}

export function AuthLayout({ naslov, podnaslov, children }: AuthLayoutProps) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-md overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-lg">
        <div className="fon-gradient flex flex-col items-center gap-2 px-8 py-8 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-fon-teal text-lg font-bold text-fon-navy">
            ФОН
          </div>
          <h1 className="text-lg font-semibold text-white">Rezervacija sala</h1>
        </div>
        <div className="p-8">
          <h2 className="mb-1 text-xl font-semibold text-fon-dark">{naslov}</h2>
          {podnaslov && (
            <p className="mb-6 text-sm text-gray-500">{podnaslov}</p>
          )}
          {children}
        </div>
      </div>
    </div>
  );
}
