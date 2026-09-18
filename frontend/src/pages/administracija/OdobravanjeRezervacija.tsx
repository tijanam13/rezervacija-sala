import { useState, useEffect, useCallback } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { AlertCircle, Check, X, ChevronLeft, ChevronRight } from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { DetaljiSvrhe } from "@/components/rezervacije/DetaljiSvrhe";
import { RezervacijaKartica } from "@/components/rezervacije/RezervacijaKartica";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Red } from "@/components/common/Red";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  fetchSveRezervacije,
  fetchRezervacijaById,
  azurirajStatusRezervacije,
  azurirajStatusStavke,
  odbijRezervacijuSaRazlogom,
  odbijStavkuSaRazlogom,
  izvuciPorukuGreske,
} from "@/lib/rezervacijaApi";
import { getStatusStyle, statusStyles } from "@/lib/statusColors";
import { nazivSvrhe, formatVreme, formatDatum } from "@/lib/svrhaHelpers";
import type { RezervacijaDto, StatusRezervacije } from "@/types";

const VELICINA_STRANICE = 9;
const SELECT_PROPS = { side: "bottom" as const, alignItemWithTrigger: false };

function vremeUDecimalni(vremeString: string): number {
  const [satiStr, minutiStr] = vremeString.split(":");
  return Number(satiStr) + Number(minutiStr) / 60;
}

