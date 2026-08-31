import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Mail, Lock, Eye, EyeOff, AlertCircle } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { login } from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { sacuvajSesiju } from "@/lib/auth";

export default function Prijava() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [lozinka, setLozinka] = useState("");
  const [prikaziLozinku, setPrikaziLozinku] = useState(false);
  const [saljeSe, setSaljeSe] = useState(false);
  const [greska, setGreska] = useState<string | null>(null);

  async function prijaviSe() {
    setGreska(null);
    setSaljeSe(true);
    try {
      const odgovor = await login({ email, lozinka });
      sacuvajSesiju(odgovor.token, odgovor.korisnik);
      navigate(odgovor.korisnik.status === "BLOKIRAN" ? "/blokiran" : "/");
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
      setLozinka("");
    } finally {
      setSaljeSe(false);
    }
  }

  return (
    <AuthLayout
      naslov="Prijava"
      podnaslov="Prijavi se svojim fakultetskim nalogom"
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
              onKeyDown={(e) => e.key === "Enter" && prijaviSe()}
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
              type={prikaziLozinku ? "text" : "password"}
              value={lozinka}
              onChange={(e) => setLozinka(e.target.value)}
              className="border-fon-blue/25 pr-9 pl-9"
              onKeyDown={(e) => e.key === "Enter" && prijaviSe()}
            />
            <button
              type="button"
              onClick={() => setPrikaziLozinku((p) => !p)}
              className="absolute top-1/2 right-3 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {prikaziLozinku ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </div>

        <div className="text-right">
          <Link
            to="/zaboravljena-lozinka"
            className="text-sm text-fon-blue hover:underline"
          >
            Zaboravili ste lozinku?
          </Link>
        </div>

        <Button
          onClick={prijaviSe}
          disabled={saljeSe}
          className="w-full bg-fon-teal text-fon-dark"
        >
          {saljeSe ? "Prijavljivanje..." : "Prijavi se"}
        </Button>

        <p className="text-center text-sm text-gray-500">
          Nemate nalog?{" "}
          <Link
            to="/registracija"
            className="font-medium text-fon-blue hover:underline"
          >
            Registrujte se
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
