# Controlei na VPS do JavAI

O Controlei roda na **mesma VPS do JavAI** (`javai-vps`), com a **Tailscale como caminho padrão** para tudo que é interno: deploy, SSH e ferramentas de depuração. A internet só enxerga o que o Caddy do JavAI publica, e só pela Cloudflare.

```
                         internet
                            │  HTTPS (só faixas da Cloudflare, Authenticated Origin Pulls)
                  Cloudflare ── Access (e-mail do dono) ──┐
                            │                              │
          ┌─────────────────▼───────────── VPS ───────────────────────────────────────┐
          │ Caddy do JavAI (único que publica porta: 443)                              │
          │   admin.   conteudo.   api.   controlei.gilvanborges.com.br ──┐            │
          │                                                               │ rede       │
          │   ┌──── JavAI (projeto "javai") ────┐   ┌─── Controlei (projeto "controlei") ───────────┐
          │   │ admin-api · ai-api · indexer    │   │ gateway ─► front                              │
          │   │ 2 × Postgres                    │   │    └────► back ─► postgres · redis · kafka    │
          │   └─────────────────────────────────┘   └───────────────────────────────────────────────┘
          │                                                                            │
          └──── Tailscale (tailscale0): SSH, deploy pelo CI, Kafka UI ◄── seus aparelhos ┘
```

| Camada | Controlei |
|---|---|
| Entrada pública | `controlei.gilvanborges.com.br` → Cloudflare → Caddy → gateway (rede `controlei-edge`). **Nenhuma porta publicada pelo Controlei.** |
| Quem entra | Cloudflare Access (só o e-mail do dono) enquanto for privado; o cadastro de famílias fica **fechado** por padrão |
| Deploy | CI do GitHub entra na tailnet (OIDC, `tag:ci-javai`), faz SSH como `deploy-controlei` (comando forçado: só aceita um SHA) |
| SSH e depuração | Só pela Tailscale. O Kafka UI é publicado **no IP da Tailscale**, nunca em `0.0.0.0` |
| Dados | Postgres, Redis e Kafka numa rede `internal` (sem rota para a internet) |
| Limites | Memória por serviço (~1,7 GB no total) para não derrubar o JavAI, que divide a VPS |

## 1. Uma vez, na VPS (como `gilvan`, pela Tailscale)

```bash
ssh -i ~/.ssh/javai_vps_ed25519 -o IdentitiesOnly=yes gilvan@100.74.45.1
```

**Rede de borda** (subnet fixa: o gateway só confia no `X-Real-IP` vindo dela). Crie **antes** de subir o Caddy com o Controlei:

```bash
docker network create --subnet 172.30.50.0/24 controlei-edge
```

**Pasta e segredos:**

```bash
sudo install -d -m 755 -o root -g root /opt/controlei /opt/controlei/nginx
sudo install -m 600 -o root -g root /dev/null /opt/controlei/.env
sudo tee /opt/controlei/.env >/dev/null <<EOF
DB_PASSWORD=$(openssl rand -base64 32 | tr -d '/+=' | cut -c1-32)
REDIS_PASSWORD=$(openssl rand -base64 32 | tr -d '/+=' | cut -c1-32)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
DB_NAME=controlei
DB_USER=controlei
PUBLIC_URL=https://controlei.gilvanborges.com.br
REGISTRATION_ENABLED=true
CONTROLEI_AI_ENABLED=false
TAILSCALE_IP=$(tailscale ip -4)
EOF
```

`REGISTRATION_ENABLED=true` só até você criar a sua conta (passo 5); depois vira `false`. Os valores gerados nunca passam pelo chat nem pelo GitHub.

**Usuário de deploy** (mesmo desenho do `deploy` do JavAI: chave restrita à tailnet e comando forçado):

```bash
sudo adduser --system --group --shell /bin/bash --home /home/deploy-controlei deploy-controlei
sudo install -d -m 700 -o deploy-controlei -g deploy-controlei /home/deploy-controlei/.ssh

# A chave do CI é gerada no seu PC, nunca na VPS. Só a pública vai para cá:
#   ssh-keygen -t ed25519 -N '' -C controlei-deploy@github-actions -f controlei-deploy
echo 'restrict,command="/usr/local/sbin/controlei-deploy",from="100.64.0.0/10" <CONTEUDO DE controlei-deploy.pub>' \
  | sudo tee /home/deploy-controlei/.ssh/authorized_keys
sudo chown deploy-controlei:deploy-controlei /home/deploy-controlei/.ssh/authorized_keys
sudo chmod 600 /home/deploy-controlei/.ssh/authorized_keys

echo 'deploy-controlei ALL=(root) NOPASSWD: /usr/local/sbin/controlei-deploy-root' \
  | sudo tee /etc/sudoers.d/92-controlei-deploy && sudo chmod 440 /etc/sudoers.d/92-controlei-deploy
sudo visudo -cf /etc/sudoers.d/92-controlei-deploy
```

**Instalar os scripts** (do seu PC, na raiz do repositório do Controlei; o CI não consegue, de propósito):

```bash
scp deploy/controlei-deploy deploy/controlei-deploy-root gilvan@100.74.45.1:/tmp/
ssh gilvan@100.74.45.1 'sudo install -o root -g root -m 755 /tmp/controlei-deploy /tmp/controlei-deploy-root /usr/local/sbin/'
```

