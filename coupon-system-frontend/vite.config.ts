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
  test: {
    globals: true,
    environment: 'happy-dom',
    setupFiles: './tests/setup/vitest.setup.ts',
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov', 'json-summary'],
      reportsDirectory: './coverage',
      thresholds: {
        lines: 90,
        functions: 90,
        branches: 85,
        statements: 90,
      },
      exclude: [
        'node_modules/',
        'tests/',
        'build/',
        '**/*.test.{ts,tsx}',
        '**/*.config.{ts,js}',
        'src/types/',
        'src/theme/',
        'public/',
        'src/index.tsx',
        'src/App.tsx',
      ],
    },
  },
})
