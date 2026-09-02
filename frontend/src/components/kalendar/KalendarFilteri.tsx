import { Search, Building2, Layers, Users, Tag, Filter } from "lucide-react";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { statusStyles } from "@/lib/statusColors";
import type { StatusStavke } from "@/types";

interface KalendarFilteriProps {
  pretraga: string;
  setPretraga: (v: string) => void;
  zgrada: string;
  setZgrada: (v: string) => void;
  zgrade: string[];
  sprat: string;
  setSprat: (v: string) => void;
  spratovi: number[];
  minKapacitet: number;
  setMinKapacitet: (v: number) => void;
  tipSale: string;
  setTipSale: (v: string) => void;
  tipoviSale: string[];
  administracija: boolean;
  filterStatus: StatusStavke | "SVI";
  setFilterStatus: (v: StatusStavke | "SVI") => void;
}

const SELECT_PROPS = {
  align: "start" as const,
  side: "bottom" as const,
  alignItemWithTrigger: false,
};

const UNIFORM_SELECT_CLASS =
  "flex-1 min-w-[150px] bg-white [&>span]:truncate text-left shrink-0";

export function KalendarFilteri({
  pretraga,
  setPretraga,
  zgrada,
  setZgrada,
  zgrade,
  sprat,
  setSprat,
  spratovi,
  minKapacitet,
  setMinKapacitet,
  tipSale,
  setTipSale,
  tipoviSale,
  administracija,
  filterStatus,
  setFilterStatus,
}: KalendarFilteriProps) {
  const spratLabel =
    sprat === "SVI"
      ? "Svi spratovi"
      : sprat === "0"
        ? "Prizemlje"
        : `Sprat ${sprat}`;

  const kapacitetLabel =
    minKapacitet === 0 ? "Bilo koji kapacitet" : `${minKapacitet}+ mesta`;

  const statusLabel =
    filterStatus === "SVI"
      ? "Svi statusi"
      : statusStyles[filterStatus]?.label || filterStatus;

  return (
    <div className="mb-5 flex flex-wrap items-center gap-3 rounded-2xl border border-fon-teal/20 bg-fon-teal/5 p-3">
      <div className="relative min-w-[200px] flex-1">
        <Search
          size={16}
          className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-purple"
        />
        <Input
          placeholder="Pretraži salu po nazivu..."
          value={pretraga}
          onChange={(e) => setPretraga(e.target.value)}
          title={pretraga ? `Pretraga: ${pretraga}` : "Pretraži salu po nazivu"}
          className="border-fon-purple/30 bg-white pl-9"
        />
      </div>

      <Select value={zgrada} onValueChange={(v) => v !== null && setZgrada(v)}>
        <SelectTrigger
          title={zgrada || "Zgrada"}
          className={`${UNIFORM_SELECT_CLASS} border-fon-pink/30`}
        >
          <Building2 size={14} className="text-fon-pink shrink-0" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          {zgrade.map((z) => (
            <SelectItem key={z} value={z} title={z}>
              {z}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select value={sprat} onValueChange={(v) => v !== null && setSprat(v)}>
        <SelectTrigger
          title={spratLabel}
          className={`${UNIFORM_SELECT_CLASS} border-fon-purple/30`}
        >
          <Layers size={14} className="text-fon-purple shrink-0" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          <SelectItem value="SVI" title="Svi spratovi">
            Svi spratovi
          </SelectItem>
          {spratovi.map((s) => {
            const label = s === 0 ? "Prizemlje" : `Sprat ${s}`;
            return (
              <SelectItem key={s} value={String(s)} title={label}>
                {label}
              </SelectItem>
            );
          })}
        </SelectContent>
      </Select>

      <Select
        value={String(minKapacitet)}
        onValueChange={(v) => v !== null && setMinKapacitet(Number(v))}
      >
        <SelectTrigger
          title={kapacitetLabel}
          className={`${UNIFORM_SELECT_CLASS} border-fon-pink/30`}
        >
          <Users size={14} className="text-fon-pink shrink-0" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          <SelectItem value="0" title="Bilo koji kapacitet">
            Bilo koji kapacitet
          </SelectItem>
          <SelectItem value="10" title="10+ mesta">
            10+ mesta
          </SelectItem>
          <SelectItem value="20" title="20+ mesta">
            20+ mesta
          </SelectItem>
          <SelectItem value="50" title="50+ mesta">
            50+ mesta
          </SelectItem>
          <SelectItem value="100" title="100+ mesta">
            100+ mesta
          </SelectItem>
        </SelectContent>
      </Select>

      <Select
        value={tipSale}
        onValueChange={(v) => v !== null && setTipSale(v)}
      >
        <SelectTrigger
          title={tipSale === "SVI" ? "Svi tipovi sala" : tipSale}
          className={`${UNIFORM_SELECT_CLASS} border-fon-purple/30`}
        >
          <Tag size={14} className="text-fon-purple shrink-0" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          <SelectItem value="SVI" title="Svi tipovi sala">
            Svi tipovi sala
          </SelectItem>
          {tipoviSale.map((t) => (
            <SelectItem key={t} value={t} title={t}>
              {t}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {administracija && (
        <Select
          value={filterStatus}
          onValueChange={(v) =>
            v !== null && setFilterStatus(v as StatusStavke | "SVI")
          }
        >
          <SelectTrigger
            title={statusLabel}
            className={`${UNIFORM_SELECT_CLASS} border-fon-navy/30`}
          >
            <Filter size={14} className="text-fon-navy shrink-0" />
            <SelectValue>{statusLabel}</SelectValue>
          </SelectTrigger>
          <SelectContent {...SELECT_PROPS}>
            <SelectItem value="SVI" title="Svi statusi">
              Svi statusi
            </SelectItem>
            <SelectItem value="NA_CEKANJU" title="Na čekanju">
              Na čekanju
            </SelectItem>
            <SelectItem value="ODOBRENA" title="Odobrena">
              Odobrena
            </SelectItem>
            <SelectItem value="ODBIJENA" title="Odbijena">
              Odbijena
            </SelectItem>
            <SelectItem value="OTKAZANA" title="Otkazana">
              Otkazana
            </SelectItem>
            <SelectItem value="ISTEKLA" title="Istekla">
              Istekla
            </SelectItem>
          </SelectContent>
        </Select>
      )}
    </div>
  );
}
