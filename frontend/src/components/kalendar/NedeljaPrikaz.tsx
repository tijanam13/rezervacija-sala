import { getStatusStyle } from "@/lib/statusColors";
import { nazivSvrhe, formatVreme } from "@/lib/svrhaHelpers";
import { toISODatum, jeIstiDan, pocetakNedelje } from "@/lib/kalendarDatumi";
import type { TerminPodaci } from "@/lib/kalendarTipovi";
import type { SalaDto } from "@/types";

interface NedeljaPrikazProps {
  datum: Date;
  danas: Date;
  terminiState: TerminPodaci[];
  saleFiltrirane: SalaDto[];
  jePrikazljiv: (t: TerminPodaci) => boolean;
  onIzaberiTermin: (t: TerminPodaci) => void;
}

export function NedeljaPrikaz({
  datum,
  danas,
  terminiState,
  saleFiltrirane,
  jePrikazljiv,
  onIzaberiTermin,
}: NedeljaPrikazProps) {
  return (
    <div className="grid grid-cols-7 gap-2.5">
      {Array.from({ length: 7 }, (_, i) => {
        const dan = new Date(pocetakNedelje(datum));
        dan.setDate(dan.getDate() + i);
        const isoDan = toISODatum(dan);
        const terminiDana = terminiState.filter(
          (t) =>
            t.datum === isoDan &&
            jePrikazljiv(t) &&
            saleFiltrirane.some((s) => s.naziv === t.salaNaziv),
        );
        const jeDanas = jeIstiDan(dan, danas);

        return (
          <div
            key={i}
            className={`min-h-[230px] rounded-xl border bg-white p-2.5 shadow-sm ${
              jeDanas
                ? "border-fon-teal ring-1 ring-fon-teal/30"
                : "border-gray-100"
            }`}
          >
            <p
              className={`mb-2 border-b pb-2 text-center text-xs font-medium ${jeDanas ? "border-fon-teal/30 text-fon-teal" : "border-gray-100 text-fon-dark"}`}
            >
              {dan.toLocaleDateString("sr-Latn-RS", { weekday: "short" })}
              <br />
              <span className={jeDanas ? "text-base font-semibold" : ""}>
                {dan.getDate()}
              </span>
            </p>
            <div className="flex flex-col gap-1">
              {terminiDana
                .sort((a, b) => a.vremeOd - b.vremeOd)
                .map((t) => {
                  const stil = getStatusStyle(t.status);
                  return (
                    <button
                      key={t.id}
                      onClick={() => onIzaberiTermin(t)}
                      title={`${formatVreme(t.vremeOd)}-${formatVreme(t.vremeDo)} ${nazivSvrhe(t.svrha)}`}
                      className={`truncate rounded-md border-2 p-1 text-left text-[10px] outline-none transition-transform hover:scale-[1.03] focus-visible:ring-2 focus-visible:ring-fon-navy focus-visible:ring-offset-1 ${stil.bg} ${stil.border} ${stil.text}`}
                    >
                      {formatVreme(t.vremeOd)} {nazivSvrhe(t.svrha)}
                    </button>
                  );
                })}
            </div>
          </div>
        );
      })}
    </div>
  );
}
