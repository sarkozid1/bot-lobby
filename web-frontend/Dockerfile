# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# Build-time env valtozo: a production build ebbe az URL-be fogja sutni az alapertelmezett
# backend cimet (a Settings oldalon a felhasznalo utolag felulirhatja - lasd api/client.js).
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# --- Runtime stage: statikus fajlok kiszolgalasa nginx-szel ---
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/app.conf.template

# A tobbseg PaaS (pl. Railway) a PORT env valtozoban adja meg a figyelendo portot -
# indulaskor envsubst-tel irjuk be a nginx configba (kizarolag ${PORT}-ot helyettesitve,
# hogy az nginx sajat "$uri"/"$host" valtozoit ne bantsa).
ENV PORT=8080
EXPOSE 8080
CMD ["/bin/sh", "-c", "envsubst '${PORT}' < /etc/nginx/templates/app.conf.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
