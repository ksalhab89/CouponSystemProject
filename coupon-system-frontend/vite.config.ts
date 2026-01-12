import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    open: true,
    host: true, // Listen on all addresses for Docker
  },
  build: {
    outDir: 'build',
    sourcemap: true,
  },
  // Resolve index.html from project root
  root: './',
})
