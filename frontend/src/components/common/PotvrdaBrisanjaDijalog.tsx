import { AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";

interface PotvrdaBrisanjaDijalogProps {
  otvoren: boolean;
  naslov: string;
  opis: string;
  greska: string | null;
  brisanjeUToku: boolean;
  onOtkazi: () => void;
  onPotvrdi: () => void;
}

export function PotvrdaBrisanjaDijalog({
  otvoren,
  naslov,
  opis,
  greska,
  brisanjeUToku,
  onOtkazi,
  onPotvrdi,
}: PotvrdaBrisanjaDijalogProps) {
  return (
    <Dialog open={otvoren} onOpenChange={(o) => !o && onOtkazi()}>
      <DialogContent className="border-2 border-fon-coral bg-white sm:max-w-sm">
        <DialogHeader>
          <DialogTitle className="text-fon-coral">{naslov}</DialogTitle>
          <DialogDescription>{opis}</DialogDescription>
        </DialogHeader>

        {greska && (
          <div className="flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-2.5 text-sm text-fon-coral">
            <AlertCircle size={16} className="mt-0.5 shrink-0" />
            <span>{greska}</span>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onOtkazi} disabled={brisanjeUToku}>
            Otkaži
          </Button>
          <Button
            onClick={onPotvrdi}
            disabled={brisanjeUToku}
            className="bg-fon-coral text-white hover:bg-fon-coral/90"
          >
            {brisanjeUToku ? "Brisanje..." : "Obriši"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
