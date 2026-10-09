# RentHub

Local-first rental training platform for cars, properties, bookings, and extra services.

## Current scope (Phase 1 — Business System only)
- Realistic cars catalog with search, location filter and sorting
- Realistic properties catalog
- Real booking flow with overlap validation and server-side pricing
- User booking history and cancellation
- Extra services catalog
- Oracle schema and seed data
- React frontend
- Spring Boot backend
- Real demo photography with local cache + online fallback
- One-command local runtime launcher and optional Windows autostart

> AI / agents / orchestrators are intentionally **not part of this phase**.

## Stack
- Java 21
- Spring Boot 4.1.1
- Oracle Database
- React 19.3
- Vite 8

## Database setup
For a fresh database run:
1. `database/001_schema.sql`
2. `database/002_seed.sql`

For the existing RentHub database created before the expanded catalog, also run once:
3. `database/003_catalog_expansion.sql`

## Easy local startup
Run from PowerShell:

```powershell
powershell -File .\scripts\start-renthub.ps1
```

On the first run only, RentHub will:
- ask for the Oracle `RENTHUB` password and store it encrypted with Windows DPAPI for the current user;
- download a portable Maven copy if Maven is not installed;
- cache the real demo photography locally;
- start the Spring Boot backend on port `8080`;
- start Vite on port `5173`;
- open `http://localhost:5173`.

Runtime data, secrets, portable tools and downloaded media are local-only and ignored by Git.

### Start automatically with Windows
After the launcher has been run successfully once:

```powershell
powershell -File .\scripts\install-autostart.ps1
```

RentHub will then start silently after Windows sign-in. To remove it:

```powershell
powershell -File .\scripts\uninstall-autostart.ps1
```

To stop processes started by the RentHub launcher:

```powershell
powershell -File .\scripts\stop-renthub.ps1
```

## Manual startup
Backend: `cd backend && mvn spring-boot:run`

Frontend: `cd frontend && npm.cmd install && npm.cmd run dev`

## Demo media
Catalog photography is sourced from Wikimedia Commons under the license terms on the original file pages. The project caches selected demo images locally on first launch; the frontend can fall back to the original Commons image if a local cached file is missing.

## Next implementation slices
Authentication, favorites, reviews, mock payments, admin/owner dashboards, image upload/storage, service bookings, validation/error model, and automated tests.
