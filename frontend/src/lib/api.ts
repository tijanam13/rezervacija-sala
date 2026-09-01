import axios from "axios";
import { odjaviSe } from "@/lib/auth";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const jeLoginZahtev = error.config?.url?.includes("/auth/login");
    if (error.response?.status === 401 && !jeLoginZahtev) {
      odjaviSe("istekla-sesija");
    }
    return Promise.reject(error);
  },
);

export default api;
