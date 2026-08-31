interface RedProps {
  naziv: string;
  vrednost: string;
}

export function Red({ naziv, vrednost }: RedProps) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-gray-500">{naziv}</span>
      <span className="font-medium text-fon-dark">{vrednost}</span>
    </div>
  );
}
