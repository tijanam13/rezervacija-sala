import { useState, useEffect } from "react";
import { Plus, Trash2, AlertCircle } from "lucide-react";
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
import { fetchPredavaci } from "@/lib/salaPredavacApi";
import { kreirajRezervaciju, izvuciPorukuGreske } from "@/lib/rezervacijaApi";

const SELECT_PROPS = { side: "bottom" as const, alignItemWithTrigger: false };

const NAZIVI_SVRHE: Record<SvrhaRezervacijeDto["tip"], string> = {
  NASTAVA: "Nastava",
  ISPIT: "Ispit",
  ZAVRSNI_RAD: "Završni rad",
  SASTANAK: "Sastanak",
  DOGADJAJ: "Događaj",
};
const NAZIVI_NIVOA: Record<NivoStudija, string> = {
  OSNOVNE_AKADEMSKE: "Osnovne akademske",
  MASTER: "Master",
  DOKTORSKE: "Doktorske",
};
const NAZIVI_VRSTE: Record<VrstaNastave, string> = {
  PREDAVANJE: "Predavanje",
  VEZBE: "Vežbe",
};
const NAZIVI_VRSTE_VEZBI: Record<VrstaVezbi, string> = {
  AUDITORNE: "Auditorne",
  RACUNSKE: "Računske",
};
const NAZIVI_TIPA_ISPITA: Record<TipIspita, string> = {
  PISMENI: "Pismeni",
  USMENI: "Usmeni",
  SEMINARSKI_RAD: "Seminarski rad",
};

interface StavkaForma {
  sala: string;
  datum: string;
  vremeOd: string;
  vremeDo: string;
  brojOsoba: number;
  opis: string;
}

function praznaStavka(prvaSala: string): StavkaForma {
  return {
    sala: prvaSala,
    datum: new Date().toISOString().slice(0, 10),
    vremeOd: "10:00",
    vremeDo: "11:00",
    brojOsoba: 1,
    opis: "",
  };
}

interface UcesnikForma {
  ucesnik: string;
  email: string;
}

interface NovaRezervacijaFormeProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  saleFiltrirane: SalaDto[];
  onUspesnoKreirano: () => void;
}

function vremeZaBackend(vreme: string): string {
  return `${vreme}:00`;
}

