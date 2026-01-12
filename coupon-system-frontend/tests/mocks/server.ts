import { setupServer } from 'msw/node';
import { handlers } from './handlers';

/**
 * MSW Server Setup for Node.js environment (Vitest)
 * This server intercepts HTTP requests during tests
 */
export const server = setupServer(...handlers);
