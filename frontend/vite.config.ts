import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    // In development the API runs locally, so calls to /api are forwarded to it.
    proxy: { "/api": "http://localhost:8080" },
  },
});
