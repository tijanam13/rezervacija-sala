import { Link } from "react-router-dom";
import { ShieldAlert } from "lucide-react";
import { AuthLayout } from "@/components/auth/AuthLayout";
import { Button } from "@/components/ui/button";

export default function ZabranjenPristup() {
  return (
    <AuthLayout naslov="Zabranjen pristup">
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-fon-coral/15">
          <ShieldAlert size={28} className="text-fon-coral" />
        </div>
        <p className="text-sm text-gray-600">
          Vaš nalog nema dozvolu za pristup ovoj stranici. Ako mislite da je ovo
          greška, obratite se administratoru.
        </p>
        <Link to="/">
          <Button className="w-full bg-fon-teal text-fon-dark">
            Idi na početnu
          </Button>
        </Link>
      </div>
    </AuthLayout>
  );
}
