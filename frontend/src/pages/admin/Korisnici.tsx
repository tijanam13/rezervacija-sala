import { useEffect, useState, useCallback } from "react";
import {
  ShieldCheck,
  ShieldOff,
  Ban,
  CheckCircle2,
  Search,
  X,
} from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { PorukaBanner } from "@/components/common/PorukaBanner";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  fetchKorisnici,
  dodeliUlogu,
  oduzmiUlogu,
  promeniStatusNaloga,
} from "@/lib/korisnikApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { preuzmiKorisnika, odjaviSe } from "@/lib/auth";
import type { KorisnikDto } from "@/types";

const VELICINA_STRANICE = 6;

export default function Korisnici() {
  const [stranica, setStranica] = useState(0);
  const [ukupnoStranica, setUkupnoStranica] = useState(0);
  const [korisnici, setKorisnici] = useState<KorisnikDto[]>([]);
  const [ucitava, setUcitava] = useState(true);
  const [greska, setGreska] = useState<string | null>(null);
  const [akcijaUToku, setAkcijaUToku] = useState<number | null>(null);

  const [pretragaTekst, setPretragaTekst] = useState("");
  const [pretraga, setPretraga] = useState("");

  useEffect(() => {
    const tajmer = setTimeout(() => setPretraga(pretragaTekst), 350);
    return () => clearTimeout(tajmer);
  }, [pretragaTekst]);

  useEffect(() => {
    setStranica(0);
  }, [pretraga]);

  const ucitajStranicu = useCallback(
    async (brojStranice: number, tekstPretrage: string) => {
      setUcitava(true);
      setGreska(null);
      try {
        const odgovor = await fetchKorisnici(
          brojStranice,
          VELICINA_STRANICE,
          tekstPretrage,
        );
        setKorisnici(odgovor.sadrzaj);
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
    ucitajStranicu(stranica, pretraga);
  }, [stranica, pretraga, ucitajStranicu]);

  async function obradiAkciju(id: number, akcija: () => Promise<KorisnikDto>) {
    setAkcijaUToku(id);
    setGreska(null);
    try {
      const azuriran = await akcija();
      if (id === preuzmiKorisnika()?.id) {
        odjaviSe("uloga-promenjena");
        return;
      }
      setKorisnici((prethodni) =>
        prethodni.map((k) => (k.id === id ? azuriran : k)),
      );
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setAkcijaUToku(null);
    }
  }

  function prikazStatusa(status: KorisnikDto["status"]) {
    if (status === "AKTIVAN") {
      return <Badge className="bg-fon-teal/15 text-fon-teal">Aktivan</Badge>;
    }
    if (status === "BLOKIRAN") {
      return <Badge className="bg-fon-coral/15 text-fon-coral">Blokiran</Badge>;
    }
    return <Badge className="bg-gray-100 text-gray-500">Neaktivan</Badge>;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-5xl p-6">
        <div className="mb-6">
          <p className="text-2xl font-semibold text-fon-navy">
            Upravljanje korisnicima
          </p>
          <p className="text-base text-gray-600">
            Dodelite ili oduzmite uloge, blokirajte ili odblokirajte naloge.
          </p>
        </div>

        <div className="relative mb-4 max-w-sm">
          <Search
            size={16}
            className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue/60"
          />
          <Input
            value={pretragaTekst}
            onChange={(e) => setPretragaTekst(e.target.value)}
            placeholder="Pretraga po imenu, prezimenu ili email adresi..."
            className="border-fon-blue/30 pl-9 focus-visible:border-fon-blue focus-visible:ring-fon-blue/30"
          />
          {pretragaTekst && (
            <button
              type="button"
              onClick={() => setPretragaTekst("")}
              className="absolute top-1/2 right-2.5 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              aria-label="Obriši pretragu"
            >
              <X size={16} />
            </button>
          )}
        </div>

        <PorukaBanner greska={greska} />

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje korisnika...
          </p>
        ) : korisnici.length === 0 ? (
          <p className="py-10 text-center text-sm text-gray-500">
            {pretraga
              ? `Nema korisnika koji odgovaraju pretrazi "${pretraga}".`
              : "Nema korisnika za prikaz."}
          </p>
        ) : (
          <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
                <tr>
                  <th className="px-4 py-3 font-medium">Ime i prezime</th>
                  <th className="px-4 py-3 font-medium">Email</th>
                  <th className="px-4 py-3 font-medium">Tip</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium">Uloge</th>
                  <th className="px-4 py-3 font-medium">Akcije</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {korisnici.map((korisnik) => {
                  const jeAdminVec = korisnik.uloge.includes("ADMIN");
                  const jeKoordinatorVec =
                    korisnik.uloge.includes("KOORDINATOR");
                  const jeBlokiran = korisnik.status === "BLOKIRAN";
                  const uAkciji = akcijaUToku === korisnik.id;

                  return (
                    <tr key={korisnik.id}>
                      <td className="px-4 py-3 font-medium text-fon-dark">
                        {korisnik.ime} {korisnik.prezime}
                      </td>
                      <td className="px-4 py-3 text-gray-600">
                        {korisnik.email}
                      </td>
                      <td className="px-4 py-3 text-gray-600">
                        {korisnik.tipKorisnika === "PREDAVAC"
                          ? "Predavač"
                          : "Službenik"}
                      </td>
                      <td className="px-4 py-3">
                        {prikazStatusa(korisnik.status)}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex flex-wrap gap-1">
                          {korisnik.uloge.length === 0 ? (
                            <span className="text-xs text-gray-400">—</span>
                          ) : (
                            korisnik.uloge.map((uloga) => (
                              <Badge
                                key={uloga}
                                className="bg-fon-blue/10 text-fon-blue"
                              >
                                {uloga === "ADMIN"
                                  ? "Administrator"
                                  : "Koordinator"}
                              </Badge>
                            ))
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex flex-wrap gap-1.5">
                          <Button
                            size="sm"
                            variant="outline"
                            className={
                              jeKoordinatorVec
                                ? "border-fon-blue/30 bg-fon-blue/10 text-fon-blue hover:bg-fon-blue/20"
                                : "border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
                            }
                            disabled={uAkciji}
                            onClick={() =>
                              obradiAkciju(korisnik.id, () =>
                                jeKoordinatorVec
                                  ? oduzmiUlogu(korisnik.id, "KOORDINATOR")
                                  : dodeliUlogu(korisnik.id, "KOORDINATOR"),
                              )
                            }
                          >
                            {jeKoordinatorVec ? (
                              <ShieldOff size={14} />
                            ) : (
                              <ShieldCheck size={14} />
                            )}
                            {jeKoordinatorVec
                              ? "Oduzmi koordinatora"
                              : "Postavi za koordinatora"}
                          </Button>

                          <Button
                            size="sm"
                            variant="outline"
                            className={
                              jeAdminVec
                                ? "border-fon-blue/30 bg-fon-blue/10 text-fon-blue hover:bg-fon-blue/20"
                                : "border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
                            }
                            disabled={uAkciji}
                            onClick={() =>
                              obradiAkciju(korisnik.id, () =>
                                jeAdminVec
                                  ? oduzmiUlogu(korisnik.id, "ADMIN")
                                  : dodeliUlogu(korisnik.id, "ADMIN"),
                              )
                            }
                          >
                            {jeAdminVec ? (
                              <ShieldOff size={14} />
                            ) : (
                              <ShieldCheck size={14} />
                            )}
                            {jeAdminVec ? "Oduzmi admina" : "Postavi za admina"}
                          </Button>

                          <Button
                            size="sm"
                            variant="outline"
                            className={
                              jeBlokiran ? "text-fon-teal" : "text-fon-coral"
                            }
                            disabled={uAkciji}
                            onClick={() =>
                              obradiAkciju(korisnik.id, () =>
                                promeniStatusNaloga(
                                  korisnik.id,
                                  jeBlokiran ? "AKTIVAN" : "BLOKIRAN",
                                ),
                              )
                            }
                          >
                            {jeBlokiran ? (
                              <CheckCircle2 size={14} />
                            ) : (
                              <Ban size={14} />
                            )}
                            {jeBlokiran ? "Odblokiraj" : "Blokiraj"}
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {ukupnoStranica > 1 && (
          <div className="mt-4 flex items-center justify-center gap-3">
            <Button
              size="sm"
              variant="outline"
              className="border-fon-blue/30 text-fon-blue hover:bg-fon-blue/10"
              disabled={stranica === 0 || ucitava}
              onClick={() => setStranica((s) => Math.max(0, s - 1))}
            >
              Prethodna
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
              Sledeća
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
