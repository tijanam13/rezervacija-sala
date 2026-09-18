import { useState } from "react";
import { Link } from "react-router-dom";
import { Mail, Lock, AlertCircle, CheckCircle2 } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { registrujKorisnika } from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";

export default function Registracija() {
  const [email, setEmail] = useState("");
  const [lozinka, setLozinka] = useState("");
  const [potvrdaLozinke, setPotvrdaLozinke] = useState("");

  const [saljeSe, setSaljeSe] = useState(false);
  const [greska, setGreska] = useState<string | null>(null);
  const [uspeh, setUspeh] = useState(false);

  async function posaljiRegistraciju() {
    setGreska(null);

    if (lozinka !== potvrdaLozinke) {
      setGreska("Unete lozinke se ne poklapaju.");
      return;
    }

    setSaljeSe(true);
    try {
      await registrujKorisnika({ email, lozinka });
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
            Poslali smo vam mejl na{" "}
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
      podnaslov="Unesite svoju poslovnu FON email adresu i izaberite lozinku."
    >
      <div className="space-y-4">
        {greska && (
          <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

        <div>
          <label className="mb-1 block text-sm text-gray-500">Email</label>
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
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-500">Lozinka</label>
          <div className="relative">
            <Lock
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
            />
            <Input
              type="password"
              placeholder="Bar 6 karaktera, slovo i broj"
              value={lozinka}
              onChange={(e) => setLozinka(e.target.value)}
              className="border-fon-blue/25 pl-9"
            />
          </div>
        </div>

        <div>
          <label className="mb-1 block text-sm text-gray-500">
            Potvrdite lozinku
          </label>
          <div className="relative">
            <Lock
              size={16}
              className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-blue"
            />
            <Input
              type="password"
              placeholder="Ponovite lozinku"
              value={potvrdaLozinke}
              onChange={(e) => setPotvrdaLozinke(e.target.value)}
              className="border-fon-blue/25 pl-9"
            />
          </div>
        </div>

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
