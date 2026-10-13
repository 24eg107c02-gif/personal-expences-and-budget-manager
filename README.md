# Personal Expense and Budget Manager

## Deploy to Render

Deploy the backend as a Render **Web Service** with **Docker** selected. The backend is in the `backend` subdirectory.

### Backend Web Service settings

In Render, create a Web Service from this repository and configure:

| Setting | Value |
| --- | --- |
| Root Directory | Leave blank (repository root) |
| Runtime | `Docker` |
| Dockerfile Path | `backend/Dockerfile` |
| Docker Context Directory | `backend` |

The Dockerfile Path must point to the Dockerfile **file**, not the `backend` directory. In the current Render service, replace the Dockerfile Path value `backend` with `backend/Dockerfile`. Keep the Root Directory blank and set the Docker Context Directory to `backend`, so the Dockerfile's `COPY pom.xml` and `COPY src ./src` instructions use the backend module as their build context.

Set these environment variables on the backend service:

| Variable | Required | Value |
| --- | --- | --- |
| `DB_URL` | Yes | Full JDBC URL for a remotely reachable MySQL database, e.g. `jdbc:mysql://DB_HOST:3306/DB_NAME?sslMode=REQUIRED` |
| `DB_USERNAME` | Yes | MySQL username |
| `DB_PASSWORD` | Yes | MySQL password |
| `JWT_SECRET` | Yes | A randomly generated secret with at least 32 bytes |
| `ALLOWED_ORIGINS` | Yes for production | Exact frontend origin, e.g. `https://your-frontend.onrender.com` (no path or trailing slash) |
| `PORT` | Set by Render | Render's assigned port; Spring Boot uses it automatically |
| `JWT_EXPIRATION_MS` | No | Token lifetime in milliseconds; defaults to `86400000` |
| `DEMO_USER_ENABLED` | No | Defaults to `false`; leave disabled in production |

Generate a JWT secret locally, for example with `openssl rand -base64 48`, and enter it directly in Render's environment settings. Do not commit secrets or put them in the Dockerfile.

Use a MySQL server that accepts connections from Render. Render does not make a MySQL server running on your computer reachable from the deployed service. Configure the database firewall/network access as required by your database provider and use TLS where supported. `DB_URL` must be a JDBC URL, not a browser-facing or raw MySQL URL.

### Frontend

The frontend can be deployed separately as a Render **Static Site**:

| Setting | Value |
| --- | --- |
| Root Directory | `frontend` |
| Build Command | `npm ci && npm run build` |
| Publish Directory | `dist` |
| Environment Variable | `VITE_API_BASE_URL=https://YOUR-BACKEND.onrender.com/api` |

Add a Static Site rewrite rule `/*` → `/index.html` with action **Rewrite** so React Router routes load directly.

Replace the example backend host with the deployed backend service's public HTTPS URL. `VITE_API_BASE_URL` is embedded into the frontend at build time, so set it before building/redeploying the static site. Set the backend's `ALLOWED_ORIGINS` to the static site's exact HTTPS origin. For a custom frontend domain, use that domain instead.

The repository also contains a frontend Dockerfile intended for a Docker-based deployment; its default `/api` path is proxied by Nginx to the Compose service named `backend`. When deploying the frontend as a separate Render Static Site, set `VITE_API_BASE_URL` to the public backend URL as shown above instead of relying on that internal Compose proxy.

### Local Docker Compose

For local testing, set `JWT_SECRET`, `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the shell or a local, untracked `.env` file, then run:

```sh
docker compose up --build
```

The Compose frontend uses its internal Nginx `/api` proxy. Supply a MySQL database reachable from the backend container.
