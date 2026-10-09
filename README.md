# RentHub

Local-first rental training platform for cars, properties, bookings, and extra services.

## Current scope (Phase 1 — Business System only)
- Cars catalog
- Properties catalog
- Extra services catalog
- Booking API
- Oracle schema and seed data
- React frontend
- Spring Boot backend

> AI / agents / orchestrators are intentionally **not part of this phase**.

## Stack
- Java 21
- Spring Boot 4.1.1
- Oracle Database
- React 19.3
- Vite 8

## Local setup
1. Create an Oracle schema/user for RentHub.
2. Run `database/001_schema.sql` then `database/002_seed.sql`.
3. Set environment variables:
   - `RENTHUB_DB_URL`
   - `RENTHUB_DB_USER`
   - `RENTHUB_DB_PASSWORD`
4. Backend: `cd backend && mvn spring-boot:run`
5. Frontend: `cd frontend && npm install && npm run dev`
6. Open `http://localhost:5173`

## Next implementation slices
Authentication, booking availability checks, payments, favorites, reviews, admin/owner dashboards, image upload/storage, service bookings, validation/error model, and automated tests.
