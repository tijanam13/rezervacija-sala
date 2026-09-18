import { useState, useEffect, useCallback } from "react";
import {
  AlertCircle,
  Ban,
  ChevronLeft,
  ChevronRight,
  Filter,
  CalendarDays,
} from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { DetaljiSvrhe } from "@/components/rezervacije/DetaljiSvrhe";
import { RezervacijaKartica } from "@/components/rezervacije/RezervacijaKartica";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Red } from "@/components/common/Red";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  fetchMojeRezervacije,
  otkaziRezervaciju,
  otkaziStavku,
  izvuciPorukuGreske,
} from "@/lib/rezervacijaApi";
import { getStatusStyle } from "@/lib/statusColors";
import { nazivSvrhe, formatVreme, formatDatum } from "@/lib/svrhaHelpers";
import type { RezervacijaDto, StatusRezervacije } from "@/types";

const VELICINA_STRANICE = 9;

const OPCIJE_STATUSA: { vrednost: StatusRezervacije | "SVE"; naziv: string }[] =
  [
    { vrednost: "SVE", naziv: "Svi statusi" },
    { vrednost: "NA_CEKANJU", naziv: "Na čekanju" },
    { vrednost: "ODOBRENA", naziv: "Odobrena" },
    { vrednost: "DELIMICNO_ODOBRENA", naziv: "Delimično odobrena" },
    { vrednost: "ODBIJENA", naziv: "Odbijena" },
    { vrednost: "OTKAZANA", naziv: "Otkazana" },
    { vrednost: "ISTEKLA", naziv: "Istekla" },
  ];

function vremeUDecimalni(vremeString: string): number {
  const [satiStr, minutiStr] = vremeString.split(":");
  return Number(satiStr) + Number(minutiStr) / 60;
}

