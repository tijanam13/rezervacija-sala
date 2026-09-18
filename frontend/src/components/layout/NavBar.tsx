import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { LogOut, ChevronDown } from "lucide-react";
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

  const [adminMenuOtvoren, setAdminMenuOtvoren] = useState(false);
  const adminMenuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function naKlikVan(e: MouseEvent) {
      if (
        adminMenuRef.current &&
        !adminMenuRef.current.contains(e.target as Node)
      ) {
        setAdminMenuOtvoren(false);
      }
    }
    document.addEventListener("mousedown", naKlikVan);
    return () => document.removeEventListener("mousedown", naKlikVan);
  }, []);

  return (
    <div className="fon-gradient flex items-center justify-between px-6 py-3">
      <Link to="/pregled-rezervacija" className="flex items-center gap-2.5">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-fon-teal text-sm leading-none font-bold text-fon-navy">
          ФОН
        </div>
        <span className="text-lg font-semibold text-white">
          Rezervacija sala
        </span>
      </Link>

      <div className="flex items-center gap-5">
        <Link
          to="/moje-rezervacije"
          className="text-base whitespace-nowrap text-white/80 hover:text-white"
        >
          Moje rezervacije
        </Link>
        <Link
          to="/pregled-rezervacija"
          className="text-base whitespace-nowrap text-white/80 hover:text-white"
        >
          Pregled rezervacija
        </Link>
        <Link
          to="/nova-rezervacija"
          className="text-base whitespace-nowrap text-white/80 hover:text-white"
        >
          Nova rezervacija
        </Link>
        {(koordinator || admin) && (
          <Link
            to="/odobravanje-rezervacija"
            className="text-base whitespace-nowrap text-white/80 hover:text-white"
          >
            Odobravanje rezervacija
          </Link>
        )}
        {admin && (
          <div className="relative" ref={adminMenuRef}>
            <button
              onClick={() => setAdminMenuOtvoren((v) => !v)}
              className="flex items-center gap-1 text-base whitespace-nowrap text-white/80 hover:text-white"
            >
              Administracija
              <ChevronDown
                size={16}
                className={`transition-transform ${adminMenuOtvoren ? "rotate-180" : ""}`}
              />
            </button>
            {adminMenuOtvoren && (
              <div className="absolute top-full left-0 z-20 mt-2 w-48 overflow-hidden rounded-lg border border-fon-navy/20 bg-fon-navy py-1 shadow-lg">
                <Link
                  to="/admin/korisnici"
                  onClick={() => setAdminMenuOtvoren(false)}
                  className="block px-4 py-2 text-sm text-white/85 hover:bg-white/10 hover:text-white"
                >
                  Korisnici
                </Link>
                <Link
                  to="/admin/sale"
                  onClick={() => setAdminMenuOtvoren(false)}
                  className="block px-4 py-2 text-sm text-white/85 hover:bg-white/10 hover:text-white"
                >
                  Sale
                </Link>
                <Link
                  to="/admin/sifarnici"
                  onClick={() => setAdminMenuOtvoren(false)}
                  className="block px-4 py-2 text-sm text-white/85 hover:bg-white/10 hover:text-white"
                >
                  Katedre i službe
                </Link>
                <Link
                  to="/admin/zaposleni"
                  onClick={() => setAdminMenuOtvoren(false)}
                  className="block px-4 py-2 text-sm text-white/85 hover:bg-white/10 hover:text-white"
                >
                  Zaposleni
                </Link>
              </div>
            )}
          </div>
        )}
        <Link
          to="/moj-profil"
          className="text-base whitespace-nowrap text-white/80 hover:text-white"
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
