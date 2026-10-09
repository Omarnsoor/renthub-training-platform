# RentHub

Local-first rental marketplace and business-flow training platform.

## Implemented business scope
- Realistic car, property and service catalogs
- Register / login / persistent local sessions
- Customer bookings with date-overlap protection and server-side pricing
- Favorites and reviews
- Booking service add-ons
- Mock card payment and paid booking status
- Customer booking history and cancellation rules
- Owner and Admin dashboards
- Consistent API validation/error responses
- Oracle persistence and basic backend tests
- Real demo photography with local cache/fallback
- One-command launcher and optional Windows autostart

> AI / agents / orchestrators remain intentionally out of scope for this business-system phase.

## Existing local database upgrade
Run these once in order if your DB already has the original schema:
1. `database/003_catalog_expansion.sql` (if not already applied)
2. `database/004_complete_platform.sql`

Demo role accounts after migration:
- Owner: `owner@renthub.local` / `Owner123!`
- Admin: `admin@renthub.local` / `Admin123!`
The first successful login replaces the temporary reset marker with a BCrypt password hash.

## Start RentHub
```powershell
powershell.exe -ExecutionPolicy Bypass -File .\scripts\start-renthub.ps1
```
Open `http://localhost:5173`.

## Windows autostart
```powershell
powershell.exe -ExecutionPolicy Bypass -File .\scripts\install-autostart.ps1
```

## Database GUI
Use DBeaver against `localhost:1521/XEPDB1` with the `RENTHUB` schema. Core tables now include RH_USERS, RH_BOOKINGS, RH_FAVORITES, RH_REVIEWS, RH_PAYMENTS, RH_BOOKING_SERVICES, RH_CARS, RH_PROPERTIES and RH_EXTRA_SERVICES.
