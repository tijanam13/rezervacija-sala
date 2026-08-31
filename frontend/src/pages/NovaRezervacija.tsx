import { useState, useEffect, useRef } from "react";
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  Search,
  Layers,
  Users,
  Building2,
  Tag,
} from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { Red } from "@/components/common/Red";
import { DetaljiSvrhe } from "@/components/rezervacije/DetaljiSvrhe";
import { NovaRezervacijaForma } from "@/components/rezervacije/NovaRezervacijaForma";
import { Input } from "@/components/ui/input";
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
import { Badge } from "@/components/ui/badge";
import { getStatusStyle } from "@/lib/statusColors";
import {
  nazivSvrhe,
  ikonicaSvrhe,
  formatVreme,
  formatDatum,
} from "@/lib/svrhaHelpers";
import { fetchSale } from "@/lib/salaPredavacApi";
import { fetchTerminiZaPeriod } from "@/lib/kalendarApi";
import type { TerminPodaci } from "@/lib/kalendarTipovi";
import type { SalaDto } from "@/types";

const POCETNI_SAT = 8;
const KRAJNJI_SAT = 21;
const REDAK_PX = 52;

function toISODatum(d: Date): string {
  const godina = d.getFullYear();
  const mesec = String(d.getMonth() + 1).padStart(2, "0");
  const dan = String(d.getDate()).padStart(2, "0");
  return `${godina}-${mesec}-${dan}`;
}

function jeIstiDan(a: Date, b: Date): boolean {
  return toISODatum(a) === toISODatum(b);
}

function pocetakNedelje(d: Date): Date {
  const kopija = new Date(d);
  const pomeraj = (kopija.getDay() + 6) % 7;
  kopija.setDate(kopija.getDate() - pomeraj);
  return kopija;
}