export function NovaRezervacijaForma({
  open,
  onOpenChange,
  saleFiltrirane,
  onUspesnoKreirano,
}: NovaRezervacijaFormeProps) {
  const [tipSvrhe, setTipSvrhe] =
    useState<SvrhaRezervacijeDto["tip"]>("DOGADJAJ");
  const [napomenaRezervacije, setNapomenaRezervacije] = useState("");

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
    { ucesnik: "", email: "" },
  ]);

  const [nazivDogadjaja, setNazivDogadjaja] = useState("");
  const [opisDogadjaja, setOpisDogadjaja] = useState("");

  const [predavaci, setPredavaci] = useState<PredavacDto[]>([]);

  useEffect(() => {
    if (open) {
      fetchPredavaci()
        .then(setPredavaci)
        .catch(() => setPredavaci([]));
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

  function dodajUcesnika() {
    setUcesnici((prev) => [...prev, { ucesnik: "", email: "" }]);
  }

  function ukloniUcesnika(index: number) {
    setUcesnici((prev) => prev.filter((_, i) => i !== index));
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
          .map((u) => ({ ucesnik: u.ucesnik, email: u.email || undefined }));
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
        datumTermina: s.datum,
        vremeOd: vremeZaBackend(s.vremeOd),
        vremeDo: vremeZaBackend(s.vremeDo),
        brojOsoba: s.brojOsoba,
        opis: s.opis || undefined,
        sala: pravaSala,
      };
    });

    const dto: RezervacijaDto = {
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

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="border-2 border-fon-navy bg-white sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle className="text-xl text-fon-navy">
              Nova rezervacija
            </DialogTitle>
          </DialogHeader>

          <div className="max-h-[65vh] space-y-4 overflow-y-auto text-base">
            <div>
              <label className="mb-1 block text-gray-500">Svrha</label>
              <Select
                value={tipSvrhe}
                onValueChange={(v) =>
                  v !== null && setTipSvrhe(v as SvrhaRezervacijeDto["tip"])
                }
              >
                <SelectTrigger className="w-full border-fon-blue">
                  <SelectValue>{NAZIVI_SVRHE[tipSvrhe]}</SelectValue>
                </SelectTrigger>
                <SelectContent {...SELECT_PROPS} align="start">
                  <SelectItem value="NASTAVA">Nastava</SelectItem>
                  <SelectItem value="ISPIT">Ispit</SelectItem>
                  <SelectItem value="ZAVRSNI_RAD">Završni rad</SelectItem>
                  <SelectItem value="SASTANAK">Sastanak</SelectItem>
                  <SelectItem value="DOGADJAJ">Događaj</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {(tipSvrhe === "NASTAVA" ||
              tipSvrhe === "ISPIT" ||
              tipSvrhe === "ZAVRSNI_RAD") && (
              <div className="flex gap-2">
                <div className="flex-1">
                  <label className="mb-1 block text-gray-500">Semestar</label>
                  <Input
                    className="border-fon-blue"
                    type="number"
                    min={1}
                    max={8}
                    value={semestar}
                    onChange={(e) => setSemestar(Number(e.target.value))}
                  />
                </div>
                <div className="flex-1">
                  <label className="mb-1 block text-gray-500">
                    Nivo studija
                  </label>
                  <Select
                    value={nivoStudija}
                    onValueChange={(v) =>
                      v !== null && setNivoStudija(v as NivoStudija)
                    }
                  >
                    <SelectTrigger className="w-full border-fon-blue">
                      <SelectValue>{NAZIVI_NIVOA[nivoStudija]}</SelectValue>
                    </SelectTrigger>
                    <SelectContent {...SELECT_PROPS} align="start">
                      <SelectItem value="OSNOVNE_AKADEMSKE">
                        Osnovne akademske
                      </SelectItem>
                      <SelectItem value="MASTER">Master</SelectItem>
                      <SelectItem value="DOKTORSKE">Doktorske</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            )}

            {tipSvrhe === "NASTAVA" && (
              <div className="flex gap-2">
                <div className="flex-1">
                  <label className="mb-1 block text-gray-500">Vrsta</label>
                  <Select
                    value={vrsta}
                    onValueChange={(v) =>
                      v !== null && setVrsta(v as VrstaNastave)
                    }
                  >
                    <SelectTrigger className="w-full border-fon-blue">
                      <SelectValue>{NAZIVI_VRSTE[vrsta]}</SelectValue>
                    </SelectTrigger>
                    <SelectContent {...SELECT_PROPS} align="start">
                      <SelectItem value="PREDAVANJE">Predavanje</SelectItem>
                      <SelectItem value="VEZBE">Vežbe</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                {vrsta === "VEZBE" && (
                  <div className="flex-1">
                    <label className="mb-1 block text-gray-500">
                      Vrsta vežbi
                    </label>
                    <Select
                      value={vrstaVezbi}
                      onValueChange={(v) =>
                        v !== null && setVrstaVezbi(v as VrstaVezbi)
                      }
                    >
                      <SelectTrigger className="w-full border-fon-blue">
                        <SelectValue placeholder="Izaberi">
                          {vrstaVezbi
                            ? NAZIVI_VRSTE_VEZBI[vrstaVezbi]
                            : undefined}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent {...SELECT_PROPS} align="start">
                        <SelectItem value="AUDITORNE">Auditorne</SelectItem>
                        <SelectItem value="RACUNSKE">Računske</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                )}
              </div>
            )}

            {tipSvrhe === "ISPIT" && (
              <div>
                <label className="mb-1 block text-gray-500">Tip ispita</label>
                <Select
                  value={tipIspita}
                  onValueChange={(v) =>
                    v !== null && setTipIspita(v as TipIspita)
                  }
                >
                  <SelectTrigger className="w-full border-fon-blue">
                    <SelectValue>{NAZIVI_TIPA_ISPITA[tipIspita]}</SelectValue>
                  </SelectTrigger>
                  <SelectContent {...SELECT_PROPS} align="start">
                    <SelectItem value="PISMENI">Pismeni</SelectItem>
                    <SelectItem value="USMENI">Usmeni</SelectItem>
                    <SelectItem value="SEMINARSKI_RAD">
                      Seminarski rad
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}

            {tipSvrhe === "ZAVRSNI_RAD" && (
              <>
                <div>
                  <label className="mb-1 block text-gray-500">Naziv teme</label>
                  <Input
                    className="border-fon-blue"
                    value={nazivTeme}
                    onChange={(e) => setNazivTeme(e.target.value)}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-gray-500">Student</label>
                  <Input
                    className="border-fon-blue"
                    value={student}
                    onChange={(e) => setStudent(e.target.value)}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-gray-500">Mentor</label>
                  <Select
                    value={mentorId}
                    onValueChange={(v) => v !== null && setMentorId(v)}
                  >
                    <SelectTrigger className="w-full border-fon-blue">
                      <SelectValue placeholder="Izaberi mentora">
                        {(() => {
                          const p = predavaci.find(
                            (p) => String(p.id) === mentorId,
                          );
                          return p ? `${p.ime} ${p.prezime}` : undefined;
                        })()}
                      </SelectValue>
                    </SelectTrigger>
                    <SelectContent {...SELECT_PROPS} align="start">
                      {predavaci.map((p) => (
                        <SelectItem key={p.id} value={String(p.id)}>
                          {p.ime} {p.prezime}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <label className="mb-1 block text-gray-500">
                    Komisija (tačno 3 člana)
                  </label>
                  <div className="space-y-2">
                    {[0, 1, 2].map((i) => (
                      <Select
                        key={i}
                        value={komisijaIds[i]}
                        onValueChange={(v) =>
                          v !== null &&
                          setKomisijaIds((prev) =>
                            prev.map((k, idx) => (idx === i ? v : k)),
                          )
                        }
                      >
                        <SelectTrigger className="w-full border-fon-blue">
                          <SelectValue placeholder={`Član komisije ${i + 1}`}>
                            {(() => {
                              const p = predavaci.find(
                                (p) => String(p.id) === komisijaIds[i],
                              );
                              return p ? `${p.ime} ${p.prezime}` : undefined;
                            })()}
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent {...SELECT_PROPS} align="start">
                          {predavaci.map((p) => (
                            <SelectItem key={p.id} value={String(p.id)}>
                              {p.ime} {p.prezime}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    ))}
                  </div>
                </div>
              </>
            )}

            {tipSvrhe === "SASTANAK" && (
              <>
                <div>
                  <label className="mb-1 block text-gray-500">Tema</label>
                  <Input
                    className="border-fon-blue"
                    value={tema}
                    onChange={(e) => setTema(e.target.value)}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-gray-500">Napomena</label>
                  <Input
                    className="border-fon-blue"
                    value={napomenaSastanka}
                    onChange={(e) => setNapomenaSastanka(e.target.value)}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-gray-500">Učesnici</label>
                  <div className="space-y-2">
                    {ucesnici.map((u, i) => (
                      <div key={i} className="flex gap-2">
                        <Input
                          className="border-fon-blue"
                          placeholder="Ime i prezime"
                          value={u.ucesnik}
                          onChange={(e) =>
                            setUcesnici((prev) =>
                              prev.map((x, idx) =>
                                idx === i
                                  ? { ...x, ucesnik: e.target.value }
                                  : x,
                              ),
                            )
                          }
                        />
                        <Input
                          className="border-fon-blue"
                          placeholder="Email (opciono)"
                          value={u.email}
                          onChange={(e) =>
                            setUcesnici((prev) =>
                              prev.map((x, idx) =>
                                idx === i ? { ...x, email: e.target.value } : x,
                              ),
                            )
                          }
                        />
                        {ucesnici.length > 1 && (
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => ukloniUcesnika(i)}
                          >
                            <Trash2 size={16} className="text-fon-coral" />
                          </Button>
                        )}
                      </div>
                    ))}
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={dodajUcesnika}
                      className="gap-1"
                    >
                      <Plus size={14} /> Dodaj učesnika
                    </Button>
                  </div>
                </div>
              </>
            )}

            {tipSvrhe === "DOGADJAJ" && (
              <>
                <div>
                  <label className="mb-1 block text-gray-500">
                    Naziv događaja
                  </label>
                  <Input
                    className="border-fon-blue"
                    value={nazivDogadjaja}
                    onChange={(e) => setNazivDogadjaja(e.target.value)}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-gray-500">Opis</label>
                  <Input
                    className="border-fon-blue"
                    value={opisDogadjaja}
                    onChange={(e) => setOpisDogadjaja(e.target.value)}
                  />
                </div>
              </>
            )}

            <div className="border-t border-gray-100 pt-4">
              <div className="mb-2 flex items-center justify-between">
                <label className="text-gray-500">Sale i termini</label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={dodajStavku}
                  className="gap-1"
                >
                  <Plus size={14} /> Dodaj još jednu salu
                </Button>
              </div>

              <div className="space-y-3">
                {stavke.map((s, i) => (
                  <div
                    key={i}
                    className="rounded-lg border border-gray-100 p-3"
                  >
                    <div className="mb-2 flex items-center justify-between">
                      <span className="text-sm font-medium text-fon-dark">
                        Stavka {i + 1}
                      </span>
                      {stavke.length > 1 && (
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          onClick={() => ukloniStavku(i)}
                        >
                          <Trash2 size={16} className="text-fon-coral" />
                        </Button>
                      )}
                    </div>

                    <div className="space-y-2">
                      <Select
                        value={s.sala}
                        onValueChange={(v) => {
                          if (v === null) return;
                          const novaSala = saleFiltrirane.find(
                            (sala) => sala.naziv === v,
                          );
                          const novBrojOsoba = novaSala
                            ? Math.min(s.brojOsoba, novaSala.kapacitet)
                            : s.brojOsoba;
                          azurirajStavku(i, {
                            sala: v,
                            brojOsoba: novBrojOsoba,
                          });
                        }}
                      >
                        <SelectTrigger className="w-full border-fon-blue">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent {...SELECT_PROPS} align="start">
                          {saleFiltrirane.map((sala) => (
                            <SelectItem key={sala.naziv} value={sala.naziv}>
                              {sala.naziv}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>

                      <div>
                        <label className="mb-1 block text-xs text-gray-500">
                          Datum
                        </label>
                        <Input
                          className="border-fon-blue"
                          type="date"
                          value={s.datum}
                          onChange={(e) =>
                            azurirajStavku(i, { datum: e.target.value })
                          }
                        />
                      </div>

                      <div className="flex gap-2">
                        <div className="flex-1">
                          <label className="mb-1 block text-xs text-gray-500">
                            Vreme od
                          </label>
                          <Input
                            className="border-fon-blue"
                            type="time"
                            value={s.vremeOd}
                            onChange={(e) =>
                              azurirajStavku(i, { vremeOd: e.target.value })
                            }
                          />
                        </div>
                        <div className="flex-1">
                          <label className="mb-1 block text-xs text-gray-500">
                            Vreme do
                          </label>
                          <Input
                            className="border-fon-blue"
                            type="time"
                            value={s.vremeDo}
                            onChange={(e) =>
                              azurirajStavku(i, { vremeDo: e.target.value })
                            }
                          />
                        </div>
                        {(() => {
                          const izabranaSala = saleFiltrirane.find(
                            (sala) => sala.naziv === s.sala,
                          );
                          const maxOsoba = izabranaSala?.kapacitet ?? 999;
                          return (
                            <div className="flex-1">
                              <label className="mb-1 block text-xs text-gray-500">
                                Broj osoba
                              </label>
                              <Input
                                className="border-fon-blue"
                                type="number"
                                min={1}
                                max={maxOsoba}
                                value={s.brojOsoba}
                                onChange={(e) => {
                                  const vrednost = Math.min(
                                    Number(e.target.value) || 1,
                                    maxOsoba,
                                  );
                                  azurirajStavku(i, { brojOsoba: vrednost });
                                }}
                              />
                              {izabranaSala && (
                                <p className="mt-1 text-xs text-gray-400">
                                  Kapacitet sale: {izabranaSala.kapacitet}
                                </p>
                              )}
                            </div>
                          );
                        })()}
                      </div>

                      <div>
                        <label className="mb-1 block text-xs text-gray-500">
                          Napomena za ovu salu/termin (opciono)
                        </label>
                        <Input
                          className="border-fon-blue"
                          value={s.opis}
                          onChange={(e) =>
                            azurirajStavku(i, { opis: e.target.value })
                          }
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <label className="mb-1 block text-gray-500">
                Napomena (opciono, za celu rezervaciju)
              </label>
              <Input
                className="border-fon-blue"
                value={napomenaRezervacije}
                onChange={(e) => setNapomenaRezervacije(e.target.value)}
              />
            </div>

            <Button
              onClick={sacuvaj}
              disabled={saljeSe}
              className="w-full bg-fon-teal py-2.5 font-medium text-fon-dark"
            >
              {saljeSe ? "Slanje..." : "Sačuvaj rezervaciju"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog
        open={greska !== null}
        onOpenChange={(otvoren) => !otvoren && setGreska(null)}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-fon-coral">
              <AlertCircle size={20} />
              Greška
            </DialogTitle>
          </DialogHeader>
          <p className="text-base text-fon-dark">{greska}</p>
          <Button onClick={() => setGreska(null)} className="w-full">
            U redu
          </Button>
        </DialogContent>
      </Dialog>
    </>
  );
}
