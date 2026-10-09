FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Sem root: o processo roda como o usuario "nginx" e escuta na 8080 (porta alta, sem capability)
FROM nginxinc/nginx-unprivileged:1.30-alpine

# CVE-2026-103111 (pcre2 10.48, HIGH, corrigida na 10.49) ainda vem na base Alpine 3.24.2. Atualiza so esse pacote e volta
# ao usuario sem privilegio da imagem (uid 101). Remover quando a base trouxer o pcre2 corrigido.
# CVE-2026-4775 (tiff 4.7.1-r0, HIGH, corrigida na 4.7.2-r0): mesmo tratamento. Remover quando a base trouxer o tiff corrigido.
USER root
RUN apk add --no-cache --upgrade pcre2 tiff
USER 101
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/front/browser /usr/share/nginx/html

EXPOSE 8080
