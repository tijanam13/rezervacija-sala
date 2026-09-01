import { Component } from "react";
import type { ErrorInfo, ReactNode } from "react";
import { AlertTriangle } from "lucide-react";

interface Props {
  children: ReactNode;
}

interface State {
  imaGresku: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { imaGresku: false };
  }

  static getDerivedStateFromError(): State {
    return { imaGresku: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(
      "Neuhvaćena greška u komponenti:",
      error,
      info.componentStack,
    );
  }

  render() {
    if (this.state.imaGresku) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-gray-50 p-6">
          <div className="max-w-sm rounded-2xl border border-fon-coral/30 bg-white p-6 text-center shadow-sm">
            <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-fon-coral/10">
              <AlertTriangle className="text-fon-coral" size={24} />
            </div>
            <p className="mb-1 text-lg font-semibold text-fon-navy">
              Nešto je pošlo po zlu
            </p>
            <p className="mb-4 text-sm text-gray-600">
              Došlo je do neočekivane greške. Osvežite stranicu i pokušajte
              ponovo.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="rounded-lg bg-fon-navy px-4 py-2 text-sm font-medium text-white hover:bg-fon-navy/90"
            >
              Osveži stranicu
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
