import { useEffect, useState, useRef } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/button";
import { verifikujEmail } from "@/lib/authApi";
import { izvuciPorukuGreske } from "@/lib/rezervacijaApi";
import { sacuvajSesiju } from "@/lib/auth";

export default function VerifikujEmail() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [stanje, setStanje] = useState<"ucitava" | "uspeh" | "greska">(
    "ucitava",
  );
  const [poruka, setPoruka] = useState("");
  const vecPozvano = useRef(false);

  useEffect(() => {
    if (vecPozvano.current) return;
    vecPozvano.current = true;

    const token = searchParams.get("token");
    if (!token) {
      setStanje("greska");
      setPoruka("Nedostaje token za potvrdu u linku.");
      return;
    }

    verifikujEmail(token)
      .then((odgovor) => {
        sacuvajSesiju(odgovor.token, odgovor.korisnik);
        setStanje("uspeh");
      })
      .catch((err) => {
        setStanje("greska");
        setPoruka(izvuciPorukuGreske(err));
      });
  }, [searchParams]);

  if (stanje === "ucitava") {
    return (
      <AuthLayout naslov="Potvrđivanje naloga...">
        <div className="flex justify-center py-6">
          <Loader2 size={32} className="animate-spin text-fon-teal" />
        </div>
      </AuthLayout>
    );
  }

  if (stanje === "uspeh") {
    return (
      <AuthLayout naslov="Nalog je aktiviran!">
        <div className="space-y-4 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-teal/15">
            <CheckCircle2 size={28} className="text-fon-teal" />
          </div>
          <p className="text-sm text-gray-600">
            Vaš nalog je uspešno potvrđen i prijavljeni ste. Možete odmah da
            počnete da koristite sistem.
          </p>
          <Button
            onClick={() => navigate("/")}
            className="w-full bg-fon-teal text-fon-dark"
          >
            Idi na početnu
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout naslov="Potvrda nije uspela">
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-coral/15">
          <XCircle size={28} className="text-fon-coral" />
        </div>
        <p className="text-sm text-gray-600">{poruka}</p>
        <Link to="/prijava">
          <Button className="w-full bg-fon-teal text-fon-dark">
            Idi na prijavu
          </Button>
        </Link>
      </div>
    </AuthLayout>
  );
}
