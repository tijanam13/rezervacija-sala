import api from "@/lib/api";
import type { SalaDto, PredavacDto } from "@/types";

export async function fetchSale(): Promise<SalaDto[]> {
  const { data } = await api.get<SalaDto[]>("/sala");
  return data;
}

export async function fetchPredavaci(): Promise<PredavacDto[]> {
  const { data } = await api.get<PredavacDto[]>("/predavac");
  return data;
}
