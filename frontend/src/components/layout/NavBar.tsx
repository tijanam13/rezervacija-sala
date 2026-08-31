import { Link } from "react-router-dom";
import { LogOut, ShieldCheck } from "lucide-react";
import { odjaviSe, preuzmiKorisnika, jeAdmin, jeKoordinator } from "@/lib/auth";

interface NavBarProps {
  korisnikIme?: string;
}

export function NavBar({ korisnikIme }: NavBarProps) {
  const korisnik = preuzmiKorisnika();
  const punoIme =
    korisnikIme ??
    (korisnik ? `${korisnik.ime} ${korisnik.prezime}` : undefined);

  const inicijali = korisnik
    ? `${korisnik.ime[0] ?? ""}${korisnik.prezime[0] ?? ""}`.toUpperCase()
    : "?";

  const admin = jeAdmin();
  const koordinator = jeKoordinator();
  const bedzUloge = admin
    ? "Administrator"
    : koordinator
      ? "Koordinator"
      : null;

  return (
    <div className="fon-gradient flex items-center justify-between px-6 py-3">
      <div className="flex items-center gap-2.5">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-fon-teal text-xs font-medium text-fon-navy">
          FON
        </div>
        <span className="text-sm font-medium text-white">Rezervacija sala</span>
      </div>

      <div className="flex items-center gap-5">
        <Link
          to="/moje-rezervacije"
          className="text-base text-white/80 hover:text-white"
        >
          Moje rezervacije
        </Link>
        <Link
          to="/nova-rezervacija"
          className="text-base text-white/80 hover:text-white"
        >
          Nova rezervacija
        </Link>
        <Link
          to="/moj-profil"
          className="text-base text-white/80 hover:text-white"
        >
          Moj profil
        </Link>
        {admin && (
          <Link
            to="/admin/korisnici"
            className="flex items-center gap-1.5 text-base text-white/80 hover:text-white"
          >
            <ShieldCheck size={16} />
            Admin panel
          </Link>
        )}

        <div className="flex items-center gap-2">
          {bedzUloge && (
            <span className="rounded-full bg-white/15 px-2.5 py-1 text-xs font-medium text-white">
              {bedzUloge}
            </span>
          )}
          <div
            className="flex h-9 w-9 items-center justify-center rounded-full border border-white/30 bg-white/15 text-sm font-medium text-white"
            title={punoIme}
          >
            {inicijali}
          </div>
        </div>

        <button
          onClick={odjaviSe}
          aria-label="Odjavi se"
          title="Odjavi se"
          className="flex h-9 w-9 items-center justify-center rounded-full bg-fon-coral/15 text-fon-coral hover:bg-fon-coral/25"
        >
          <LogOut size={20} />
        </button>
      </div>
    </div>
  );
}
