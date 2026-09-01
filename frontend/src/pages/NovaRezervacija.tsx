import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { NavBar } from "@/components/layout/NavBar";
import { NovaRezervacijaForma } from "@/components/rezervacije/NovaRezervacijaForma";
import { fetchSale } from "@/lib/salaPredavacApi";
import type { SalaDto } from "@/types";

export default function NovaRezervacija() {
  const navigate = useNavigate();
  const [sale, setSale] = useState<SalaDto[]>([]);
  const [ucitano, setUcitano] = useState(false);

  useEffect(() => {
    fetchSale()
      .then(setSale)
      .finally(() => setUcitano(true));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />

      {ucitano && (
        <NovaRezervacijaForma
          open={true}
          onOpenChange={(otvoreno) => {
            if (!otvoreno) navigate("/pregled-rezervacija");
          }}
          saleFiltrirane={sale}
          onUspesnoKreirano={() => navigate("/moje-rezervacije")}
          kaoStranica
        />
      )}
    </div>
  );
}
