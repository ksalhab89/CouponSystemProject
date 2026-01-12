# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci

# Copy source code
COPY . .

# Accept build-time environment variable for Vite
# Default to /api/v1 for nginx proxy setup
ARG VITE_API_URL=/api/v1
ENV VITE_API_URL=$VITE_API_URL

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine

# Create non-root user and adjust permissions
RUN addgroup -g 1001 -S nginx-user && \
    adduser -u 1001 -S nginx-user -G nginx-user && \
    chown -R nginx-user:nginx-user /var/cache/nginx && \
    chown -R nginx-user:nginx-user /var/log/nginx && \
    chown -R nginx-user:nginx-user /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx-user:nginx-user /var/run/nginx.pid && \
    chown -R nginx-user:nginx-user /usr/share/nginx/html

# Copy built assets from build stage
COPY --from=build --chown=nginx-user:nginx-user /app/build /usr/share/nginx/html

# Copy nginx configuration
COPY --chown=nginx-user:nginx-user nginx.conf /etc/nginx/conf.d/default.conf

# Switch to non-root user
USER nginx-user

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
