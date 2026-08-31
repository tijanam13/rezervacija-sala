import api from "@/lib/api";
import type { KorisnikDto, StranicaDto, StatusNaloga } from "@/types";

export async function fetchKorisnici(
  stranica = 0,
  velicina = 6,
  pretraga?: string,
): Promise<StranicaDto<KorisnikDto>> {
  const { data } = await api.get<StranicaDto<KorisnikDto>>("/korisnik", {
    params: {
      stranica,
      velicina,
      ...(pretraga?.trim() ? { pretraga: pretraga.trim() } : {}),
    },
  });
  return data;
}

export async function fetchKorisnik(id: number): Promise<KorisnikDto> {
  const { data } = await api.get<KorisnikDto>(`/korisnik/${id}`);
  return data;
}

export async function promeniStatusNaloga(
  id: number,
  noviStatus: StatusNaloga,
): Promise<KorisnikDto> {
  const { data } = await api.patch<KorisnikDto>(
    `/korisnik/${id}/status`,
    JSON.stringify(noviStatus),
    { headers: { "Content-Type": "application/json" } },
  );
  return data;
}

export async function dodeliUlogu(
  id: number,
  uloga: "ADMIN" | "KOORDINATOR",
): Promise<KorisnikDto> {
  const { data } = await api.post<KorisnikDto>(
    `/korisnik/${id}/uloga/${uloga}`,
  );
  return data;
}

export async function oduzmiUlogu(
  id: number,
  uloga: "ADMIN" | "KOORDINATOR",
): Promise<KorisnikDto> {
  const { data } = await api.delete<KorisnikDto>(
    `/korisnik/${id}/uloga/${uloga}`,
  );
  return data;
}
