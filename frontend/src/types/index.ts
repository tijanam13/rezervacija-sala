export type StatusRezervacije =
  | "NA_CEKANJU"
  | "ODOBRENA"
  | "DELIMICNO_ODOBRENA"
  | "ODBIJENA"
  | "OTKAZANA"
  | "ISTEKLA";

export type StatusStavke =
  | "NA_CEKANJU"
  | "ODOBRENA"
  | "ODBIJENA"
  | "OTKAZANA"
  | "ISTEKLA";

export interface KatedraDto {
  id: number;
  naziv: string;
  opis?: string;
}

export interface ZvanjeDto {
  id: number;
  naziv: string;
  opis?: string;
}

export interface SluzbaDto {
  id: number;
  naziv: string;
  opis?: string;
}

export interface TipSaleDto {
  id: number;
  naziv: string;
  opis?: string;
}

export type StatusSale = "SLOBODNA" | "ZAUZETA" | "VAN_UPOTREBE";

export interface SalaDto {
  id: number;
  naziv: string;
  zgrada: string;
  sprat: number;
  kapacitet: number;
  brojRacunara: number;
  status: StatusSale;
  tipSale: TipSaleDto;
}

export interface PredavacDto {
  id?: number;
  ime: string;
  prezime: string;
  brojTelefona?: string;
  brojRadneKnjizice?: string;
  poslovniEmail?: string;
  titula?: string;
  terminKonsultacija?: string;
  katedra: KatedraDto;
  zvanje: ZvanjeDto;
}

export interface SluzbenikDto {
  id?: number;
  ime: string;
  prezime: string;
  brojTelefona?: string;
  brojRadneKnjizice?: string;
  poslovniEmail?: string;
  pozicija?: string;
  sluzba: SluzbaDto;
}

export type TipKorisnika = "PREDAVAC" | "SLUZBENIK";
export type StatusNaloga = "AKTIVAN" | "NEAKTIVAN" | "BLOKIRAN";

export interface KorisnikDto {
  id: number;
  email: string;
  ime: string;
  prezime: string;
  tipKorisnika: TipKorisnika;
  status: StatusNaloga;
  uloge: string[];
  predavac: PredavacDto | null;
  sluzbenik: SluzbenikDto | null;
}

export type NivoStudija = "OSNOVNE_AKADEMSKE" | "MASTER" | "DOKTORSKE";
export type VrstaNastave = "PREDAVANJE" | "VEZBE";
export type VrstaVezbi = "AUDITORNE" | "RACUNSKE";
export type TipIspita = "PISMENI" | "USMENI" | "SEMINARSKI_RAD";

interface AkademskaAktivnostBase {
  id?: number;
  semestar: number;
  nivoStudija: NivoStudija;
}

export interface NastavaDto extends AkademskaAktivnostBase {
  tip: "NASTAVA";
  vrsta: VrstaNastave;
  vrstaVezbi?: VrstaVezbi;
}

export interface IspitDto extends AkademskaAktivnostBase {
  tip: "ISPIT";
  tipIspita: TipIspita;
}

export interface ZavrsniRadDto extends AkademskaAktivnostBase {
  tip: "ZAVRSNI_RAD";
  nazivTeme: string;
  student: string;
  mentor: PredavacDto;
  clanoviKomisije: PredavacDto[];
}

export interface UcesnikSastankaDto {
  id?: number;
  ucesnik: string;
  email?: string;
  zaposleniId?: number;
}

export interface SastanakDto {
  id?: number;
  tip: "SASTANAK";
  tema: string;
  napomena?: string;
  ucesnici: UcesnikSastankaDto[];
}

export interface DogadjajDto {
  id?: number;
  tip: "DOGADJAJ";
  naziv: string;
  opis?: string;
}

export type SvrhaRezervacijeDto =
  | NastavaDto
  | IspitDto
  | ZavrsniRadDto
  | SastanakDto
  | DogadjajDto;

export interface StavkaRezervacijeDto {
  id?: number;
  datumTermina: string;
  vremeOd: string;
  vremeDo: string;
  brojOsoba: number;
  statusStavke?: StatusStavke;
  opis?: string;
  rezervacijaId?: number;
  sala: SalaDto;
}

export interface RezervacijaDto {
  id?: number;
  datumKreiranja?: string;
  status?: StatusRezervacije;
  napomena?: string;
  korisnik?: KorisnikDto;
  svrha: SvrhaRezervacijeDto;
  stavke: StavkaRezervacijeDto[];
}

export interface StranicaDto<T> {
  sadrzaj: T[];
  brojStranice: number;
  velicinaStranice: number;
  ukupnoElemenata: number;
  ukupnoStranica: number;
}

export interface AuthResponse {
  token: string;
  korisnik: KorisnikDto;
  uloge: string[];
}

export interface ZauzetostDto {
  salaId: number;
  datumTermina: string;
  vremeOd: string;
  vremeDo: string;
  statusStavke: "NA_CEKANJU" | "ODOBRENA";
}
