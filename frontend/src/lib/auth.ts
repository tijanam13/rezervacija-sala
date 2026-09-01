import type { KorisnikDto } from "@/types";

const KLJUC_TOKEN = "token";
const KLJUC_KORISNIK = "korisnik";

export function jeUlogovan(): boolean {
  return localStorage.getItem(KLJUC_TOKEN) !== null;
}

export function sacuvajSesiju(token: string, korisnik: KorisnikDto) {
  localStorage.setItem(KLJUC_TOKEN, token);
  localStorage.setItem(KLJUC_KORISNIK, JSON.stringify(korisnik));
}

export function preuzmiKorisnika(): KorisnikDto | null {
  const sirovo = localStorage.getItem(KLJUC_KORISNIK);
  if (!sirovo) return null;
  try {
    return JSON.parse(sirovo) as KorisnikDto;
  } catch {
    return null;
  }
}

export function imaUlogu(uloga: "ADMIN" | "KOORDINATOR"): boolean {
  const korisnik = preuzmiKorisnika();
  return korisnik?.uloge?.includes(uloga) ?? false;
}

export function jeAdmin(): boolean {
  return imaUlogu("ADMIN");
}

export function jeKoordinator(): boolean {
  return imaUlogu("KOORDINATOR");
}

export function jeAdministracija(): boolean {
  return jeKoordinator() || jeAdmin();
}

export function odjaviSe(razlog?: "istekla-sesija") {
  localStorage.removeItem(KLJUC_TOKEN);
  localStorage.removeItem(KLJUC_KORISNIK);
  window.location.href =
    razlog === "istekla-sesija" ? "/prijava?istekla=1" : "/prijava";
}
