import type { SvrhaRezervacijeDto, StatusStavke } from "@/types";

export interface SalaInfo {
  naziv: string;
  zgrada: string;
  sprat: number;
  kapacitet: number;
  tip: string;
}

export interface TerminPodaci {
  id: number;
  salaNaziv: string;
  datum: string;
  vremeOd: number;
  vremeDo: number;
  status: StatusStavke;
  brojOsoba: number;
  opis?: string;
  korisnikImePrezime: string;
  svrha: SvrhaRezervacijeDto;
}
