import { useState, useEffect, useMemo } from "react";
import { Link } from "react-router-dom";
import {
  User,
  Mail,
  Lock,
  Phone,
  Hash,
  GraduationCap,
  Briefcase,
  AlertCircle,
  CheckCircle2,
} from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  registrujPredavaca,
  registrujSluzbenika,
  fetchKatedre,
  fetchZvanja,
  fetchSluzbe,
} from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { selectItemsOd } from "@/lib/utils";
import type { KatedraDto, ZvanjeDto, SluzbaDto } from "@/types";

const SELECT_PROPS = {
  side: "bottom" as const,
  sideOffset: 4,
  align: "start" as const,
};

export default function Registracija() {
  const [tip, setTip] = useState<"PREDAVAC" | "SLUZBENIK">("PREDAVAC");

  const [ime, setIme] = useState("");
  const [prezime, setPrezime] = useState("");
  const [email, setEmail] = useState("");
  const [lozinka, setLozinka] = useState("");
  const [brojTelefona, setBrojTelefona] = useState("");
  const [brojRadneKnjizice, setBrojRadneKnjizice] = useState("");

  const [titula, setTitula] = useState("");
  const [terminKonsultacija, setTerminKonsultacija] = useState("");
  const [katedraId, setKatedraId] = useState("");
  const [zvanjeId, setZvanjeId] = useState("");

  const [pozicija, setPozicija] = useState("");
  const [sluzbaId, setSluzbaId] = useState("");

  const [katedre, setKatedre] = useState<KatedraDto[]>([]);
  const [zvanja, setZvanja] = useState<ZvanjeDto[]>([]);
  const [sluzbe, setSluzbe] = useState<SluzbaDto[]>([]);

  const [saljeSe, setSaljeSe] = useState(false);
  const [greska, setGreska] = useState<string | null>(null);
  const [uspeh, setUspeh] = useState(false);

  const katedreItems = useMemo(() => selectItemsOd(katedre), [katedre]);
  const zvanjaItems = useMemo(() => selectItemsOd(zvanja), [zvanja]);
  const sluzbeItems = useMemo(() => selectItemsOd(sluzbe), [sluzbe]);

  useEffect(() => {
    fetchKatedre()
      .then(setKatedre)
      .catch(() => {});
    fetchZvanja()
      .then(setZvanja)
      .catch(() => {});
    fetchSluzbe()
      .then(setSluzbe)
      .catch(() => {});
  }, []);

  async function posaljiRegistraciju() {
    setGreska(null);
    setSaljeSe(true);
    try {
      if (tip === "PREDAVAC") {
        await registrujPredavaca({
          ime,
          prezime,
          email,
          lozinka,
          brojTelefona: brojTelefona || undefined,
          brojRadneKnjizice,
          titula: titula || undefined,
          terminKonsultacija: terminKonsultacija || undefined,
          katedraId: Number(katedraId),
          zvanjeId: Number(zvanjeId),
        });
      } else {
        await registrujSluzbenika({
          ime,
          prezime,
          email,
          lozinka,
          brojTelefona: brojTelefona || undefined,
          brojRadneKnjizice,
          pozicija: pozicija || undefined,
          sluzbaId: Number(sluzbaId),
        });
      }
      setUspeh(true);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setSaljeSe(false);
    }
  }

  if (uspeh) {
    return (
      <AuthLayout naslov="Registracija uspešna!">
        <div className="space-y-4 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-teal/15">
            <CheckCircle2 size={28} className="text-fon-teal" />
          </div>
          <p className="text-sm text-gray-600">
            Poslali smo vam email na{" "}
            <span className="font-medium text-fon-dark">{email}</span> sa linkom
            za potvrdu naloga. Kliknite na link iz mejla da aktivirate nalog.
          </p>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      naslov="Registracija"
      podnaslov="Napravi nalog fakultetskom email adresom"
    >
      <div className="space-y-4">
        {greska && (
          <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

        <div className="flex items-center gap-1 rounded-lg bg-gray-50 p-1">
          {(["PREDAVAC", "SLUZBENIK"] as const).map((opcija) => (
            <button
              key={opcija}
              onClick={() => setTip(opcija)}
              className={`flex-1 rounded-md py-1.5 text-sm font-medium transition-colors ${
                tip === opcija
                  ? "bg-fon-navy text-white shadow-sm"
                  : "text-gray-500"
              }`}
            >
              {opcija === "PREDAVAC" ? "Predavač" : "Službenik"}
            </button>
          ))}
        </div>

        <div className="flex gap-2">
          <div className="relative flex-1">
            <User
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
            />
            <Input
              placeholder="Ime"
              value={ime}
              onChange={(e) => setIme(e.target.value)}
              className="border-fon-blue/25 pl-9"
            />
          </div>
          <div className="relative flex-1">
            <User
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
            />
            <Input
              placeholder="Prezime"
              value={prezime}
              onChange={(e) => setPrezime(e.target.value)}
              className="border-fon-blue/25 pl-9"
            />
          </div>
        </div>

        <div className="relative">
          <Mail
            size={16}
            className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
          />
          <Input
            type="email"
            placeholder="ime.prezime@fon.bg.ac.rs"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="border-fon-blue/25 pl-9"
          />
        </div>

        <div className="relative">
          <Lock
            size={16}
            className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
          />
          <Input
            type="password"
            placeholder="Lozinka (bar 6 karaktera, slovo i broj)"
            value={lozinka}
            onChange={(e) => setLozinka(e.target.value)}
            className="border-fon-blue/25 pl-9"
          />
        </div>

        <div className="flex gap-2">
          <div className="relative flex-1">
            <Phone
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
            />
            <Input
              placeholder="Telefon"
              value={brojTelefona}
              onChange={(e) => setBrojTelefona(e.target.value)}
              className="border-fon-blue/25 pl-9"
            />
          </div>
          <div className="relative flex-1">
            <Hash
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
            />
            <Input
              placeholder="Broj radne knjižice"
              value={brojRadneKnjizice}
              onChange={(e) => setBrojRadneKnjizice(e.target.value)}
              className="border-fon-blue/25 pl-9"
            />
          </div>
        </div>

        {tip === "PREDAVAC" ? (
          <>
            <div className="relative">
              <GraduationCap
                size={16}
                className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
              />
              <Input
                placeholder="Titula (npr. dr)"
                value={titula}
                onChange={(e) => setTitula(e.target.value)}
                className="border-fon-blue/25 pl-9"
              />
            </div>
            <Input
              placeholder="Termin konsultacija (opciono)"
              value={terminKonsultacija}
              onChange={(e) => setTerminKonsultacija(e.target.value)}
              className="border-fon-blue/25"
            />
            <Select
              items={katedreItems}
              value={katedraId}
              onValueChange={(v) => v !== null && setKatedraId(v)}
            >
              <SelectTrigger className="w-full border-fon-blue/25 bg-white">
                <SelectValue placeholder="Izaberi katedru" />
              </SelectTrigger>

              <SelectContent {...SELECT_PROPS}>
                {katedre.map((k) => (
                  <SelectItem
                    key={k.id}
                    value={String(k.id)}
                    className="cursor-pointer focus:bg-fon-blue/10 focus:text-fon-dark"
                  >
                    {k.naziv}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              items={zvanjaItems}
              value={zvanjeId}
              onValueChange={(v) => v !== null && setZvanjeId(v)}
            >
              <SelectTrigger className="w-full border-fon-blue/25 bg-white">
                <SelectValue placeholder="Izaberi zvanje" />
              </SelectTrigger>

              <SelectContent {...SELECT_PROPS}>
                {zvanja.map((z) => (
                  <SelectItem
                    key={z.id}
                    value={String(z.id)}
                    className="cursor-pointer focus:bg-fon-blue/10 focus:text-fon-dark"
                  >
                    {z.naziv}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </>
        ) : (
          <>
            <div className="relative">
              <Briefcase
                size={16}
                className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
              />
              <Input
                placeholder="Pozicija"
                value={pozicija}
                onChange={(e) => setPozicija(e.target.value)}
                className="border-fon-blue/25 pl-9"
              />
            </div>
            <Select
              items={sluzbeItems}
              value={sluzbaId}
              onValueChange={(v) => v !== null && setSluzbaId(v)}
            >
              <SelectTrigger className="w-full border-fon-blue/25 bg-white">
                <SelectValue placeholder="Izaberi službu" />
              </SelectTrigger>

              <SelectContent {...SELECT_PROPS}>
                {sluzbe.map((s) => (
                  <SelectItem
                    key={s.id}
                    value={String(s.id)}
                    className="cursor-pointer focus:bg-fon-blue/10 focus:text-fon-dark"
                  >
                    {s.naziv}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </>
        )}

        <Button
          onClick={posaljiRegistraciju}
          disabled={saljeSe}
          className="w-full bg-fon-teal text-fon-dark"
        >
          {saljeSe ? "Slanje..." : "Registruj se"}
        </Button>

        <p className="text-center text-sm text-gray-500">
          Već imate nalog?{" "}
          <Link
            to="/prijava"
            className="font-medium text-fon-blue hover:underline"
          >
            Prijavite se
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
