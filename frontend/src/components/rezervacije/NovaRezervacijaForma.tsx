import { useState, useEffect } from "react";
import { AlertCircle, Clock } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import type {
  SvrhaRezervacijeDto,
  NivoStudija,
  VrstaNastave,
  VrstaVezbi,
  TipIspita,
  RezervacijaDto,
  StavkaRezervacijeDto,
  UcesnikSastankaDto,
  PredavacDto,
  SalaDto,
} from "@/types";
import { fetchPredavaci, fetchSluzbenici } from "@/lib/salaPredavacApi";
import type { ZaposleniOpcija } from "./PretragaZaposlenog";
import { kreirajRezervaciju, izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import {
  danasnjiDatumLokalno,
  trenutnoVremeLokalno,
  vremeZaBackend,
} from "@/lib/datumVreme";
import { SvrhaPolja, type UcesnikForma } from "./SvrhaPolja";
import {
  praznaStavka,
  StavkeRezervacije,
  type StavkaForma,
} from "./StavkeRezervacije";
import { SvrhaTipSelektor } from "./SvrhaTipSelektor";

interface NovaRezervacijaFormeProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  saleFiltrirane: SalaDto[];
  onUspesnoKreirano: () => void;
  kaoStranica?: boolean;
}

export function NovaRezervacijaForma({
  open,
  onOpenChange,
  saleFiltrirane,
  onUspesnoKreirano,
  kaoStranica = false,
}: NovaRezervacijaFormeProps) {
  const [tipSvrhe, setTipSvrhe] =
    useState<SvrhaRezervacijeDto["tip"]>("DOGADJAJ");
  const [napomenaRezervacije, setNapomenaRezervacije] = useState("");

  const [datum, setDatum] = useState(danasnjiDatumLokalno());
  const [vremeOd, setVremeOd] = useState("10:00");
  const [vremeDo, setVremeDo] = useState("11:00");

  const [semestar, setSemestar] = useState(1);
  const [nivoStudija, setNivoStudija] =
    useState<NivoStudija>("OSNOVNE_AKADEMSKE");
  const [vrsta, setVrsta] = useState<VrstaNastave>("PREDAVANJE");
  const [vrstaVezbi, setVrstaVezbi] = useState<VrstaVezbi | "">("");
  const [tipIspita, setTipIspita] = useState<TipIspita>("PISMENI");
  const [nazivTeme, setNazivTeme] = useState("");
  const [student, setStudent] = useState("");
  const [mentorId, setMentorId] = useState<string>("");
  const [komisijaIds, setKomisijaIds] = useState<string[]>(["", "", ""]);

  const [tema, setTema] = useState("");
  const [napomenaSastanka, setNapomenaSastanka] = useState("");
  const [ucesnici, setUcesnici] = useState<UcesnikForma[]>([
    { ucesnik: "", email: "", zaposleniId: null, rezim: "eksterni" },
  ]);

  const [nazivDogadjaja, setNazivDogadjaja] = useState("");
  const [opisDogadjaja, setOpisDogadjaja] = useState("");

  const [predavaci, setPredavaci] = useState<PredavacDto[]>([]);
  const [zaposleniOpcije, setZaposleniOpcije] = useState<ZaposleniOpcija[]>([]);

  useEffect(() => {
    if (open) {
      fetchPredavaci()
        .then(setPredavaci)
        .catch(() => setPredavaci([]));

      Promise.all([fetchPredavaci(), fetchSluzbenici()])
        .then(([predavaciData, sluzbeniciData]) => {
          const opcije: ZaposleniOpcija[] = [
            ...predavaciData.map((p) => ({
              id: p.id!,
              ime: p.ime,
              prezime: p.prezime,
              tip: "PREDAVAC" as const,
            })),
            ...sluzbeniciData.map((s) => ({
              id: s.id!,
              ime: s.ime,
              prezime: s.prezime,
              tip: "SLUZBENIK" as const,
            })),
          ];
          setZaposleniOpcije(opcije);
        })
        .catch(() => setZaposleniOpcije([]));
    }
  }, [open]);

  const [stavke, setStavke] = useState<StavkaForma[]>([
    praznaStavka(saleFiltrirane[0]?.naziv ?? ""),
  ]);

  const [saljeSe, setSaljeSe] = useState(false);
  const [greska, setGreska] = useState<string | null>(null);

  function dodajStavku() {
    setStavke((prev) => [
      ...prev,
      praznaStavka(saleFiltrirane[0]?.naziv ?? ""),
    ]);
  }

  function ukloniStavku(index: number) {
    setStavke((prev) => prev.filter((_, i) => i !== index));
  }

  function azurirajStavku(index: number, izmena: Partial<StavkaForma>) {
    setStavke((prev) =>
      prev.map((s, i) => (i === index ? { ...s, ...izmena } : s)),
    );
  }

  async function sacuvaj() {
    setGreska(null);

    if (tipSvrhe === "ZAVRSNI_RAD") {
      const jedinstveni = new Set(komisijaIds.filter(Boolean));
      if (komisijaIds.some((id) => !id) || jedinstveni.size !== 3) {
        setGreska("Komisija mora imati tačno 3 različita, izabrana člana.");
        return;
      }
      if (!mentorId) {
        setGreska("Mentor je obavezan.");
        return;
      }
    }

    let svrha: SvrhaRezervacijeDto;
    switch (tipSvrhe) {
      case "NASTAVA":
        svrha = {
          tip: "NASTAVA",
          semestar,
          nivoStudija,
          vrsta,
          vrstaVezbi:
            vrsta === "VEZBE" ? (vrstaVezbi as VrstaVezbi) : undefined,
        };
        break;
      case "ISPIT":
        svrha = { tip: "ISPIT", semestar, nivoStudija, tipIspita };
        break;
      case "ZAVRSNI_RAD": {
        const mentor = predavaci.find((p) => String(p.id) === mentorId)!;
        const komisija = komisijaIds.map(
          (id) => predavaci.find((p) => String(p.id) === id)!,
        );
        svrha = {
          tip: "ZAVRSNI_RAD",
          semestar,
          nivoStudija,
          nazivTeme,
          student,
          mentor,
          clanoviKomisije: komisija,
        };
        break;
      }
      case "SASTANAK": {
        const ucesniciDto: UcesnikSastankaDto[] = ucesnici
          .filter((u) => u.ucesnik.trim())
          .map((u) => ({
            ucesnik: u.ucesnik,
            email: u.email || undefined,
            zaposleniId: u.zaposleniId ?? undefined,
          }));
        svrha = {
          tip: "SASTANAK",
          tema,
          napomena: napomenaSastanka || undefined,
          ucesnici: ucesniciDto,
        };
        break;
      }
      case "DOGADJAJ":
        svrha = {
          tip: "DOGADJAJ",
          naziv: nazivDogadjaja,
          opis: opisDogadjaja || undefined,
        };
        break;
    }

    const stavkeDto: StavkaRezervacijeDto[] = stavke.map((s) => {
      const pravaSala = saleFiltrirane.find((sala) => sala.naziv === s.sala)!;
      return {
        brojOsoba: s.brojOsoba,
        opis: s.opis || undefined,
        sala: pravaSala,
      };
    });

    const dto: RezervacijaDto = {
      datumTermina: datum,
      vremeOd: vremeZaBackend(vremeOd),
      vremeDo: vremeZaBackend(vremeDo),
      napomena: napomenaRezervacije || undefined,
      svrha,
      stavke: stavkeDto,
    };

    setSaljeSe(true);
    try {
      await kreirajRezervaciju(dto);
      onUspesnoKreirano();
      onOpenChange(false);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setSaljeSe(false);
    }
  }

  const poljaForme = (
    <div
      className={
        kaoStranica
          ? "space-y-4 text-lg"
          : "max-h-[65vh] space-y-4 overflow-y-auto text-lg"
      }
    >
      <SvrhaTipSelektor tipSvrhe={tipSvrhe} onChange={setTipSvrhe} />

      <SvrhaPolja
        tipSvrhe={tipSvrhe}
        predavaci={predavaci}
        zaposleniOpcije={zaposleniOpcije}
        semestar={semestar}
        setSemestar={setSemestar}
        nivoStudija={nivoStudija}
        setNivoStudija={setNivoStudija}
        vrsta={vrsta}
        setVrsta={setVrsta}
        vrstaVezbi={vrstaVezbi}
        setVrstaVezbi={setVrstaVezbi}
        tipIspita={tipIspita}
        setTipIspita={setTipIspita}
        nazivTeme={nazivTeme}
        setNazivTeme={setNazivTeme}
        student={student}
        setStudent={setStudent}
        mentorId={mentorId}
        setMentorId={setMentorId}
        komisijaIds={komisijaIds}
        setKomisijaIds={setKomisijaIds}
        tema={tema}
        setTema={setTema}
        napomenaSastanka={napomenaSastanka}
        setNapomenaSastanka={setNapomenaSastanka}
        ucesnici={ucesnici}
        setUcesnici={setUcesnici}
        nazivDogadjaja={nazivDogadjaja}
        setNazivDogadjaja={setNazivDogadjaja}
        opisDogadjaja={opisDogadjaja}
        setOpisDogadjaja={setOpisDogadjaja}
      />

      <div className="border-t border-gray-400 pt-4">
        <label className="mb-2 flex items-center gap-1.5 text-fon-navy">
          <Clock size={18} className="text-fon-teal" />
          Termin rezervacije (zajednički za sve sale ispod)
        </label>
        <div>
          <label className="mb-1 block text-xs text-fon-navy">
            Datum <span className="text-red-500">*</span>
          </label>
          <Input
            className="border-2 border-gray-500 text-fon-navy"
            type="date"
            min={danasnjiDatumLokalno()}
            value={datum}
            onChange={(e) => setDatum(e.target.value)}
          />
        </div>
        <div className="mt-2 flex gap-2">
          <div className="flex-1">
            <label className="mb-1 block text-xs text-fon-navy">
              Vreme od <span className="text-red-500">*</span>
            </label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              type="time"
              min={
                datum === danasnjiDatumLokalno()
                  ? trenutnoVremeLokalno()
                  : undefined
              }
              value={vremeOd}
              onChange={(e) => setVremeOd(e.target.value)}
            />
          </div>
          <div className="flex-1">
            <label className="mb-1 block text-xs text-fon-navy">
              Vreme do <span className="text-red-500">*</span>
            </label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              type="time"
              value={vremeDo}
              onChange={(e) => setVremeDo(e.target.value)}
            />
          </div>
        </div>
      </div>

      <StavkeRezervacije
        stavke={stavke}
        saleFiltrirane={saleFiltrirane}
        onDodaj={dodajStavku}
        onUkloni={ukloniStavku}
        onAzuriraj={azurirajStavku}
      />

      <div>
        <label className="mb-1 block text-fon-navy">
          Napomena (opciono, za celu rezervaciju)
        </label>
        <Input
          className="border-2 border-gray-500 text-fon-navy"
          value={napomenaRezervacije}
          onChange={(e) => setNapomenaRezervacije(e.target.value)}
        />
      </div>

      <Button
        onClick={sacuvaj}
        disabled={saljeSe}
        className="w-full bg-gradient-to-r from-fon-teal to-fon-teal-light py-2.5 font-medium text-fon-dark shadow-md transition-transform hover:scale-[1.01] hover:shadow-lg disabled:opacity-70 disabled:hover:scale-100"
      >
        {saljeSe ? "Slanje..." : "Sačuvaj rezervaciju"}
      </Button>
    </div>
  );

  return (
    <>
      {kaoStranica ? (
        <div className="mx-auto max-w-3xl p-6">
          <button
            onClick={() => onOpenChange(false)}
            className="mb-3 text-sm text-fon-blue hover:underline"
          >
            ← Idi na pregled rezervacija
          </button>
          <div className="overflow-hidden rounded-2xl border-2 border-fon-navy bg-white shadow-lg">
            <div className="fon-gradient px-8 py-5">
              <h1 className="text-2xl font-semibold text-white">
                Nova rezervacija
              </h1>
            </div>
            <div className="p-8">{poljaForme}</div>
          </div>
        </div>
      ) : (
        <Dialog open={open} onOpenChange={onOpenChange}>
          <DialogContent className="border-2 border-fon-navy bg-white sm:max-w-3xl">
            <DialogHeader>
              <DialogTitle className="text-2xl text-fon-navy">
                Nova rezervacija
              </DialogTitle>
            </DialogHeader>
            {poljaForme}
          </DialogContent>
        </Dialog>
      )}

      <Dialog
        open={greska !== null}
        onOpenChange={(otvoren) => !otvoren && setGreska(null)}
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
    </>
  );
}
