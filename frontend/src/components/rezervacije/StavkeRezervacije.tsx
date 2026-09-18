import { Plus, Trash2, MapPin } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import type { SalaDto } from "@/types";

const SELECT_PROPS = { side: "bottom" as const, alignItemWithTrigger: false };

const STAVKA_BOJE = [
  { border: "border-fon-teal", bedz: "bg-fon-teal" },
  { border: "border-fon-blue", bedz: "bg-fon-blue" },
  { border: "border-fon-purple", bedz: "bg-fon-purple" },
  { border: "border-fon-pink", bedz: "bg-fon-pink" },
  { border: "border-fon-amber", bedz: "bg-fon-amber" },
] as const;

export interface StavkaForma {
  sala: string;
  brojOsoba: number;
  opis: string;
}

export function praznaStavka(prvaSala: string): StavkaForma {
  return {
    sala: prvaSala,
    brojOsoba: 1,
    opis: "",
  };
}

interface StavkeRezervacijeProps {
  stavke: StavkaForma[];
  saleFiltrirane: SalaDto[];
  onDodaj: () => void;
  onUkloni: (index: number) => void;
  onAzuriraj: (index: number, izmena: Partial<StavkaForma>) => void;
}

export function StavkeRezervacije({
  stavke,
  saleFiltrirane,
  onDodaj,
  onUkloni,
  onAzuriraj,
}: StavkeRezervacijeProps) {
  return (
    <div className="border-t border-gray-400 pt-4">
      <div className="mb-2 flex items-center justify-between">
        <label className="flex items-center gap-1.5 text-fon-navy">
          <MapPin size={18} className="text-fon-teal" />
          Sale (za isti termin)
        </label>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={onDodaj}
          className="gap-1 border-fon-teal text-fon-teal hover:bg-fon-teal/10"
        >
          <Plus size={14} /> Dodaj još jednu salu
        </Button>
      </div>

      <div className="space-y-3">
        {stavke.map((s, i) => {
          const boja = STAVKA_BOJE[i % STAVKA_BOJE.length];
          const izabranaSala = saleFiltrirane.find(
            (sala) => sala.naziv === s.sala,
          );
          const maxOsoba = izabranaSala?.kapacitet ?? 999;
          return (
            <div
              key={i}
              className={`rounded-lg border-2 border-l-4 border-gray-300 p-3 shadow-sm ${boja.border}`}
            >
              <div className="mb-2 flex items-center justify-between">
                <span className="flex items-center gap-2 text-sm font-medium text-fon-dark">
                  <span
                    className={`flex h-5 w-5 items-center justify-center rounded-full text-xs font-semibold text-white ${boja.bedz}`}
                  >
                    {i + 1}
                  </span>
                  Stavka {i + 1}
                </span>
                {stavke.length > 1 && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => onUkloni(i)}
                  >
                    <Trash2 size={16} className="text-fon-coral" />
                  </Button>
                )}
              </div>

              <div className="space-y-2">
                <label className="mb-1 block text-xs text-fon-navy">
                  Sala <span className="text-red-500">*</span>
                </label>
                <Select
                  value={s.sala}
                  onValueChange={(v) => {
                    if (v === null) return;
                    const novaSala = saleFiltrirane.find(
                      (sala) => sala.naziv === v,
                    );
                    const novBrojOsoba = novaSala
                      ? Math.min(s.brojOsoba, novaSala.kapacitet)
                      : s.brojOsoba;
                    onAzuriraj(i, { sala: v, brojOsoba: novBrojOsoba });
                  }}
                >
                  <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent {...SELECT_PROPS} align="start">
                    {saleFiltrirane.map((sala) => (
                      <SelectItem key={sala.naziv} value={sala.naziv}>
                        {sala.naziv}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <div>
                  <label className="mb-1 block text-xs text-fon-navy">
                    Broj osoba <span className="text-red-500">*</span>
                  </label>
                  <Input
                    className="border-2 border-gray-500 text-fon-navy"
                    type="number"
                    min={1}
                    max={maxOsoba}
                    value={s.brojOsoba}
                    onChange={(e) => {
                      const vrednost = Math.min(
                        Number(e.target.value) || 1,
                        maxOsoba,
                      );
                      onAzuriraj(i, { brojOsoba: vrednost });
                    }}
                  />
                  {izabranaSala && (
                    <p className="mt-1 text-xs text-gray-400">
                      Kapacitet sale: {izabranaSala.kapacitet}
                    </p>
                  )}
                </div>

                <div>
                  <label className="mb-1 block text-xs text-fon-navy">
                    Napomena za ovu salu (opciono)
                  </label>
                  <Input
                    className="border-2 border-gray-500 text-fon-navy"
                    value={s.opis}
                    onChange={(e) => onAzuriraj(i, { opis: e.target.value })}
                  />
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
