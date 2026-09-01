import { useState } from "react";

export function usePorukaGreske() {
  const [poruka, setPoruka] = useState<string | null>(null);
  const [greska, setGreska] = useState<string | null>(null);

  function javiUspeh(tekst: string) {
    setPoruka(tekst);
    setGreska(null);
    setTimeout(() => setPoruka(null), 3500);
  }

  function javiGresku(tekst: string) {
    setGreska(tekst);
    setPoruka(null);
  }

  return { poruka, greska, javiUspeh, javiGresku, setGreska, setPoruka };
}
