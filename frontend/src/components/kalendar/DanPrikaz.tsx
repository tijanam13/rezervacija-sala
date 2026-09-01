import { useRef } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { getStatusStyle } from "@/lib/statusColors";
import { nazivSvrhe, ikonicaSvrhe, formatVreme } from "@/lib/svrhaHelpers";
import type { TerminPodaci } from "@/lib/kalendarTipovi";
import type { SalaDto } from "@/types";

const POCETNI_SAT = 8;
const REDAK_PX = 52;

interface DanPrikazProps {
  saleFiltrirane: SalaDto[];
  satiNiz: number[];
  terminiDanas: TerminPodaci[];
  onIzaberiTermin: (t: TerminPodaci) => void;
}

export function DanPrikaz({
  saleFiltrirane,
  satiNiz,
  terminiDanas,
  onIzaberiTermin,
}: DanPrikazProps) {
  const skrolRef = useRef<HTMLDivElement>(null);

  return (
    <div className="relative overflow-hidden rounded-2xl border-x border-b border-gray-100 bg-white shadow-sm">
      {saleFiltrirane.length > 3 && (
        <>
          <button
            onClick={() =>
              skrolRef.current?.scrollBy({ left: -200, behavior: "smooth" })
            }
            aria-label="Sale levo"
            className="absolute top-1 left-[62px] z-20 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-fon-navy shadow-sm hover:bg-gray-50"
          >
            <ChevronLeft size={18} />
          </button>
          <button
            onClick={() =>
              skrolRef.current?.scrollBy({ left: 200, behavior: "smooth" })
            }
            aria-label="Sale desno"
            className="absolute top-1 right-1 z-20 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-fon-navy shadow-sm hover:bg-gray-50"
          >
            <ChevronRight size={18} />
          </button>
        </>
      )}
      <div ref={skrolRef} className="bez-skrol-trake overflow-x-auto">
        <div
          className="grid"
          style={{
            gridTemplateColumns: `60px repeat(${saleFiltrirane.length}, minmax(180px, 1fr))`,
            height: 44,
          }}
        >
          <div className="sticky left-0 z-10 flex items-center justify-center border-b-2 border-b-gray-300 bg-white text-xs font-medium text-gray-600">
            Vreme
          </div>
          {saleFiltrirane.map((s) => (
            <div
              key={s.naziv}
              className="flex items-center justify-center border-b-2 border-l border-l-gray-200 border-b-gray-300 text-sm font-semibold text-fon-dark"
            >
              {s.naziv}
            </div>
          ))}
        </div>

        <div
          className="relative grid"
          style={{
            gridTemplateColumns: `60px repeat(${saleFiltrirane.length}, minmax(180px, 1fr))`,
            gridTemplateRows: `repeat(${satiNiz.length}, ${REDAK_PX}px)`,
          }}
        >
          {saleFiltrirane.map((s, i) => (
            <div
              key={`kolona-${s.naziv}`}
              className="border-l border-l-gray-100"
              style={{
                gridColumn: i + 2,
                gridRow: `1 / span ${satiNiz.length}`,
              }}
            />
          ))}

          {satiNiz.slice(1).map((sat, i) => (
            <div
              key={`linija-${sat}`}
              className="pointer-events-none border-t border-gray-200"
              style={{ gridColumn: "1 / -1", gridRow: i + 2 }}
            />
          ))}

          {satiNiz.map((sat, i) => (
            <span
              key={sat}
              className="sticky left-0 z-10 bg-white pt-0.5 pl-1.5 text-xs font-medium text-gray-600"
              style={{ gridColumn: 1, gridRow: i + 1, alignSelf: "start" }}
            >
              {sat}:00
            </span>
          ))}

          {terminiDanas.map((t) => {
            const kolona = saleFiltrirane.findIndex(
              (s) => s.naziv === t.salaNaziv,
            );
            if (kolona === -1) return null;
            const stil = getStatusStyle(t.status);
            const Ikonica = ikonicaSvrhe(t.svrha.tip);

            const gore = (t.vremeOd - POCETNI_SAT) * REDAK_PX;
            const visina = (t.vremeDo - t.vremeOd) * REDAK_PX;
            return (
              <button
                key={t.id}
                onClick={() => onIzaberiTermin(t)}
                title={`${nazivSvrhe(t.svrha)} (${formatVreme(t.vremeOd)}-${formatVreme(t.vremeDo)})`}
                className={`absolute mx-1.5 flex flex-col items-start overflow-hidden rounded-lg border-2 p-1.5 text-left shadow-[0_1px_2px_rgba(0,0,0,0.04)] outline-none transition-transform hover:scale-[1.02] focus-visible:ring-2 focus-visible:ring-fon-navy focus-visible:ring-offset-1 ${stil.bg} ${stil.border}`}
                style={{
                  gridColumn: `${kolona + 2} / ${kolona + 3}`,
                  top: gore,
                  height: visina,
                  left: 0,
                  right: 0,
                  boxSizing: "border-box",
                }}
              >
                <div className="flex items-center gap-1">
                  <Ikonica size={11} className={stil.text} />
                  <p
                    className={`truncate text-[11px] font-medium ${stil.text}`}
                  >
                    {nazivSvrhe(t.svrha)}
                  </p>
                </div>
                <p className={`text-[10px] ${stil.text} opacity-80`}>
                  {formatVreme(t.vremeOd)}-{formatVreme(t.vremeDo)}
                </p>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
