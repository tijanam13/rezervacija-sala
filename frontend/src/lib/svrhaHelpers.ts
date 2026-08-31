import {
  BookOpen,
  FileText,
  GraduationCap,
  Users,
  CalendarDays,
} from "lucide-react";
import type { SvrhaRezervacijeDto } from "@/types";

export function nazivSvrhe(svrha: SvrhaRezervacijeDto): string {
  switch (svrha.tip) {
    case "NASTAVA":
      return svrha.vrsta === "VEZBE" ? "Vežbe" : "Predavanje";
    case "ISPIT":
      return "Ispit";
    case "ZAVRSNI_RAD":
      return svrha.nazivTeme;
    case "SASTANAK":
      return svrha.tema;
    case "DOGADJAJ":
      return svrha.naziv;
  }
}

export function ikonicaSvrhe(tip: SvrhaRezervacijeDto["tip"]) {
  switch (tip) {
    case "NASTAVA":
      return BookOpen;
    case "ISPIT":
      return FileText;
    case "ZAVRSNI_RAD":
      return GraduationCap;
    case "SASTANAK":
      return Users;
    case "DOGADJAJ":
      return CalendarDays;
  }
}

export function formatVreme(sat: number): string {
  const h = Math.floor(sat);
  const m = Math.round((sat - h) * 60);
  return `${h}:${m.toString().padStart(2, "0")}`;
}

export function formatDatum(isoDatum: string): string {
  const [godina, mesec, dan] = isoDatum.split("-");
  return `${dan}.${mesec}.${godina}.`;
}

export function bezDonjeCrte(vrednost: string): string {
  return vrednost.replaceAll("_", " ");
}

export function vremeURedMreze(vreme: number, pocetniSat: number): number {
  return 2 + (vreme - pocetniSat);
}

export function vremeUDecimalni(vremeString: string): number {
  const [satiStr, minutiStr] = vremeString.split(":");
  return Number(satiStr) + Number(minutiStr) / 60;
}
