import { Plus, Trash2 } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import type {
  SvrhaRezervacijeDto,
  NivoStudija,
  VrstaNastave,
  VrstaVezbi,
  TipIspita,
  PredavacDto,
} from "@/types";
import { SVRHA_STIL } from "./SvrhaTipSelektor";

const SELECT_PROPS = { side: "bottom" as const, alignItemWithTrigger: false };

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

export interface UcesnikForma {
  ucesnik: string;
  email: string;
}

interface SvrhaPoljaProps {
  tipSvrhe: SvrhaRezervacijeDto["tip"];
  predavaci: PredavacDto[];

  semestar: number;
  setSemestar: (v: number) => void;
  nivoStudija: NivoStudija;
  setNivoStudija: (v: NivoStudija) => void;

  vrsta: VrstaNastave;
  setVrsta: (v: VrstaNastave) => void;
  vrstaVezbi: VrstaVezbi | "";
  setVrstaVezbi: (v: VrstaVezbi) => void;

  tipIspita: TipIspita;
  setTipIspita: (v: TipIspita) => void;

  nazivTeme: string;
  setNazivTeme: (v: string) => void;
  student: string;
  setStudent: (v: string) => void;
  mentorId: string;
  setMentorId: (v: string) => void;
  komisijaIds: string[];
  setKomisijaIds: (v: string[] | ((prev: string[]) => string[])) => void;

  tema: string;
  setTema: (v: string) => void;
  napomenaSastanka: string;
  setNapomenaSastanka: (v: string) => void;
  ucesnici: UcesnikForma[];
  setUcesnici: (
    v: UcesnikForma[] | ((prev: UcesnikForma[]) => UcesnikForma[]),
  ) => void;

  nazivDogadjaja: string;
  setNazivDogadjaja: (v: string) => void;
  opisDogadjaja: string;
  setOpisDogadjaja: (v: string) => void;
}

