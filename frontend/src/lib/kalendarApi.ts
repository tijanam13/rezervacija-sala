import api from "@/lib/api";
import { vremeUDecimalni } from "@/lib/svrhaHelpers";
import type {
  RezervacijaDto,
  ZauzetostDto,
  StranicaDto,
  SalaDto,
} from "@/types";
import type { TerminPodaci } from "./kalendarTipovi";

type Aktivan = "NA_CEKANJU" | "ODOBRENA";

function rezervacijaUTermine(r: RezervacijaDto): TerminPodaci[] {
  return r.stavke.map((s) => ({
    id: s.id ?? Math.random(),
    salaNaziv: s.sala.naziv,
    datum: s.datumTermina,
    vremeOd: vremeUDecimalni(s.vremeOd),
    vremeDo: vremeUDecimalni(s.vremeDo),
    status: (s.statusStavke ?? "NA_CEKANJU") as TerminPodaci["status"],
    brojOsoba: s.brojOsoba,
    opis: s.opis,
    korisnikImePrezime: r.korisnik
      ? `${r.korisnik.ime} ${r.korisnik.prezime}`
      : "",
    svrha: r.svrha,
  }));
}

function zauzetostUTermin(
  z: ZauzetostDto,
  sale: SalaDto[],
): TerminPodaci | null {
  const sala = sale.find((s) => s.id === z.salaId);
  if (!sala) return null;

  return {
    id: -Math.abs(
      z.salaId * 1_000_000 +
        Number(z.datumTermina.replaceAll("-", "")) +
        Number(z.vremeOd.replaceAll(":", "")),
    ),
    salaNaziv: sala.naziv,
    datum: z.datumTermina,
    vremeOd: vremeUDecimalni(z.vremeOd),
    vremeDo: vremeUDecimalni(z.vremeDo),
    status: z.statusStavke as Aktivan,
    brojOsoba: 0,
    korisnikImePrezime: "",
    svrha: { tip: "DOGADJAJ", naziv: "Zauzeto", opis: undefined },
  };
}

export async function fetchTerminiZaPeriod(
  od: string,
  doDatum: string,
  sale: SalaDto[],
): Promise<TerminPodaci[]> {
  const [mojeOdgovor, zauzetost] = await Promise.all([
    api.get<StranicaDto<RezervacijaDto>>("/rezervacija/moje-rezervacije", {
      params: { stranica: 0, velicina: 200 },
    }),
    api.get<ZauzetostDto[]>("/rezervacija/zauzetost", {
      params: { od, doDatum },
    }),
  ]);

  const mojiTermini = mojeOdgovor.data.sadrzaj
    .flatMap(rezervacijaUTermine)
    .filter((t) => t.datum >= od && t.datum <= doDatum);

  const mojKljuc = (t: TerminPodaci) =>
    `${t.salaNaziv}|${t.datum}|${t.vremeOd}|${t.vremeDo}`;
  const mojiKljucevi = new Set(mojiTermini.map(mojKljuc));

  const tudjiTermini = zauzetost.data
    .map((z) => zauzetostUTermin(z, sale))
    .filter(
      (t): t is TerminPodaci => t !== null && !mojiKljucevi.has(mojKljuc(t)),
    );

  return [...mojiTermini, ...tudjiTermini];
}
