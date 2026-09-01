import { Ban } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/button";
import { odjaviSe } from "@/lib/auth";

export default function Blokiran() {
  return (
    <AuthLayout naslov="Nalog je blokiran">
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-coral/15">
          <Ban size={28} className="text-fon-coral" />
        </div>
        <p className="text-sm text-gray-600">
          Vaš nalog je blokiran i nemate pristup sistemu. Za više informacija,
          obratite se administratoru.
        </p>
        <Button
          onClick={() => odjaviSe()}
          className="w-full bg-fon-coral text-white hover:bg-fon-coral/90"
        >
          Odjavi se
        </Button>
      </div>
    </AuthLayout>
  );
}
