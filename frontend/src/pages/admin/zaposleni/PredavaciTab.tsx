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
  kreirajPredavaca,
  azurirajPredavaca,
  obrisiPredavaca,
} from "@/lib/zaposleniAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { selectItemsOd } from "@/lib/utils";
import type { PredavacDto, KatedraDto, ZvanjeDto } from "@/types";

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
  titula: "",
  terminKonsultacija: "",
  katedraId: "",
  zvanjeId: "",
};

interface PredavaciTabProps {
  predavaci: PredavacDto[];
  setPredavaci: React.Dispatch<React.SetStateAction<PredavacDto[]>>;
  katedre: KatedraDto[];
  zvanja: ZvanjeDto[];
  javiUspeh: (tekst: string) => void;
}

export function PredavaciTab({
  predavaci,
  setPredavaci,
  katedre,
  zvanja,
  javiUspeh,
}: PredavaciTabProps) {
  const katedreItems = useMemo(() => selectItemsOd(katedre), [katedre]);
  const zvanjaItems = useMemo(() => selectItemsOd(zvanja), [zvanja]);

  const [dijalogOtvoren, setDijalogOtvoren] = useState(false);
  const [kojiSeUredjuje, setKojiSeUredjuje] = useState<PredavacDto | null>(
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

  function otvoriIzmenu(p: PredavacDto) {
    setKojiSeUredjuje(p);
    setForma({
      ime: p.ime,
      prezime: p.prezime,
      brojTelefona: p.brojTelefona ?? "",
      brojRadneKnjizice: p.brojRadneKnjizice ?? "",
      poslovniEmail: p.poslovniEmail ?? "",
      titula: p.titula ?? "",
      terminKonsultacija: p.terminKonsultacija ?? "",
      katedraId: String(p.katedra.id),
      zvanjeId: String(p.zvanje.id),
    });
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  async function sacuvaj() {
    if (
      !forma.ime.trim() ||
      !forma.prezime.trim() ||
      !forma.katedraId ||
      !forma.zvanjeId
    ) {
      setFormaGreska("Ime, prezime, katedra i zvanje su obavezni podaci.");
      return;
    }

    const izabranaKatedra = katedre.find(
      (k) => String(k.id) === forma.katedraId,
    );
    const izabranoZvanje = zvanja.find((z) => String(z.id) === forma.zvanjeId);
    if (!izabranaKatedra || !izabranoZvanje) {
      setFormaGreska("Izabrana katedra ili zvanje nisu pronađeni.");
      return;
    }

    const dto = {
      ime: forma.ime.trim(),
      prezime: forma.prezime.trim(),
      brojTelefona: forma.brojTelefona.trim() || undefined,
      brojRadneKnjizice: forma.brojRadneKnjizice.trim() || undefined,
      poslovniEmail: forma.poslovniEmail.trim() || undefined,
      titula: forma.titula.trim() || undefined,
      terminKonsultacija: forma.terminKonsultacija.trim() || undefined,
      katedra: izabranaKatedra,
      zvanje: izabranoZvanje,
    };

    setCuvaSe(true);
    setFormaGreska(null);
    try {
      if (kojiSeUredjuje) {
        const azuriran = await azurirajPredavaca(kojiSeUredjuje.id!, dto);
        setPredavaci((prev) =>
          prev.map((p) => (p.id === azuriran.id ? azuriran : p)),
        );
        javiUspeh(
          `Profil "${azuriran.ime} ${azuriran.prezime}" je uspešno izmenjen.`,
        );
      } else {
        const kreiran = await kreirajPredavaca(dto);
        setPredavaci((prev) => [...prev, kreiran]);
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

  const [zaBrisanje, setZaBrisanje] = useState<PredavacDto | null>(null);
  const [brisanjeUToku, setBrisanjeUToku] = useState(false);
  const [brisanjeGreska, setBrisanjeGreska] = useState<string | null>(null);

  async function potvrdiBrisanje() {
    if (!zaBrisanje) return;
    setBrisanjeUToku(true);
    setBrisanjeGreska(null);
    try {
      await obrisiPredavaca(zaBrisanje.id!);
      setPredavaci((prev) => prev.filter((p) => p.id !== zaBrisanje.id));
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
          Dodaj predavača
        </Button>
      </div>

      {predavaci.length === 0 ? (
        <p className="py-10 text-center text-sm text-gray-500">
          Nema predavača za prikaz.
        </p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
              <tr>
                <th className="px-4 py-3 font-medium">Ime i prezime</th>
                <th className="px-4 py-3 font-medium">Kontakt</th>
                <th className="px-4 py-3 font-medium">Katedra</th>
                <th className="px-4 py-3 font-medium">Zvanje</th>
                <th className="px-4 py-3 font-medium">Titula</th>
                <th className="px-4 py-3 font-medium">Akcije</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {predavaci.map((p) => (
                <tr key={p.id}>
                  <td className="px-4 py-3 font-medium text-fon-dark">
                    {p.ime} {p.prezime}
                  </td>
                  <td className="px-4 py-3 text-gray-600">
                    <div className="flex flex-col gap-0.5">
                      {p.poslovniEmail && (
                        <span className="flex items-center gap-1.5">
                          <Mail size={12} className="text-fon-teal/70" />
                          {p.poslovniEmail}
                        </span>
                      )}
                      {p.brojTelefona && (
                        <span className="flex items-center gap-1.5">
                          <Phone size={12} className="text-fon-blue/60" />
                          {p.brojTelefona}
                        </span>
                      )}
                      {p.brojRadneKnjizice && (
                        <span className="flex items-center gap-1.5 text-xs text-gray-400">
                          <IdCard size={12} />
                          {p.brojRadneKnjizice}
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{p.katedra.naziv}</td>
                  <td className="px-4 py-3 text-gray-600">{p.zvanje.naziv}</td>
                  <td className="px-4 py-3 text-gray-600">
                    {p.titula || <span className="text-gray-400">—</span>}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-1.5">
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
                        onClick={() => otvoriIzmenu(p)}
                      >
                        <Pencil size={14} />
                        Izmeni
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                        onClick={() => {
                          setZaBrisanje(p);
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
                ? "Izmena profila predavača"
                : "Dodavanje novog predavača"}
            </DialogTitle>
            <DialogDescription>
              {kojiSeUredjuje
                ? "Izmenite podatke o predavaču i sačuvajte promene."
                : "Novi profil se pravi BEZ naloga za prijavu - može se kasnije registrovati posebno."}
            </DialogDescription>
          </DialogHeader>

          <div className="flex max-h-[60vh] flex-col gap-3 overflow-y-auto pr-1">
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
              placeholder="Titula (npr. Docent)"
              value={forma.titula}
              onChange={(e) =>
                setForma((f) => ({ ...f, titula: e.target.value }))
              }
              className="border-gray-200 bg-gray-50"
            />
            <Input
              placeholder="Termin konsultacija"
              value={forma.terminKonsultacija}
              onChange={(e) =>
                setForma((f) => ({ ...f, terminKonsultacija: e.target.value }))
              }
              className="border-gray-200 bg-gray-50"
            />
            <Select
              items={katedreItems}
              value={forma.katedraId}
              onValueChange={(v) =>
                v !== null && setForma((f) => ({ ...f, katedraId: v }))
              }
            >
              <SelectTrigger className="w-full border-gray-200 bg-gray-50">
                <SelectValue placeholder="Izaberi katedru" />
              </SelectTrigger>
              <SelectContent {...SELECT_PROPS}>
                {katedre.map((k) => (
                  <SelectItem key={k.id} value={String(k.id)}>
                    {k.naziv}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              items={zvanjaItems}
              value={forma.zvanjeId}
              onValueChange={(v) =>
                v !== null && setForma((f) => ({ ...f, zvanjeId: v }))
              }
            >
              <SelectTrigger className="w-full border-gray-200 bg-gray-50">
                <SelectValue placeholder="Izaberi zvanje" />
              </SelectTrigger>
              <SelectContent {...SELECT_PROPS}>
                {zvanja.map((z) => (
                  <SelectItem key={z.id} value={String(z.id)}>
                    {z.naziv}
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
              className="bg-fon-blue text-white hover:bg-fon-blue/90"
            >
              {cuvaSe
                ? "Čuvanje..."
                : kojiSeUredjuje
                  ? "Sačuvaj izmene"
                  : "Dodaj predavača"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <PotvrdaBrisanjaDijalog
        otvoren={zaBrisanje !== null}
        naslov="Brisanje profila predavača"
        opis={`Da li ste sigurni da želite da obrišete profil "${zaBrisanje?.ime} ${zaBrisanje?.prezime}"? Ovo je moguće samo ako profil nema povezan nalog za prijavu i nije naveden kao mentor/član komisije ni na jednom završnom radu.`}
        greska={brisanjeGreska}
        brisanjeUToku={brisanjeUToku}
        onOtkazi={() => setZaBrisanje(null)}
        onPotvrdi={potvrdiBrisanje}
      />
    </>
  );
}
