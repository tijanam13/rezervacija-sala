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
  return (
    <div className="mb-5 flex flex-wrap items-center gap-3 rounded-2xl border border-fon-teal/20 bg-fon-teal/5 p-3">
      <div className="relative min-w-[180px] flex-1">
        <Search
          size={16}
          className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-purple"
        />
        <Input
          placeholder="Pretraži salu po nazivu..."
          value={pretraga}
          onChange={(e) => setPretraga(e.target.value)}
          className="border-fon-purple/30 bg-white pl-9"
        />
      </div>

      <Select value={zgrada} onValueChange={(v) => v !== null && setZgrada(v)}>
        <SelectTrigger className="w-[140px] border-fon-pink/30 bg-white">
          <Building2 size={14} className="text-fon-pink" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          {zgrade.map((z) => (
            <SelectItem key={z} value={z}>
              {z}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select value={sprat} onValueChange={(v) => v !== null && setSprat(v)}>
        <SelectTrigger className="w-[140px] border-fon-purple/30 bg-white">
          <Layers size={14} className="text-fon-purple" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          <SelectItem value="SVI">Svi spratovi</SelectItem>
          {spratovi.map((s) => (
            <SelectItem key={s} value={String(s)}>
              {s === 0 ? "Prizemlje" : `Sprat ${s}`}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select
        value={String(minKapacitet)}
        onValueChange={(v) => v !== null && setMinKapacitet(Number(v))}
      >
        <SelectTrigger className="w-[170px] border-fon-pink/30 bg-white">
          <Users size={14} className="text-fon-pink" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          <SelectItem value="0">Bilo koji kapacitet</SelectItem>
          <SelectItem value="10">10+ mesta</SelectItem>
          <SelectItem value="20">20+ mesta</SelectItem>
          <SelectItem value="50">50+ mesta</SelectItem>
          <SelectItem value="100">100+ mesta</SelectItem>
        </SelectContent>
      </Select>

      <Select
        value={tipSale}
        onValueChange={(v) => v !== null && setTipSale(v)}
      >
        <SelectTrigger className="w-[190px] border-fon-purple/30 bg-white">
          <Tag size={14} className="text-fon-purple" />
          <SelectValue />
        </SelectTrigger>
        <SelectContent {...SELECT_PROPS}>
          <SelectItem value="SVI">Svi tipovi sala</SelectItem>
          {tipoviSale.map((t) => (
            <SelectItem key={t} value={t}>
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
          <SelectTrigger className="w-[190px] border-fon-navy/30 bg-white">
            <Filter size={14} className="text-fon-navy" />
            <SelectValue>
              {filterStatus === "SVI"
                ? "Svi statusi"
                : statusStyles[filterStatus].label}
            </SelectValue>
          </SelectTrigger>
          <SelectContent {...SELECT_PROPS}>
            <SelectItem value="SVI">Svi statusi</SelectItem>
            <SelectItem value="NA_CEKANJU">Na čekanju</SelectItem>
            <SelectItem value="ODOBRENA">Odobrena</SelectItem>
            <SelectItem value="ODBIJENA">Odbijena</SelectItem>
            <SelectItem value="OTKAZANA">Otkazana</SelectItem>
            <SelectItem value="ISTEKLA">Istekla</SelectItem>
          </SelectContent>
        </Select>
      )}
    </div>
  );
}
