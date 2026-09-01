export type StatusRezervacije =
  | "NA_CEKANJU"
  | "ODOBRENA"
  | "DELIMICNO_ODOBRENA"
  | "ODBIJENA"
  | "OTKAZANA"
  | "ISTEKLA";

export type StatusStavke =
  | "NA_CEKANJU"
  | "ODOBRENA"
  | "ODBIJENA"
  | "OTKAZANA"
  | "ISTEKLA";

interface StatusStyle {
  label: string;
  bg: string;
  text: string;
  border: string;
}

export const statusStyles: Record<StatusRezervacije, StatusStyle> = {
  NA_CEKANJU: {
    label: "Na čekanju",
    bg: "bg-[rgba(253,186,77,0.18)]",
    text: "text-[#8a5a09]",
    border: "border-fon-amber",
  },
  ODOBRENA: {
    label: "Odobrena",
    bg: "bg-[rgba(43,187,157,0.15)]",
    text: "text-[#0F6E56]",
    border: "border-fon-teal-light",
  },
  DELIMICNO_ODOBRENA: {
    label: "Delimično odobrena",
    bg: "bg-[rgba(135,129,189,0.15)]",
    text: "text-[#4A4680]",
    border: "border-fon-purple",
  },
  ODBIJENA: {
    label: "Odbijena",
    bg: "bg-[rgba(241,112,109,0.15)]",
    text: "text-[#a03330]",
    border: "border-fon-coral",
  },
  OTKAZANA: {
    label: "Otkazana",
    bg: "bg-[rgba(198,60,150,0.13)]",
    text: "text-[#8a2966]",
    border: "border-fon-pink",
  },
  ISTEKLA: {
    label: "Istekla",
    bg: "bg-gray-200",
    text: "text-gray-700",
    border: "border-gray-400",
  },
};

export function getStatusStyle(status: StatusRezervacije): StatusStyle {
  return statusStyles[status];
}

export type StatusSale = "SLOBODNA" | "ZAUZETA" | "VAN_UPOTREBE";

export const statusSaleStyles: Record<StatusSale, StatusStyle> = {
  SLOBODNA: {
    label: "Slobodna",
    bg: "bg-[rgba(43,187,157,0.15)]",
    text: "text-[#0F6E56]",
    border: "border-fon-teal-light",
  },
  ZAUZETA: {
    label: "Zauzeta",
    bg: "bg-[rgba(253,186,77,0.18)]",
    text: "text-[#8a5a09]",
    border: "border-fon-amber",
  },
  VAN_UPOTREBE: {
    label: "Van upotrebe",
    bg: "bg-[rgba(241,112,109,0.15)]",
    text: "text-[#a03330]",
    border: "border-fon-coral",
  },
};

export function getStatusSaleStyle(status: StatusSale): StatusStyle {
  return statusSaleStyles[status];
}
