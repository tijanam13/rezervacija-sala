import api from "@/lib/api";
import type { SalaDto, TipSaleDto, StatusSale } from "@/types";

export async function fetchSaleAdmin(): Promise<SalaDto[]> {
  const { data } = await api.get<SalaDto[]>("/sala");
  return data;
}

export async function kreirajSalu(
  dto: Omit<SalaDto, "id" | "status">,
): Promise<SalaDto> {
  const { data } = await api.post<SalaDto>("/sala", dto);
  return data;
}

export async function azurirajSalu(
  id: number,
  dto: Omit<SalaDto, "id" | "status">,
): Promise<SalaDto> {
  const { data } = await api.put<SalaDto>(`/sala/${id}`, dto);
  return data;
}

export async function promeniStatusSale(
  id: number,
  noviStatus: StatusSale,
): Promise<SalaDto> {
  const { data } = await api.patch<SalaDto>(
    `/sala/${id}/status`,
    JSON.stringify(noviStatus),
    { headers: { "Content-Type": "application/json" } },
  );
  return data;
}

export async function obrisiSalu(id: number): Promise<void> {
  await api.delete(`/sala/${id}`);
}

export async function fetchTipoveSalaAdmin(): Promise<TipSaleDto[]> {
  const { data } = await api.get<TipSaleDto[]>("/tip-sale");
  return data;
}

export async function kreirajTipSale(
  dto: Omit<TipSaleDto, "id">,
): Promise<TipSaleDto> {
  const { data } = await api.post<TipSaleDto>("/tip-sale", dto);
  return data;
}

export async function azurirajTipSale(
  id: number,
  dto: Omit<TipSaleDto, "id">,
): Promise<TipSaleDto> {
  const { data } = await api.put<TipSaleDto>(`/tip-sale/${id}`, dto);
  return data;
}

export async function obrisiTipSale(id: number): Promise<void> {
  await api.delete(`/tip-sale/${id}`);
}
