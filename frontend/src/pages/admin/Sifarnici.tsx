import { useEffect, useState, useCallback } from "react";
import {
  Building2,
  GraduationCap,
  Briefcase,
  Plus,
  Pencil,
  Trash2,
  AlertCircle,
  Search,
} from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  fetchKatedreAdmin,
  kreirajKatedru,
  azurirajKatedru,
  obrisiKatedru,
  fetchZvanjaAdmin,
  kreirajZvanje,
  azurirajZvanje,
  obrisiZvanje,
  fetchSluzbeAdmin,
  kreirajSluzbu,
  azurirajSluzbu,
  obrisiSluzbu,
} from "@/lib/sifarniciAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { PorukaBanner } from "@/components/common/PorukaBanner";
import { PotvrdaBrisanjaDijalog } from "@/components/common/PotvrdaBrisanjaDijalog";
import { usePorukaGreske } from "@/lib/usePorukaGreske";
import type { KatedraDto, ZvanjeDto, SluzbaDto } from "@/types";

type Tab = "katedre" | "zvanja" | "sluzbe";

const TAB_STIL: Record<
  Tab,
  {
    naziv: string;
    nazivJednine: string;
    nazivGenitiv: string;
    nazivAkuzativ: string;
    nastavak: "a" | "o";
    ikona: typeof Building2;
    aktivniTab: string;
    bedz: string;
    dugme: string;
    porukaBrisanja: string;
  }
> = {
  katedre: {
    naziv: "Katedre",
    nazivJednine: "katedra",
    nazivGenitiv: "katedre",
    nazivAkuzativ: "katedru",
    nastavak: "a",
    ikona: Building2,
    aktivniTab: "bg-fon-blue text-white shadow-sm",
    bedz: "bg-fon-blue/10 text-fon-blue",
    dugme: "bg-fon-blue text-white hover:bg-fon-blue/90",
    porukaBrisanja: "Ako je vezana za predavača, brisanje neće uspeti.",
  },
  zvanja: {
    naziv: "Zvanja",
    nazivJednine: "zvanje",
    nazivGenitiv: "zvanja",
    nazivAkuzativ: "zvanje",
    nastavak: "o",
    ikona: GraduationCap,
    aktivniTab: "bg-fon-purple text-white shadow-sm",
    bedz: "bg-fon-purple/10 text-fon-purple",
    dugme: "bg-fon-purple text-white hover:bg-fon-purple/90",
    porukaBrisanja: "Ako je vezano za predavača, brisanje neće uspeti.",
  },
  sluzbe: {
    naziv: "Službe",
    nazivJednine: "služba",
    nazivGenitiv: "službe",
    nazivAkuzativ: "službu",
    nastavak: "a",
    ikona: Briefcase,
    aktivniTab: "bg-fon-pink text-white shadow-sm",
    bedz: "bg-fon-pink/10 text-fon-pink",
    dugme: "bg-fon-pink text-white hover:bg-fon-pink/90",
    porukaBrisanja: "Ako je vezana za službenika, brisanje neće uspeti.",
  },
};

interface Stavka {
  id: number;
  naziv: string;
  opis?: string;
}

const PRAZNA_FORMA = { naziv: "", opis: "" };

function velikoPrvoSlovo(tekst: string): string {
  return tekst.charAt(0).toUpperCase() + tekst.slice(1);
}

