import { useEffect, useState, useCallback } from "react";
import { BookOpen, Briefcase } from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { Badge } from "@/components/ui/badge";
import { PorukaBanner } from "@/components/common/PorukaBanner";
import { usePorukaGreske } from "@/lib/usePorukaGreske";
import {
  fetchPredavaciAdmin,
  fetchSluzbeniciAdmin,
} from "@/lib/zaposleniAdminApi";
import {
  fetchKatedreAdmin,
  fetchZvanjaAdmin,
  fetchSluzbeAdmin,
} from "@/lib/sifarniciAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import type {
  PredavacDto,
  SluzbenikDto,
  KatedraDto,
  ZvanjeDto,
  SluzbaDto,
} from "@/types";
import { SluzbeniciTab } from "./zaposleni/SluzbeniciTab";
import { PredavaciTab } from "./zaposleni/PredavaciTab";

type Tab = "predavaci" | "sluzbenici";

export default function Zaposleni() {
  const [tab, setTab] = useState<Tab>("predavaci");

  const [predavaci, setPredavaci] = useState<PredavacDto[]>([]);
  const [sluzbenici, setSluzbenici] = useState<SluzbenikDto[]>([]);
  const [katedre, setKatedre] = useState<KatedraDto[]>([]);
  const [zvanja, setZvanja] = useState<ZvanjeDto[]>([]);
  const [sluzbe, setSluzbe] = useState<SluzbaDto[]>([]);

  const [ucitava, setUcitava] = useState(true);
  const { poruka, greska, javiUspeh, setGreska } = usePorukaGreske();

  const ucitajSve = useCallback(async () => {
    setUcitava(true);
    setGreska(null);
    try {
      const [
        predavaciData,
        sluzbeniciData,
        katedreData,
        zvanjaData,
        sluzbeData,
      ] = await Promise.all([
        fetchPredavaciAdmin(),
        fetchSluzbeniciAdmin(),
        fetchKatedreAdmin(),
        fetchZvanjaAdmin(),
        fetchSluzbeAdmin(),
      ]);
      setPredavaci(predavaciData);
      setSluzbenici(sluzbeniciData);
      setKatedre(katedreData);
      setZvanja(zvanjaData);
      setSluzbe(sluzbeData);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setUcitava(false);
    }
  }, [setGreska]);

  useEffect(() => {
    ucitajSve();
  }, [ucitajSve]);

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-5xl p-6">
        <div className="mb-6">
          <p className="text-2xl font-semibold text-fon-navy">
            Predavači i službenici
          </p>
          <p className="text-base text-gray-600">
            Dodajte, izmenite ili obrišite profile.
          </p>
        </div>

        <div className="mb-6 flex gap-1 rounded-2xl border border-gray-100 bg-white p-1.5 shadow-sm">
          <button
            type="button"
            onClick={() => setTab("predavaci")}
            className={`flex flex-1 items-center justify-center gap-2 rounded-lg py-2 text-sm font-medium transition-colors ${
              tab === "predavaci"
                ? "bg-fon-blue text-white shadow-sm"
                : "text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
            }`}
          >
            <BookOpen size={16} />
            Predavači
            <Badge
              className={
                tab === "predavaci"
                  ? "bg-white/15 text-white"
                  : "bg-fon-blue/10 text-fon-blue"
              }
            >
              {predavaci.length}
            </Badge>
          </button>
          <button
            type="button"
            onClick={() => setTab("sluzbenici")}
            className={`flex flex-1 items-center justify-center gap-2 rounded-lg py-2 text-sm font-medium transition-colors ${
              tab === "sluzbenici"
                ? "bg-fon-pink text-white shadow-sm"
                : "text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
            }`}
          >
            <Briefcase size={16} />
            Službenici
            <Badge
              className={
                tab === "sluzbenici"
                  ? "bg-white/15 text-white"
                  : "bg-fon-pink/10 text-fon-pink"
              }
            >
              {sluzbenici.length}
            </Badge>
          </button>
        </div>

        <PorukaBanner poruka={poruka} greska={greska} />

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje podataka...
          </p>
        ) : tab === "predavaci" ? (
          <PredavaciTab
            predavaci={predavaci}
            setPredavaci={setPredavaci}
            katedre={katedre}
            zvanja={zvanja}
            javiUspeh={javiUspeh}
          />
        ) : (
          <SluzbeniciTab
            sluzbenici={sluzbenici}
            setSluzbenici={setSluzbenici}
            sluzbe={sluzbe}
            javiUspeh={javiUspeh}
          />
        )}
      </div>
    </div>
  );
}
