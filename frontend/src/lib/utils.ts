import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function selectItemsOd<T extends { id?: number; naziv: string }>(
  lista: T[],
): Record<string, string> {
  return Object.fromEntries(lista.map((x) => [String(x.id), x.naziv]));
}
