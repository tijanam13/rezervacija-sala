import { useState, useEffect } from "react";
import { AlertCircle, User, GraduationCap, Briefcase } from "lucide-react";
import { NavBar } from "@/components/layout/NavBar";
import { Red } from "@/components/common/Red";
import { Badge } from "@/components/ui/badge";
import { fetchMojProfil } from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import type { KorisnikDto } from "@/types";

export default function MojProfil() {
  const [profil, setProfil] = useState<KorisnikDto | null>(null);
  const [ucitava, setUcitava] = useState(true);
  const [greska, setGreska] = useState<string | null>(null);

  useEffect(() => {
    fetchMojProfil()
      .then(setProfil)
      .catch((err) => setGreska(izvuciPorukuGreske(err)))
      .finally(() => setUcitava(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      <div className="mx-auto max-w-2xl p-6">
        <div className="mb-6">
          <p className="text-lg font-medium text-fon-navy">Moj profil</p>
          <p className="text-sm text-gray-500">
            Pregled podataka o tvom nalogu.
          </p>
        </div>

        {greska && (
          <div className="mb-4 flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

        {ucitava ? (
          <p className="py-10 text-center text-sm text-gray-500">
            Učitavanje...
          </p>
        ) : profil ? (
          <div className="space-y-4">
            {/* Osnovni podaci naloga */}
            <div className="rounded-2xl border border-fon-blue/20 bg-white p-5 shadow-sm">
              <div className="mb-4 flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-fon-blue/10 text-fon-blue">
                  <User size={22} />
                </div>
                <div>
                  <p className="text-lg font-medium text-fon-navy">
                    {profil.ime} {profil.prezime}
                  </p>
                  <Badge className="bg-fon-teal/15 text-fon-teal border-0">
                    {profil.tipKorisnika === "PREDAVAC"
                      ? "Predavač"
                      : "Službenik"}
                  </Badge>
                </div>
              </div>

              <div className="space-y-2">
                <Red naziv="Email" vrednost={profil.email} />
                {profil.predavac?.brojTelefona && (
                  <Red
                    naziv="Telefon"
                    vrednost={profil.predavac.brojTelefona}
                  />
                )}
                {profil.sluzbenik?.brojTelefona && (
                  <Red
                    naziv="Telefon"
                    vrednost={profil.sluzbenik.brojTelefona}
                  />
                )}
                {profil.predavac?.brojRadneKnjizice && (
                  <Red
                    naziv="Broj radne knjižice"
                    vrednost={profil.predavac.brojRadneKnjizice}
                  />
                )}
                {profil.sluzbenik?.brojRadneKnjizice && (
                  <Red
                    naziv="Broj radne knjižice"
                    vrednost={profil.sluzbenik.brojRadneKnjizice}
                  />
                )}
              </div>
            </div>

            {profil.predavac && (
              <div className="rounded-2xl border border-fon-blue/20 bg-white p-5 shadow-sm">
                <div className="mb-3 flex items-center gap-2">
                  <GraduationCap size={18} className="text-fon-blue" />
                  <p className="font-medium text-fon-navy">
                    Podaci o predavaču
                  </p>
                </div>
                <div className="space-y-2">
                  {profil.predavac.titula && (
                    <Red naziv="Titula" vrednost={profil.predavac.titula} />
                  )}
                  <Red naziv="Zvanje" vrednost={profil.predavac.zvanje.naziv} />
                  <Red
                    naziv="Katedra"
                    vrednost={profil.predavac.katedra.naziv}
                  />
                  {profil.predavac.terminKonsultacija && (
                    <Red
                      naziv="Termin konsultacija"
                      vrednost={profil.predavac.terminKonsultacija}
                    />
                  )}
                </div>
              </div>
            )}

            {profil.sluzbenik && (
              <div className="rounded-2xl border border-fon-blue/20 bg-white p-5 shadow-sm">
                <div className="mb-3 flex items-center gap-2">
                  <Briefcase size={18} className="text-fon-blue" />
                  <p className="font-medium text-fon-navy">
                    Podaci o službeniku
                  </p>
                </div>
                <div className="space-y-2">
                  {profil.sluzbenik.pozicija && (
                    <Red
                      naziv="Pozicija"
                      vrednost={profil.sluzbenik.pozicija}
                    />
                  )}
                  <Red
                    naziv="Služba"
                    vrednost={profil.sluzbenik.sluzba.naziv}
                  />
                </div>
              </div>
            )}
          </div>
        ) : null}
      </div>
    </div>
  );
}