function daniMeseca(d: Date): (Date | null)[] {
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

function formatNaslov(
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

export default function NovaRezervacija() {
  const [datum, setDatum] = useState(new Date());
  const [prikaz, setPrikaz] = useState<"dan" | "nedelja" | "mesec">("dan");
  const [zgrada, setZgrada] = useState<string>("");
  const [pretraga, setPretraga] = useState("");
  const [minKapacitet, setMinKapacitet] = useState(0);
  const [tipSale, setTipSale] = useState<string>("SVI");
  const [sprat, setSprat] = useState<string>("SVI");
  const [sve, setSve] = useState<SalaDto[]>([]);
  const [terminiState, setTerminiState] = useState<TerminPodaci[]>([]);
  const [izabraniTermin, setIzabraniTermin] = useState<TerminPodaci | null>(
    null,
  );
  const [kreiranjeOtvoreno, setKreiranjeOtvoreno] = useState(false);
  const skrolRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchSale()
      .then((podaci) => {
        setSve(podaci);
        if (podaci.length > 0 && !zgrada) setZgrada(podaci[0].zgrada);
      })
      .catch(() => setSve([]));
  }, []);

  useEffect(() => {
    if (sve.length === 0) return;
    const pocetakOpsega = new Date(
      datum.getFullYear(),
      datum.getMonth() - 1,
      1,
    );
    const krajOpsega = new Date(datum.getFullYear(), datum.getMonth() + 2, 0);
    fetchTerminiZaPeriod(toISODatum(pocetakOpsega), toISODatum(krajOpsega), sve)
      .then(setTerminiState)
      .catch(() => setTerminiState([]));
  }, [datum.getFullYear(), datum.getMonth(), sve]);

  const danas = new Date();
  const saleFiltrirane = sve.filter(
    (s) =>
      s.zgrada === zgrada &&
      (sprat === "SVI" || s.sprat === Number(sprat)) &&
      s.kapacitet >= minKapacitet &&
      (tipSale === "SVI" || s.tipSale.naziv === tipSale) &&
      s.naziv.toLowerCase().includes(pretraga.toLowerCase()),
  );
  const zgrade = Array.from(new Set(sve.map((s) => s.zgrada)));
  const tipoviSale = Array.from(new Set(sve.map((s) => s.tipSale.naziv)));
  const spratovi = Array.from(new Set(sve.map((s) => s.sprat))).sort(
    (a, b) => a - b,
  );
  const satiNiz = Array.from(
    { length: KRAJNJI_SAT - POCETNI_SAT },
    (_, i) => POCETNI_SAT + i,
  );
  const isoDatum = toISODatum(datum);

  function promeniDan(delta: number) {
    const novi = new Date(datum);
    if (prikaz === "mesec") novi.setMonth(novi.getMonth() + delta);
    else if (prikaz === "nedelja") novi.setDate(novi.getDate() + delta * 7);
    else novi.setDate(novi.getDate() + delta);
    setDatum(novi);
  }

  const izabranaSala = izabraniTermin
    ? (sve.find((s) => s.naziv === izabraniTermin.salaNaziv) ?? null)
    : null;

  function naUspesnoKreiranje() {
    const pocetakOpsega = new Date(
      datum.getFullYear(),
      datum.getMonth() - 1,
      1,
    );
    const krajOpsega = new Date(datum.getFullYear(), datum.getMonth() + 2, 0);
    fetchTerminiZaPeriod(toISODatum(pocetakOpsega), toISODatum(krajOpsega), sve)
      .then(setTerminiState)
      .catch(() => {});
  }

  function jeAktivan(t: TerminPodaci): boolean {
    return t.status === "NA_CEKANJU" || t.status === "ODOBRENA";
  }

  const terminiDanas = terminiState.filter(
    (t) => t.datum === isoDatum && jeAktivan(t),
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-6xl p-6">
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-gray-100 bg-white p-3 shadow-sm">
          <div className="flex items-center gap-1 rounded-lg bg-gray-50 p-1">
            {(["dan", "nedelja", "mesec"] as const).map((opcija) => (
              <button
                key={opcija}
                onClick={() => setPrikaz(opcija)}
                className={`rounded-md px-3.5 py-1.5 text-base capitalize transition-colors ${
                  prikaz === opcija
                    ? "bg-fon-navy font-medium text-white shadow-sm"
                    : "text-gray-600 hover:text-fon-navy"
                }`}
              >
                {opcija}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => promeniDan(-1)}
              aria-label="Prethodni"
              className="flex h-9 w-9 items-center justify-center rounded-full text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
            >
              <ChevronLeft size={22} />
            </button>
            <span className="min-w-[240px] text-center text-lg font-medium capitalize text-fon-navy">
              {formatNaslov(datum, prikaz)}
            </span>
            <button
              onClick={() => promeniDan(1)}
              aria-label="Sledeći"
              className="flex h-9 w-9 items-center justify-center rounded-full text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
            >
              <ChevronRight size={22} />
            </button>
            <button
              onClick={() => setDatum(new Date())}
              className="rounded-md border border-gray-200 px-4 py-2 text-base font-medium text-gray-600 hover:border-fon-teal hover:text-fon-navy"
            >
              Danas
            </button>
          </div>
        </div>

        <div className="mb-5 flex flex-wrap items-center gap-3 rounded-2xl border border-fon-teal/20 bg-fon-teal/5 p-3">
          <div className="relative min-w-[180px] flex-1">
            <Search
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-purple"
            />
            <Input
              placeholder="Pretraži salu po nazivu..."
              value={pretraga}
              onChange={(e) => setPretraga(e.target.value)}
              className="border-fon-purple/30 bg-white pl-9"
            />
          </div>

          <Select
            value={zgrada}
            onValueChange={(v) => v !== null && setZgrada(v)}
          >
            <SelectTrigger className="w-[140px] border-fon-pink/30 bg-white">
              <Building2 size={14} className="text-fon-pink" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent
              align="start"
              side="bottom"
              alignItemWithTrigger={false}
            >
              {zgrade.map((z) => (
                <SelectItem key={z} value={z}>
                  {z}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select
            value={sprat}
            onValueChange={(v) => v !== null && setSprat(v)}
          >
            <SelectTrigger className="w-[140px] border-fon-purple/30 bg-white">
              <Layers size={14} className="text-fon-purple" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent
              align="start"
              side="bottom"
              alignItemWithTrigger={false}
            >
              <SelectItem value="SVI">Svi spratovi</SelectItem>
              {spratovi.map((s) => (
                <SelectItem key={s} value={String(s)}>
                  {s === 0 ? "Prizemlje" : `Sprat ${s}`}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select
            value={String(minKapacitet)}
            onValueChange={(v) => v !== null && setMinKapacitet(Number(v))}
          >
            <SelectTrigger className="w-[170px] border-fon-pink/30 bg-white">
              <Users size={14} className="text-fon-pink" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent
              align="start"
              side="bottom"
              alignItemWithTrigger={false}
            >
              <SelectItem value="0">Bilo koji kapacitet</SelectItem>
              <SelectItem value="10">10+ mesta</SelectItem>
              <SelectItem value="20">20+ mesta</SelectItem>
              <SelectItem value="50">50+ mesta</SelectItem>
              <SelectItem value="100">100+ mesta</SelectItem>
            </SelectContent>
          </Select>

          <Select
            value={tipSale}
            onValueChange={(v) => v !== null && setTipSale(v)}
          >
            <SelectTrigger className="w-[190px] border-fon-purple/30 bg-white">
              <Tag size={14} className="text-fon-purple" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent
              align="start"
              side="bottom"
              alignItemWithTrigger={false}
            >
              <SelectItem value="SVI">Svi tipovi sala</SelectItem>
              {tipoviSale.map((t) => (
                <SelectItem key={t} value={t}>
                  {t}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {prikaz === "dan" && (
          <div className="relative overflow-hidden rounded-2xl border-x border-b border-gray-100 bg-white shadow-sm">
            {saleFiltrirane.length > 3 && (
              <>
                <button
                  onClick={() =>
                    skrolRef.current?.scrollBy({
                      left: -200,
                      behavior: "smooth",
                    })
                  }
                  aria-label="Sale levo"
                  className="absolute top-1 left-[62px] z-20 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-fon-navy shadow-sm hover:bg-gray-50"
                >
                  <ChevronLeft size={18} />
                </button>
                <button
                  onClick={() =>
                    skrolRef.current?.scrollBy({
                      left: 200,
                      behavior: "smooth",
                    })
                  }
                  aria-label="Sale desno"
                  className="absolute top-1 right-1 z-20 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-fon-navy shadow-sm hover:bg-gray-50"
                >
                  <ChevronRight size={18} />
                </button>
              </>
            )}
            <div ref={skrolRef} className="bez-skrol-trake overflow-x-auto">
              <div
                className="grid"
                style={{
                  gridTemplateColumns: `60px repeat(${saleFiltrirane.length}, minmax(180px, 1fr))`,
                  height: 44,
                }}
              >
                <div className="sticky left-0 z-10 flex items-center justify-center border-b-2 border-b-gray-300 bg-white text-xs font-medium text-gray-600">
                  Vreme
                </div>
                {saleFiltrirane.map((s, i) => (
                  <div
                    key={s.naziv}
                    className="flex items-center justify-center border-b-2 border-l border-l-gray-200 border-b-gray-300 text-sm font-semibold text-fon-dark"
                  >
                    {s.naziv}
                  </div>
                ))}
              </div>

              <div
                className="relative grid"
                style={{
                  gridTemplateColumns: `60px repeat(${saleFiltrirane.length}, minmax(180px, 1fr))`,
                  gridTemplateRows: `repeat(${satiNiz.length}, ${REDAK_PX}px)`,
                }}
              >
                {saleFiltrirane.map((s, i) => (
                  <div
                    key={`kolona-${s.naziv}`}
                    className="border-l border-l-gray-100"
                    style={{
                      gridColumn: i + 2,
                      gridRow: `1 / span ${satiNiz.length}`,
                    }}
                  />
                ))}

                {satiNiz.slice(1).map((sat, i) => (
                  <div
                    key={`linija-${sat}`}
                    className="pointer-events-none border-t border-gray-200"
                    style={{ gridColumn: "1 / -1", gridRow: i + 2 }}
                  />
                ))}

                {satiNiz.map((sat, i) => (
                  <span
                    key={sat}
                    className="sticky left-0 z-10 bg-white pt-0.5 pl-1.5 text-xs font-medium text-gray-600"
                    style={{
                      gridColumn: 1,
                      gridRow: i + 1,
                      alignSelf: "start",
                    }}
                  >
                    {sat}:00
                  </span>
                ))}

                {terminiDanas.map((t) => {
                  const kolona = saleFiltrirane.findIndex(
                    (s) => s.naziv === t.salaNaziv,
                  );
                  if (kolona === -1) return null;
                  const stil = getStatusStyle(t.status);
                  const Ikonica = ikonicaSvrhe(t.svrha.tip);

                  const gore = (t.vremeOd - POCETNI_SAT) * REDAK_PX;
                  const visina = (t.vremeDo - t.vremeOd) * REDAK_PX;
                  return (
                    <button
                      key={t.id}
                      onClick={() => setIzabraniTermin(t)}
                      className={`absolute mx-1.5 flex flex-col items-start overflow-hidden rounded-lg border-2 p-1.5 text-left shadow-[0_1px_2px_rgba(0,0,0,0.04)] transition-transform hover:scale-[1.02] ${stil.bg} ${stil.border}`}
                      style={{
                        gridColumn: `${kolona + 2} / ${kolona + 3}`,
                        top: gore,
                        height: visina,
                        left: 0,
                        right: 0,
                        boxSizing: "border-box",
                      }}
                    >
                      <div className="flex items-center gap-1">
                        <Ikonica size={11} className={stil.text} />
                        <p
                          className={`truncate text-[11px] font-medium ${stil.text}`}
                        >
                          {nazivSvrhe(t.svrha)}
                        </p>
                      </div>
                      <p className={`text-[10px] ${stil.text} opacity-80`}>
                        {formatVreme(t.vremeOd)}-{formatVreme(t.vremeDo)}
                      </p>
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {prikaz === "nedelja" && (
          <div className="grid grid-cols-7 gap-2.5">
            {Array.from({ length: 7 }, (_, i) => {
              const dan = new Date(pocetakNedelje(datum));
              dan.setDate(dan.getDate() + i);
              const isoDan = toISODatum(dan);
              const terminiDana = terminiState.filter(
                (t) =>
                  t.datum === isoDan &&
                  jeAktivan(t) &&
                  saleFiltrirane.some((s) => s.naziv === t.salaNaziv),
              );
              const jeDanas = jeIstiDan(dan, danas);

              return (
                <div
                  key={i}
                  className={`min-h-[230px] rounded-xl border bg-white p-2.5 shadow-sm ${
                    jeDanas
                      ? "border-fon-teal ring-1 ring-fon-teal/30"
                      : "border-gray-100"
                  }`}
                >
                  <p
                    className={`mb-2 border-b pb-2 text-center text-xs font-medium ${jeDanas ? "border-fon-teal/30 text-fon-teal" : "border-gray-100 text-fon-dark"}`}
                  >
                    {dan.toLocaleDateString("sr-Latn-RS", { weekday: "short" })}
                    <br />
                    <span className={jeDanas ? "text-base font-semibold" : ""}>
                      {dan.getDate()}
                    </span>
                  </p>
                  <div className="flex flex-col gap-1">
                    {terminiDana
                      .sort((a, b) => a.vremeOd - b.vremeOd)
                      .map((t) => {
                        const stil = getStatusStyle(t.status);
                        return (
                          <button
                            key={t.id}
                            onClick={() => setIzabraniTermin(t)}
                            className={`truncate rounded-md border-2 p-1 text-left text-[10px] transition-transform hover:scale-[1.03] ${stil.bg} ${stil.border} ${stil.text}`}
                          >
                            {formatVreme(t.vremeOd)} {nazivSvrhe(t.svrha)}
                          </button>
                        );
                      })}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {prikaz === "mesec" && (
          <div className="overflow-hidden rounded-2xl border border-gray-100 bg-white p-3 shadow-sm">
            <div className="grid grid-cols-7 gap-px bg-gray-50 pb-2">
              {["Pon", "Uto", "Sre", "Čet", "Pet", "Sub", "Ned"].map((d) => (
                <p
                  key={d}
                  className="bg-white text-center text-xs font-semibold text-gray-600"
                >
                  {d}
                </p>
              ))}
            </div>
            <div className="grid grid-cols-7 gap-px bg-gray-100">
              {daniMeseca(datum).map((dan, i) => {
                if (!dan) return <div key={i} className="bg-white" />;
                const isoDan = toISODatum(dan);
                const terminiDana = terminiState.filter(
                  (t) =>
                    t.datum === isoDan &&
                    jeAktivan(t) &&
                    saleFiltrirane.some((s) => s.naziv === t.salaNaziv),
                );
                const jeDanas = jeIstiDan(dan, danas);
                const prikazani = terminiDana
                  .sort((a, b) => a.vremeOd - b.vremeOd)
                  .slice(0, 2);
                const josIma = terminiDana.length - prikazani.length;

                return (
                  <button
                    key={i}
                    onClick={() => {
                      setDatum(dan);
                      setPrikaz("dan");
                    }}
                    className={`flex min-h-[86px] flex-col items-start gap-1 bg-white p-2 text-left transition-colors hover:bg-gray-50 ${
                      jeDanas
                        ? "border-2 border-fon-teal bg-fon-teal/[0.06]"
                        : ""
                    }`}
                  >
                    <span
                      className={`flex h-6 w-6 items-center justify-center rounded-full text-xs ${
                        jeDanas
                          ? "bg-fon-teal font-semibold text-fon-dark"
                          : "text-fon-dark"
                      }`}
                    >
                      {dan.getDate()}
                    </span>
                    <div className="flex w-full flex-col gap-0.5">
                      {prikazani.map((t) => {
                        const stil = getStatusStyle(t.status);
                        return (
                          <span
                            key={t.id}
                            className={`truncate rounded px-1 py-0.5 text-[9px] font-medium ${stil.bg} ${stil.text}`}
                          >
                            {formatVreme(t.vremeOd)} {nazivSvrhe(t.svrha)}
                          </span>
                        );
                      })}
                      {josIma > 0 && (
                        <span className="text-[9px] font-medium text-gray-500">
                          +{josIma} više
                        </span>
                      )}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        )}
      </div>

      <button
        onClick={() => setKreiranjeOtvoreno(true)}
        className="fixed right-6 bottom-6 flex items-center gap-2.5 rounded-full bg-fon-teal py-4 pr-7 pl-5 text-lg font-medium text-fon-dark shadow-lg transition-transform hover:scale-105"
        aria-label="Nova rezervacija"
      >
        <Plus size={26} />
        Nova rezervacija
      </button>

      <Dialog
        open={izabraniTermin !== null}
        onOpenChange={(otvoren) => {
          if (!otvoren) setIzabraniTermin(null);
        }}
      >
        <DialogContent className="border-2 border-fon-navy bg-white">
          {izabraniTermin && (
            <>
              <DialogHeader>
                <DialogTitle>{nazivSvrhe(izabraniTermin.svrha)}</DialogTitle>
              </DialogHeader>
              <div className="space-y-3 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-gray-500">Status</span>
                  <Badge
                    className={`${getStatusStyle(izabraniTermin.status).bg} ${getStatusStyle(izabraniTermin.status).text} border-0`}
                  >
                    {getStatusStyle(izabraniTermin.status).label}
                  </Badge>
                </div>
                <Red naziv="Sala" vrednost={izabraniTermin.salaNaziv} />
                {izabranaSala && (
                  <Red
                    naziv="Kapacitet sale"
                    vrednost={`${izabranaSala.kapacitet} mesta`}
                  />
                )}
                <Red
                  naziv="Datum"
                  vrednost={formatDatum(izabraniTermin.datum)}
                />
                <Red
                  naziv="Vreme"
                  vrednost={`${formatVreme(izabraniTermin.vremeOd)} - ${formatVreme(izabraniTermin.vremeDo)}`}
                />
                <Red
                  naziv="Broj osoba"
                  vrednost={String(izabraniTermin.brojOsoba)}
                />
                {izabraniTermin.korisnikImePrezime && (
                  <Red
                    naziv="Podnosilac"
                    vrednost={izabraniTermin.korisnikImePrezime}
                  />
                )}
                {izabraniTermin.opis && (
                  <Red naziv="Napomena" vrednost={izabraniTermin.opis} />
                )}
                <div className="border-t border-gray-100 pt-3">
                  <DetaljiSvrhe svrha={izabraniTermin.svrha} />
                </div>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>

      <NovaRezervacijaForma
        open={kreiranjeOtvoreno}
        onOpenChange={setKreiranjeOtvoreno}
        saleFiltrirane={saleFiltrirane}
        onUspesnoKreirano={naUspesnoKreiranje}
      />
    </div>
  );
}
