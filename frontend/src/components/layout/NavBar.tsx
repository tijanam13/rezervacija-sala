import { Link } from "react-router-dom";
import { LogOut } from "lucide-react";
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
      <Link to="/pregled-rezervacija" className="flex items-center gap-2.5">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-fon-teal text-sm leading-none font-bold text-fon-navy">
          FON
        </div>
        <span className="text-lg font-semibold text-white">
          Rezervacija sala
        </span>
      </Link>

      <div className="flex items-center gap-5">
        <Link
          to="/moje-rezervacije"
          className="text-base text-white/80 hover:text-white"
        >
          Moje rezervacije
        </Link>
        <Link
          to="/pregled-rezervacija"
          className="text-base text-white/80 hover:text-white"
        >
          Pregled rezervacija
        </Link>
        <Link
          to="/nova-rezervacija"
          className="text-base text-white/80 hover:text-white"
        >
          Nova rezervacija
        </Link>
        {(koordinator || admin) && (
          <Link
            to="/odobravanje-rezervacija"
            className="text-base text-white/80 hover:text-white"
          >
            Odobravanje rezervacija
          </Link>
        )}
        {admin && (
          <Link
            to="/admin/korisnici"
            className="text-base text-white/80 hover:text-white"
          >
            Korisnici
          </Link>
        )}
        {admin && (
          <Link
            to="/admin/sale"
            className="text-base text-white/80 hover:text-white"
          >
            Sale
          </Link>
        )}
        {admin && (
          <Link
            to="/admin/sifarnici"
            className="text-base text-white/80 hover:text-white"
          >
            Katedre i službe
          </Link>
        )}
        {admin && (
          <Link
            to="/admin/zaposleni"
            className="text-base text-white/80 hover:text-white"
          >
            Zaposleni
          </Link>
        )}
        <Link
          to="/moj-profil"
          className="text-base text-white/80 hover:text-white"
        >
          Moj profil
        </Link>

        <div className="flex items-center gap-2">
          {bedzUloge && (
            <span className="rounded-full bg-white/15 px-2.5 py-1 text-xs font-medium text-white">
              {bedzUloge}
            </span>
          )}
          <Link
            to="/moj-profil"
            className="flex h-9 w-9 items-center justify-center rounded-full border border-white/30 bg-white/15 text-sm font-medium text-white transition-colors hover:bg-white/25"
            title={punoIme}
          >
            {inicijali}
          </Link>
        </div>

        <button
          onClick={() => odjaviSe()}
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
