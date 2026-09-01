import { useEffect, useState, useCallback } from "react";
import { DoorOpen, LayoutGrid } from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { Badge } from "@/components/ui/badge";
import { PorukaBanner } from "@/components/common/PorukaBanner";
import { usePorukaGreske } from "@/lib/usePorukaGreske";
import { fetchSaleAdmin, fetchTipoveSalaAdmin } from "@/lib/salaAdminApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import type { SalaDto, TipSaleDto } from "@/types";
import { SaleTab } from "./sale/SaleTab";
import { TipoviSaleTab } from "./sale/TipoviSaleTab";

type Tab = "sale" | "tipovi";

export default function Sale() {
  const [tab, setTab] = useState<Tab>("sale");

  const [sale, setSale] = useState<SalaDto[]>([]);
  const [tipovi, setTipovi] = useState<TipSaleDto[]>([]);
  const [ucitava, setUcitava] = useState(true);
  const { poruka, greska, javiUspeh, javiGresku, setGreska } =
    usePorukaGreske();

  const ucitajSve = useCallback(async () => {
    setUcitava(true);
    setGreska(null);
    try {
      const [saleData, tipoviData] = await Promise.all([
        fetchSaleAdmin(),
        fetchTipoveSalaAdmin(),
      ]);
      setSale(saleData);
      setTipovi(tipoviData);
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
            Upravljanje salama
          </p>
          <p className="text-base text-gray-600">
            Pregledajte, dodajte, izmenite ili obrišite sale i tipove sala.
          </p>
        </div>

        <div className="mb-6 flex gap-1 rounded-2xl border border-gray-100 bg-white p-1.5 shadow-sm">
          <button
            type="button"
            onClick={() => setTab("sale")}
            className={`flex flex-1 items-center justify-center gap-2 rounded-lg py-2 text-sm font-medium transition-colors ${
              tab === "sale"
                ? "bg-fon-navy text-white shadow-sm"
                : "text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
            }`}
          >
            <DoorOpen size={16} />
            Sale
            <Badge
              className={
                tab === "sale"
                  ? "bg-white/15 text-white"
                  : "bg-fon-blue/10 text-fon-blue"
              }
            >
              {sale.length}
            </Badge>
          </button>
          <button
            type="button"
            onClick={() => setTab("tipovi")}
            className={`flex flex-1 items-center justify-center gap-2 rounded-lg py-2 text-sm font-medium transition-colors ${
              tab === "tipovi"
                ? "bg-fon-navy text-white shadow-sm"
                : "text-gray-500 hover:bg-gray-50 hover:text-fon-navy"
            }`}
          >
            <LayoutGrid size={16} />
            Tipovi sala
            <Badge
              className={
                tab === "tipovi"
                  ? "bg-white/15 text-white"
                  : "bg-fon-blue/10 text-fon-blue"
              }
            >
              {tipovi.length}
            </Badge>
          </button>
        </div>

        <PorukaBanner poruka={poruka} greska={greska} />

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje podataka...
          </p>
        ) : tab === "sale" ? (
          <SaleTab
            sale={sale}
            setSale={setSale}
            tipovi={tipovi}
            javiUspeh={javiUspeh}
            onGreska={javiGresku}
          />
        ) : (
          <TipoviSaleTab
            tipovi={tipovi}
            setTipovi={setTipovi}
            sale={sale}
            setSale={setSale}
            javiUspeh={javiUspeh}
          />
        )}
      </div>
    </div>
  );
}
