import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Plus } from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { NovaRezervacijaForma } from "@/components/rezervacije/NovaRezervacijaForma";
import { KalendarKontrole } from "@/components/kalendar/KalendarKontrole";
import { KalendarFilteri } from "@/components/kalendar/KalendarFilteri";
import { DanPrikaz } from "@/components/kalendar/DanPrikaz";
import { NedeljaPrikaz } from "@/components/kalendar/NedeljaPrikaz";
import { MesecPrikaz } from "@/components/kalendar/MesecPrikaz";
import { DetaljiTerminaDijalog } from "@/components/kalendar/DetaljiTerminaDijalog";
import { toISODatum } from "@/lib/kalendarDatumi";
import { fetchSale } from "@/lib/salaPredavacApi";
import {
  fetchTerminiZaPeriod,
  fetchTerminiZaPeriodAdministracija,
} from "@/lib/kalendarApi";
import { jeAdministracija } from "@/lib/auth";
import type { TerminPodaci } from "@/lib/kalendarTipovi";
import type { SalaDto, StatusStavke } from "@/types";

const POCETNI_SAT = 8;
const KRAJNJI_SAT = 21;

export default function PregledRezervacija() {
  const navigate = useNavigate();
  const administracija = jeAdministracija();

  const [datum, setDatum] = useState(new Date());
  const [prikaz, setPrikaz] = useState<"dan" | "nedelja" | "mesec">("dan");
  const [zgrada, setZgrada] = useState<string>("");
  const [pretraga, setPretraga] = useState("");
  const [minKapacitet, setMinKapacitet] = useState(0);
  const [tipSale, setTipSale] = useState<string>("SVI");
  const [sprat, setSprat] = useState<string>("SVI");

  const [filterStatus, setFilterStatus] = useState<StatusStavke | "SVI">("SVI");
  const [sve, setSve] = useState<SalaDto[]>([]);
  const [terminiState, setTerminiState] = useState<TerminPodaci[]>([]);
  const [izabraniTermin, setIzabraniTermin] = useState<TerminPodaci | null>(
    null,
  );
  const [kreiranjeOtvoreno, setKreiranjeOtvoreno] = useState(false);

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
    const od = toISODatum(pocetakOpsega);
    const doD = toISODatum(krajOpsega);
    const ucitaj = administracija
      ? fetchTerminiZaPeriodAdministracija(od, doD)
      : fetchTerminiZaPeriod(od, doD, sve);
    ucitaj.then(setTerminiState).catch(() => setTerminiState([]));
  }, [datum.getFullYear(), datum.getMonth(), sve, administracija]);

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
    const od = toISODatum(pocetakOpsega);
    const doD = toISODatum(krajOpsega);
    const ucitaj = administracija
      ? fetchTerminiZaPeriodAdministracija(od, doD)
      : fetchTerminiZaPeriod(od, doD, sve);
    ucitaj.then(setTerminiState).catch(() => {});
  }

  function jePrikazljiv(t: TerminPodaci): boolean {
    if (!administracija)
      return t.status === "NA_CEKANJU" || t.status === "ODOBRENA";
    return filterStatus === "SVI" || t.status === filterStatus;
  }

  const terminiDanas = terminiState.filter(
    (t) => t.datum === isoDatum && jePrikazljiv(t),
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-6xl p-6">
        <KalendarKontrole
          prikaz={prikaz}
          setPrikaz={setPrikaz}
          datum={datum}
          onPromeniDan={promeniDan}
          onDanas={() => setDatum(new Date())}
        />

        <KalendarFilteri
          pretraga={pretraga}
          setPretraga={setPretraga}
          zgrada={zgrada}
          setZgrada={setZgrada}
          zgrade={zgrade}
          sprat={sprat}
          setSprat={setSprat}
          spratovi={spratovi}
          minKapacitet={minKapacitet}
          setMinKapacitet={setMinKapacitet}
          tipSale={tipSale}
          setTipSale={setTipSale}
          tipoviSale={tipoviSale}
          administracija={administracija}
          filterStatus={filterStatus}
          setFilterStatus={setFilterStatus}
        />

        {prikaz === "dan" && (
          <DanPrikaz
            saleFiltrirane={saleFiltrirane}
            satiNiz={satiNiz}
            terminiDanas={terminiDanas}
            onIzaberiTermin={setIzabraniTermin}
          />
        )}

        {prikaz === "nedelja" && (
          <NedeljaPrikaz
            datum={datum}
            danas={danas}
            terminiState={terminiState}
            saleFiltrirane={saleFiltrirane}
            jePrikazljiv={jePrikazljiv}
            onIzaberiTermin={setIzabraniTermin}
          />
        )}

        {prikaz === "mesec" && (
          <MesecPrikaz
            datum={datum}
            danas={danas}
            terminiState={terminiState}
            saleFiltrirane={saleFiltrirane}
            jePrikazljiv={jePrikazljiv}
            onIzaberiDan={(dan) => {
              setDatum(dan);
              setPrikaz("dan");
            }}
          />
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

      <DetaljiTerminaDijalog
        izabraniTermin={izabraniTermin}
        izabranaSala={izabranaSala}
        administracija={administracija}
        onClose={() => setIzabraniTermin(null)}
        onObradiRezervaciju={(rezervacijaId) =>
          navigate("/odobravanje-rezervacija", {
            state: { otvoriRezervacijuId: rezervacijaId },
          })
        }
      />

      <NovaRezervacijaForma
        open={kreiranjeOtvoreno}
        onOpenChange={setKreiranjeOtvoreno}
        saleFiltrirane={saleFiltrirane}
        onUspesnoKreirano={naUspesnoKreiranje}
      />
    </div>
  );
}
