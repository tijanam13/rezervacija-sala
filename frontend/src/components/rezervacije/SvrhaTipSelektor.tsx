import {
  BookOpen,
  ClipboardList,
  GraduationCap,
  Users,
  PartyPopper,
} from "lucide-react";
import type { SvrhaRezervacijeDto } from "@/types";

export const NAZIVI_SVRHE: Record<SvrhaRezervacijeDto["tip"], string> = {
  NASTAVA: "Nastava",
  ISPIT: "Ispit",
  ZAVRSNI_RAD: "Završni rad",
  SASTANAK: "Sastanak",
  DOGADJAJ: "Događaj",
};

export const SVRHA_STIL: Record<
  SvrhaRezervacijeDto["tip"],
  {
    ikona: typeof BookOpen;
    aktivnaKartica: string;
    ikonaBg: string;
    akcentText: string;
    akcentBorder: string;
    akcentBg: string;
  }
> = {
  NASTAVA: {
    ikona: BookOpen,
    aktivnaKartica: "border-fon-blue bg-fon-blue/10 ring-2 ring-fon-blue/30",
    ikonaBg: "bg-fon-blue",
    akcentText: "text-fon-blue",
    akcentBorder: "border-fon-blue",
    akcentBg: "bg-fon-blue/5",
  },
  ISPIT: {
    ikona: ClipboardList,
    aktivnaKartica:
      "border-fon-purple bg-fon-purple/10 ring-2 ring-fon-purple/30",
    ikonaBg: "bg-fon-purple",
    akcentText: "text-fon-purple",
    akcentBorder: "border-fon-purple",
    akcentBg: "bg-fon-purple/5",
  },
  ZAVRSNI_RAD: {
    ikona: GraduationCap,
    aktivnaKartica: "border-fon-amber bg-fon-amber/10 ring-2 ring-fon-amber/30",
    ikonaBg: "bg-fon-amber",
    akcentText: "text-fon-amber",
    akcentBorder: "border-fon-amber",
    akcentBg: "bg-fon-amber/5",
  },
  SASTANAK: {
    ikona: Users,
    aktivnaKartica: "border-fon-pink bg-fon-pink/10 ring-2 ring-fon-pink/30",
    ikonaBg: "bg-fon-pink",
    akcentText: "text-fon-pink",
    akcentBorder: "border-fon-pink",
    akcentBg: "bg-fon-pink/5",
  },
  DOGADJAJ: {
    ikona: PartyPopper,
    aktivnaKartica: "border-fon-teal bg-fon-teal/10 ring-2 ring-fon-teal/30",
    ikonaBg: "bg-fon-teal",
    akcentText: "text-fon-teal",
    akcentBorder: "border-fon-teal",
    akcentBg: "bg-fon-teal/5",
  },
};

interface SvrhaTipSelektorProps {
  tipSvrhe: SvrhaRezervacijeDto["tip"];
  onChange: (tip: SvrhaRezervacijeDto["tip"]) => void;
}

export function SvrhaTipSelektor({
  tipSvrhe,
  onChange,
}: SvrhaTipSelektorProps) {
  return (
    <div>
      <label className="mb-2 block text-fon-navy">Svrha rezervacije</label>
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-5">
        {(Object.keys(SVRHA_STIL) as Array<SvrhaRezervacijeDto["tip"]>).map(
          (tip) => {
            const stil = SVRHA_STIL[tip];
            const Ikona = stil.ikona;
            const aktivan = tipSvrhe === tip;
            return (
              <button
                key={tip}
                type="button"
                onClick={() => onChange(tip)}
                className={`flex flex-col items-center gap-1.5 rounded-xl border-2 p-3 text-center transition-all ${
                  aktivan
                    ? stil.aktivnaKartica
                    : "border-gray-300 bg-white hover:border-gray-400"
                }`}
              >
                <span
                  className={`flex h-9 w-9 items-center justify-center rounded-full text-white ${stil.ikonaBg}`}
                >
                  <Ikona size={18} />
                </span>
                <span
                  className={`text-sm font-medium ${aktivan ? stil.akcentText : "text-fon-navy"}`}
                >
                  {NAZIVI_SVRHE[tip]}
                </span>
              </button>
            );
          },
        )}
      </div>
    </div>
  );
}
