import { ChevronRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { getStatusStyle } from "@/lib/statusColors";
import { nazivSvrhe, formatDatum } from "@/lib/svrhaHelpers";
import type { RezervacijaDto } from "@/types";

interface RezervacijaKarticaProps {
  rezervacija: RezervacijaDto;
  onClick: () => void;
  prikaziVlasnika?: boolean;
  pokaziStrelicu?: boolean;
}

export function RezervacijaKartica({
  rezervacija: r,
  onClick,
  prikaziVlasnika = false,
  pokaziStrelicu = false,
}: RezervacijaKarticaProps) {
  const stilRez = getStatusStyle(r.status ?? "NA_CEKANJU");

  return (
    <button
      onClick={onClick}
      className={`flex w-full items-center justify-between rounded-xl border-2 p-5 text-left shadow-sm transition-colors ${stilRez.border} ${stilRez.bg} hover:brightness-95`}
    >
      <div>
        <p className="text-lg font-semibold text-fon-navy">
          {nazivSvrhe(r.svrha)}
        </p>
        <p className="text-base text-gray-600">
          {prikaziVlasnika && r.korisnik && (
            <>
              {r.korisnik.ime} {r.korisnik.prezime} ·{" "}
            </>
          )}
          {r.stavke.length}{" "}
          {r.stavke.length === 1 ? "sala/termin" : "sale, isti termin"}
          {" · "}
          {formatDatum(r.datumTermina)}
        </p>
      </div>
      <div className="flex items-center gap-3">
        <Badge className={`${stilRez.bg} ${stilRez.text} border-0`}>
          {stilRez.label}
        </Badge>
        {pokaziStrelicu && <ChevronRight size={18} className="text-gray-400" />}
      </div>
    </button>
  );
}
