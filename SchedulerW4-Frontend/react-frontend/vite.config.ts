import path from "path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    host: true,        // 👈 This is critical for Docker to bind to 0.0.0.0
    port: 5173,        // 👈 Explicitly set the port
    strictPort: true,  // 👈 Fail if port 5173 is not available (optional)
    watch: {
      usePolling: true // 👈 Fixes issues with volume-mounted code changes in Docker
    }
  }
});
