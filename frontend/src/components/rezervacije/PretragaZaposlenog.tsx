import { useEffect, useRef, useState } from "react";
import { Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";

export interface ZaposleniOpcija {
  id: number;
  ime: string;
  prezime: string;
  tip: "PREDAVAC" | "SLUZBENIK";
}

interface PretragaZaposlenogProps {
  opcije: ZaposleniOpcija[];
  izabranId: number | null;
  izabranoIme: string;
  onIzaberi: (opcija: ZaposleniOpcija) => void;
  onOcisti: () => void;
  placeholder?: string;
}

export function PretragaZaposlenog({
  opcije,
  izabranId,
  izabranoIme,
  onIzaberi,
  onOcisti,
  placeholder = "Pretraži po imenu ili prezimenu...",
}: PretragaZaposlenogProps) {
  const [upit, setUpit] = useState("");
  const [otvoreno, setOtvoreno] = useState(false);
  const kontejnerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function naKlikVan(e: MouseEvent) {
      if (
        kontejnerRef.current &&
        !kontejnerRef.current.contains(e.target as Node)
      ) {
        setOtvoreno(false);
      }
    }
    document.addEventListener("mousedown", naKlikVan);
    return () => document.removeEventListener("mousedown", naKlikVan);
  }, []);

  const rezultati =
    upit.trim().length === 0
      ? opcije
      : opcije.filter((o) =>
          `${o.ime} ${o.prezime}`
            .toLowerCase()
            .includes(upit.trim().toLowerCase()),
        );

  if (izabranId !== null) {
    return (
      <div className="flex items-center gap-2 rounded-md border-2 border-gray-500 bg-fon-teal/5 px-3 py-1.5">
        <span className="flex-1 text-sm text-fon-navy">{izabranoIme}</span>
        <button
          type="button"
          onClick={onOcisti}
          className="shrink-0 text-gray-400 hover:text-fon-coral"
        >
          <X size={16} />
        </button>
      </div>
    );
  }

  return (
    <div className="relative" ref={kontejnerRef}>
      <div className="relative">
        <Search
          size={16}
          className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-gray-400"
        />
        <Input
          className="border-2 border-gray-500 pl-9 text-fon-navy"
          placeholder={placeholder}
          value={upit}
          onFocus={() => setOtvoreno(true)}
          onChange={(e) => {
            setUpit(e.target.value);
            setOtvoreno(true);
          }}
        />
      </div>
      {otvoreno && (
        <div className="absolute top-full left-0 z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-md border border-gray-200 bg-white shadow-lg">
          {rezultati.length === 0 ? (
            <p className="p-3 text-sm text-gray-500">Nema rezultata.</p>
          ) : (
            rezultati.map((o) => (
              <button
                type="button"
                key={`${o.tip}-${o.id}`}
                onClick={() => {
                  onIzaberi(o);
                  setUpit("");
                  setOtvoreno(false);
                }}
                className="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-gray-50"
              >
                <span className="text-fon-navy">
                  {o.ime} {o.prezime}
                </span>
                <span className="text-xs text-gray-400">
                  {o.tip === "PREDAVAC" ? "Predavač" : "Službenik"}
                </span>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}