export default function Sifarnici() {
  const [tab, setTab] = useState<Tab>("katedre");

  const [katedre, setKatedre] = useState<KatedraDto[]>([]);
  const [zvanja, setZvanja] = useState<ZvanjeDto[]>([]);
  const [sluzbe, setSluzbe] = useState<SluzbaDto[]>([]);

  const [ucitava, setUcitava] = useState(true);
  const { poruka, greska, javiUspeh, setGreska } = usePorukaGreske();

  const ucitajSve = useCallback(async () => {
    setUcitava(true);
    setGreska(null);
    try {
      const [katedreData, zvanjaData, sluzbeData] = await Promise.all([
        fetchKatedreAdmin(),
        fetchZvanjaAdmin(),
        fetchSluzbeAdmin(),
      ]);
      setKatedre(katedreData);
      setZvanja(zvanjaData);
      setSluzbe(sluzbeData);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setUcitava(false);
    }
  }, [setGreska]);

  useEffect(() => {
    ucitajSve();
  }, [ucitajSve]);

  const akcije: Record<
    Tab,
    {
      lista: Stavka[];
      postaviListu: (l: Stavka[]) => void;
      kreiraj: (dto: { naziv: string; opis?: string }) => Promise<Stavka>;
      azuriraj: (
        id: number,
        dto: { naziv: string; opis?: string },
      ) => Promise<Stavka>;
      obrisi: (id: number) => Promise<void>;
    }
  > = {
    katedre: {
      lista: katedre,
      postaviListu: (l) => setKatedre(l as KatedraDto[]),
      kreiraj: kreirajKatedru,
      azuriraj: azurirajKatedru,
      obrisi: obrisiKatedru,
    },
    zvanja: {
      lista: zvanja,
      postaviListu: (l) => setZvanja(l as ZvanjeDto[]),
      kreiraj: kreirajZvanje,
      azuriraj: azurirajZvanje,
      obrisi: obrisiZvanje,
    },
    sluzbe: {
      lista: sluzbe,
      postaviListu: (l) => setSluzbe(l as SluzbaDto[]),
      kreiraj: kreirajSluzbu,
      azuriraj: azurirajSluzbu,
      obrisi: obrisiSluzbu,
    },
  };

  const [dijalogOtvoren, setDijalogOtvoren] = useState(false);
  const [stavkaKojaSeUredjuje, setStavkaKojaSeUredjuje] =
    useState<Stavka | null>(null);
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [formaGreska, setFormaGreska] = useState<string | null>(null);
  const [cuvaSe, setCuvaSe] = useState(false);

  function otvoriDodavanje() {
    setStavkaKojaSeUredjuje(null);
    setForma(PRAZNA_FORMA);
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  function otvoriIzmenu(s: Stavka) {
    setStavkaKojaSeUredjuje(s);
    setForma({ naziv: s.naziv, opis: s.opis ?? "" });
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  async function sacuvaj() {
    if (!forma.naziv.trim()) {
      setFormaGreska(`Naziv ${TAB_STIL[tab].nazivGenitiv} je obavezan.`);
      return;
    }

    const dto = {
      naziv: forma.naziv.trim(),
      opis: forma.opis.trim() || undefined,
    };

    const { azuriraj, kreiraj, lista, postaviListu } = akcije[tab];

    setCuvaSe(true);
    setFormaGreska(null);
    try {
      if (stavkaKojaSeUredjuje) {
        const azurirana = await azuriraj(stavkaKojaSeUredjuje.id, dto);
        postaviListu(lista.map((s) => (s.id === azurirana.id ? azurirana : s)));
        javiUspeh(
          `${velikoPrvoSlovo(TAB_STIL[tab].nazivJednine)} "${azurirana.naziv}" je uspešno izmenjen${TAB_STIL[tab].nastavak}.`,
        );
      } else {
        const nova = await kreiraj(dto);
        postaviListu([...lista, nova]);
        javiUspeh(
          `${velikoPrvoSlovo(TAB_STIL[tab].nazivJednine)} "${nova.naziv}" je uspešno dodat${TAB_STIL[tab].nastavak}.`,
        );
      }
      setDijalogOtvoren(false);
    } catch (err) {
      setFormaGreska(izvuciPorukuGreske(err));
    } finally {
      setCuvaSe(false);
    }
  }

  const [zaBrisanje, setZaBrisanje] = useState<Stavka | null>(null);
  const [brisanjeUToku, setBrisanjeUToku] = useState(false);
  const [brisanjeGreska, setBrisanjeGreska] = useState<string | null>(null);

  async function potvrdiBrisanje() {
    if (!zaBrisanje) return;
    const { obrisi, lista, postaviListu } = akcije[tab];

    setBrisanjeUToku(true);
    setBrisanjeGreska(null);
    try {
      await obrisi(zaBrisanje.id);
      postaviListu(lista.filter((s) => s.id !== zaBrisanje.id));
      javiUspeh(
        `${velikoPrvoSlovo(TAB_STIL[tab].nazivJednine)} "${zaBrisanje.naziv}" je obrisan${TAB_STIL[tab].nastavak}.`,
      );
      setZaBrisanje(null);
    } catch (err) {
      setBrisanjeGreska(izvuciPorukuGreske(err));
    } finally {
      setBrisanjeUToku(false);
    }
  }

  const stilAktivnogTaba = TAB_STIL[tab];
  const listaAktivnogTaba = akcije[tab].lista;

  const [pretraga, setPretraga] = useState("");
  const listaFiltrirana = listaAktivnogTaba.filter((s) =>
    s.naziv.toLowerCase().includes(pretraga.toLowerCase()),
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-4xl p-6">
        <div className="mb-6">
          <p className="text-2xl font-semibold text-fon-navy">
            Katedre, zvanja i službe
          </p>
          <p className="text-base text-gray-600">
            Pregledajte, dodajte, izmenite ili obrišite katedre, zvanja
            nastavnog osoblja i službe.
          </p>
        </div>

        <div className="mb-6 flex gap-1 rounded-2xl border border-gray-100 bg-white p-1.5 shadow-sm">
          {(Object.keys(TAB_STIL) as Tab[]).map((t) => {
            const stil = TAB_STIL[t];
            const Ikona = stil.ikona;
            return (
              <button
                key={t}
                type="button"
                onClick={() => {
                  setTab(t);
                  setPretraga("");
                }}
                className={`flex flex-1 items-center justify-center gap-2 rounded-lg py-2 text-sm font-medium transition-colors ${
                  tab === t
                    ? stil.aktivniTab
                    : "text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
                }`}
              >
                <Ikona size={16} />
                {stil.naziv}
                <Badge
                  className={tab === t ? "bg-white/15 text-white" : stil.bedz}
                >
                  {akcije[t].lista.length}
                </Badge>
              </button>
            );
          })}
        </div>

        <PorukaBanner poruka={poruka} greska={greska} />

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje podataka...
          </p>
        ) : (
          <>
            <div className="mb-4 flex items-center justify-between gap-3">
              <div className="relative max-w-xs flex-1">
                <Search
                  size={16}
                  className="absolute top-1/2 left-3 -translate-y-1/2 text-gray-400"
                />
                <Input
                  placeholder={`Pretraži ${stilAktivnogTaba.naziv.toLowerCase()}...`}
                  value={pretraga}
                  onChange={(e) => setPretraga(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Button
                onClick={otvoriDodavanje}
                className={stilAktivnogTaba.dugme}
              >
                <Plus size={16} />
                Dodaj {stilAktivnogTaba.nazivAkuzativ}
              </Button>
            </div>

            {listaFiltrirana.length === 0 ? (
              <p className="py-10 text-center text-sm text-gray-500">
                {listaAktivnogTaba.length === 0
                  ? "Nema stavki za prikaz."
                  : "Nema stavki koje odgovaraju pretrazi."}
              </p>
            ) : (
              <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
                <table className="w-full text-left text-sm">
                  <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
                    <tr>
                      <th className="px-4 py-3 font-medium">Naziv</th>
                      <th className="px-4 py-3 font-medium">Opis</th>
                      <th className="px-4 py-3 font-medium">Akcije</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {listaFiltrirana.map((s) => (
                      <tr key={s.id}>
                        <td className="px-4 py-3 font-medium text-fon-dark">
                          {s.naziv}
                        </td>
                        <td className="px-4 py-3 text-gray-600">
                          {s.opis || <span className="text-gray-400">—</span>}
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex gap-1.5">
                            <Button
                              size="sm"
                              variant="outline"
                              className="border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
                              onClick={() => otvoriIzmenu(s)}
                            >
                              <Pencil size={14} />
                              Izmeni
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              className="border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                              onClick={() => {
                                setZaBrisanje(s);
                                setBrisanjeGreska(null);
                              }}
                            >
                              <Trash2 size={14} />
                              Obriši
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>

      <Dialog open={dijalogOtvoren} onOpenChange={setDijalogOtvoren}>
        <DialogContent className="border-2 border-fon-navy bg-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-xl text-fon-navy">
              {stavkaKojaSeUredjuje
                ? `Izmena (${stilAktivnogTaba.nazivJednine})`
                : `Dodavanje (${stilAktivnogTaba.nazivJednine})`}
            </DialogTitle>
            <DialogDescription>
              {stavkaKojaSeUredjuje
                ? "Izmenite podatke i sačuvajte promene."
                : "Unesite podatke o novoj stavci."}
            </DialogDescription>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div>
              <label className="mb-1 block text-sm text-gray-600">
                Naziv <span className="text-red-500">*</span>
              </label>
              <Input
                placeholder="Naziv"
                value={forma.naziv}
                onChange={(e) =>
                  setForma((f) => ({ ...f, naziv: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
            </div>
            <Input
              placeholder="Opis (opciono)"
              value={forma.opis}
              onChange={(e) =>
                setForma((f) => ({ ...f, opis: e.target.value }))
              }
              className="border-gray-200 bg-gray-50"
            />

            {formaGreska && (
              <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-2.5 text-sm text-fon-coral">
                <AlertCircle size={16} className="mt-0.5 shrink-0" />
                <span>{formaGreska}</span>
              </div>
            )}
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setDijalogOtvoren(false)}
              disabled={cuvaSe}
            >
              Otkaži
            </Button>
            <Button
              onClick={sacuvaj}
              disabled={cuvaSe}
              className={stilAktivnogTaba.dugme}
            >
              {cuvaSe ? "Čuvanje..." : "Sačuvaj"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <PotvrdaBrisanjaDijalog
        otvoren={zaBrisanje !== null}
        naslov="Brisanje"
        opis={`Da li ste sigurni da želite da obrišete "${zaBrisanje?.naziv}"? Ova akcija je trajna. ${TAB_STIL[tab].porukaBrisanja}`}
        greska={brisanjeGreska}
        brisanjeUToku={brisanjeUToku}
        onOtkazi={() => setZaBrisanje(null)}
        onPotvrdi={potvrdiBrisanje}
      />
    </div>
  );
}
