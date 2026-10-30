# Aralinks

A peer tutoring marketplace: learners find tutors, book sessions or join classes, pay (simulated), and leave reviews.

| Part | Stack | Deploys to |
|---|---|---|
| `frontend/` | React, TypeScript, Vite | Vercel |
| `backend/` | Java 25, Spring Boot, Flyway | A Java host such as Render (Vercel does not run Spring Boot) |
| Database | PostgreSQL | Neon or Supabase |

## Run it locally

You need JDK 25 or newer and Node 20 or newer. You do not need to install PostgreSQL: the local runner starts a throwaway one and fills it with demo data, which is deleted when it stops.

Terminal 1:

```bash
cd backend && ./mvnw spring-boot:test-run -Dspring-boot.run.main-class=aralinks.LocalDevApplication
```

Terminal 2:

```bash
cd frontend && npm install && npm run dev
```

Open http://localhost:5173. Demo accounts use the password `demo-pass-123` (local only):

| Role | Email |
|---|---|
| Admin | `admin@aralinks.com` |
| Tutor | `ben@aralinks.com` |
| Tutor and learner | `gia@aralinks.com` |
| Learner | `cara@aralinks.com`, `dan@aralinks.com` |

Run the backend tests (they use a real PostgreSQL too):

```bash
cd backend && ./mvnw test
```

## Deploy

1. **Database:** create a Neon or Supabase project. The API creates its tables on first start with Flyway.
2. **Backend:** create a Docker web service with `backend/` as the root directory, and set the variables in `backend/.env.example`. `DATABASE_URL` must start with `jdbc:postgresql://`, with the username and password in their own variables. Generate `JWT_SECRET` with `openssl rand -base64 48`. Set `SEED_DEMO_DATA=true` and a private `DEMO_PASSWORD` for demo accounts.
3. **Frontend:** import the repo into Vercel with `frontend/` as the root directory, and set `VITE_API_URL` to the backend's URL.
4. **Connect them:** set `CORS_ALLOWED_ORIGINS` on the backend to your Vercel URL, then redeploy it.

To reset demo data, run `backend/src/main/resources/db/reset-demo.sql` against the database and restart the API with `SEED_DEMO_DATA=true`.

## Demo script

Reset the demo data first, and if the API runs on a free host, open the site a few minutes early so it can wake up. Book a date in the future: bookings must fall inside the tutor's available hours (seeded tutors work Monday to Saturday, 9:00 to 17:00).

1. **Sign up:** create a new learner account to show registration and login.
2. **Find a tutor:** search "programming" and open Ben Tutor's profile with its rating and review.
3. **Book:** send a request for Monday, 09:00 to 10:00, then log out.
4. **Accept as the tutor:** log in as `ben@aralinks.com`, accept the request and add a meeting link, then log out.
5. **Pay as the learner:** log back in and pay with GCash. This is simulated, and the page says so.
6. **Complete as the tutor:** log in as Ben, tick attendance and mark the session completed.
7. **Review as the learner:** leave a rating, then show it on Ben's profile.
8. **Show the admin view:** log in as `admin@aralinks.com` to show the users and session records.

To show the rules working, try booking outside Ben's hours, or booking the same time twice after Ben accepts the first request.

## Project layout

```
backend/src/main/java/aralinks/
  users/       User and roles
  model/       Domain entities and their rules
  repository/  Database queries
  service/     Use cases, including BookingRules
  api/         REST controllers and JSON shapes (Dto)
  security/    JWT login and access rules
backend/src/main/resources/db/migration/   Database schema
frontend/src/  api.ts (API client), auth.tsx (login state), pages/
```

Change the schema by adding a new `V2__...sql` migration. Never edit one that has already been deployed.

## API

All paths start with `/api`. Errors are returned as `{"message": "..."}`. Send `Authorization: Bearer <token>` except on public routes.

| Area | Routes |
|---|---|
| Public | `POST /auth/register`, `POST /auth/login`, `GET /health`, `GET /courses`, `GET /tutors?q=`, `GET /tutors/{id}`, `GET /classes`, `GET /classes/{id}` |
| Account | `GET /auth/me` |
| Tutor profile | `PUT /me/tutor`, `POST` or `DELETE /me/tutor/courses/{courseId}`, `POST /me/tutor/availability`, `DELETE /me/tutor/availability/{id}` |
| Requests | `GET` or `POST /requests`, `POST /requests/{id}/accept`, `/decline`, `/cancel` |
| Classes | `POST /classes`, `POST /classes/{id}/enroll`, `/sessions`, `/start`, `/end` |
| Sessions | `GET /sessions`, `GET /sessions/{id}`, `PUT /sessions/{id}/meeting-link`, `POST /sessions/{id}/complete`, `/cancel`, `/reviews`, `/payments` |
| Admin | `GET /admin/users`, `PATCH /admin/users/{id}/active`, `POST /admin/courses`, `DELETE /admin/classes/{id}`, `GET /admin/sessions` |

## Demo limitations

- Payments are simulated and move no money.
- Sign-up does not verify email addresses, and there is no password reset.
- Login has no rate limiting, and the token is kept in `localStorage`.