export default function OdobravanjeRezervacija() {
  const location = useLocation();
  const navigate = useNavigate();

  const [filter, setFilter] = useState<StatusRezervacije | "SVE">("NA_CEKANJU");
  const [stranica, setStranica] = useState(0);
  const [ukupnoStranica, setUkupnoStranica] = useState(0);
  const [rezervacije, setRezervacije] = useState<RezervacijaDto[]>([]);
  const [ucitava, setUcitava] = useState(true);
  const [greska, setGreska] = useState<string | null>(null);

  const [izabrana, setIzabrana] = useState<RezervacijaDto | null>(null);
  const [akcijaUToku, setAkcijaUToku] = useState(false);

  const [odbijanjeMeta, setOdbijanjeMeta] = useState<
    | { tip: "rezervacija"; id: number }
    | { tip: "stavka"; id: number; rezervacijaId: number }
    | null
  >(null);
  const [razlogOdbijanja, setRazlogOdbijanja] = useState("");

  const ucitajStranicu = useCallback(
    async (brojStranice: number, f: StatusRezervacije | "SVE") => {
      setUcitava(true);
      setGreska(null);
      try {
        const odgovor = await fetchSveRezervacije(
          f === "SVE" ? undefined : f,
          brojStranice,
          VELICINA_STRANICE,
        );
        setRezervacije(odgovor.sadrzaj);
        setUkupnoStranica(odgovor.ukupnoStranica);
      } catch (err) {
        setGreska(izvuciPorukuGreske(err));
      } finally {
        setUcitava(false);
      }
    },
    [],
  );

  useEffect(() => {
    ucitajStranicu(stranica, filter);
  }, [stranica, filter, ucitajStranicu]);

  useEffect(() => {
    const state = location.state as { otvoriRezervacijuId?: number } | null;
    const id = state?.otvoriRezervacijuId;
    if (!id) return;

    navigate(location.pathname, { replace: true, state: null });

    fetchRezervacijaById(id)
      .then(setIzabrana)
      .catch((err) => setGreska(izvuciPorukuGreske(err)));
  }, [location.state]);

  async function osveziIzabranu(id: number) {
    const odgovor = await fetchSveRezervacije(
      filter === "SVE" ? undefined : filter,
      stranica,
      VELICINA_STRANICE,
    );
    setRezervacije(odgovor.sadrzaj);
    setUkupnoStranica(odgovor.ukupnoStranica);
    const azurirana = odgovor.sadrzaj.find((r) => r.id === id);
    setIzabrana(azurirana ?? null);
  }

  async function odobriRezervaciju(id: number) {
    setAkcijaUToku(true);
    setGreska(null);
    try {
      await azurirajStatusRezervacije(id, "ODOBRENA");
      await osveziIzabranu(id);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setAkcijaUToku(false);
    }
  }

  async function odobriStavku(stavkaId: number, rezervacijaId: number) {
    setAkcijaUToku(true);
    setGreska(null);
    try {
      await azurirajStatusStavke(stavkaId, "ODOBRENA");
      await osveziIzabranu(rezervacijaId);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setAkcijaUToku(false);
    }
  }

  async function potvrdiOdbijanje() {
    if (!odbijanjeMeta || !razlogOdbijanja.trim()) return;
    setAkcijaUToku(true);
    setGreska(null);
    try {
      if (odbijanjeMeta.tip === "rezervacija") {
        await odbijRezervacijuSaRazlogom(odbijanjeMeta.id, razlogOdbijanja);
        await osveziIzabranu(odbijanjeMeta.id);
      } else {
        await odbijStavkuSaRazlogom(odbijanjeMeta.id, razlogOdbijanja);
        await osveziIzabranu(odbijanjeMeta.rezervacijaId);
      }
      setOdbijanjeMeta(null);
      setRazlogOdbijanja("");
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setAkcijaUToku(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-5xl p-6">
        <div className="mb-6 flex items-end justify-between gap-4">
          <div>
            <p className="text-2xl font-semibold text-fon-navy">
              Odobravanje rezervacija
            </p>
            <p className="text-base text-gray-600">
              Poređano po hitnosti - najskoriji termini prvi.
            </p>
          </div>
          <div className="w-56">
            <Select
              value={filter}
              onValueChange={(v) => {
                if (v === null) return;
                setFilter(v as StatusRezervacije | "SVE");
                setStranica(0);
              }}
            >
              <SelectTrigger className="w-full border-2 border-fon-blue">
                <SelectValue>
                  {filter === "SVE"
                    ? "Sve rezervacije"
                    : statusStyles[filter].label}
                </SelectValue>
              </SelectTrigger>
              <SelectContent {...SELECT_PROPS} align="start">
                <SelectItem value="SVE">Sve rezervacije</SelectItem>
                <SelectItem value="NA_CEKANJU">Na čekanju</SelectItem>
                <SelectItem value="ODOBRENA">Odobrena</SelectItem>
                <SelectItem value="DELIMICNO_ODOBRENA">
                  Delimično odobrena
                </SelectItem>
                <SelectItem value="ODBIJENA">Odbijena</SelectItem>
                <SelectItem value="OTKAZANA">Otkazana</SelectItem>
                <SelectItem value="ISTEKLA">Istekla</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje...
          </p>
        ) : rezervacije.length === 0 ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Nema rezervacija za izabrani filter.
          </p>
        ) : (
          <div className="space-y-3">
            {rezervacije.map((r) => (
              <RezervacijaKartica
                key={r.id}
                rezervacija={r}
                onClick={() => setIzabrana(r)}
                prikaziVlasnika
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
                  {izabrana.korisnik && (
                    <Red
                      naziv="Podneo/la zahtev"
                      vrednost={`${izabrana.korisnik.ime} ${izabrana.korisnik.prezime} (${izabrana.korisnik.email})`}
                    />
                  )}
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
                  {izabrana.status === "NA_CEKANJU" && (
                    <div className="flex gap-2 pt-1">
                      <Button
                        className="flex-1 gap-1 bg-fon-teal text-fon-dark hover:bg-fon-teal/90"
                        disabled={akcijaUToku}
                        onClick={() => odobriRezervaciju(izabrana.id!)}
                      >
                        <Check size={14} /> Odobri celu rezervaciju
                      </Button>
                      <Button
                        variant="outline"
                        className="flex-1 gap-1 border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                        disabled={akcijaUToku}
                        onClick={() =>
                          setOdbijanjeMeta({
                            tip: "rezervacija",
                            id: izabrana.id!,
                          })
                        }
                      >
                        <X size={14} /> Odbij celu rezervaciju
                      </Button>
                    </div>
                  )}
                </div>

                <div className="rounded-lg border border-fon-blue/20 p-3">
                  <p className="mb-2 text-lg font-medium text-fon-navy">
                    Detalji svrhe
                  </p>
                  <DetaljiSvrhe svrha={izabrana.svrha} />
                </div>

                <div>
                  <p className="mb-2 text-lg font-medium text-fon-navy">
                    Sale i termini ({izabrana.stavke.length})
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
                          {s.statusStavke === "NA_CEKANJU" && (
                            <div className="flex gap-2 pt-1">
                              <Button
                                size="sm"
                                className="flex-1 gap-1 bg-fon-teal text-fon-dark hover:bg-fon-teal/90"
                                disabled={akcijaUToku}
                                onClick={() =>
                                  odobriStavku(s.id!, izabrana.id!)
                                }
                              >
                                <Check size={14} /> Odobri
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                className="flex-1 gap-1 border-fon-coral/30 text-fon-coral hover:bg-fon-coral/10"
                                disabled={akcijaUToku}
                                onClick={() =>
                                  setOdbijanjeMeta({
                                    tip: "stavka",
                                    id: s.id!,
                                    rezervacijaId: izabrana.id!,
                                  })
                                }
                              >
                                <X size={14} /> Odbij
                              </Button>
                            </div>
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
        open={odbijanjeMeta !== null}
        onOpenChange={(o) => {
          if (!o) {
            setOdbijanjeMeta(null);
            setRazlogOdbijanja("");
          }
        }}
      >
        <DialogContent className="border-2 border-fon-coral bg-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-fon-coral">
              Razlog odbijanja
            </DialogTitle>
          </DialogHeader>
          <p className="text-sm text-gray-600">
            Korisnik će dobiti mejl sa ovim razlogom.
          </p>
          <textarea
            value={razlogOdbijanja}
            onChange={(e) => setRazlogOdbijanja(e.target.value)}
            placeholder="Npr. Sala je u međuvremenu potrebna za hitan sastanak katedre."
            rows={4}
            className="w-full rounded-lg border-2 border-gray-400 p-2.5 text-base text-fon-navy outline-none focus:border-fon-coral"
          />
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => {
                setOdbijanjeMeta(null);
                setRazlogOdbijanja("");
              }}
              disabled={akcijaUToku}
            >
              Odustani
            </Button>
            <Button
              className="flex-1 border border-fon-coral bg-fon-coral text-white shadow-md hover:bg-fon-coral/90"
              onClick={potvrdiOdbijanje}
              disabled={akcijaUToku || !razlogOdbijanja.trim()}
            >
              {akcijaUToku ? "Slanje..." : "Odbij i pošalji mejl"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog
        open={greska !== null}
        onOpenChange={(o) => !o && setGreska(null)}
      >
        <DialogContent className="border-2 border-fon-coral bg-white sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-fon-coral">
              <AlertCircle size={20} />
              Greška
            </DialogTitle>
          </DialogHeader>
          <p className="text-base text-fon-dark">{greska}</p>
          <Button
            onClick={() => setGreska(null)}
            className="w-full border border-fon-coral bg-fon-coral text-white shadow-md hover:bg-fon-coral/90"
          >
            U redu
          </Button>
        </DialogContent>
      </Dialog>
    </div>
  );
}
