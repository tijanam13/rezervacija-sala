import { getStatusStyle } from "@/lib/statusColors";
import { nazivSvrhe, formatVreme } from "@/lib/svrhaHelpers";
import { toISODatum, jeIstiDan, daniMeseca } from "@/lib/kalendarDatumi";
import type { TerminPodaci } from "@/lib/kalendarTipovi";
import type { SalaDto } from "@/types";

interface MesecPrikazProps {
  datum: Date;
  danas: Date;
  terminiState: TerminPodaci[];
  saleFiltrirane: SalaDto[];
  jePrikazljiv: (t: TerminPodaci) => boolean;
  onIzaberiDan: (dan: Date) => void;
}

export function MesecPrikaz({
  datum,
  danas,
  terminiState,
  saleFiltrirane,
  jePrikazljiv,
  onIzaberiDan,
}: MesecPrikazProps) {
  return (
    <div className="overflow-hidden rounded-2xl border border-gray-100 bg-white p-3 shadow-sm">
      <div className="grid grid-cols-7 gap-px bg-gray-50 pb-2">
        {["Pon", "Uto", "Sre", "Čet", "Pet", "Sub", "Ned"].map((d) => (
          <p
            key={d}
            className="bg-white text-center text-xs font-semibold text-gray-600"
          >
            {d}
          </p>
        ))}
      </div>
      <div className="grid grid-cols-7 gap-px bg-gray-100">
        {daniMeseca(datum).map((dan, i) => {
          if (!dan) return <div key={i} className="bg-white" />;
          const isoDan = toISODatum(dan);
          const terminiDana = terminiState.filter(
            (t) =>
              t.datum === isoDan &&
              jePrikazljiv(t) &&
              saleFiltrirane.some((s) => s.naziv === t.salaNaziv),
          );
          const jeDanas = jeIstiDan(dan, danas);
          const prikazani = terminiDana
            .sort((a, b) => a.vremeOd - b.vremeOd)
            .slice(0, 2);
          const josIma = terminiDana.length - prikazani.length;

          return (
            <button
              key={i}
              onClick={() => onIzaberiDan(dan)}
              className={`flex min-h-[86px] flex-col items-start gap-1 bg-white p-2 text-left outline-none transition-colors hover:bg-gray-50 focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-fon-navy ${
                jeDanas ? "border-2 border-fon-teal bg-fon-teal/[0.06]" : ""
              }`}
            >
              <span
                className={`flex h-6 w-6 items-center justify-center rounded-full text-xs ${
                  jeDanas
                    ? "bg-fon-teal font-semibold text-fon-dark"
                    : "text-fon-dark"
                }`}
              >
                {dan.getDate()}
              </span>
              <div className="flex w-full flex-col gap-0.5">
                {prikazani.map((t) => {
                  const stil = getStatusStyle(t.status);
                  return (
                    <span
                      key={t.id}
                      title={`${formatVreme(t.vremeOd)}-${formatVreme(t.vremeDo)} ${nazivSvrhe(t.svrha)}`}
                      className={`truncate rounded px-1 py-0.5 text-[9px] font-medium ${stil.bg} ${stil.text}`}
                    >
                      {formatVreme(t.vremeOd)} {nazivSvrhe(t.svrha)}
                    </span>
                  );
                })}
                {josIma > 0 && (
                  <span className="text-[9px] font-medium text-gray-500">
                    +{josIma} više
                  </span>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
