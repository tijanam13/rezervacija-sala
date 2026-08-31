import api from "@/lib/api";
import type {
  AuthResponse,
  KatedraDto,
  ZvanjeDto,
  SluzbaDto,
  KorisnikDto,
} from "@/types";

export interface LoginZahtev {
  email: string;
  lozinka: string;
}

export interface RegistracijaPredavacZahtev {
  ime: string;
  prezime: string;
  brojTelefona?: string;
  brojRadneKnjizice: string;
  email: string;
  lozinka: string;
  titula?: string;
  terminKonsultacija?: string;
  katedraId: number;
  zvanjeId: number;
}

export interface RegistracijaSluzbenikZahtev {
  ime: string;
  prezime: string;
  brojTelefona?: string;
  brojRadneKnjizice: string;
  email: string;
  lozinka: string;
  pozicija?: string;
  sluzbaId: number;
}

export async function login(zahtev: LoginZahtev): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/login", zahtev);
  return data;
}

export async function registrujPredavaca(zahtev: RegistracijaPredavacZahtev) {
  const { data } = await api.post("/auth/registracija/predavac", zahtev);
  return data;
}

export async function registrujSluzbenika(zahtev: RegistracijaSluzbenikZahtev) {
  const { data } = await api.post("/auth/registracija/sluzbenik", zahtev);
  return data;
}

export async function verifikujEmail(token: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>(
    `/auth/verifikuj-email?token=${encodeURIComponent(token)}`,
  );
  return data;
}

export async function zatraziResetLozinke(email: string) {
  await api.post(
    `/auth/lozinka/zaboravljena?email=${encodeURIComponent(email)}`,
  );
}

export async function resetujLozinku(token: string, novaLozinka: string) {
  await api.post(
    `/auth/lozinka/reset?token=${encodeURIComponent(token)}&novaLozinka=${encodeURIComponent(novaLozinka)}`,
  );
}

export async function fetchKatedre(): Promise<KatedraDto[]> {
  const { data } = await api.get<KatedraDto[]>("/katedra");
  return data;
}

export async function fetchZvanja(): Promise<ZvanjeDto[]> {
  const { data } = await api.get<ZvanjeDto[]>("/zvanje");
  return data;
}

export async function fetchSluzbe(): Promise<SluzbaDto[]> {
  const { data } = await api.get<SluzbaDto[]>("/sluzba");
  return data;
}

export async function fetchMojProfil(): Promise<KorisnikDto> {
  const { data } = await api.get<KorisnikDto>("/auth/me");
  return data;
}
