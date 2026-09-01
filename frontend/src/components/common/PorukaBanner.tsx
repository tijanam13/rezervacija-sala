import { AlertCircle, CheckCircle2 } from "lucide-react";

interface PorukaBannerProps {
  poruka?: string | null;
  greska?: string | null;
}

export function PorukaBanner({ poruka, greska }: PorukaBannerProps) {
  if (!poruka && !greska) return null;

  return (
    <>
      {poruka && (
        <div className="mb-4 flex items-start gap-2 rounded-lg border border-fon-teal-light/30 bg-fon-teal-light/10 p-3 text-sm text-[#0F6E56]">
          <CheckCircle2 size={16} className="mt-0.5 shrink-0" />
          <span>{poruka}</span>
        </div>
      )}
      {greska && (
        <div className="mb-4 flex items-start gap-2 rounded-lg border border-fon-coral/30 bg-fon-coral/10 p-3 text-sm text-fon-coral">
          <AlertCircle size={16} className="mt-0.5 shrink-0" />
          <span>{greska}</span>
        </div>
      )}
    </>
  );
}
