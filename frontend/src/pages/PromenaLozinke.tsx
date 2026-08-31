import { useState } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { Lock, Eye, EyeOff, CheckCircle2, AlertCircle } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { resetujLozinku } from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";

export default function PromenaLozinke() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token") ?? "";

  const [novaLozinka, setNovaLozinka] = useState("");
  const [potvrdaLozinke, setPotvrdaLozinke] = useState("");
  const [prikazi, setPrikazi] = useState(false);
  const [saljeSe, setSaljeSe] = useState(false);
  const [uspeh, setUspeh] = useState(false);
  const [greska, setGreska] = useState<string | null>(null);

  async function posalji() {
    if (!token) {
      setGreska("Nedostaje token za promenu lozinke u linku.");
      return;
    }
    if (novaLozinka !== potvrdaLozinke) {
      setGreska("Lozinke se ne poklapaju.");
      return;
    }
    setGreska(null);
    setSaljeSe(true);
    try {
      await resetujLozinku(token, novaLozinka);
      setUspeh(true);
    } catch (err) {
      setGreska(izvuciPorukuGreske(err));
    } finally {
      setSaljeSe(false);
    }
  }

  if (uspeh) {
    return (
      <AuthLayout naslov="Lozinka je promenjena!">
        <div className="space-y-4 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-teal/15">
            <CheckCircle2 size={28} className="text-fon-teal" />
          </div>
          <p className="text-sm text-gray-600">
            Vaša lozinka je uspešno promenjena. Prijavite se novom lozinkom.
          </p>
          <Button
            onClick={() => navigate("/prijava")}
            className="w-full bg-fon-teal text-fon-dark"
          >
            Idi na prijavu
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      naslov="Nova lozinka"
      podnaslov="Unesite novu lozinku za vaš nalog"
    >
      <div className="space-y-4">
        {greska && (
          <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

        <div className="relative">
          <Lock
            size={16}
            className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-teal"
          />
          <Input
            type={prikazi ? "text" : "password"}
            placeholder="Nova lozinka (bar 6 karaktera, slovo i broj)"
            value={novaLozinka}
            onChange={(e) => setNovaLozinka(e.target.value)}
            className="border-fon-teal/25 pr-9 pl-9"
            onKeyDown={(e) => e.key === "Enter" && posalji()}
          />
          <button
            type="button"
            onClick={() => setPrikazi((p) => !p)}
            className="absolute top-1/2 right-3 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            {prikazi ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>

        <div className="relative">
          <Lock
            size={16}
            className="absolute top-1/2 left-3 -translate-y-1/2 text-fon-teal"
          />
          <Input
            type={prikazi ? "text" : "password"}
            placeholder="Potvrdite novu lozinku"
            value={potvrdaLozinke}
            onChange={(e) => setPotvrdaLozinke(e.target.value)}
            className="border-fon-teal/25 pl-9"
            onKeyDown={(e) => e.key === "Enter" && posalji()}
          />
        </div>

        <Button
          onClick={posalji}
          disabled={saljeSe}
          className="w-full bg-fon-teal text-fon-dark"
        >
          {saljeSe ? "Slanje..." : "Postavi novu lozinku"}
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