export default function MojeRezervacije() {
  const [stranica, setStranica] = useState(0);
  const [ukupnoStranica, setUkupnoStranica] = useState(0);
  const [rezervacije, setRezervacije] = useState<RezervacijaDto[]>([]);
  const [ucitava, setUcitava] = useState(true);
  const [greska, setGreska] = useState<string | null>(null);

  const [filterStatus, setFilterStatus] = useState<StatusRezervacije | "SVE">(
    "SVE",
  );
  const [filterOd, setFilterOd] = useState("");
  const [filterDo, setFilterDo] = useState("");

  const [izabrana, setIzabrana] = useState<RezervacijaDto | null>(null);

  const [potvrdaZaOtkaz, setPotvrdaZaOtkaz] = useState<
    { tip: "rezervacija"; id: number } | { tip: "stavka"; id: number } | null
  >(null);
  const [otkazivanjeUToku, setOtkazivanjeUToku] = useState(false);

  const ucitajStranicu = useCallback(
    async (brojStranice: number) => {
      setUcitava(true);
      setGreska(null);
      try {
        const odgovor = await fetchMojeRezervacije(
          brojStranice,
          VELICINA_STRANICE,
          filterStatus === "SVE" ? undefined : filterStatus,
          filterOd || undefined,
          filterDo || undefined,
        );
        setRezervacije(odgovor.sadrzaj);
        setUkupnoStranica(odgovor.ukupnoStranica);
      } catch (err) {
        setGreska(izvuciPorukuGreske(err));
      } finally {
        setUcitava(false);
      }
    },
    [filterStatus, filterOd, filterDo],
  );

  useEffect(() => {
    ucitajStranicu(stranica);
  }, [stranica, ucitajStranicu]);

  useEffect(() => {
    setStranica(0);
  }, [filterStatus, filterOd, filterDo]);

  async function potvrdiOtkazivanje() {
    if (!potvrdaZaOtkaz) return;
    setOtkazivanjeUToku(true);
    setGreska(null);
    try {
      if (potvrdaZaOtkaz.tip === "rezervacija") {
        await otkaziRezervaciju(potvrdaZaOtkaz.id);
      } else {
        await otkaziStavku(potvrdaZaOtkaz.id);
      }
      setPotvrdaZaOtkaz(null);
      setIzabrana(null);
      await ucitajStranicu(stranica);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setOtkazivanjeUToku(false);
    }
  }

  function mozeSeOtkazati(status: string | undefined): boolean {
    return (
      status === "NA_CEKANJU" ||
      status === "ODOBRENA" ||
      status === "DELIMICNO_ODOBRENA"
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />
      <div className="mx-auto max-w-5xl p-6">
        <div className="mb-6">
          <p className="text-2xl font-semibold text-fon-navy">
            Moje rezervacije
          </p>
          <p className="text-base text-gray-600">
            Klikni na rezervaciju da vidiš sve detalje.
          </p>
        </div>

        <div className="mb-5 flex flex-wrap items-center gap-3 rounded-2xl border border-fon-teal/20 bg-fon-teal/5 p-3">
          <Select
            value={filterStatus}
            onValueChange={(v) => {
              if (v) setFilterStatus(v as StatusRezervacije | "SVE");
            }}
          >
            <SelectTrigger
              title={
                filterStatus === "SVE"
                  ? "Svi statusi"
                  : OPCIJE_STATUSA.find((o) => o.vrednost === filterStatus)
                      ?.naziv
              }
              className="min-w-[170px] flex-1 shrink-0 border-fon-navy/30 bg-white text-left [&>span]:truncate"
            >
              <Filter size={14} className="shrink-0 text-fon-navy" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent side="bottom" align="start">
              {OPCIJE_STATUSA.map((opcija) => (
                <SelectItem key={opcija.vrednost} value={opcija.vrednost}>
                  {opcija.naziv}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <div className="relative min-w-[160px] flex-1">
            <CalendarDays
              size={14}
              className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-fon-purple"
            />
            <Input
              type="date"
              title={filterOd ? `Termin od: ${filterOd}` : "Termin od"}
              className="border-fon-purple/30 bg-white pl-9"
              value={filterOd}
              onChange={(e) => setFilterOd(e.target.value)}
            />
          </div>

          <div className="relative min-w-[160px] flex-1">
            <CalendarDays
              size={14}
              className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-fon-pink"
            />
            <Input
              type="date"
              title={filterDo ? `Termin do: ${filterDo}` : "Termin do"}
              className="border-fon-pink/30 bg-white pl-9"
              value={filterDo}
              onChange={(e) => setFilterDo(e.target.value)}
            />
          </div>

          {(filterStatus !== "SVE" || filterOd || filterDo) && (
            <Button
              variant="outline"
              className="shrink-0 border-fon-teal/30 bg-white"
              onClick={() => {
                setFilterStatus("SVE");
                setFilterOd("");
                setFilterDo("");
              }}
            >
              Poništi filtere
            </Button>
          )}
        </div>

        {greska && (
          <div className="mb-4 flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje...
          </p>
        ) : rezervacije.length === 0 ? (
          <p className="py-10 text-center text-sm text-gray-500">
            {filterStatus !== "SVE" || filterOd || filterDo
              ? "Nema rezervacija koje odgovaraju izabranim filterima."
              : "Nemaš još nijednu rezervaciju."}
          </p>
        ) : (
          <div className="space-y-3">
            {rezervacije.map((r) => (
              <RezervacijaKartica
                key={r.id}
                rezervacija={r}
                onClick={() => setIzabrana(r)}
                pokaziStrelicu
              />
            ))}
          </div>
        )}

        {ukupnoStranica > 1 && (
          <div className="mt-6 flex items-center justify-center gap-3">
            <Button
              size="sm"
              variant="outline"
              className="border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
              disabled={stranica === 0 || ucitava}
              onClick={() => setStranica((s) => Math.max(0, s - 1))}
            >
              <ChevronLeft size={16} />
            </Button>
            <span className="text-sm text-gray-500">
              Strana {stranica + 1} od {ukupnoStranica}
            </span>
            <Button
              size="sm"
              variant="outline"
              className="border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
              disabled={stranica >= ukupnoStranica - 1 || ucitava}
              onClick={() => setStranica((s) => s + 1)}
            >
              <ChevronRight size={16} />
            </Button>
          </div>
        )}
      </div>
      #{" "}
      <Dialog
        open={izabrana !== null}
        onOpenChange={(o) => !o && setIzabrana(null)}
      >
        <DialogContent className="max-h-[85vh] overflow-y-auto border-2 border-fon-navy bg-white sm:max-w-2xl">
          {izabrana && (
            <>
              <DialogHeader>
                <DialogTitle className="text-xl text-fon-navy">
                  {nazivSvrhe(izabrana.svrha)}
                </DialogTitle>
              </DialogHeader>

              <div className="space-y-4 text-base">
                <div className="space-y-2 rounded-lg border border-fon-blue/20 bg-white p-3">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-700">Status rezervacije</span>
                    <Badge
                      className={`${getStatusStyle(izabrana.status ?? "NA_CEKANJU").bg} ${getStatusStyle(izabrana.status ?? "NA_CEKANJU").text} border-0`}
                    >
                      {getStatusStyle(izabrana.status ?? "NA_CEKANJU").label}
                    </Badge>
                  </div>
                  {izabrana.datumKreiranja && (
                    <Red
                      naziv="Kreirana"
                      vrednost={formatDatum(
                        izabrana.datumKreiranja.slice(0, 10),
                      )}
                    />
                  )}
                  <Red
                    naziv="Datum termina"
                    vrednost={formatDatum(izabrana.datumTermina)}
                  />
                  <Red
                    naziv="Vreme"
                    vrednost={`${formatVreme(vremeUDecimalni(izabrana.vremeOd))} - ${formatVreme(vremeUDecimalni(izabrana.vremeDo))}`}
                  />
                  {izabrana.napomena && (
                    <Red
                      naziv="Napomena (cela rezervacija)"
                      vrednost={izabrana.napomena}
                    />
                  )}
                  {mozeSeOtkazati(izabrana.status) && (
                    <Button
                      size="sm"
                      variant="outline"
                      className="mt-1 w-full gap-1 border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                      onClick={() =>
                        setPotvrdaZaOtkaz({
                          tip: "rezervacija",
                          id: izabrana.id!,
                        })
                      }
                    >
                      <Ban size={14} /> Otkaži celu rezervaciju
                    </Button>
                  )}
                </div>

                <div className="rounded-lg border border-fon-blue/20 p-3">
                  <p className="mb-2 text-lg font-medium text-fon-navy">
                    Detalji svrhe
                  </p>
                  <DetaljiSvrhe svrha={izabrana.svrha} />
                </div>

                <div>
                  <p className="mb-2 font-medium text-fon-navy">
                    <span className="text-lg">
                      Sale i termini ({izabrana.stavke.length})
                    </span>
                  </p>
                  <div className="space-y-3">
                    {izabrana.stavke.map((s) => {
                      const stilStavke = getStatusStyle(
                        s.statusStavke ?? "NA_CEKANJU",
                      );
                      return (
                        <div
                          key={s.id}
                          className="space-y-2 rounded-lg border border-fon-blue/20 p-3"
                        >
                          <div className="flex items-center justify-between">
                            <span className="text-lg font-medium text-fon-dark">
                              {s.sala.naziv}
                            </span>
                            <Badge
                              className={`${stilStavke.bg} ${stilStavke.text} border-0`}
                            >
                              {stilStavke.label}
                            </Badge>
                          </div>
                          <Red naziv="Zgrada" vrednost={s.sala.zgrada} />
                          <Red
                            naziv="Sprat"
                            vrednost={
                              s.sala.sprat === 0
                                ? "Prizemlje"
                                : `Sprat ${s.sala.sprat}`
                            }
                          />
                          <Red
                            naziv="Tip sale"
                            vrednost={s.sala.tipSale.naziv}
                          />
                          <Red
                            naziv="Kapacitet sale"
                            vrednost={`${s.sala.kapacitet} mesta`}
                          />
                          {s.sala.brojRacunara > 0 && (
                            <Red
                              naziv="Broj računara"
                              vrednost={String(s.sala.brojRacunara)}
                            />
                          )}
                          <Red
                            naziv="Broj osoba"
                            vrednost={String(s.brojOsoba)}
                          />
                          {s.opis && (
                            <Red
                              naziv="Napomena za ovu stavku"
                              vrednost={s.opis}
                            />
                          )}
                          {mozeSeOtkazati(s.statusStavke) && (
                            <Button
                              size="sm"
                              variant="outline"
                              className="w-full gap-1 border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                              onClick={() =>
                                setPotvrdaZaOtkaz({ tip: "stavka", id: s.id! })
                              }
                            >
                              <Ban size={14} /> Otkaži ovu stavku
                            </Button>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
      <Dialog
        open={potvrdaZaOtkaz !== null}
        onOpenChange={(otvoreno) => !otvoreno && setPotvrdaZaOtkaz(null)}
      >
        <DialogContent className="border-2 border-fon-coral bg-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-fon-coral">
              Potvrda otkazivanja
            </DialogTitle>
          </DialogHeader>
          <p className="text-sm text-fon-dark">
            {potvrdaZaOtkaz?.tip === "rezervacija"
              ? "Da li si sigurna da želiš da otkažeš CELU rezervaciju? Ova radnja je trajna i ne može se poništiti."
              : "Da li si sigurna da želiš da otkažeš ovu stavku (salu/termin)? Ova radnja je trajna i ne može se poništiti."}
          </p>
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => setPotvrdaZaOtkaz(null)}
              disabled={otkazivanjeUToku}
            >
              Odustani
            </Button>
            <Button
              className="flex-1 bg-fon-coral text-white hover:bg-fon-coral/90"
              onClick={potvrdiOtkazivanje}
              disabled={otkazivanjeUToku}
            >
              {otkazivanjeUToku ? "Otkazivanje..." : "Da, otkaži"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
