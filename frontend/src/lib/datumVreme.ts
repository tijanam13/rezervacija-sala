export function danasnjiDatumLokalno(): string {
  const d = new Date();
  const godina = d.getFullYear();
  const mesec = String(d.getMonth() + 1).padStart(2, "0");
  const dan = String(d.getDate()).padStart(2, "0");
  return `${godina}-${mesec}-${dan}`;
}

export function trenutnoVremeLokalno(): string {
  const d = new Date();
  const sati = String(d.getHours()).padStart(2, "0");
  const minuti = String(d.getMinutes()).padStart(2, "0");
  return `${sati}:${minuti}`;
}

export function vremeZaBackend(vreme: string): string {
  return `${vreme}:00`;
}