## 2. Uma vez, no Cloudflare e na Tailscale

1. **DNS:** registro `controlei` (CNAME ou A, **proxied/laranja**) apontando para o mesmo destino do `api`. O certificado de origem é o curinga `*.gilvanborges.com.br`, que já cobre o novo host.
2. **Access:** Zero Trust → Access → Applications → *Self-hosted*, domínio `controlei.gilvanborges.com.br`, política *Allow* só com o seu e-mail, sessão de 6 h (igual ao `admin`). Remova ou amplie a política só quando decidir abrir ao público.
3. **Tailscale, credencial OIDC do CI:** Settings → Trust credentials → o mesmo modelo do JavAI, com o **subject do repositório do Controlei** (`repo:gilvan-Borges@<id-da-conta>/controlei@<id-do-repo>:environment:producao`; os ids vêm de `gh api repos/gilvan-Borges/controlei`), escopo *Auth Keys: Write* e tag `tag:ci-javai`. A política da tailnet que já deixa `tag:ci-javai` chegar na porta 22 da `javai-vps` vale para os dois projetos.

## 3. Variáveis e segredos no GitHub (repositório `controlei`)

Environment `producao`, e:

| Onde | Nome | Valor |
|---|---|---|
| Variável | `DEPLOY_HOST` | `100.74.45.1` (IP da Tailscale da VPS) |
| Variável | `TS_OIDC_CLIENT_ID`, `TS_OIDC_AUDIENCE` | os da credencial do passo 2.3 |
| Segredo | `DEPLOY_SSH_KEY` | conteúdo de `controlei-deploy` (a chave **privada** do passo 1) |
| Segredo | `DEPLOY_KNOWN_HOSTS` | `ssh-keyscan -t ed25519 100.74.45.1` |
| Variável | `DEPLOY_ENABLED` | `true`, **só depois** do primeiro deploy manual funcionar |

Apague `controlei-deploy` e `controlei-deploy.pub` do seu PC depois de cadastrar o segredo.

## 4. Primeiro deploy (à mão, como o do JavAI)

O CI precisa já ter publicado as imagens daquele commit (aba Actions).

```bash
ssh gilvan@100.74.45.1
echo '<token classic só com read:packages>' | docker login ghcr.io -u gilvan-Borges --password-stdin
cd /opt/controlei
# docker-compose.yml e nginx/* do commit (o script faz isso sozinho nas próximas vezes)
for f in docker-compose.vps.yml:docker-compose.yml nginx/nginx.conf:nginx/nginx.conf nginx/proxy_api.conf:nginx/proxy_api.conf; do
  src="${f%%:*}"; dst="${f##*:}"
  sudo curl -fsS -H "Authorization: Bearer $(gh auth token)" -H 'Accept: application/vnd.github.raw' \
    "https://api.github.com/repos/gilvan-Borges/controlei/contents/$src?ref=<SHA>" -o "$dst"
done
echo 'CONTROLEI_TAG=<SHA>' | sudo tee -a .env
sudo docker compose pull && sudo docker compose up -d
sudo docker compose ps          # todos "healthy"
```

Depois, o Caddy do JavAI precisa ganhar a rede e o host novos: é a mudança em `infra/` do repositório JavAI (`docker-compose.yml` e `caddy/Caddyfile`), que o deploy normal do JavAI aplica.

## 5. Verificações

| Teste | Esperado |
|---|---|
| `docker compose ps` em `/opt/controlei` | Seis serviços `healthy` |
| `ss -tlnp` na VPS | Nenhuma porta nova (só a 443 do Caddy e a 22 da Tailscale) |
| `https://controlei.gilvanborges.com.br` sem login | Tela do Cloudflare Access |
| Depois do login: criar a sua família e entrar | Funciona; depois ponha `REGISTRATION_ENABLED=false` no `.env` e `docker compose up -d back` |
| `curl -s -o /dev/null -w '%{http_code}' https://controlei.gilvanborges.com.br/actuator/health` | 404 (o gateway nunca expõe o actuator) |
| `docker stats --no-stream` | Memória dentro dos tetos; some com o JavAI e confira o `free -m` |
| Do seu PC na tailnet: `http://100.74.45.1:8085` depois de `docker compose -f docker-compose.yml -f docker-compose.tailnet.yml up -d kafka-ui` | Abre o Kafka UI. Pela internet pública a porta não existe |

## 6. Operação

- **Atualizar:** push na `main` → build, testes, Trivy, imagens, deploy, com **rollback automático** se algum serviço não ficar saudável.
- **Logs:** `sudo docker compose logs -f back` (a rotação é de 3 × 10 MB por serviço).
- **Abrir ao público:** tire a política do Access, ponha `REGISTRATION_ENABLED=true` se quiser cadastro livre, e decida sobre a IA (`CONTROLEI_AI_ENABLED`, com a cota por família já ativa).
- **Backup (pendente):** o Postgres está num volume Docker, sem backup automático. Mínimo: `docker compose exec -T postgres pg_dump -U controlei controlei | gzip > /opt/javai/backups/controlei-$(date +%F).sql.gz` num `cron`, e copiar para fora da VPS.
- **Tailscale como padrão:** nada interno é publicado na internet. Para depurar, use SSH pela tailnet; para ferramentas web, o override `docker-compose.tailnet.yml`, que prende a porta ao IP da Tailscale.
