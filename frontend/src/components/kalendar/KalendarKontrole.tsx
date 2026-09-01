import { ChevronLeft, ChevronRight } from "lucide-react";
import { formatNaslov } from "@/lib/kalendarDatumi";

type Prikaz = "dan" | "nedelja" | "mesec";

interface KalendarKontroleProps {
  prikaz: Prikaz;
  setPrikaz: (p: Prikaz) => void;
  datum: Date;
  onPromeniDan: (delta: number) => void;
  onDanas: () => void;
}

export function KalendarKontrole({
  prikaz,
  setPrikaz,
  datum,
  onPromeniDan,
  onDanas,
}: KalendarKontroleProps) {
  return (
    <div className="mb-5 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-gray-100 bg-white p-3 shadow-sm">
      <div className="flex items-center gap-1 rounded-lg bg-gray-50 p-1">
        {(["dan", "nedelja", "mesec"] as const).map((opcija) => (
          <button
            key={opcija}
            onClick={() => setPrikaz(opcija)}
            className={`rounded-md px-3.5 py-1.5 text-base capitalize transition-colors ${
              prikaz === opcija
                ? "bg-fon-navy font-medium text-white shadow-sm"
                : "text-gray-600 hover:text-fon-navy"
            }`}
          >
            {opcija}
          </button>
        ))}
      </div>

      <div className="flex items-center gap-3">
        <button
          onClick={() => onPromeniDan(-1)}
          aria-label="Prethodni"
          className="flex h-9 w-9 items-center justify-center rounded-full text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
        >
          <ChevronLeft size={22} />
        </button>
        <span className="min-w-[240px] text-center text-lg font-medium capitalize text-fon-navy">
          {formatNaslov(datum, prikaz)}
        </span>
        <button
          onClick={() => onPromeniDan(1)}
          aria-label="Sledeći"
          className="flex h-9 w-9 items-center justify-center rounded-full text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
        >
          <ChevronRight size={22} />
        </button>
        <button
          onClick={onDanas}
          className="rounded-md border border-gray-200 px-4 py-2 text-base font-medium text-gray-600 hover:border-fon-teal hover:text-fon-navy"
        >
          Danas
        </button>
      </div>
    </div>
  );
}
