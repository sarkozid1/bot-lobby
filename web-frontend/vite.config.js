import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // sockjs-client Node.js "global"-ra hivatkozik, amit a bongeszo nem ismer - Vite alatt ezt kell polyfillelni.
  define: {
    global: 'globalThis'
  },
  server: {
    port: 5173
  }
});
