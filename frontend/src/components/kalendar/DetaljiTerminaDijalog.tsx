import { ClipboardCheck } from "lucide-react";
import { Red } from "@/components/common/Red";
import { DetaljiSvrhe } from "@/components/rezervacije/DetaljiSvrhe";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { getStatusStyle } from "@/lib/statusColors";
import { nazivSvrhe, formatVreme, formatDatum } from "@/lib/svrhaHelpers";
import type { TerminPodaci } from "@/lib/kalendarTipovi";
import type { SalaDto } from "@/types";

interface DetaljiTerminaDijalogProps {
  izabraniTermin: TerminPodaci | null;
  izabranaSala: SalaDto | null;
  administracija: boolean;
  onClose: () => void;
  onObradiRezervaciju: (rezervacijaId: number) => void;
}

export function DetaljiTerminaDijalog({
  izabraniTermin,
  izabranaSala,
  administracija,
  onClose,
  onObradiRezervaciju,
}: DetaljiTerminaDijalogProps) {
  return (
    <Dialog
      open={izabraniTermin !== null}
      onOpenChange={(otvoren) => {
        if (!otvoren) onClose();
      }}
    >
      <DialogContent className="border-2 border-fon-navy bg-white sm:max-w-lg">
        {izabraniTermin && (
          <>
            <DialogHeader>
              <DialogTitle>{nazivSvrhe(izabraniTermin.svrha)}</DialogTitle>
            </DialogHeader>
            <div className="space-y-3 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-500">Status</span>
                <Badge
                  className={`${getStatusStyle(izabraniTermin.status).bg} ${getStatusStyle(izabraniTermin.status).text} border-0`}
                >
                  {getStatusStyle(izabraniTermin.status).label}
                </Badge>
              </div>
              <Red naziv="Sala" vrednost={izabraniTermin.salaNaziv} />
              {izabranaSala && (
                <Red
                  naziv="Kapacitet sale"
                  vrednost={`${izabranaSala.kapacitet} mesta`}
                />
              )}
              <Red naziv="Datum" vrednost={formatDatum(izabraniTermin.datum)} />
              <Red
                naziv="Vreme"
                vrednost={`${formatVreme(izabraniTermin.vremeOd)} - ${formatVreme(izabraniTermin.vremeDo)}`}
              />
              <Red
                naziv="Broj osoba"
                vrednost={String(izabraniTermin.brojOsoba)}
              />
              {izabraniTermin.korisnikImePrezime && (
                <Red
                  naziv="Podnosilac"
                  vrednost={izabraniTermin.korisnikImePrezime}
                />
              )}
              {izabraniTermin.opis && (
                <Red
                  naziv="Napomena za termin"
                  vrednost={izabraniTermin.opis}
                />
              )}
              {izabraniTermin.napomena && (
                <Red
                  naziv="Napomena uz rezervaciju"
                  vrednost={izabraniTermin.napomena}
                />
              )}
              <div className="border-t border-gray-100 pt-3">
                <DetaljiSvrhe svrha={izabraniTermin.svrha} />
              </div>
              {administracija &&
                izabraniTermin.status === "NA_CEKANJU" &&
                izabraniTermin.rezervacijaId && (
                  <Button
                    className="w-full gap-2 bg-fon-navy text-white hover:bg-fon-navy/90"
                    onClick={() =>
                      onObradiRezervaciju(izabraniTermin.rezervacijaId!)
                    }
                  >
                    <ClipboardCheck size={16} />
                    Obradi rezervaciju
                  </Button>
                )}
            </div>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
