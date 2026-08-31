import axios from "axios";
import api from "@/lib/api";
import type { RezervacijaDto, StranicaDto } from "@/types";

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
): Promise<StranicaDto<RezervacijaDto>> {
  const { data } = await api.get<StranicaDto<RezervacijaDto>>(
    "/rezervacija/moje-rezervacije",
    { params: { stranica, velicina } },
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
