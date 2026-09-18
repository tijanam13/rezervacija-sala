import { useState } from "react";
import { Link } from "react-router-dom";
import { Mail, CheckCircle2, AlertCircle } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { zatraziResetLozinke } from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";

export default function ZaboravljenaLozinka() {
  const [email, setEmail] = useState("");
  const [saljeSe, setSaljeSe] = useState(false);
  const [poslato, setPoslato] = useState(false);
  const [greska, setGreska] = useState<string | null>(null);

  async function posalji() {
    setGreska(null);
    if (!email.trim()) {
      setGreska("Morate uneti email adresu.");
      return;
    }
    setSaljeSe(true);
    try {
      await zatraziResetLozinke(email);
      setPoslato(true);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setSaljeSe(false);
    }
  }

  if (poslato) {
    return (
      <AuthLayout naslov="Proverite email">
        <div className="space-y-4 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-teal/15">
            <CheckCircle2 size={28} className="text-fon-teal" />
          </div>
          <p className="text-sm text-gray-600">
            Ukoliko nalog sa adresom{" "}
            <span className="font-medium text-fon-dark">{email}</span> postoji,
            link za promenu lozinke je poslat. Link važi 30 minuta.
          </p>
          <Link to="/prijava">
            <Button className="w-full bg-fon-teal text-fon-dark">
              Nazad na prijavu
            </Button>
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      naslov="Zaboravljena lozinka"
      podnaslov="Unesite email da dobijete link za promenu"
    >
      <div className="space-y-4">
        {greska && (
          <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

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
            onKeyDown={(e) => e.key === "Enter" && posalji()}
          />
        </div>

        <Button
          onClick={posalji}
          disabled={saljeSe}
          className="w-full bg-fon-teal text-fon-dark"
        >
          {saljeSe ? "Slanje..." : "Pošalji link za promenu"}
        </Button>

        <p className="text-center text-sm text-gray-500">
          <Link
            to="/prijava"
            className="font-medium text-fon-blue hover:underline"
          >
            Nazad na prijavu
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
