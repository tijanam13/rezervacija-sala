import type { SvrhaRezervacijeDto } from "@/types";
import { Red } from "@/components/common/Red";
import { bezDonjeCrte } from "@/lib/svrhaHelpers";

export function DetaljiSvrhe({ svrha }: { svrha: SvrhaRezervacijeDto }) {
  switch (svrha.tip) {
    case "NASTAVA":
      return (
        <div className="space-y-2">
          <Red naziv="Semestar" vrednost={String(svrha.semestar)} />
          <Red
            naziv="Nivo studija"
            vrednost={bezDonjeCrte(svrha.nivoStudija)}
          />
          <Red naziv="Vrsta" vrednost={bezDonjeCrte(svrha.vrsta)} />
          {svrha.vrstaVezbi && (
            <Red
              naziv="Vrsta vežbi"
              vrednost={bezDonjeCrte(svrha.vrstaVezbi)}
            />
          )}
        </div>
      );
    case "ISPIT":
      return (
        <div className="space-y-2">
          <Red naziv="Semestar" vrednost={String(svrha.semestar)} />
          <Red
            naziv="Nivo studija"
            vrednost={bezDonjeCrte(svrha.nivoStudija)}
          />
          <Red naziv="Tip ispita" vrednost={bezDonjeCrte(svrha.tipIspita)} />
        </div>
      );
    case "ZAVRSNI_RAD":
      return (
        <div className="space-y-2">
          <Red naziv="Student" vrednost={svrha.student} />
          <Red
            naziv="Mentor"
            vrednost={`${svrha.mentor.ime} ${svrha.mentor.prezime}`}
          />
          <div>
            <p className="mb-1 text-gray-500">Komisija</p>
            <ul className="list-inside list-disc text-fon-dark">
              {svrha.clanoviKomisije.map((c) => (
                <li key={c.id}>
                  {c.ime} {c.prezime}
                </li>
              ))}
            </ul>
          </div>
        </div>
      );
    case "SASTANAK":
      return (
        <div className="space-y-2">
          {svrha.napomena && <Red naziv="Napomena" vrednost={svrha.napomena} />}
          <div>
            <p className="mb-1 text-gray-500">Učesnici</p>
            <ul className="list-inside list-disc text-fon-dark">
              {svrha.ucesnici.map((u, i) => (
                <li key={i}>
                  {u.ucesnik} {u.zaposleniId ? "(interni)" : "(eksterni)"}
                </li>
              ))}
            </ul>
          </div>
        </div>
      );
    case "DOGADJAJ":
      return svrha.opis ? <Red naziv="Opis" vrednost={svrha.opis} /> : null;
  }
}
