import { useMemo, useState } from "react";
import { Plus, Pencil, Trash2, AlertCircle } from "lucide-react";
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
import { PotvrdaBrisanjaDijalog } from "@/components/common/PotvrdaBrisanjaDijalog";
import {
  kreirajTipSale,
  azurirajTipSale,
  obrisiTipSale,
} from "@/lib/salaAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import type { SalaDto, TipSaleDto } from "@/types";

const PRAZNA_FORMA = { naziv: "", opis: "" };

interface TipoviSaleTabProps {
  tipovi: TipSaleDto[];
  setTipovi: React.Dispatch<React.SetStateAction<TipSaleDto[]>>;
  sale: SalaDto[];
  setSale: React.Dispatch<React.SetStateAction<SalaDto[]>>;
  javiUspeh: (tekst: string) => void;
}

export function TipoviSaleTab({
  tipovi,
  setTipovi,
  sale,
  setSale,
  javiUspeh,
}: TipoviSaleTabProps) {
  const brojSalaPoTipu = useMemo(() => {
    const mapa = new Map<number, number>();
    for (const s of sale) {
      mapa.set(s.tipSale.id, (mapa.get(s.tipSale.id) ?? 0) + 1);
    }
    return mapa;
  }, [sale]);

  const [dijalogOtvoren, setDijalogOtvoren] = useState(false);
  const [kojiSeUredjuje, setKojiSeUredjuje] = useState<TipSaleDto | null>(null);
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [formaGreska, setFormaGreska] = useState<string | null>(null);
  const [cuvaSe, setCuvaSe] = useState(false);

  function otvoriDodavanje() {
    setKojiSeUredjuje(null);
    setForma(PRAZNA_FORMA);
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  function otvoriIzmenu(t: TipSaleDto) {
    setKojiSeUredjuje(t);
    setForma({ naziv: t.naziv, opis: t.opis ?? "" });
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  async function sacuvaj() {
    if (!forma.naziv.trim()) {
      setFormaGreska("Naziv tipa sale je obavezan.");
      return;
    }

    const dto = {
      naziv: forma.naziv.trim(),
      opis: forma.opis.trim() || undefined,
    };

    setCuvaSe(true);
    setFormaGreska(null);
    try {
      if (kojiSeUredjuje) {
        const azuriran = await azurirajTipSale(kojiSeUredjuje.id, dto);
        setTipovi((prev) =>
          prev.map((t) => (t.id === azuriran.id ? azuriran : t)),
        );
        setSale((prev) =>
          prev.map((s) =>
            s.tipSale.id === azuriran.id ? { ...s, tipSale: azuriran } : s,
          ),
        );
        javiUspeh(`Tip sale "${azuriran.naziv}" je uspešno izmenjen.`);
      } else {
        const novi = await kreirajTipSale(dto);
        setTipovi((prev) => [...prev, novi]);
        javiUspeh(`Tip sale "${novi.naziv}" je uspešno dodat.`);
      }
      setDijalogOtvoren(false);
    } catch (err) {
      setFormaGreska(izvuciPorukuGreske(err));
    } finally {
      setCuvaSe(false);
    }
  }

  const [zaBrisanje, setZaBrisanje] = useState<TipSaleDto | null>(null);
  const [brisanjeUToku, setBrisanjeUToku] = useState(false);
  const [brisanjeGreska, setBrisanjeGreska] = useState<string | null>(null);

  async function potvrdiBrisanje() {
    if (!zaBrisanje) return;
    setBrisanjeUToku(true);
    setBrisanjeGreska(null);
    try {
      await obrisiTipSale(zaBrisanje.id);
      setTipovi((prev) => prev.filter((t) => t.id !== zaBrisanje.id));
      javiUspeh(`Tip sale "${zaBrisanje.naziv}" je obrisan.`);
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
          className="bg-fon-teal text-fon-dark hover:bg-fon-teal/90"
        >
          <Plus size={16} />
          Dodaj tip sale
        </Button>
      </div>

      {tipovi.length === 0 ? (
        <p className="py-10 text-center text-sm text-gray-500">
          Nema tipova sala za prikaz.
        </p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
              <tr>
                <th className="px-4 py-3 font-medium">Naziv</th>
                <th className="px-4 py-3 font-medium">Opis</th>
                <th className="px-4 py-3 font-medium">Broj sala</th>
                <th className="px-4 py-3 font-medium">Akcije</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {tipovi.map((t) => (
                <tr key={t.id}>
                  <td className="px-4 py-3 font-medium text-fon-dark">
                    {t.naziv}
                  </td>
                  <td className="px-4 py-3 text-gray-600">
                    {t.opis || <span className="text-gray-400">—</span>}
                  </td>
                  <td className="px-4 py-3">
                    <Badge className="bg-fon-blue/10 text-fon-blue">
                      {brojSalaPoTipu.get(t.id) ?? 0}
                    </Badge>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-1.5">
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
                        onClick={() => otvoriIzmenu(t)}
                      >
                        <Pencil size={14} />
                        Izmeni
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                        onClick={() => {
                          setZaBrisanje(t);
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
              {kojiSeUredjuje ? "Izmena tipa sale" : "Dodavanje tipa sale"}
            </DialogTitle>
            <DialogDescription>
              {kojiSeUredjuje
                ? "Izmenite podatke o tipu sale i sačuvajte promene."
                : "Unesite podatke o novom tipu sale (npr. amfiteatar, kabinet)."}
            </DialogDescription>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <Input
              placeholder="Naziv tipa (npr. Amfiteatar)"
              value={forma.naziv}
              onChange={(e) =>
                setForma((f) => ({ ...f, naziv: e.target.value }))
              }
              className="border-gray-200 bg-gray-50"
            />
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
              className="bg-fon-teal text-fon-dark hover:bg-fon-teal/90"
            >
              {cuvaSe ? "Čuvanje..." : "Sačuvaj"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <PotvrdaBrisanjaDijalog
        otvoren={zaBrisanje !== null}
        naslov="Brisanje tipa sale"
        opis={`Da li ste sigurni da želite da obrišete tip sale "${zaBrisanje?.naziv}"? Ova akcija je trajna.`}
        greska={brisanjeGreska}
        brisanjeUToku={brisanjeUToku}
        onOtkazi={() => setZaBrisanje(null)}
        onPotvrdi={potvrdiBrisanje}
      />
    </>
  );
}
