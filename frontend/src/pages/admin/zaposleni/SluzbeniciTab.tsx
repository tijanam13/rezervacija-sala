import { useMemo, useState } from "react";
import { Pencil, Trash2, Mail, Phone, IdCard, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PotvrdaBrisanjaDijalog } from "@/components/common/PotvrdaBrisanjaDijalog";
import {
  kreirajSluzbenika,
  azurirajSluzbenika,
  obrisiSluzbenika,
} from "@/lib/zaposleniAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { selectItemsOd } from "@/lib/utils";
import type { SluzbenikDto, SluzbaDto } from "@/types";

const SELECT_PROPS = {
  side: "bottom" as const,
  sideOffset: 4,
  align: "start" as const,
};

const PRAZNA_FORMA = {
  ime: "",
  prezime: "",
  brojTelefona: "",
  brojRadneKnjizice: "",
  poslovniEmail: "",
  pozicija: "",
  sluzbaId: "",
};

interface SluzbeniciTabProps {
  sluzbenici: SluzbenikDto[];
  setSluzbenici: React.Dispatch<React.SetStateAction<SluzbenikDto[]>>;
  sluzbe: SluzbaDto[];
  javiUspeh: (tekst: string) => void;
}

export function SluzbeniciTab({
  sluzbenici,
  setSluzbenici,
  sluzbe,
  javiUspeh,
}: SluzbeniciTabProps) {
  const sluzbeItems = useMemo(() => selectItemsOd(sluzbe), [sluzbe]);

  const [dijalogOtvoren, setDijalogOtvoren] = useState(false);
  const [kojiSeUredjuje, setKojiSeUredjuje] = useState<SluzbenikDto | null>(
    null,
  );
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [formaGreska, setFormaGreska] = useState<string | null>(null);
  const [cuvaSe, setCuvaSe] = useState(false);

  function otvoriDodavanje() {
    setKojiSeUredjuje(null);
    setForma(PRAZNA_FORMA);
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  function otvoriIzmenu(s: SluzbenikDto) {
    setKojiSeUredjuje(s);
    setForma({
      ime: s.ime,
      prezime: s.prezime,
      brojTelefona: s.brojTelefona ?? "",
      brojRadneKnjizice: s.brojRadneKnjizice ?? "",
      poslovniEmail: s.poslovniEmail ?? "",
      pozicija: s.pozicija ?? "",
      sluzbaId: String(s.sluzba.id),
    });
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  async function sacuvaj() {
    if (!forma.ime.trim() || !forma.prezime.trim() || !forma.sluzbaId) {
      setFormaGreska("Ime, prezime i služba su obavezni podaci.");
      return;
    }

    const izabranaSluzba = sluzbe.find((s) => String(s.id) === forma.sluzbaId);
    if (!izabranaSluzba) {
      setFormaGreska("Izabrana služba nije pronađena.");
      return;
    }

    const dto = {
      ime: forma.ime.trim(),
      prezime: forma.prezime.trim(),
      brojTelefona: forma.brojTelefona.trim() || undefined,
      brojRadneKnjizice: forma.brojRadneKnjizice.trim() || undefined,
      poslovniEmail: forma.poslovniEmail.trim() || undefined,
      pozicija: forma.pozicija.trim() || undefined,
      sluzba: izabranaSluzba,
    };

    setCuvaSe(true);
    setFormaGreska(null);
    try {
      if (kojiSeUredjuje) {
        const azuriran = await azurirajSluzbenika(kojiSeUredjuje.id!, dto);
        setSluzbenici((prev) =>
          prev.map((s) => (s.id === azuriran.id ? azuriran : s)),
        );
        javiUspeh(
          `Profil "${azuriran.ime} ${azuriran.prezime}" je uspešno izmenjen.`,
        );
      } else {
        const kreiran = await kreirajSluzbenika(dto);
        setSluzbenici((prev) => [...prev, kreiran]);
        javiUspeh(
          `Profil "${kreiran.ime} ${kreiran.prezime}" je uspešno dodat.`,
        );
      }
      setDijalogOtvoren(false);
    } catch (err) {
      setFormaGreska(izvuciPorukuGreske(err));
    } finally {
      setCuvaSe(false);
    }
  }

  const [zaBrisanje, setZaBrisanje] = useState<SluzbenikDto | null>(null);
  const [brisanjeUToku, setBrisanjeUToku] = useState(false);
  const [brisanjeGreska, setBrisanjeGreska] = useState<string | null>(null);

  async function potvrdiBrisanje() {
    if (!zaBrisanje) return;
    setBrisanjeUToku(true);
    setBrisanjeGreska(null);
    try {
      await obrisiSluzbenika(zaBrisanje.id!);
      setSluzbenici((prev) => prev.filter((s) => s.id !== zaBrisanje.id));
      javiUspeh(`Profil "${zaBrisanje.ime} ${zaBrisanje.prezime}" je obrisan.`);
      setZaBrisanje(null);
    } catch (err) {
      setBrisanjeGreska(izvuciPorukuGreske(err));
    } finally {
      setBrisanjeUToku(false);
    }
  }

  return (
    <>
      <div className="mb-4 flex justify-end">
        <Button
          onClick={otvoriDodavanje}
          className="gap-1.5 bg-fon-teal text-fon-dark hover:bg-fon-teal/90"
        >
          <Plus size={16} />
          Dodaj službenika
        </Button>
      </div>

      {sluzbenici.length === 0 ? (
        <p className="py-10 text-center text-sm text-gray-500">
          Nema službenika za prikaz.
        </p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
              <tr>
                <th className="px-4 py-3 font-medium">Ime i prezime</th>
                <th className="px-4 py-3 font-medium">Kontakt</th>
                <th className="px-4 py-3 font-medium">Služba</th>
                <th className="px-4 py-3 font-medium">Pozicija</th>
                <th className="px-4 py-3 font-medium">Akcije</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {sluzbenici.map((s) => (
                <tr key={s.id}>
                  <td className="px-4 py-3 font-medium text-fon-dark">
                    {s.ime} {s.prezime}
                  </td>
                  <td className="px-4 py-3 text-gray-600">
                    <div className="flex flex-col gap-0.5">
                      {s.poslovniEmail && (
                        <span className="flex items-center gap-1.5">
                          <Mail size={12} className="text-fon-teal/70" />
                          {s.poslovniEmail}
                        </span>
                      )}
                      {s.brojTelefona && (
                        <span className="flex items-center gap-1.5">
                          <Phone size={12} className="text-fon-pink/60" />
                          {s.brojTelefona}
                        </span>
                      )}
                      {s.brojRadneKnjizice && (
                        <span className="flex items-center gap-1.5 text-xs text-gray-400">
                          <IdCard size={12} />
                          {s.brojRadneKnjizice}
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{s.sluzba.naziv}</td>
                  <td className="px-4 py-3 text-gray-600">
                    {s.pozicija || <span className="text-gray-400">—</span>}
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

      <Dialog open={dijalogOtvoren} onOpenChange={setDijalogOtvoren}>
        <DialogContent className="border-2 border-fon-navy bg-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-xl text-fon-navy">
              {kojiSeUredjuje
                ? "Izmena profila službenika"
                : "Dodavanje novog službenika"}
            </DialogTitle>
            <DialogDescription>
              {kojiSeUredjuje
                ? "Izmenite podatke o službeniku i sačuvajte promene."
                : "Novi profil se pravi BEZ naloga za prijavu - može se kasnije registrovati posebno."}
            </DialogDescription>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div className="grid grid-cols-2 gap-2">
              <Input
                placeholder="Ime"
                value={forma.ime}
                onChange={(e) =>
                  setForma((f) => ({ ...f, ime: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
              <Input
                placeholder="Prezime"
                value={forma.prezime}
                onChange={(e) =>
                  setForma((f) => ({ ...f, prezime: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
            </div>
            <div className="grid grid-cols-2 gap-2">
              <Input
                placeholder="Broj telefona"
                value={forma.brojTelefona}
                onChange={(e) =>
                  setForma((f) => ({ ...f, brojTelefona: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
              <Input
                placeholder="Broj radne knjižice"
                value={forma.brojRadneKnjizice}
                onChange={(e) =>
                  setForma((f) => ({ ...f, brojRadneKnjizice: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
            </div>
            <Input
              type="email"
              placeholder="Poslovni email"
              value={forma.poslovniEmail}
              onChange={(e) =>
                setForma((f) => ({ ...f, poslovniEmail: e.target.value }))
              }
              className="border-gray-200 bg-gray-50"
            />
            <Input
              placeholder="Pozicija"
              value={forma.pozicija}
              onChange={(e) =>
                setForma((f) => ({ ...f, pozicija: e.target.value }))
              }
              className="border-gray-200 bg-gray-50"
            />
            <Select
              items={sluzbeItems}
              value={forma.sluzbaId}
              onValueChange={(v) =>
                v !== null && setForma((f) => ({ ...f, sluzbaId: v }))
              }
            >
              <SelectTrigger className="w-full border-gray-200 bg-gray-50">
                <SelectValue placeholder="Izaberi službu" />
              </SelectTrigger>
              <SelectContent {...SELECT_PROPS}>
                {sluzbe.map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>
                    {s.naziv}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            {formaGreska && (
              <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-2.5 text-sm text-fon-coral">
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
              className="bg-fon-pink text-white hover:bg-fon-pink/90"
            >
              {cuvaSe
                ? "Čuvanje..."
                : kojiSeUredjuje
                  ? "Sačuvaj izmene"
                  : "Dodaj službenika"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <PotvrdaBrisanjaDijalog
        otvoren={zaBrisanje !== null}
        naslov="Brisanje profila službenika"
        opis={`Da li ste sigurni da želite da obrišete profil "${zaBrisanje?.ime} ${zaBrisanje?.prezime}"? Ovo je moguće samo ako profil nema povezan nalog za prijavu.`}
        greska={brisanjeGreska}
        brisanjeUToku={brisanjeUToku}
        onOtkazi={() => setZaBrisanje(null)}
        onPotvrdi={potvrdiBrisanje}
      />
    </>
  );
}
