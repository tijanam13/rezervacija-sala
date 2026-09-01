import api from "@/lib/api";
import type { KatedraDto, ZvanjeDto, SluzbaDto } from "@/types";

export async function fetchKatedreAdmin(): Promise<KatedraDto[]> {
  const { data } = await api.get<KatedraDto[]>("/katedra");
  return data;
}

export async function kreirajKatedru(
  dto: Omit<KatedraDto, "id">,
): Promise<KatedraDto> {
  const { data } = await api.post<KatedraDto>("/katedra", dto);
  return data;
}

export async function azurirajKatedru(
  id: number,
  dto: Omit<KatedraDto, "id">,
): Promise<KatedraDto> {
  const { data } = await api.put<KatedraDto>(`/katedra/${id}`, dto);
  return data;
}

export async function obrisiKatedru(id: number): Promise<void> {
  await api.delete(`/katedra/${id}`);
}

export async function fetchZvanjaAdmin(): Promise<ZvanjeDto[]> {
  const { data } = await api.get<ZvanjeDto[]>("/zvanje");
  return data;
}

export async function kreirajZvanje(
  dto: Omit<ZvanjeDto, "id">,
): Promise<ZvanjeDto> {
  const { data } = await api.post<ZvanjeDto>("/zvanje", dto);
  return data;
}

export async function azurirajZvanje(
  id: number,
  dto: Omit<ZvanjeDto, "id">,
): Promise<ZvanjeDto> {
  const { data } = await api.put<ZvanjeDto>(`/zvanje/${id}`, dto);
  return data;
}

export async function obrisiZvanje(id: number): Promise<void> {
  await api.delete(`/zvanje/${id}`);
}

export async function fetchSluzbeAdmin(): Promise<SluzbaDto[]> {
  const { data } = await api.get<SluzbaDto[]>("/sluzba");
  return data;
}

export async function kreirajSluzbu(
  dto: Omit<SluzbaDto, "id">,
): Promise<SluzbaDto> {
  const { data } = await api.post<SluzbaDto>("/sluzba", dto);
  return data;
}

export async function azurirajSluzbu(
  id: number,
  dto: Omit<SluzbaDto, "id">,
): Promise<SluzbaDto> {
  const { data } = await api.put<SluzbaDto>(`/sluzba/${id}`, dto);
  return data;
}

export async function obrisiSluzbu(id: number): Promise<void> {
  await api.delete(`/sluzba/${id}`);
}
