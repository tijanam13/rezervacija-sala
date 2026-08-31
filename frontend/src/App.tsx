import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import NovaRezervacija from "@/pages/NovaRezervacija";
import MojProfil from "@/pages/MojProfil";
import MojeRezervacije from "@/pages/MojeRezervacije";
import Prijava from "@/pages/Prijava";
import Registracija from "@/pages/Registracija";
import VerifikujEmail from "@/pages/VerifikujEmail";
import ZaboravljenaLozinka from "@/pages/ZaboravljenaLozinka";
import PromenaLozinke from "@/pages/PromenaLozinke";
import ZabranjenPristup from "@/pages/ZabranjenPristup";
import Blokiran from "@/pages/Blokiran";
import Korisnici from "@/pages/admin/Korisnici";
import { ProtectedRoute } from "@/components/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Navigate to="/nova-rezervacija" replace />
            </ProtectedRoute>
          }
        />
        <Route
          path="/nova-rezervacija"
          element={
            <ProtectedRoute>
              <NovaRezervacija />
            </ProtectedRoute>
          }
        />
        <Route
          path="/moj-profil"
          element={
            <ProtectedRoute>
              <MojProfil />
            </ProtectedRoute>
          }
        />
        <Route
          path="/moje-rezervacije"
          element={
            <ProtectedRoute>
              <MojeRezervacije />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/korisnici"
          element={
            <ProtectedRoute uloge={["ADMIN"]}>
              <Korisnici />
            </ProtectedRoute>
          }
        />

        <Route path="/prijava" element={<Prijava />} />
        <Route path="/registracija" element={<Registracija />} />
        <Route path="/verifikuj" element={<VerifikujEmail />} />
        <Route path="/zaboravljena-lozinka" element={<ZaboravljenaLozinka />} />
        <Route path="/blokiran" element={<Blokiran />} />
        <Route path="/promena-lozinke" element={<PromenaLozinke />} />
        <Route path="/zabranjen-pristup" element={<ZabranjenPristup />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
