import axios from "axios";
import api from "@/lib/api";
import type {
  RezervacijaDto,
  StranicaDto,
  StatusRezervacije,
  StatusStavke,
} from "@/types";

interface BackendGreska {
  timestamp: string;
  status: number;
  poruka: string;
  greske?: Record<string, string>;
}

export async function kreirajRezervaciju(
  dto: RezervacijaDto,
): Promise<RezervacijaDto> {
  const { data } = await api.post<RezervacijaDto>("/rezervacija", dto);
  return data;
}

export async function fetchMojeRezervacije(
  stranica = 0,
  velicina = 9,
  status?: StatusRezervacije,
  odDatum?: string,
  doDatum?: string,
): Promise<StranicaDto<RezervacijaDto>> {
  const { data } = await api.get<StranicaDto<RezervacijaDto>>(
    "/rezervacija/moje-rezervacije",
    { params: { stranica, velicina, status, odDatum, doDatum } },
  );
  return data;
}

export async function otkaziRezervaciju(id: number): Promise<RezervacijaDto> {
  const { data } = await api.patch<RezervacijaDto>(`/rezervacija/${id}/otkazi`);
  return data;
}

export async function otkaziStavku(stavkaId: number): Promise<RezervacijaDto> {
  const { data } = await api.patch<RezervacijaDto>(
    `/rezervacija/stavka/${stavkaId}/otkazi`,
  );
  return data;
}

export async function fetchRezervacijaById(
  id: number,
): Promise<RezervacijaDto> {
  const { data } = await api.get<RezervacijaDto>(`/rezervacija/${id}`);
  return data;
}

export async function fetchSveRezervacije(
  status?: StatusRezervacije,
  stranica = 0,
  velicina = 9,
): Promise<StranicaDto<RezervacijaDto>> {
  const { data } = await api.get<StranicaDto<RezervacijaDto>>("/rezervacija", {
    params: { status, stranica, velicina },
  });
  return data;
}

export async function azurirajStatusRezervacije(
  id: number,
  noviStatus: StatusRezervacije,
): Promise<RezervacijaDto> {
  const { data } = await api.patch<RezervacijaDto>(
    `/rezervacija/${id}/status`,
    JSON.stringify(noviStatus),
    { headers: { "Content-Type": "application/json" } },
  );
  return data;
}

export async function azurirajStatusStavke(
  stavkaId: number,
  noviStatus: StatusStavke,
): Promise<RezervacijaDto> {
  const { data } = await api.patch<RezervacijaDto>(
    `/rezervacija/stavka/${stavkaId}/status`,
    JSON.stringify(noviStatus),
    { headers: { "Content-Type": "application/json" } },
  );
  return data;
}

export async function odbijRezervacijuSaRazlogom(
  id: number,
  razlog: string,
): Promise<RezervacijaDto> {
  const { data } = await api.patch<RezervacijaDto>(
    `/rezervacija/${id}/odbij-sa-razlogom`,
    JSON.stringify(razlog),
    { headers: { "Content-Type": "application/json" } },
  );
  return data;
}

export async function odbijStavkuSaRazlogom(
  stavkaId: number,
  razlog: string,
): Promise<RezervacijaDto> {
  const { data } = await api.patch<RezervacijaDto>(
    `/rezervacija/stavka/${stavkaId}/odbij-sa-razlogom`,
    JSON.stringify(razlog),
    { headers: { "Content-Type": "application/json" } },
  );
  return data;
}

export function izvuciPorukuGreske(err: unknown): string {
  if (axios.isAxiosError(err) && err.response?.data) {
    const podaci = err.response.data as BackendGreska;
    if (podaci.greske && Object.keys(podaci.greske).length > 0) {
      return Object.values(podaci.greske).join(" ");
    }
    if (podaci.poruka) {
      return podaci.poruka;
    }
  }
  return "Došlo je do neočekivane greške. Proverite internet konekciju i pokušajte ponovo.";
}
