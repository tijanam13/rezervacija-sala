import { useMemo, useState } from "react";
import {
  Plus,
  Pencil,
  Trash2,
  AlertCircle,
  Building2,
  Users,
  Monitor,
  Search,
} from "lucide-react";
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
  kreirajSalu,
  azurirajSalu,
  promeniStatusSale,
  obrisiSalu,
} from "@/lib/salaAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { getStatusSaleStyle } from "@/lib/statusColors";
import { selectItemsOd } from "@/lib/utils";
import type { SalaDto, TipSaleDto, StatusSale } from "@/types";

const SELECT_PROPS = {
  side: "bottom" as const,
  sideOffset: 4,
  align: "start" as const,
};

const PRAZNA_FORMA = {
  naziv: "",
  zgrada: "",
  sprat: "",
  kapacitet: "",
  brojRacunara: "",
  tipSaleId: "",
};

interface SaleTabProps {
  sale: SalaDto[];
  setSale: React.Dispatch<React.SetStateAction<SalaDto[]>>;
  tipovi: TipSaleDto[];
  javiUspeh: (tekst: string) => void;
  onGreska: (tekst: string) => void;
}

export function SaleTab({
  sale,
  setSale,
  tipovi,
  javiUspeh,
  onGreska,
}: SaleTabProps) {
  const tipoviItems = useMemo(() => selectItemsOd(tipovi), [tipovi]);
  const [pretraga, setPretraga] = useState("");
  const saleFiltrirane = useMemo(
    () =>
      sale.filter(
        (s) =>
          s.naziv.toLowerCase().includes(pretraga.toLowerCase()) ||
          s.zgrada.toLowerCase().includes(pretraga.toLowerCase()),
      ),
    [sale, pretraga],
  );

  const [dijalogOtvoren, setDijalogOtvoren] = useState(false);
  const [kojaSeUredjuje, setKojaSeUredjuje] = useState<SalaDto | null>(null);
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [formaGreska, setFormaGreska] = useState<string | null>(null);
  const [cuvaSe, setCuvaSe] = useState(false);

  function otvoriDodavanje() {
    setKojaSeUredjuje(null);
    setForma(PRAZNA_FORMA);
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  function otvoriIzmenu(s: SalaDto) {
    setKojaSeUredjuje(s);
    setForma({
      naziv: s.naziv,
      zgrada: s.zgrada,
      sprat: String(s.sprat),
      kapacitet: String(s.kapacitet),
      brojRacunara: String(s.brojRacunara),
      tipSaleId: String(s.tipSale.id),
    });
    setFormaGreska(null);
    setDijalogOtvoren(true);
  }

  async function sacuvaj() {
    if (
      !forma.naziv.trim() ||
      !forma.zgrada.trim() ||
      forma.sprat === "" ||
      forma.kapacitet === "" ||
      forma.brojRacunara === "" ||
      !forma.tipSaleId
    ) {
      setFormaGreska("Popunite sva polja pre čuvanja.");
      return;
    }

    const izabraniTip = tipovi.find((t) => String(t.id) === forma.tipSaleId);
    if (!izabraniTip) {
      setFormaGreska("Izabrani tip sale nije pronađen.");
      return;
    }

    const dto = {
      naziv: forma.naziv.trim(),
      zgrada: forma.zgrada.trim(),
      sprat: Number(forma.sprat),
      kapacitet: Number(forma.kapacitet),
      brojRacunara: Number(forma.brojRacunara),
      tipSale: izabraniTip,
    };

    setCuvaSe(true);
    setFormaGreska(null);
    try {
      if (kojaSeUredjuje) {
        const azurirana = await azurirajSalu(kojaSeUredjuje.id, dto);
        setSale((prev) =>
          prev.map((s) => (s.id === azurirana.id ? azurirana : s)),
        );
        javiUspeh(`Sala "${azurirana.naziv}" je uspešno izmenjena.`);
      } else {
        const nova = await kreirajSalu(dto);
        setSale((prev) => [...prev, nova]);
        javiUspeh(`Sala "${nova.naziv}" je uspešno dodata.`);
      }
      setDijalogOtvoren(false);
    } catch (err) {
      setFormaGreska(izvuciPorukuGreske(err));
    } finally {
      setCuvaSe(false);
    }
  }

  const [statusUToku, setStatusUToku] = useState<number | null>(null);
  const [statusGreska, setStatusGreska] = useState<string | null>(null);
  async function promeniStatus(s: SalaDto, noviStatus: StatusSale) {
    if (s.status === noviStatus) return;
    setStatusUToku(s.id);
    try {
      const azurirana = await promeniStatusSale(s.id, noviStatus);
      setSale((prev) =>
        prev.map((x) => (x.id === azurirana.id ? azurirana : x)),
      );
    } catch (err) {
      setStatusGreska(izvuciPorukuGreske(err));
    } finally {
      setStatusUToku(null);
    }
  }

  const [zaBrisanje, setZaBrisanje] = useState<SalaDto | null>(null);
  const [brisanjeUToku, setBrisanjeUToku] = useState(false);
  const [brisanjeGreska, setBrisanjeGreska] = useState<string | null>(null);

  async function potvrdiBrisanje() {
    if (!zaBrisanje) return;
    setBrisanjeUToku(true);
    setBrisanjeGreska(null);
    try {
      await obrisiSalu(zaBrisanje.id);
      setSale((prev) => prev.filter((s) => s.id !== zaBrisanje.id));
      javiUspeh(`Sala "${zaBrisanje.naziv}" je obrisana.`);
      setZaBrisanje(null);
    } catch (err) {
      setBrisanjeGreska(izvuciPorukuGreske(err));
    } finally {
      setBrisanjeUToku(false);
    }
  }

  return (
    <>
      <div className="mb-4 flex items-center justify-between gap-3">
        <div className="relative max-w-xs flex-1">
          <Search
            size={16}
            className="absolute top-1/2 left-3 -translate-y-1/2 text-gray-400"
          />
          <Input
            placeholder="Pretraži salu po nazivu ili zgradi..."
            value={pretraga}
            onChange={(e) => setPretraga(e.target.value)}
            className="pl-9"
          />
        </div>
        <Button
          onClick={otvoriDodavanje}
          disabled={tipovi.length === 0}
          title={
            tipovi.length === 0 ? "Prvo dodajte bar jedan tip sale" : undefined
          }
          className="bg-fon-teal text-fon-dark hover:bg-fon-teal/90"
        >
          <Plus size={16} />
          Dodaj salu
        </Button>
      </div>

      {saleFiltrirane.length === 0 ? (
        <p className="py-10 text-center text-sm text-gray-500">
          {sale.length === 0
            ? "Nema sala za prikaz."
            : "Nema sala koje odgovaraju pretrazi."}
        </p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
              <tr>
                <th className="px-4 py-3 font-medium">Naziv</th>
                <th className="px-4 py-3 font-medium">Zgrada / sprat</th>
                <th className="px-4 py-3 font-medium">Kapacitet</th>
                <th className="px-4 py-3 font-medium">Računari</th>
                <th className="px-4 py-3 font-medium">Tip sale</th>
                <th className="w-[152px] px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Akcije</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {saleFiltrirane.map((s) => {
                const stil = getStatusSaleStyle(s.status);
                const uAkciji = statusUToku === s.id;
                return (
                  <tr key={s.id}>
                    <td className="px-4 py-3 font-medium text-fon-dark">
                      {s.naziv}
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      <div className="flex items-center gap-1.5">
                        <Building2 size={14} className="text-fon-blue/60" />
                        {s.zgrada}, sprat {s.sprat}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      <div className="flex items-center gap-1.5">
                        <Users size={14} className="text-fon-blue/60" />
                        {s.kapacitet}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      <div className="flex items-center gap-1.5">
                        <Monitor size={14} className="text-fon-blue/60" />
                        {s.brojRacunara}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {s.tipSale.naziv}
                    </td>
                    <td className="px-4 py-3">
                      <Select
                        items={{
                          SLOBODNA: "Slobodna",
                          ZAUZETA: "Zauzeta",
                          VAN_UPOTREBE: "Van upotrebe",
                        }}
                        value={s.status}
                        onValueChange={(v) =>
                          v && promeniStatus(s, v as StatusSale)
                        }
                        disabled={uAkciji}
                      >
                        <SelectTrigger
                          size="sm"
                          className={`w-[136px] border ${stil.border} ${stil.bg} ${stil.text} font-medium`}
                        >
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent {...SELECT_PROPS}>
                          <SelectItem value="SLOBODNA">Slobodna</SelectItem>
                          <SelectItem value="ZAUZETA">Zauzeta</SelectItem>
                          <SelectItem value="VAN_UPOTREBE">
                            Van upotrebe
                          </SelectItem>
                        </SelectContent>
                      </Select>
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
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <Dialog open={dijalogOtvoren} onOpenChange={setDijalogOtvoren}>
        <DialogContent className="border-2 border-fon-navy bg-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-xl text-fon-navy">
              {kojaSeUredjuje ? "Izmena sale" : "Dodavanje nove sale"}
            </DialogTitle>
            <DialogDescription>
              {kojaSeUredjuje
                ? "Izmenite podatke o sali i sačuvajte promene."
                : "Unesite podatke o novoj sali."}
            </DialogDescription>
          </DialogHeader>

          <div className="flex flex-col gap-3">
            <div>
              <label className="mb-1 block text-sm text-gray-600">
                Naziv sale <span className="text-red-500">*</span>
              </label>
              <Input
                placeholder="Naziv sale (npr. 301)"
                value={forma.naziv}
                onChange={(e) =>
                  setForma((f) => ({ ...f, naziv: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">
                Zgrada <span className="text-red-500">*</span>
              </label>
              <Input
                placeholder="Zgrada"
                value={forma.zgrada}
                onChange={(e) =>
                  setForma((f) => ({ ...f, zgrada: e.target.value }))
                }
                className="border-gray-200 bg-gray-50"
              />
            </div>
            <div className="grid grid-cols-3 gap-2">
              <div>
                <label className="mb-1 block text-sm text-gray-600">
                  Sprat <span className="text-red-500">*</span>
                </label>
                <Input
                  type="number"
                  placeholder="Sprat"
                  value={forma.sprat}
                  onChange={(e) =>
                    setForma((f) => ({ ...f, sprat: e.target.value }))
                  }
                  className="border-gray-200 bg-gray-50"
                />
              </div>
              <div>
                <label className="mb-1 block text-sm text-gray-600">
                  Kapacitet <span className="text-red-500">*</span>
                </label>
                <Input
                  type="number"
                  placeholder="Kapacitet"
                  value={forma.kapacitet}
                  onChange={(e) =>
                    setForma((f) => ({ ...f, kapacitet: e.target.value }))
                  }
                  className="border-gray-200 bg-gray-50"
                />
              </div>
              <div>
                <label className="mb-1 block text-sm text-gray-600">
                  Računari <span className="text-red-500">*</span>
                </label>
                <Input
                  type="number"
                  placeholder="Računari"
                  value={forma.brojRacunara}
                  onChange={(e) =>
                    setForma((f) => ({ ...f, brojRacunara: e.target.value }))
                  }
                  className="border-gray-200 bg-gray-50"
                />
              </div>
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">
                Tip sale <span className="text-red-500">*</span>
              </label>
              <Select
                items={tipoviItems}
                value={forma.tipSaleId}
                onValueChange={(v) =>
                  v !== null && setForma((f) => ({ ...f, tipSaleId: v }))
                }
              >
                <SelectTrigger className="w-full border-gray-200 bg-gray-50">
                  <SelectValue placeholder="Izaberi tip sale" />
                </SelectTrigger>
                <SelectContent {...SELECT_PROPS}>
                  {tipovi.map((t) => (
                    <SelectItem
                      key={t.id}
                      value={String(t.id)}
                      className="cursor-pointer focus:bg-fon-blue/10 focus:text-fon-dark"
                    >
                      {t.naziv}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

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
        naslov="Brisanje sale"
        opis={`Da li ste sigurni da želite da obrišete salu "${zaBrisanje?.naziv}"? Ova akcija je trajna.`}
        greska={brisanjeGreska}
        brisanjeUToku={brisanjeUToku}
        onOtkazi={() => setZaBrisanje(null)}
        onPotvrdi={potvrdiBrisanje}
      />

      <Dialog
        open={statusGreska !== null}
        onOpenChange={(otvoren) => !otvoren && setStatusGreska(null)}
      >
        <DialogContent className="sm:max-w-md bg-white">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-fon-coral">
              <AlertCircle size={20} />
              Promena statusa nije uspela
            </DialogTitle>
            <DialogDescription>{statusGreska}</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              onClick={() => setStatusGreska(null)}
              className="bg-fon-blue text-white hover:bg-fon-blue/90"
            >
              U redu
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
