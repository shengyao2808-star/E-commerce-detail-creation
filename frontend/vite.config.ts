import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const VENDOR_PREFIX = "/node_modules/";

function manualChunks(id: string) {
  if (!id.includes(VENDOR_PREFIX)) {
    return undefined;
  }

  if (id.includes(`${VENDOR_PREFIX}@excalidraw/`)) {
    return "excalidraw";
  }

  if (id.includes(`${VENDOR_PREFIX}pdfjs-dist/`)) {
    return "pdfjs";
  }

  if (id.includes(`${VENDOR_PREFIX}tesseract.js/`)) {
    return "tesseract";
  }

  if (id.includes(`${VENDOR_PREFIX}xlsx/`)) {
    return "xlsx";
  }

  if (id.includes(`${VENDOR_PREFIX}papaparse/`)) {
    return "papaparse";
  }

  if (id.includes(`${VENDOR_PREFIX}cytoscape/`) || id.includes(`${VENDOR_PREFIX}dagre-`)) {
    return "graph-vendor";
  }

  if (id.includes(`${VENDOR_PREFIX}mermaid/`)) {
    return "excalidraw";
  }

  if (id.includes(`${VENDOR_PREFIX}katex/`)) {
    return "katex";
  }

  if (id.includes(`${VENDOR_PREFIX}echarts/`)) {
    return "echarts";
  }

  return undefined;
}

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks
      }
    }
  },
  server: {
    port: 5173,
    strictPort: false,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  }
});
