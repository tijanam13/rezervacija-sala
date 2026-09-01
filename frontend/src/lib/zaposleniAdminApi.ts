import api from "@/lib/api";
import type { PredavacDto, SluzbenikDto } from "@/types";

export async function fetchPredavaciAdmin(): Promise<PredavacDto[]> {
  const { data } = await api.get<PredavacDto[]>("/predavac");
  return data;
}

export async function kreirajPredavaca(
  dto: Omit<PredavacDto, "id">,
): Promise<PredavacDto> {
  const { data } = await api.post<PredavacDto>("/predavac", dto);
  return data;
}

export async function azurirajPredavaca(
  id: number,
  dto: Omit<PredavacDto, "id">,
): Promise<PredavacDto> {
  const { data } = await api.put<PredavacDto>(`/predavac/${id}`, dto);
  return data;
}

export async function obrisiPredavaca(id: number): Promise<void> {
  await api.delete(`/predavac/${id}`);
}

export async function fetchSluzbeniciAdmin(): Promise<SluzbenikDto[]> {
  const { data } = await api.get<SluzbenikDto[]>("/sluzbenik");
  return data;
}

export async function kreirajSluzbenika(
  dto: Omit<SluzbenikDto, "id">,
): Promise<SluzbenikDto> {
  const { data } = await api.post<SluzbenikDto>("/sluzbenik", dto);
  return data;
}

export async function azurirajSluzbenika(
  id: number,
  dto: Omit<SluzbenikDto, "id">,
): Promise<SluzbenikDto> {
  const { data } = await api.put<SluzbenikDto>(`/sluzbenik/${id}`, dto);
  return data;
}

export async function obrisiSluzbenika(id: number): Promise<void> {
  await api.delete(`/sluzbenik/${id}`);
}
