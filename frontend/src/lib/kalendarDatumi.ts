export function toISODatum(d: Date): string {
  const godina = d.getFullYear();
  const mesec = String(d.getMonth() + 1).padStart(2, "0");
  const dan = String(d.getDate()).padStart(2, "0");
  return `${godina}-${mesec}-${dan}`;
}

export function jeIstiDan(a: Date, b: Date): boolean {
  return toISODatum(a) === toISODatum(b);
}

export function pocetakNedelje(d: Date): Date {
  const kopija = new Date(d);
  const pomeraj = (kopija.getDay() + 6) % 7;
  kopija.setDate(kopija.getDate() - pomeraj);
  return kopija;
}

export function daniMeseca(d: Date): (Date | null)[] {
  const godina = d.getFullYear();
  const mesec = d.getMonth();
  const prviDan = new Date(godina, mesec, 1);
  const brojDana = new Date(godina, mesec + 1, 0).getDate();
  const pomeraj = (prviDan.getDay() + 6) % 7;
  const dani: (Date | null)[] = Array(pomeraj).fill(null);
  for (let dan = 1; dan <= brojDana; dan++) {
    dani.push(new Date(godina, mesec, dan));
  }
  const preostalo = (7 - (dani.length % 7)) % 7;
  return [...dani, ...Array(preostalo).fill(null)];
}

export function formatNaslov(
  datum: Date,
  prikaz: "dan" | "nedelja" | "mesec",
): string {
  if (prikaz === "mesec") {
    return datum.toLocaleDateString("sr-Latn-RS", {
      month: "long",
      year: "numeric",
    });
  }
  if (prikaz === "nedelja") {
    const pocetak = pocetakNedelje(datum);
    const kraj = new Date(pocetak);
    kraj.setDate(kraj.getDate() + 6);
    return `${pocetak.getDate()} - ${kraj.getDate()}. ${kraj.toLocaleDateString("sr-Latn-RS", { month: "long", year: "numeric" })}`;
  }
  return datum.toLocaleDateString("sr-Latn-RS", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}