export function SvrhaPolja(props: SvrhaPoljaProps) {
  const { tipSvrhe, predavaci } = props;

  function dodajUcesnika() {
    props.setUcesnici((prev) => [...prev, { ucesnik: "", email: "" }]);
  }

  function ukloniUcesnika(index: number) {
    props.setUcesnici((prev) => prev.filter((_, i) => i !== index));
  }

  return (
    <div
      className={`space-y-4 rounded-xl border-l-4 p-4 ${SVRHA_STIL[tipSvrhe].akcentBorder} ${SVRHA_STIL[tipSvrhe].akcentBg}`}
    >
      {(tipSvrhe === "NASTAVA" ||
        tipSvrhe === "ISPIT" ||
        tipSvrhe === "ZAVRSNI_RAD") && (
        <div className="flex gap-2">
          <div className="flex-1">
            <label className="mb-1 block text-fon-navy">Semestar</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              type="number"
              min={1}
              max={8}
              value={props.semestar}
              onChange={(e) => props.setSemestar(Number(e.target.value))}
            />
          </div>
          <div className="flex-1">
            <label className="mb-1 block text-fon-navy">Nivo studija</label>
            <Select
              value={props.nivoStudija}
              onValueChange={(v) =>
                v !== null && props.setNivoStudija(v as NivoStudija)
              }
            >
              <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
                <SelectValue>{NAZIVI_NIVOA[props.nivoStudija]}</SelectValue>
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
            <label className="mb-1 block text-fon-navy">Vrsta</label>
            <Select
              value={props.vrsta}
              onValueChange={(v) =>
                v !== null && props.setVrsta(v as VrstaNastave)
              }
            >
              <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
                <SelectValue>{NAZIVI_VRSTE[props.vrsta]}</SelectValue>
              </SelectTrigger>
              <SelectContent {...SELECT_PROPS} align="start">
                <SelectItem value="PREDAVANJE">Predavanje</SelectItem>
                <SelectItem value="VEZBE">Vežbe</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {props.vrsta === "VEZBE" && (
            <div className="flex-1">
              <label className="mb-1 block text-fon-navy">Vrsta vežbi</label>
              <Select
                value={props.vrstaVezbi}
                onValueChange={(v) =>
                  v !== null && props.setVrstaVezbi(v as VrstaVezbi)
                }
              >
                <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
                  <SelectValue placeholder="Izaberi">
                    {props.vrstaVezbi
                      ? NAZIVI_VRSTE_VEZBI[props.vrstaVezbi]
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
          <label className="mb-1 block text-fon-navy">Tip ispita</label>
          <Select
            value={props.tipIspita}
            onValueChange={(v) =>
              v !== null && props.setTipIspita(v as TipIspita)
            }
          >
            <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
              <SelectValue>{NAZIVI_TIPA_ISPITA[props.tipIspita]}</SelectValue>
            </SelectTrigger>
            <SelectContent {...SELECT_PROPS} align="start">
              <SelectItem value="PISMENI">Pismeni</SelectItem>
              <SelectItem value="USMENI">Usmeni</SelectItem>
              <SelectItem value="SEMINARSKI_RAD">Seminarski rad</SelectItem>
            </SelectContent>
          </Select>
        </div>
      )}

      {tipSvrhe === "ZAVRSNI_RAD" && (
        <>
          <div>
            <label className="mb-1 block text-fon-navy">Naziv teme</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              value={props.nazivTeme}
              onChange={(e) => props.setNazivTeme(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-fon-navy">Student</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              value={props.student}
              onChange={(e) => props.setStudent(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-fon-navy">Mentor</label>
            <Select
              value={props.mentorId}
              onValueChange={(v) => v !== null && props.setMentorId(v)}
            >
              <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
                <SelectValue placeholder="Izaberi mentora">
                  {(() => {
                    const p = predavaci.find(
                      (p) => String(p.id) === props.mentorId,
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
            <label className="mb-1 block text-fon-navy">
              Komisija (tačno 3 člana)
            </label>
            <div className="space-y-2">
              {[0, 1, 2].map((i) => (
                <Select
                  key={i}
                  value={props.komisijaIds[i]}
                  onValueChange={(v) =>
                    v !== null &&
                    props.setKomisijaIds((prev) =>
                      prev.map((k, idx) => (idx === i ? v : k)),
                    )
                  }
                >
                  <SelectTrigger className="w-full border-2 border-gray-500 text-fon-navy">
                    <SelectValue placeholder={`Član komisije ${i + 1}`}>
                      {(() => {
                        const p = predavaci.find(
                          (p) => String(p.id) === props.komisijaIds[i],
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
            <label className="mb-1 block text-fon-navy">Tema</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              value={props.tema}
              onChange={(e) => props.setTema(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-fon-navy">Napomena</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              value={props.napomenaSastanka}
              onChange={(e) => props.setNapomenaSastanka(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-fon-navy">Učesnici</label>
            <div className="space-y-2">
              {props.ucesnici.map((u, i) => (
                <div key={i} className="flex gap-2">
                  <Input
                    className="border-2 border-gray-500 text-fon-navy"
                    placeholder="Ime i prezime"
                    value={u.ucesnik}
                    onChange={(e) =>
                      props.setUcesnici((prev) =>
                        prev.map((x, idx) =>
                          idx === i ? { ...x, ucesnik: e.target.value } : x,
                        ),
                      )
                    }
                  />
                  <Input
                    className="border-2 border-gray-500 text-fon-navy"
                    placeholder="Email (opciono)"
                    value={u.email}
                    onChange={(e) =>
                      props.setUcesnici((prev) =>
                        prev.map((x, idx) =>
                          idx === i ? { ...x, email: e.target.value } : x,
                        ),
                      )
                    }
                  />
                  {props.ucesnici.length > 1 && (
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
            <label className="mb-1 block text-fon-navy">Naziv događaja</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              value={props.nazivDogadjaja}
              onChange={(e) => props.setNazivDogadjaja(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-fon-navy">Opis</label>
            <Input
              className="border-2 border-gray-500 text-fon-navy"
              value={props.opisDogadjaja}
              onChange={(e) => props.setOpisDogadjaja(e.target.value)}
            />
          </div>
        </>
      )}
    </div>
  );
}
