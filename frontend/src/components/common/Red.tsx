interface RedProps {
  naziv: string;
  vrednost: string;
}

export function Red({ naziv, vrednost }: RedProps) {
  return (
    <div className="flex items-center justify-between text-base">
      <span className="text-gray-700">{naziv}</span>
      <span className="font-medium text-fon-navy">{vrednost}</span>
    </div>
  );
}
