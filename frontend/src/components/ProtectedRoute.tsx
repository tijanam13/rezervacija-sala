import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { jeUlogovan, imaUlogu, preuzmiKorisnika } from "@/lib/auth";

interface ProtectedRouteProps {
  children: ReactNode;
  uloge?: Array<"ADMIN" | "KOORDINATOR">;
}

export function ProtectedRoute({ children, uloge }: ProtectedRouteProps) {
  if (!jeUlogovan()) {
    return <Navigate to="/prijava" replace />;
  }

  const korisnik = preuzmiKorisnika();
  if (korisnik?.status === "BLOKIRAN") {
    return <Navigate to="/blokiran" replace />;
  }

  if (uloge && uloge.length > 0 && !uloge.some((uloga) => imaUlogu(uloga))) {
    return <Navigate to="/zabranjen-pristup" replace />;
  }

  return children;
}
